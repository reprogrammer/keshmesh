/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the detector distinguishes reusable and unreusable
 * Byte objects.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {

		Byte byteUnsafe = (byte) 2;
		Byte byteSafe = new Byte((byte) 2);

		/* [LCK01J,01,java.lang.Byte */synchronized (byteUnsafe) {
		}/* ] */

		synchronized (byteSafe) {
		}
	}
}