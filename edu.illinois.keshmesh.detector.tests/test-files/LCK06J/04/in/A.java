/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {

	int f; 
	
	final static A a = new A();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		/*[LCK06J,01*/synchronized (new Object()) {
			a.f=5;
		}/*]*/
	}
}