/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test ensures that Keshmesh reports unprotected accesses to static fields
 * of local variables as instances of VNA00-J but not such accesses to nonstatic
 * fields. Accesses to nonstatic fields of local varaibles should not be
 * reported as bugs because local varaibles cannot be shared.
 * 
 */
public class A extends Thread {

	private int field = 0;

	private static int staticField = 0;

	@EntryPoint
	@Override
	public void run() {
		A localA = new A();
		if (localA.field == 0) { 
			System.out.println("localA.field is 0.");
		}
		/* [VNA00J,01 */if (localA.staticField == 0) { /* ] */
			System.out.println("localA.staticField is 0.");
		}
	}

}