/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class LCK01J {

	@EntryPoint
	public static void main(String args[]) {
		new LCK01J().integerLocks();
		new LCK01J().stringLocks();
	}

	private void integerLocks() {
		Integer integerUnsafe = -100;
		Integer integerSafe = new Integer(5);

		synchronized (integerUnsafe) {
		}

		synchronized (integerSafe) {
		}
	}

	private void stringLocks() {
		String stringUnsafe1 = "LOCK";
		String stringUnsafe2 = new String("LOCK").intern();
		String stringSafe = new String("LOCK");
		String internedStringConstantUnsafe = "CONSTANT_STRING".intern();

		synchronized (stringUnsafe1) {
		}

		synchronized (stringUnsafe2) {
		}

		synchronized (stringSafe) {
		}

		synchronized (internedStringConstantUnsafe) {
		}
	}

}