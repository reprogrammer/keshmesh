/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the detector distinguishes reusable and unreusable
 * Boolean objects.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Boolean booleanUnsafe1 = Boolean.FALSE;
		Boolean booleanUnsafe2 = true;
		Boolean booleanSafe = new Boolean(true);

		/* [LCK01J,01,java.lang.Boolean */synchronized (booleanUnsafe1) {
		}/* ] */

		/* [LCK01J,02,java.lang.Boolean */synchronized (booleanUnsafe2) {
		}/* ] */

		synchronized (booleanSafe) {
		}
	}
}