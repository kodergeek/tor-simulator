package com.sina.torrentsim;

import java.util.Enumeration;
import java.util.Hashtable;

public class Peer {

	private static final int  BANDWIDTH = 128;
	private static final int  Connec = 4;
	private static final int  MAX_UPLOAD_PER_SEC   = BANDWIDTH * 1024; //should be equal to 128Kb
	private static final int  MAX_DOWNLOAD_PER_SEC = BANDWIDTH  * 1024; //should be equal to 128Kb
	private static final int  MAX_NUM_UPLOADERS    = Connec;
	private static final int  MAX_NUM_DOWNLOADERS  = Connec;
	private static final int  NUMBER_OF_PEERS 	   = 35;
	private static final int  PIECE_SIZE 		   = 256 * 8 * 1024 ; //256 KB
	//	Id of a peer
	private int id;
//	Up/down bandwidth
	private int maxUpLoadPerSec   = MAX_UPLOAD_PER_SEC;
	private int maxDownLoadPerSec = MAX_DOWNLOAD_PER_SEC;
//	Flag indicating if this peer is a seed
	private boolean isSeed = false;

	private Hashtable<Integer, LinkData> uploadersToMe     = new Hashtable<Integer, LinkData>(); 
	private Hashtable<Integer, LinkData> downloadersFromMe = new Hashtable<Integer, LinkData>();


	//A list of downloaders from this peer
	Hashtable<Integer, Peer> DownloadersFromMe = new Hashtable<Integer, Peer>();

	//reference to the tracker
	Tracker tracker;

	//reference to other peers
	Peer[] peers = new Peer[NUMBER_OF_PEERS ];
	/****************************************************************/

//	Initialization of a peer
	public Peer(int _id, Peer _peers, Tracker _tracker, boolean _isSeed) {
		id      = _id;          // save your id
		//this shuold be removed from here to some other method, when all peers are initialized
		tracker = _tracker;     // save reference to tracker
		isSeed = _isSeed;
		tracker.register(id,isSeed, this); //Inform tracker about yourself
	}

	public Peer(int id) {
		this.id = id;
	}

	public Peer() {
	}

	/**	Each second, when given the chance to run, do the following
	 * 
	 * @param now
	 */
	public void step(int now) {
		//if seed, do not need to do anything
		if (this.isSeed == true ) {
			return;
		}
		else {
			//If you are not connected to enough uploaders,
			//contact the tracker and it will select new uploaders
			completeYourUploaders();
			//For any uploader where no pieces are chosen, assign pieces
			selectPieces();
			//Make some progress in downloading
			downloadFromUploaders();
		}
	}


	private void downloadFromUploaders() {
		Enumeration<LinkData> allUploaders  =  uploadersToMe.elements();

		while (allUploaders.hasMoreElements()) {
			downloadPieceFrom (allUploaders.nextElement() );
		}

	}

	private void downloadPieceFrom(LinkData data) {

		int maxBytes;
		if (uploadersToMe.size() == 0)
			maxBytes = MAX_DOWNLOAD_PER_SEC;
		else 
			maxBytes = MAX_DOWNLOAD_PER_SEC / uploadersToMe.size();

		int nBytesRcv = peers[data.getUploaderID()].giveMeXBytes(this.id, maxBytes); 

		data.addTotalBytesRcvd(nBytesRcv);
		if (data.isComplete()) {
			boolean lastPiece = tracker.finishedPiece(this.id, data.getPiece());
			data.reset();
		
			peers[data.getUploaderID()].bye(id);
			
			if (lastPiece) {
				//System.out.println(this.id +  " calling bye at node " + data.getUploaderID());
				peers[data.getUploaderID()].bye(id);
				uploadersToMe.remove(data);
				this.isSeed = true;
				//System.out.println(this.id + calling is am seed while isAmSeed is " + isSeed);
				tracker.iAmSeed(this.id);
			} 
		}
	}

	private int giveMeXBytes(int id2, int maxBytes) {
		//System.out.println(this.id + " Object ref: " + this + " giving byte to " + id2);
		if  ( downloadersFromMe.size() == 0 ) return maxBytes;
		int maxBytesICanSend = maxUpLoadPerSec / downloadersFromMe.size();
		if (maxBytes <= maxBytesICanSend) return maxBytes;
		return maxBytesICanSend;

	}

	private void selectPieces() {
		Enumeration<LinkData> allUps = uploadersToMe.elements();
		while (allUps.hasMoreElements() ) {
			LinkData tmpLink = allUps.nextElement(); 
			if ( tmpLink.getPiece() == -1) {
				int piece = tracker.selectPiece(this.id, tmpLink.getUploaderID() );
				if ( piece != -1 ) {
					tmpLink.setPiece(piece);
				}
				else {
					peers[tmpLink.getUploaderID()].bye(this.id);
					//System.out.println(this.id + " calling bye at " + tmpLink.getUploaderID());
					uploadersToMe.remove(tmpLink.getUploaderID());
				}
			}
		}

	}

	private void completeYourUploaders() {
		int missingLinks = MAX_NUM_UPLOADERS - uploadersToMe.size();

		for (int i=1; i<=missingLinks; i++){
		
			//Ask the tracker to select you a peer
			int otherPeerId = tracker.selectPeer(id);
			//If the tracker found a peer for me, and I am not already connected to that peer
			if (otherPeerId != -1 && ( ! uploadersToMe.containsKey(otherPeerId)) ) 

			{
				//Try to connect to the other peer

				//****** TODO: before using the peers array, you should initialize it with the list of all available peers in the system.
				if (peers[otherPeerId].handshake(id)) {
					//if he accepts you, Create a fresh link, and add it to your uploaders
					//******* This is wrong, you should pass otherPeer reference not to create a new one
					uploadersToMe.put(otherPeerId, new LinkData(otherPeerId));
					//System.out.println(this.id + " : handshake with " + otherPeerId + " is true ");
				}

			}
		}
		
	}//completeYourUploaders()


//	Disconnnect from a peer
	void bye(int id){
		// remove id from DownloadersFromMe;
		DownloadersFromMe.remove(id);
	}


	/**
	 * establishes a link between me and this node, so that I can download from it
	 * @param id2
	 * @return
	 */
	private boolean handshake(int id2) {
		// if my current number of people downloading from me
		// is not maximum, I will accept you
		//System.out.print(this.id + " : DEBUG: handshake called. from " + id2);
		//System.out.println(" will tell him " + ( DownloadersFromMe.size()  < MAX_NUM_DOWNLOADERS )  + " cause downloader from me is " + DownloadersFromMe.size());
		
		if (DownloadersFromMe.size()  < MAX_NUM_DOWNLOADERS){
			//System.out.println("Yes!");
			DownloadersFromMe.put(id2, new Peer() );
			//System.out.println("now size is " + DownloadersFromMe.size());
			return true;
		}
		return false;
	}

	public void getPeersInfo(Enumeration<Peer> name) {
		Peer tmp;
		while (name.hasMoreElements()) {
			//System.out.println(this.id + ": printing node: " + name.nextElement().id);
			tmp = name.nextElement();
			peers[tmp.id] = tmp;
		}

	}

	public int getId() {
		return id;
	}

}