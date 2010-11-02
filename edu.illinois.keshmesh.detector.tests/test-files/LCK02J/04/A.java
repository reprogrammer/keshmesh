/** * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details. */
package p;

import java.util.Random;

public class A {
	static B b;

	public static void main(String args[]) {
		new A().m();
	}

	public A() {
		b = new B();
	}

	private void m() {
		Class l = b.getClass();
		if (new Random().nextBoolean()) {
			l = new C().getClass();
		}
		synchronized (l) {
			System.out
					.println("multiple replacements: p.A.B.class, p.A.C.class");
		}
	}

	class B {
	}

	class C {
	}
}