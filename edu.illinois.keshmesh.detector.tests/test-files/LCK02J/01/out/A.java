/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		synchronized (p.A.class) {
			System.out.println("replace by p.A.class");
		}
	}
}