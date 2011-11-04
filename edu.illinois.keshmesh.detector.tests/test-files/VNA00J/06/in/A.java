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
 * Note: If A extends Thread, the test fails because of a limitation of WALA.
 * The limitation is that all Thread objects in WALA point to each other,
 * perhaps because they all belong to a common container. In the following
 * program, if class A is a Thread, the instance of A in fakeRootMethod will
 * point to localA, because both are instances of Thread. This external
 * reference to the field makes the detector consider localA a nonlocal object.
 * As a result, the detector produces a false positive because it finds an
 * uprotected access to localA.field.
 * 
 */
public class A implements Runnable {

	private int field = 0;

	private static int staticField = 0;

	@EntryPoint
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