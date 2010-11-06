/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Class l1 = new B().getClass();
		Class l2 = new C().getClass();
		synchronized (l1) {
			System.out.println("replace with p.B.class");
		}
		synchronized (this) {
			System.err.println("don't replace");
		}
		synchronized (p.C.class) {
			System.out.println("replace with p.C.class");
		}
	}
}