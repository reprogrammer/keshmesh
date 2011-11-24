/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test ensures that the detector considers assigment as a kind of access.
 * 
 */
public class A {

	int i;

	@EntryPoint
	public static void main(String args[]) {
		new A().m1();
	}

	void m1() {
		synchronized (new Object()) {
			m2();
		}
	}

	void m2() {
		/* [VNA00J,01 */i = 0;/* ] */
	}
}