/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import com.ibm.wala.ssa.SSAInstruction;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public interface InstructionFilter {
	public boolean accept(SSAInstruction ssaInstruction);
}