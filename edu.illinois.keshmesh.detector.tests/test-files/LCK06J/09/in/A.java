/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * TODO: We need to update this test.
 * 
 * This class tests the LCK06J detector with respect to nestings of synchronized
 * blocks on static and nonstatic locks.
 * 
 */
public class A {

	static Object staticLock = new Object();
	Object nonStaticLock = new Object();
	static int staticField;

	@EntryPoint
	public static void main(String args[]) {
		new A().m1();
		new A().m2();
	}

	void m1() {
		synchronized (staticLock) {
			synchronized (nonStaticLock) {
				staticField = 0;
			}
		}
	}

	void m2() {
		synchronized (nonStaticLock) {
			synchronized (staticLock) {
				staticField = 0;
			}
		}
	}

}