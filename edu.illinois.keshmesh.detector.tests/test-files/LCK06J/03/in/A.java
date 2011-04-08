/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This simple test checks for direct modifications of static fields using a
 * nonstatic lock.
 * 
 */
public class A {

	static Object o = new A();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		/* [LCK06J,01,p.A.o */synchronized (new Object()) {
			o = new A();
		} /* ] */
	}
}