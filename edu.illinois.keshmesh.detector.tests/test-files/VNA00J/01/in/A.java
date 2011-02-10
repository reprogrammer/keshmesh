/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {

	private int counter = 0;

	@EntryPoint
	public static void main(String args[]) {
		A a = new A();
		a.m();
	}

	void m() {
		/* [VNA00J,02 */counter++;/* ] */
		/* [VNA00J,03 */increment();/* ] */
		synchronized (new Object()) {
			counter++;
			increment();
		}
	}

	void increment() {
		/* [VNA00J,04 */counter++;/* ] */
	}

}