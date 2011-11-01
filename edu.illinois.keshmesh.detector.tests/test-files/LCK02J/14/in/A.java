/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * This test checks if the detector reports the use of getClass() in a final
 * class.
 */
public final class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	void m() {
		// Because this class is final, getClass() always returns the same object.
		// Thus, the following use of getClass() in the synchronized block is not an instance of LCK02J.
		synchronized (getClass()) {

		}
	}
}
