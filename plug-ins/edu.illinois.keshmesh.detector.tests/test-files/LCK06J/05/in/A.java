/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the detector finds modification of a static field through a method invocation.
 *
 */
public class A {

	static Object o = new Object();

	@EntryPoint
	public static void main(String args[]) {
		new A().m1();
	}

	void m1() {
		/* [LCK06J,01,p.A.o */synchronized (new Object()) {
			m2();
		}/* ] */
	}
	
	void m2() {
		o = new A();
	}
}