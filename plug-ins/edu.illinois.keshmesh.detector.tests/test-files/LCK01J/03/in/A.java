/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the detector distinguishes reusable and unreusable
 * String objects.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		String stringUnsafe1 = "LOCK";
		String stringUnsafe2 = new String("LOCK").intern();
		String stringSafe = new String("LOCK");
		String internedStringConstantUnsafe = "CONSTANT_STRING".intern();

		/* [LCK01J,01,java.lang.String */synchronized (stringUnsafe1) {
			System.out.println("lock on a primitive integer");
		}/* ] */

		/* [LCK01J,02,java.lang.String */synchronized (stringUnsafe2) {
			System.out.println("lock on a primitive integer");
		}/* ] */

		synchronized (stringSafe) {
			System.out.println("lock on a primitive integer");
		}

		/* [LCK01J,03,java.lang.String */synchronized (internedStringConstantUnsafe) {
		}/* ] */
	}
}