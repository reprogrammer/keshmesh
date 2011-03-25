/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * TODO: We need to update this test.
 * 
 * This test checks that the LCK06J detector correctly computes the 'is inside'
 * relation for synchronized blocks.
 * 
 */
public class A {

	Object nonStaticLock = new Object();
	static int staticField;

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	void m() {
		synchronized (nonStaticLock) {
		}
		staticField = 0;
	}

}