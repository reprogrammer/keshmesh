/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test case checks that the detector doesn't duplicate the bug instances
 * in a method that is invoked by multiple methods. Also, it makes sure that all
 * the bug instances in a method are reported. This includes both the bug
 * instances that are produced by a statement in the method and the ones that
 * are caused by a statement in one of the callees.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m1();
		new A().m2();
		new A().m3();
		new A().m4();
		new A().m5();
	}

	static int f;

	synchronized void m1() {
		/* [LCK06J,01,p.A.f */f = 0;/* ] */
	}

	void m2() {
		/* [LCK06J,02,p.A.f */synchronized (this) {
			f = 1;
		}/* ] */
	}

	synchronized static void m3() {
		f = 0;
	}

	synchronized void m4() {
		/* [LCK06J,03,p.A.f */m1(); /* ] */
	}

	void m5() {
		/* [LCK06J,04,p.A.f */synchronized (this) {
			m1();
		} /* ] */
	}

}