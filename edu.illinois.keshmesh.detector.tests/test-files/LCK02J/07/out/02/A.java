/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		A a = new A();
		Class l1 = a.new B().getClass();
		Class l2 = a.new C().getClass();
		synchronized (l1) {
			System.out.println("replace with p.A.B.class");
		}
		synchronized (this) {
			System.err.println("do not replace");
		}
		synchronized (p.A.class) {
			System.err.println("replace with p.A.class");
		}
		synchronized (l2) {
			System.out.println("replace with p.A.C.class");
		}
		Class l = (1 > 2) ? l1 : l2;
		synchronized (l) {
			System.err
					.println("multiple replacements: p.A.B.class, p.A.C.class");
		}
	}

	class B {
	}

	class C {
	}
}