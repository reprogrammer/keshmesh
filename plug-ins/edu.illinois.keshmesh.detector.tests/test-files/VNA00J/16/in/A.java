/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test is for identifying the instructions that may access shared field.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new C().accessNonLocally();
		new C().accessLocally();
		new B().increment();
	}

}

class C {

	B b = new B();

	void accessNonLocally() {
		/* [VNA00J,01 */b.counter = 1;/* ] */
		if (b.finalCounter == 0) {
			b.volatileCounter = 1;
		}
	}

	void accessLocally() {
		B b = new B();
		b.counter = 1;
	}

}

class B implements Runnable {

	int counter = 0;

	volatile int volatileCounter = 0;

	final int finalCounter = 0;

	void increment() {
		/* [VNA00J,02 */counter++;/* ] */
	}

	public void run() {
	}

}