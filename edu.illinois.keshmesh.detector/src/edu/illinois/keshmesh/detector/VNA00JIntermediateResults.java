/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Collection;
import java.util.Map;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JIntermediateResults extends IntermediateResults {

	private String threadSafeClasses;

	private String unsafeInstructionsThatAccessUnprotectedFields;

	public void setThreadSafeClasses(Collection<IClass> threadSafeClasses) {
		this.threadSafeClasses = getIntermediateResult(this.threadSafeClasses, threadSafeClasses);
	}

	public void setUnsafeInstructionsThatAccessUnprotectedFields(Map<CGNode, Collection<InstructionInfo>> intermediateMapOfUnsafeInstructions) {
		this.unsafeInstructionsThatAccessUnprotectedFields = getIntermediateResult(this.unsafeInstructionsThatAccessUnprotectedFields, intermediateMapOfUnsafeInstructions);
	}

	public String getThreadSafeClasses() {
		return threadSafeClasses;
	}

	public String getUnsafeInstructionsThatAccessUnprotectedFields() {
		return unsafeInstructionsThatAccessUnprotectedFields;
	}

}
