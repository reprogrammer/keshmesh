/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test is designed to demonstrate LCK02J and how Keshmesh finds instances
 * of LCK02J interprocedurally.
 * 
 */
public class A {

	static int sharedCounter;

	@EntryPoint
	public static void main(String args[]) {
		new Thread(new B()).start();
		new Thread(new C()).start();
	}

}

class B implements Runnable {

	@Override
	public void run() {
		/* [LCK02J,01,p.B.class,p.C.class */synchronized (getLock()) {
			++A.sharedCounter;
		}/* ] */
	}

	public Object getLock() {
		return getClass();
	}

}

class C extends B {

	@Override
	public void run() {
		super.run();
	}

}