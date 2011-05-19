/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the detector distinguishes reusable and unreusable
 * Float objects.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Float floatUnsafe = (float) 1.0;
		Float floatUnsafe2 = 3.14f;
		Float floatSafe = new Float(1.0);

		/* [LCK01J,01,java.lang.Float */synchronized (floatUnsafe) {
		}/* ] */

		/* [LCK01J,02,java.lang.Float */synchronized (floatUnsafe2) {
		}/* ] */
		
		synchronized (floatSafe) {
		}
	}
}