/** * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details. */
package p;

public class B {
	public static void main(String args[]) {
		new B().m();
	}

	private void m() {
		synchronized (this.getClass()) {
			System.err.println("replace with p.B.class");
		}
		Class l1 = new A().getClass();
		Class l2 = new C().getClass();
		Class l = (1 > 2) ? l1 : l2;
		synchronized (l) {
			System.err.println("multiple replacements: p.A.class, p.C.class");
		}
	}
}