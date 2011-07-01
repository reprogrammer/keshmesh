/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import p.A;
import p.B;
import p.C;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * The intention of this test is to check if the fixer works correctly if there is synchronized
 * statement within a nested comment in the line that contains synchronized block.
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		Class l1 = new B().getClass();
		Class l2 = new C().getClass();
		/* [LCK02J,01,p.B.class */ /*synchronized /* (p.B.class) */synchronized (l1) {
			System.out.println("replace with p.B.class");
		}/* ] */
		synchronized (this) {
			System.err.println("don't replace");
		}
		/* [LCK02J,02,p.C.class */synchronized (l2) /*
													 * /*synchronized (l2):
													 * replace l2 with p.C.class
													 */{
			System.out.println("replace with p.C.class");
		}/* ] */
	}
}

class B {
}

class C {
}
