/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test case checks that the detector doesn't report accesses to volatile
 * fields as instances of VNA00J.
 * 
 */
public class A extends Thread {

	private volatile int counter;

	public A() {
		counter = 0;
	}

	@EntryPoint
	@Override
	public void run() {
		if (counter == 0) {
			System.out.println("counter is 0.");
		}
	}

}