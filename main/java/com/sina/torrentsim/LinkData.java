package com.sina.torrentsim;

public class LinkData {

private static final int PIECE_SIZE = 256 * 8 * 1024;
//	the id of the piece currently downloaded
//	on this link, -1 if no piece is assigned to
//	this link yet
	private int piece;
//	Amount of bytes received for this piece
	private int totalBytesRcvd;
	private int uploaderID;
	private int pieceSize = PIECE_SIZE;
	
//	Any link should be empty when created, so we do it also in the constructor.
	LinkData(int uploaderID){
		this.uploaderID = uploaderID;
		reset();
	}
	
/**	check whether this piece is fully downloaded or not.
 * 
 * @return true if the piece is fully downloaded, false otherwise
 */
	
	public boolean isComplete(){
	      if (totalBytesRcvd >= PIECE_SIZE)
	            return true;
	      else
	            return false;
	}
	

/**Make this link empty
 *
 */ 
	public void reset() {
		piece = -1;
		totalBytesRcvd = 0;

	}

/**
 * Returns the index of the piece, so that we can inform the tracker to set it to one at bitfield
 * @return
 */
	public int getPiece() {
	return piece;
}

public void setPiece(int piece) {
	this.piece = piece;
}

public int getTotalBytesRcvd() {
	return totalBytesRcvd;
}

public void addTotalBytesRcvd(int totalBytesRcvd_) {
	this.totalBytesRcvd  += totalBytesRcvd_;
}

public int getUploaderID() {
	return uploaderID;
}

public void setUploaderID(int uploaderID) {
	this.uploaderID = uploaderID;
}

public int getPieceSize() {
	return pieceSize;
}

public void setPieceSize(int pieceSize) {
	this.pieceSize = pieceSize;
}

}
