/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

final class ControlledStop implements Runnable {
	private boolean done = false;

	@EntryPoint
	public static void main(String args[]) {
		ControlledStop cs = new ControlledStop();
		new Thread(cs).start();
		cs.shutdown();
	}

	public void run() {
		while (!done) {
			try {
				// ...
				Thread.currentThread().sleep(1000); // Do something
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt(); // Reset interrupted status
			}
		}
	}

	public void shutdown() {
		done = true;
	}

}