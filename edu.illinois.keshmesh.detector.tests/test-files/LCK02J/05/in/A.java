/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {
	static B b;
	static int counter = 0;

	public static void main(String args[]) {
		new A().m();
	}

	public A() {
		b = new B();
	}

	private void m() {
		Class l = getArbitraryClass();
		/*[LCK02J,01,p.A.B.class,p.A.C.class*/synchronized (l) {
			System.out.println("multiple replacements: p.A.B.class, p.A.C.class");
		}/*]*/
	}

	private Class getArbitraryClass() {
		if (counter++ == 0) {
			return b.getClass();
		}else {
			return new C().getClass();
		}
	}

	class B {
	}

	class C {
	}
}