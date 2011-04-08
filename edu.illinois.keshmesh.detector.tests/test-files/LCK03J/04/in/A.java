/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import java.util.concurrent.locks.*;
import java.util.Random;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {
	
	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {

		/*[LCK03J,01,java.util.concurrent.locks.ReentrantLock, java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject  */synchronized (getLockOrCondition()) {
			System.out.println("No replace");
		}/* ] */
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