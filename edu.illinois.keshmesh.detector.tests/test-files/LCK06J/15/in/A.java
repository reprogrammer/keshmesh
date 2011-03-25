/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test ensures that the detector does not report a false positive for
 * modifying an instance field of a static field, where the modification of the
 * instance field is protected by an instance lock.
 * 
 */
public class A {

	static B staticField = new B();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	void m() {
		synchronized (new Object()) {
			staticField.m();
		}
	}
}

class B {
	Object instanceField;

	synchronized void m() {
		instanceField = new Object();
	}
}