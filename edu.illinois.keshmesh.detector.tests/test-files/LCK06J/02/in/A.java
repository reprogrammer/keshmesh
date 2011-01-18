/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A {
	
	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		synchronized (new Object()) {
			System.out.println("");
		}
	}
}