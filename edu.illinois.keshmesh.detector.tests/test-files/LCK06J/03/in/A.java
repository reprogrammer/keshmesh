/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {

	static Object o = new A();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		/*[LCK06J,01*/synchronized (new Object()) {
			o.toString();
		}/*]*/
	}
}