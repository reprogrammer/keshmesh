/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * The intention of this test is to check if the fixer works correctly if there
 * is synchronized statement within the comments in the line that contains
 * synchronized block and also within the comments in the synchronized block
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		/* [LCK02J,01,p.A.class */synchronized (/* synchronized (getClass()) */getClass()) {
			System.out.println("replace by p.A.class");
		}/* ] */
	}
}