/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the detector distinguishes reusable and unreusable
 * Short objects.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Short shortUnsafe = (short) 1;
		Short shortSafe = new Short((short) 1);
		
		/* [LCK01J,01,java.lang.Short */synchronized (shortUnsafe) {
		}/* ] */

		synchronized (shortSafe) {
		}
	}
}