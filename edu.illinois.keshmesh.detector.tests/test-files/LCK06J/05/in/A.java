/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {

	static Object o = new Object();

	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		/*[LCK06J,01*/synchronized (new Object()) {
			o.toString();
		}/*]*/
	}
}