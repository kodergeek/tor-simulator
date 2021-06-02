package com.sina.torrentsim;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Tracker {

	private int numberOfPieces;//represents how many pieces a file is made of. not the size

	Random generator2;
	Random generatorForPiece;

	public PeerData[] peerData;

	Hashtable<Integer, Peer> dataOfPeers = new Hashtable<Integer, Peer>();
	Hashtable<Integer, Peer> dontHavePieces = new Hashtable<Integer, Peer>();
	Hashtable<Integer, Peer> havePieces = new Hashtable<Integer, Peer>();
	private int numOfSeeders = 0;
	private int[] seedersPosition;

	//this constructor takes how many peers we have and how many pieces a file consists of
	public Tracker(int peersSize, int numberOfPieces_) {
		this.numberOfPieces = numberOfPieces_;
		peerData = new PeerData[peersSize];

		generator2 		  = new Random( System.currentTimeMillis() );
		generatorForPiece = new Random( System.currentTimeMillis() );

		seedersPosition = new int[peersSize];
		for ( int r = 0; r < peersSize; r++) {
			seedersPosition[r] = 0;
		}
	}

	public synchronized void register(int id, boolean isSeed, Peer peer) {
		//System.out.println("Tracker: Peer number " + id + " Registered with isSeed = " + isSeed);

		dataOfPeers.put(id, peer);
		peerData[id  ] = new PeerData(isSeed, numberOfPieces);
		if (isSeed) {
			havePieces.put(id, peer);
			numOfSeeders++;

		}
		else {
			dontHavePieces.put(id, peer);
		}


	}


	public void printMsg( int id) {
		System.out.println("Tracker was called by node " + id);

	}

	public int selectPeer(int requesterID) {

//		We select a random peer from the set of peer who have 
//		data, and this peer should not be equal to the requester
		//System.out.println("DEBUG: select peer called (tracker)");
		if (havePieces.size() > 0){
			int selected = requesterID;
			while (selected == requesterID)
				selected = RandomIdFrom(havePieces);
			//System.out.println("DEBUG: select piecs returning " + selected + " to node " + requesterID);
			return selected;
		}
		else {
			return -1;

		}

	}

	private int RandomIdFrom(Hashtable<Integer, Peer> havePieces2) {

		Enumeration<Integer> tmpPeers =	havePieces2.keys();
		int[] keysArray = new int[havePieces2.size()]; 
		int i = 0;
		while ( tmpPeers.hasMoreElements()) {
			keysArray[i++] = tmpPeers.nextElement();
		}


		return keysArray[generator2.nextInt(havePieces2.size())] ;
	}

	public void propagateNodesInfo() {
		Enumeration<Peer> enumm = dataOfPeers.elements();

		while ( enumm.hasMoreElements()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			enumm.nextElement().getPeersInfo( dataOfPeers.elements() );
		}
	}

	public int selectPiece(int id, int uploaderID) {
		//System.out.println("Select Piece");
		Hashtable<Integer, Integer> intereSect = new Hashtable<Integer, Integer>();
		intereSect = calculateIntersection(id, uploaderID);
		if (intereSect.size() > 0  ) {
			//System.out.println("intersect size is " + intereSect.size() + " for " + id + " and " + uploaderID);
			int tmp =  intereSect.get(generatorForPiece.nextInt(intereSect.size()  ) ); 
			peerData[id].piecesInTransit[tmp] = 1;
			//System.out.println("gave piece " + tmp + " from node " + uploaderID + " to node " + id);
			return tmp;
		}
		return -1;


	}

	private Hashtable<Integer, Integer> calculateIntersection(int id, int uploaderID) {

		//System.out.println("calculateIntersection for " + id + " uploader: " + uploaderID);
		//System.out.println(peerData[uploaderID].bitField[0]);
		int hardIndex = 0;
		Hashtable<Integer, Integer> interSect = new Hashtable<Integer, Integer>();

		for ( int i = 0; i < peerData[uploaderID].bitField.length; i++)
		{
			if ( peerData[uploaderID].bitField[i] == 1 && peerData[id].bitField[i] == 0 && peerData[uploaderID].piecesInTransit[i] == 0) {
				//interSect.put(i, 1);
				interSect.put(hardIndex++, i);
			}
		}


		return interSect;
	}

	public boolean finishedPiece(int id, int piece) {
		peerData[id].piecesInTransit[piece] = 0;
		if ( dontHavePieces.containsKey(id)) {
			dontHavePieces.remove(id);
		}
		if ( !havePieces.contains(id))
			havePieces.put(id, dataOfPeers.get(id));

		peerData[id].bitField[piece] = 1;


		for (int j=0; j < peerData[id].bitField.length; j++) {
			if ( peerData[id].bitField[j] == 0) 
				return false; //there are some unfinished pieces, so return false
		}
		return true;
	}

	public void iAmSeed(int id) {
		if ( !dataOfPeers.containsKey(id) )
			havePieces.put(id, dataOfPeers.get(id));
		//System.out.println("Is Am Seed for " + id);
		if ( seedersPosition[id] == 0) {
			numOfSeeders ++;
			seedersPosition[id] = 1;
		}

	}

	public void reportStatus() {
		for (int i = 0; i < peerData.length; i++) {
			//System.out.print(" " + dataOfPeers.get(i).getId() + " ");
			peerData[i].printPiecesDownloaded();

		}
	}

	public int getNumOfSeeders() {
		return numOfSeeders;
	}

}
