/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {

	public static void main(String args[]) {
		new A().m1();
		new A().m2();
	}

	static int f;
	
	private /*[LCK06J,01*/synchronized void m1() {
		f = 0;
	}/*]*/
	
	private void m2() {
		/*[LCK06J,02*/synchronized (this) {
			f = 1;
		}/*]*/
	}

}