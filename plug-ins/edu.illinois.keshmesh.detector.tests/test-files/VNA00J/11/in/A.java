/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test ensures that the detector reports the problems in inner classes
 * correctly. This test is based on a pattern in <a href=
 * "https://raw.github.com/apache/cassandra/635310c4d54646f3ce8cfc7fe5219a9a90305168/src/java/org/apache/cassandra/concurrent/RetryingScheduledThreadPoolExecutor.java"
 * >Cassandra</a>.
 * 
 */
public class A {

	private int counter = 0;

	@EntryPoint
	public static void main(String args[]) {
		A a = new A();
		a.m();
	}

	void m() {
		Runnable task = new Runnable() {

			public void run() {
			}
		};

		new RunnableWrapper(task).run();
	}

	class RunnableWrapper implements Runnable {

		private Runnable task;

		public RunnableWrapper(Runnable task) {
			this.task = task;
		}

		public void run() {
			/* [VNA00J,01 */task.run();/* ] */
		}

	}
}

