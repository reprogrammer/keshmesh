/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {

	private static B b;

	public static void main(String args[]) {
		b = new B(5);
		m();
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

class B {
	int i;

	public B(int i) {
		this.i = i;
	}

	public void set(int i) {
		this.i = i;
	}
}
