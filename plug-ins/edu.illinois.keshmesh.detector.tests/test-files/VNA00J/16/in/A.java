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
		new B().accessNonLocally();
		new B().accessLocally();
		new C().increment();
	}

}

class B {

	C c1 = new C();

	void accessNonLocally() {
		/* [VNA00J,01 */c1.counter = 1;/* ] */
		if (c1.finalCounter == 0) {
			c1.staticVolatileCounter = 1;
		}
	}

	void accessLocally() {
		C c2 = new C();
		c2.counter = 1;
		/* [VNA00J,02 */c2.staticCounter = 1;/* ] */
	}

}

class C implements Runnable {

	int counter = 0;

	static volatile int staticVolatileCounter = 0;

	final int finalCounter = 0;

	static int staticCounter = 0;

	void increment() {
		/* [VNA00J,03 */counter++;/* ] */
	}

	public void run() {
	}

}