/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * We decided not to report unprotected accesses to fields that can only be
 * reached from local variables.
 * 
 * In this program, thread objects t1 and t2 invoke a method on the local
 * variable a that makes an unprotected accesses to a field. This test ensures
 * that our detector does not treat the variable a inside the anonymous classes
 * a local variable, and reports bugs due the accesses to fields of a in the
 * anonymous classes.
 * 
 * Anonymous classes can access final local variables of their enclosing
 * methods. In bytecode, such accesses are achieved by adding fields
 * corresponding to the local varaibles to the anonymous classes. So, by
 * examining the bytecode we can tell that the variable a is actually a field of
 * the anonymous class and not a local variable.
 * 
 * This program also checks that we don't report bugs because of the calls to
 * the Thread.start method. We shouldn't report unprotected access at the place
 * where we invoke Thread.start because such access cannot be made protected by
 * wrapping the call to Thread.start in a synchronized block.
 */
class A {

	private final Object lock = new Object();
	private int counter = 0;

	@EntryPoint
	public static void main(String args[]) {
		final A a = new A();
		Thread t1 = new Thread() {

			@Override
			public void run() {
				a.m();
			}
		};
		Thread t2 = new Thread() {

			@Override
			public void run() {
				a.m();
			}
		};
		t1.start();
		t2.start();
	}

	void m() {
		counter++;
		increment();
		synchronized (lock) {
			counter++;
			increment();
		}
	}

	void increment() {
		counter++;
	}

}