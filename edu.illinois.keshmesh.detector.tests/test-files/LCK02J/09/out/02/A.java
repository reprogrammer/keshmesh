/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * The intention of this test is to check if the fixer works correctly if there is any synchronized 
 * statement within the comments in the line that contains synchronized block
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		A.m();
	}

	private static void m() {
		A a = new A();
		Class l1 = a.new B().getClass();
		Class l2 = a.new C().getClass();
		/*[LCK02J,01,p.A.B.class*//*synchronized(l1)*/synchronized (l1) {
			System.out.println("replace with p.A.B.class");
		}/*]*/
		/*[LCK02J,02,p.A.C.class*/synchronized (p.A.C.class) /*synchronized(l2)*/{
			System.out.println("replace with p.A.C.class");
		}/*]*/
	}

	class B {
	}

	class C {
	}
}