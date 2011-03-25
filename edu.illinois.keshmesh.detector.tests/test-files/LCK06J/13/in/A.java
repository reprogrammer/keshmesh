/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the LCK06J detector reports indirect modifications to
 * parts of static fields that are poorly synchronized.
 * 
 */
public class A {

	static B staticField = new B();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	void m() {
		/* [LCK06J,01,staticField */synchronized (new Object()) {
			B localVariable = staticField;
			localVariable.nonStaticField = null;
		}/* ] */
	}

}

class B {

	B nonStaticField;

}