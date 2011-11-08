/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class LCK02J {

	static int sharedCounter;

	@EntryPoint
	public static void main(String args[]) {
		new Thread(new B()).start();
		// new Thread(new C()).start();
	}

}

class B implements Runnable {

	public void run() {
		synchronized (getLock()) {
			++LCK02J.sharedCounter;
		}
	}

	public Object getLock() {
		return getClass();
	}

}

// class C extends B {
//
// @Override
// public void run() {
// super.run();
// }
//
// }