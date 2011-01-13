/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class B {
	
	@EntryPoint
	public static void main(String args[]) {
		new B().m();
	}

	private void m() {
		/*[LCK02J,03,p.B.class*/synchronized (p.B.class) {
			System.err.println("replace with p.B.class");
		}/*]*/
		Class l1 = new A().getClass();
		Class l2 = new C().getClass();
		Class l = (1 > 2) ? l1 : l2;
		/*[LCK02J,04,p.A.class,p.C.class*/synchronized (l) {
			System.err.println("multiple replacements: p.A.class, p.C.class");
		}/*]*/
	}
}