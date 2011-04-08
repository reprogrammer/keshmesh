/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.*;

public class A {

	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock read = readWriteLock.readLock();

	public static void main(String args[]) {
		new A().m();
	}

	private void m() {

		/* [LCK03J,01,java.util.concurrent.ReadLock */synchronized (read) {
			System.out.println("Replace with read.lock()");

			/*
			 * [LCK03J,02,java.util.concurrent.locks.ReentrantLock,
			 * java.util.concurrent
			 * .locks.AbstractQueuedSynchronizer.ConditionObject
			 */synchronized (getLockOrCondition()) {
				System.out.println("No replace");
			}/* ] */

		}/* ] */

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