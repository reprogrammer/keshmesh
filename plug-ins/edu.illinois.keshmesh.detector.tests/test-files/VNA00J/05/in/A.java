/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test ensures that Keshmesh treats static fields (including those that
 * are private) as potentially shared variables.
 */
public class A extends Thread {

	private static int counter = 0;

	@EntryPoint
	@Override
	public void run() {
		/* [VNA00J,01 */if (counter == 0) { /* ] */
			System.out.println("counter is 0.");
		}
	}

}