/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package p;

import java.util.LinkedList;
import java.util.List;

import edu.illinois.keshmesh.annotations.EntryPoint;

/**
 * 
 * This test checks that unprotected field accesses in JDK classes are
 * considered.
 * 
 */
public class A implements Runnable {

	private List<String> l = new LinkedList<String>();

	@EntryPoint
	@Override
	public void run() {
		/* [VNA00J,01 */l.add("0"); /* ] */
	}

}