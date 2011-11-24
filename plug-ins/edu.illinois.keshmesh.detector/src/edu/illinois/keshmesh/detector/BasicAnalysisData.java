/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import com.ibm.wala.analysis.pointers.BasicHeapGraph;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class BasicAnalysisData {

	public final IClassHierarchy classHierarchy;
	public final CallGraph callGraph;
	public final PointerAnalysis pointerAnalysis;
	public final HeapModel heapModel;
	public final BasicHeapGraph basicHeapGraph;

	public BasicAnalysisData(IClassHierarchy classHierarchy, CallGraph callGraph, PointerAnalysis pointerAnalysis, HeapModel heapModel, BasicHeapGraph basicHeapGraph) {
		this.classHierarchy = classHierarchy;
		this.callGraph = callGraph;
		this.pointerAnalysis = pointerAnalysis;
		this.heapModel = heapModel;
		this.basicHeapGraph = basicHeapGraph;
	}

}
