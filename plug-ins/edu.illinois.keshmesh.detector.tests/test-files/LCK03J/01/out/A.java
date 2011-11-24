/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import java.util.concurrent.locks.*;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {
	private final Lock lock = new ReentrantLock();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {

		java.util.concurrent.locks.Lock tempLock = lock;
		try {
		tempLock.lock();
		System.out.println("replace by lock.lock()");
		int i=3;
		} finally {
		tempLock.unlock();
		}
	}
}