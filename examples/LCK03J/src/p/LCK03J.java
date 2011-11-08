/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import java.util.Random;
import java.util.concurrent.locks.*;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class LCK03J {
	final Lock lock = new ReentrantLock();
	final Condition condition = lock.newCondition();

	@EntryPoint
	public static void main(String args[]) {
		new LCK03J().m();
	}

	private void m() {

		synchronized (getLock()) {
			System.out.println("replace by lock.lock()");
		}

		synchronized (condition) {
			System.out.println("no replace");
		}

		synchronized (getLockOrCondition()) {
			System.out.println("no replace");
		}

	}

	private Lock getLock() {
		return lock;
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