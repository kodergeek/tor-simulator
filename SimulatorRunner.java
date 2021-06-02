package com.sina.torrentsim;

public class SimulatorRunner {

	private static final int NUMBER_OF_PEERS = 30;
	private static final int PIECE_SIZE = 30;
	
	static Tracker tracker = new Tracker(NUMBER_OF_PEERS, PIECE_SIZE );

	public static void main( String[] args){

		initialize();
		
		propagateNodesInfo();
		
		/**this is the main loop of the program which will continuously call node.step() so every node can proceed
		 */
		runMainLoop();
	}


	private static void initialize() {
		Peer[] peers = new Peer[NUMBER_OF_PEERS];
		Peer tmpPeer = new Peer();

		peers[0] = new Peer(0, tmpPeer, tracker, true);
		try {
			for (int i = 1; i < NUMBER_OF_PEERS; i++) {
				peers[i] = new Peer(i, tmpPeer, tracker, false);
			}
		}
		catch (NullPointerException e) { 
			e.printStackTrace();
			System.exit(1);
		}
	}


	private static void propagateNodesInfo() {
		tracker.propagateNodesInfo();
	}


	public static  void runMainLoop() {
		int step = 0;
		int i = NUMBER_OF_PEERS;
		
		while ( i++ < 5000 + NUMBER_OF_PEERS) {
			try {
				Thread.sleep(10);//sleep for 10 milliseconds to simulate some network delay
			}
			catch ( InterruptedException e) {
			}
			for (int j = NUMBER_OF_PEERS - 1 ; j >= 0; j--) {

				tracker.dataOfPeers.get(j).step(i);
			}
			System.out.print("Step " + step++ + " [");
			tracker.reportStatus();
			System.out.println(" ]");

			if ( tracker.getNumOfSeeders() >= NUMBER_OF_PEERS ) {
				System.out.println("Done in " + (step - 1) + " Seconds.");
				break;
			}
		}

	}

}
