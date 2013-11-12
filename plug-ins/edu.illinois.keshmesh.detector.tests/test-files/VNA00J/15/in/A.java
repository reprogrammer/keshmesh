/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test ensures that the detector correctly identifies the variables that
 * are reachable from method parameters or static field.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		A a = new A();
		D d = new D(new B());
		a.m1(d);
		a.m2(d);
		a.m3();
	}

	void m1(D d) {
		B b = d.b;
		/* [VNA00J,01 */b.increment();/* ] */
	}

	void m2(D d) {
		B b = new B();
		b.increment();
	}
	
	void m3() {
		new C().m();
	}
	
}

class D {
	
	B b;
	
	D(B b) {
		this.b = b;
	}
	
}

class C {

	static B b = new B();
	
	void m() {
		/* [VNA00J,02 */b.increment();/* ] */
	}
	
}

class B implements Runnable {
	
	int counter = 0;
	
	void increment() {
		/* [VNA00J,03 */counter++;/* ] */
	}
	
	public void run() {}

}