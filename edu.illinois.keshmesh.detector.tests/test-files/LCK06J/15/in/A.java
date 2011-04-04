/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test ensures that the detector does not report a false positive for
 * modifying an instance field of a static field, where the modification of the
 * instance field is protected by an instance lock. In addition, this test
 * checks that synchronized blocks are treated the same way as synchronized
 * methods.
 * 
 */
public class A {

	static B staticField = new B();

	@EntryPoint
	public static void main(String args[]) {
		new A().m1();
		new A().m2();
	}

	void m1() {
		synchronized (new Object()) {
			staticField.m1();
		}
	}

	void m2() {
		synchronized (new Object()) {
			staticField.m2();
		}
	}

}

class B {
	Object instanceField;

	void m1() {
		synchronized (this) {
			instanceField = new Object();
		}
	}

	synchronized void m2() {
		instanceField = new Object();
	}

}