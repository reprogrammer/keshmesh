/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * We don't know why this test passes modulo the fix information.
 *
 */
public class A {

	private static int counter = 0;
	private static B static_assigned;
	private B instance_assigned;
	private static B static_unassigned;
	private B instance_unassigned;
	private static A a;

	@EntryPoint
	public static void main(String args[]) {
		a = new A();
		a.m2();
		static_assigned = new B();
		/*[LCK06J,01*/synchronized (new Object()) {
			m();
		}/*]*/
	}

	void m2() {
		instance_assigned = new B();
		/*[LCK06J,02*/synchronized (instance_assigned) {
			static_assigned = new B();
		}/*]*/
		synchronized (static_assigned) {
			counter++;
			m3();
		}
		/*[LCK06J,03*/synchronized (static_unassigned) {
			counter++;
		}/*]*/
		/*[LCK06J,04*/synchronized (instance_unassigned) {
			counter++;
		}/*]*/
	}

	private static void m() {
		Object obj = new Object();
		/*[LCK06J,05*/synchronized (obj) {
			/*[LCK06J,06*/synchronized (obj) {
				static_assigned.set(10);
			}/*]*/
		}/*]*/
		m3();
	}

	private static void m3() {
		counter++;
	}
}

class B {
	
	int i;

	public void set(int i) {
		this.i = i;
	}
}
