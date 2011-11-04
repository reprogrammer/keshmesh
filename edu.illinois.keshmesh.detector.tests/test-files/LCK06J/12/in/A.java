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

	static A staticField = new A();
	A nonStaticField;

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	void m() {
		/* [LCK06J,01,p.A.staticField */synchronized (new Object()) {
			A localVariable = staticField;
			localVariable.nonStaticField = null;
		}/* ] */
	}

}