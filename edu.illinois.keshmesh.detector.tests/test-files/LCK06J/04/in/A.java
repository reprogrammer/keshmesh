/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {

	final static A a = new A();

	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		synchronized (new Object()) {
			a.toString();
		}
	}
}