/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test ensures that the detector does not mark method invocations whose
 * arguments are all local variables. Such methods do not access the fields of
 * shared variables, and should not be marked as problematic.
 * 
 */
public class A {

	private int counter = 0;

	@EntryPoint
	public static void main(String args[]) {
		A a = new A();
		a.m();
	}

	void m() {
		/* [VNA00J,01 */counter++;/* ] */
		/* [VNA00J,02 */increment();/* ] */
		synchronized (new Object()) {
			counter++;
			increment();
		}
	}

	void increment() {
		/* [VNA00J,03 */counter++;/* ] */
	}

}