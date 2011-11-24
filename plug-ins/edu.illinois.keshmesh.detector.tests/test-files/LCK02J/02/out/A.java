/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Class l = new B().getClass();
		/*[LCK02J,01,p.A.B.class*/synchronized (p.A.B.class) {
			System.out.println("replace by p.A.B.class");
		}/*]*/
	}

	class B {
	}
}