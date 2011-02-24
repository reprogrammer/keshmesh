/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {

	private static int counter = 0;

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		A a = new A();
		Class l1 = a.new B().getClass();
		Class l2 = a.new C().getClass();
		/* [LCK02J,01,p.A.B.class */synchronized (p.A.B.class) {
			System.out.println("replace with p.A.B.class");
		}/* ] */
		synchronized (this) {
			System.err.println("do not replace");
		}
		/* [LCK02J,02,p.A.class */synchronized (this.getClass()) {
			System.err.println("replace with p.A.class");
		}/* ] */
		/* [LCK02J,03,p.A.C.class */synchronized (l2) {
			System.out.println("replace with p.A.C.class");
		}/* ] */
		Class l = pickOne(l1, l2);
		/* [LCK02J,04,p.A.B.class,p.A.C.class */synchronized (l) {
			System.err
					.println("multiple replacements: p.A.B.class, p.A.C.class");
		}/* ] */
	}

	private Class pickOne(Class l1, Class l2) {
		if (counter++ == 0) {
			return l1;
		}
		return l2;
	}

	class B {
	}

	class C {
	}
}