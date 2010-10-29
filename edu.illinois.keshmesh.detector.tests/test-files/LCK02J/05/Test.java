/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

public class Test {

	int i;
	static C c;

	public static void main(String args[]) {
		Test test = new Test();
		c = test.new C();
		test.increment();
	}

	public Test() {
		i = 5;
	}

	private void increment() {
		Class l = c.getClass();
		if (1 < i) {
			l = new D().getClass();
		}
		synchronized (l) {
			i++;
		}
	}

	class C {

	}

	class D {

	}
}
