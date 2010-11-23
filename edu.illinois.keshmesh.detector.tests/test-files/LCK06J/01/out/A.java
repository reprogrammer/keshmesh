/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class A {

	private static int counter = 0;
	private static B static_assigned;
	private B instance_assigned;
	private static B static_unassigned;
	private B instance_unassigned;
	private static A a;

	public static void main(String args[]) {
		a = new A();
		a.m2();
		static_assigned = new B(a);
		m();
	}

	void m2() {
		instance_assigned = new B(new A());
		synchronized(instance_assigned) {
			static_assigned = new B(new A());
		}
		synchronized(static_assigned) {
			counter++;
		}
		synchronized(static_unassigned) {
			counter++;
		}
		synchronized(instance_unassigned) {
			counter++;
		}
	}
	
	private static void m() {
		Object obj = new Object();
		synchronized (obj) {
			/* [LCK06J,01,p.A.class */synchronized (obj) {
				static_assigned.set(10);
			}/* ] */
		}
	}

}