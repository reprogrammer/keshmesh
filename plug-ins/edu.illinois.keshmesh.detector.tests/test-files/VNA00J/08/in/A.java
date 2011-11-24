/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test ensures that the detector does not report accesses to fields of
 * nonthread-safe classes.
 * 
 */
public class A {

	int i;

	@EntryPoint
	public static void main(String args[]) {
		new A().m1();
	}

	void m1() {
		i++;
	}
}