/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test demostrates an instance of the LCK06-J bug pattern from the CERT catalogue:
 * https://www.securecoding.cert.org/confluence/display/java/LCK06-J.+Do+not+use+an+instance+lock+to+protect+shared+static+data
 * 
 * 
 */
public final class CountBoxes implements Runnable {
	private static volatile int counter;
	// ...
	private final Object lock = new Object();

	public void run() {
		/* [LCK06J,01,p.CountBoxes.counter */synchronized (lock) {
			counter++;
			// ...
		}/* ] */
	}

	@EntryPoint
	public static void main(String[] args) {
		for (int i = 0; i < 2; i++) {
			new Thread(new CountBoxes()).start();
		}
	}
}
