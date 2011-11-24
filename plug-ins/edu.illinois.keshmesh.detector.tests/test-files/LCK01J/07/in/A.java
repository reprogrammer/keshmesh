/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the detector distinguishes reusable and unreusable
 * Double objects.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Double doubleUnsafe = 2.0;
		Double doubleSafe = new Double(2.0);

		/* [LCK01J,01,java.lang.Double */synchronized (doubleUnsafe) {
		}/* ] */

		synchronized (doubleSafe) {
		}
	}
}