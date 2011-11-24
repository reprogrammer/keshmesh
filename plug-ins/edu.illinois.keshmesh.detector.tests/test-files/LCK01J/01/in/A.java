/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the detector distinguishes reusable and unreusable
 * Integer objects.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Integer integerUnsafe = -100;
		Integer unCachedIntegerUnsafe = -1000;
		Integer integerSafe = new Integer(5);

		/* [LCK01J,01,java.lang.Integer */synchronized (integerUnsafe) {
		}/* ] */

		/* [LCK01J,02,java.lang.Integer */synchronized (unCachedIntegerUnsafe) {
		}/* ] */

		synchronized (integerSafe) {
		}
	}
}