/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import java.util.Random;
import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * TODO: We need to update this test.
 * 
 */
public class A {

	@EntryPoint
	public static void main(String args[]) {
		new A().m();
	}

	private void m() {
		new Random();
		synchronized (this) {
		}
	}
}