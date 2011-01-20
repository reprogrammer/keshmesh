/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class B {

	static int counter = 0;

	@EntryPoint
	public static void main(String args[]) {
		new B().m();
	}

	private void m() {
		/*[LCK02J,03,p.B.class*/synchronized (this.getClass()) {
			System.err.println("replace with p.B.class");
		}/*]*/
		Class l = getArbitraryClass();
		/*[LCK02J,04,p.A.class,p.C.class*/synchronized (l) {
			System.err.println("multiple replacements: p.A.class, p.C.class");
		}/*]*/
	}

	private Class getArbitraryClass() {
		if (counter++ == 0) {
			return new A().getClass();
		} else {
			return new C().getClass();
		}
	}

}