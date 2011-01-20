/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {
	static B b;

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	public A() {
		b = new B();
	}

	private void m() {
		Class l = b.getClass();
		if (1 < 2) {
			l = new C().getClass();
		}
		/*[LCK02J,01,p.A.B.class,p.A.C.class*/synchronized (l) {
			System.out
					.println("multiple replacements: p.A.B.class, p.A.C.class");
		}/*]*/
	}

	class B {
	}

	class C {
	}
}