/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {
	public static void main(String args[]) {
		A.m();
	}

	private static void m() {
		A a = new A();
		Class l1 = a.new B().getClass();
		Class l2 = a.new C().getClass();
		synchronized (l1) {
			System.out.println("replace with p.A.B.class");
		}
		synchronized (l2) {
			System.out.println("replace with p.A.C.class");
		}
	}

	class B {
	}

	class C {
	}
}