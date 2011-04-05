/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks different scenarios where static or instance fields get
 * modified either directly or indirectly.
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
		/* [LCK06J,01,p.A.static_assigned,p.A.counter */synchronized (new Object()) {
			m();
		}/* ] */
	}

	void m2() {
		instance_assigned = new B();
		/* [LCK06J,02,p.A.static_assigned */synchronized (instance_assigned) {
			static_assigned = new B();
		}/* ] */
		synchronized (static_assigned) {
			counter++;
			m3();
		}
		/* [LCK06J,03,p.A.counter */synchronized (static_unassigned) {
			counter++;
		}/* ] */
		/* [LCK06J,04,p.A.counter */synchronized (instance_unassigned) {
			counter++;
		}/* ] */
	}

	private static void m() {
		Object obj = new Object();
		/* [LCK06J,05,p.A.static_assigned */synchronized (obj) {
			/* [LCK06J,06,p.A.static_assigned */synchronized (obj) {
				static_assigned.set(10);
			}/* ] */
		}/* ] */
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
