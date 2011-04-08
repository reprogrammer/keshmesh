/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {
	final Lock lock = new ReentrantLock();
	final Condition condition = lock.newCondition();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {

		/*[LCK03J,01,java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject*/synchronized (condition) {
			System.out.println("no replace");
		}/*]*/
	}
}
