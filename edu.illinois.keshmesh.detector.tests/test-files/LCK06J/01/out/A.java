/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {

	private static B b;
	private B b2;
	private static A a;

	public static void main(String args[]) {
		a = new A();
		a.m2();
		b = new B(a);
		m();
	}

	void m2() {
		b2 = new B(new A());
	}
	
	private static void m() {
		Object obj = new Object();
		synchronized (obj) {
			/* [LCK06J,01,p.A.class */synchronized (obj) {
				b.set(10);
			}/* ] */
		}
	}

}