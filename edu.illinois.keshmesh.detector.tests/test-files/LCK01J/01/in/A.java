/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {
	private final Boolean falseUnsafe = Boolean.FALSE;
	private final Boolean falseSafe = false;
	private final Boolean trueUnsafe = Boolean.TRUE;
	private final Boolean trueSafe = true;

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Integer intVar1 = 2;
		Integer intVar2 = 3;
		Integer intObjectVar = new Integer(5);
		/*[LCK01J,01,int*/synchronized (intVar1) {
			System.out.println("lock on a primitive integer");
		}/*]*/
		/*[LCK01J,01,int*/synchronized (intVar2) {
			System.out.println("lock on a primitive integer");
		}/*]*/
		synchronized (intObjectVar) {
			System.out.println("lock on a primitive integer");
		}
//		/*[LCK01J,01,int*/synchronized (trueUnsafe) {
//			System.out.println("lock on a primitive integer");
//		}/*]*/
//		synchronized (trueSafe) {
//			System.out.println("lock on a primitive integer");
//		}
	}
}