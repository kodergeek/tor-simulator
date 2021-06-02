package com.sina.torrentsim;

public class PeerData {

	// bit field is a data structure of bits marking the pieces
	// a peer has
	Integer[] bitField, piecesInTransit;
	// a list of pieces that the peer is currently downloading
	//piecesInTransit;
	//Initialization
	public PeerData(boolean isSeed, int pieceSize) {
		//Initalize the bitField taking into consideration isSeed;
		bitField = 		  new Integer[pieceSize];
		piecesInTransit = new Integer[pieceSize];
		for (int i = 0; i < pieceSize; i++) {
			bitField[i] = 0;
			piecesInTransit[i] = 0;
		}
		
		if (isSeed) {
			for (int i = 0; i < pieceSize; i++) {
				bitField[i] = 1;
			}
		}
		
	}
	
	public void printPiecesDownloaded() {
		int temp = 0;
		for ( int i = 0; i < bitField.length; i++){
			if ( bitField[i] == 1 ) temp++;
		}
		System.out.print(" " + temp);
	}




}
