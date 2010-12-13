/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import java.util.Random;

public class A {

	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		new Random();
		synchronized (this) {
		}
	}
}