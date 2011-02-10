/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

public class A extends Thread {

	private volatile int counter = 0;

	@EntryPoint
	@Override
	public void run() {
		if (counter == 0) {
			System.out.println("counter is 0.");
		}
	}

}