package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that the LCK06J detector reports indirect modifications to
 * parts of static fields that are poorly synchronized.
 * 
 */
class A {

	private Object lock = new Object();
	static B staticField = new B();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	void m() {
		synchronized (lock) {
			B localVariable = staticField;
			localVariable.nonStaticField = null;
		}
	}

}

class B {

	B nonStaticField;

}