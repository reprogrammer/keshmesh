/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * TODO: We need to update this test.
 *
 */
public class A {

	static Object o = new Object();

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		synchronized (new Object()) {
			o.toString();
		}
	}
}