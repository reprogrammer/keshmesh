/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks the the detector propagates unsafe instructions along method
 * invocations correctly.
 * 
 */
public class A {

	static Object o = new Object();

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
		synchronized (o) {
			m3();
		}
	}

	void m3() {
		/* [LCK06J,01,p.A.o */synchronized (new Object()) {
			m4();
		} /* ] */
	}

	void m4() {
		o = new Object();
	}

}