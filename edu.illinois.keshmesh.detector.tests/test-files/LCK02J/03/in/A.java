/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

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
		/*[LCK02J,01,p.A.B.class*/synchronized (l) {
			System.out.println("replace with p.A.B.class");
		}/*]*/
	}

	class B {
	}
}