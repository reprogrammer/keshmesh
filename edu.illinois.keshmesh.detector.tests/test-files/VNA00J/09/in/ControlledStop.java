/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test demonstrates an instance of the VNA00-J bug pattern from the CERT catalogue:
 * https://www.securecoding.cert.org/confluence/display/java/VNA00-J.+Ensure+visibility+when+accessing+shared+primitive+variables
 * 
 */
final class ControlledStop implements Runnable {
	/* [VNA00J,01 */private boolean done = false;/* ] */

	@EntryPoint
	public static void main(String args[]) {
		ControlledStop cs = new ControlledStop();
		new Thread(cs).start();
		cs.shutdown();
	}

	public void run() {
		/* [VNA00J,02 */while (!done) {/* ] */
			try {
				// ...
				Thread.currentThread().sleep(1000); // Do something
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt(); // Reset interrupted status
			}
		}
	}

	public void shutdown() {
		/* [VNA00J,03 */done = true;/* ] */
	}

}