/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Class l = new B().getClass();
		/*[LCK02J,01,p.A.B.class*/synchronized (l) {
			System.out.println("replace by p.A.B.class");
		}/*]*/
	}

	class B {
	}
}