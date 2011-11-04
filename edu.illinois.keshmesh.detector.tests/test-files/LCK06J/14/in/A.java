/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test demonstrates a false positive reported by the detector of LCK06J.
 * The flow insensitivity of the points-to analysis of WALA is the root cause of
 * this false alaram.
 * 
 */
public class A {

	static B staticField = new B();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	void m() {
		/* [LCK06J,01,p.A.staticField */synchronized (new Object()) {
			B oldValue = staticField;
			staticField = new B();
			oldValue.nonStaticField = null;
		}
		/* ] */
	}

}

class B {

	B nonStaticField;

}
