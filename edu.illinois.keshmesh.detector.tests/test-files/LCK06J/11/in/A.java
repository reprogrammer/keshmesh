/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the LCK06J detector reports poorly synchronized
 * modifications to parts of static fields.
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
		/* [LCK06J,01,staticField */synchronized (new Object()) {
			staticField.nonStaticField = null;
		}/* ] */
	}

}