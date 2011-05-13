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
		Long longUnsafe = (long) 200;
		Long longSafe = new Long(5);

		Short shortUnsafe = (short) 1;
		Short shortSafe = new Short((short) 1);

		Float floatUnsafe = (float) 1.0;
		Float floatSafe = new Float(1.0);

		Double doubleUnsafe = 2.0;
		Double doubleSafe = new Double(2.0);

		Byte byteUnsafe = (byte) 2;
		Byte byteSafe = new Byte((byte) 2);

		/* [LCK01J,01,java.lang.Long */synchronized (longUnsafe) {
		}/* ] */

		synchronized (longSafe) {
		}
		/* [LCK01J,02,java.lang.Short */synchronized (shortUnsafe) {
		}/* ] */

		synchronized (shortSafe) {
		}
		/* [LCK01J,03,java.lang.Float */synchronized (floatUnsafe) {
		}/* ] */

		synchronized (floatSafe) {
		}
		/* [LCK01J,04,java.lang.Double */synchronized (doubleUnsafe) {
		}/* ] */

		synchronized (doubleSafe) {
		}
		/* [LCK01J,05,java.lang.Byte */synchronized (byteUnsafe) {
		}/* ] */

		synchronized (byteSafe) {
		}
	}
}