/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import java.util.concurrent.locks.*;
import java.util.Random;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * This test examines the case in which there are nested synchronized blocks and
 * some of them are bug patterns from which some can be fixed.
 * 
 */
public class A {

	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock read = readWriteLock.readLock();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {

		/* [LCK03J,01,java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock */synchronized (read) {
			System.out.println("Replace with tempLock.lock()");
			/* ] */

			/*[LCK03J,02,java.util.concurrent.locks.ReentrantLock, java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject  */synchronized (getLockOrCondition()) {
				System.out.println("No replace");
			}/* ] */

		}

		Object object = new Object();
		synchronized (object) {
			System.out.println("No Bug Pattern");
		}
	}

	private Object getLockOrCondition() {
		Lock lock = new ReentrantLock();
		Condition condition = lock.newCondition();
		Random random = new Random();

		if (random.nextBoolean())
			return lock;
		else
			return condition;
	}
}
