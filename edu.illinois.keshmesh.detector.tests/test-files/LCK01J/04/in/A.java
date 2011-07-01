/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the detector distinguishes reusable and unreusable
 * Long objects.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Long longUnsafe = (long) 200;
		Long longSafe = new Long(5);

		/* [LCK01J,01,java.lang.Long */synchronized (longUnsafe) {
		}/* ] */

		synchronized (longSafe) {
		}
	}
}