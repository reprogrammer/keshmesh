/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public final class CountBoxes implements Runnable {
	private static volatile int counter;
	// ...
	private final Object lock = new Object();

	public void run() {
		synchronized (lock) {
			counter++;
			// ...
		}
	}

	@EntryPoint
	public static void main(String[] args) {
		for (int i = 0; i < 2; i++) {
			new Thread(new CountBoxes()).start();
		}
	}
}
