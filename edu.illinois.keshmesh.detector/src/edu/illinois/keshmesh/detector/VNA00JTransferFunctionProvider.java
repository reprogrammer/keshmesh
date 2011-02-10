/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.BitVectorUnion;
import com.ibm.wala.dataflow.graph.BitVectorUnionVector;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixpoint.BitVectorVariable;
import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.util.intset.BitVector;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;

import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * This class is based on LCK06JTransferFunctionProvider.
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JTransferFunctionProvider implements ITransferFunctionProvider<CGNode, BitVectorVariable> {

	private final IJavaProject javaProject;
	private final CallGraph callGraph;
	private final Map<CGNode, CGNodeInfo> cgNodeInfoMap;

	public VNA00JTransferFunctionProvider(IJavaProject javaProject, CallGraph callGraph, Map<CGNode, CGNodeInfo> cgNodeInfoMap) {
		this.javaProject = javaProject;
		this.callGraph = callGraph;
		this.cgNodeInfoMap = cgNodeInfoMap;
	}

	@Override
	public AbstractMeetOperator<BitVectorVariable> getMeetOperator() {
		return BitVectorUnion.instance();
	}

	@Override
	public boolean hasEdgeTransferFunctions() {
		return true;
	}

	/**
	 * This method is the same as
	 * LCK06JTransferFunctionProvider#getEdgeTransferFunction except that the
	 * conditions of the if statements are different.
	 */
	@Override
	public UnaryOperator<BitVectorVariable> getEdgeTransferFunction(CGNode src, CGNode dst) {
		if (!dst.getMethod().isSynchronized()) {
			CGNodeInfo srcNodeInfo = cgNodeInfoMap.get(src);
			CGNodeInfo dstNodeInfo = cgNodeInfoMap.get(dst);
			Iterator<CallSiteReference> callSitesIterator = callGraph.getPossibleSites(dst, src);
			IR dstIR = dst.getIR();
			while (callSitesIterator.hasNext()) {
				CallSiteReference callSiteReference = callSitesIterator.next();
				IntSet callInstructionIndices = dstIR.getCallInstructionIndices(callSiteReference);
				IntIterator instructionIndicesIterator = callInstructionIndices.intIterator();
				while (instructionIndicesIterator.hasNext()) {
					int invokeInstructionIndex = instructionIndicesIterator.next();
					InstructionInfo instructionInfo = new InstructionInfo(javaProject, dst, invokeInstructionIndex);
					if (!AnalysisUtils.isProtectedByAnySynchronizedBlock(dstNodeInfo.getSafeSynchronizedBlocks(), instructionInfo)) {
						return new BitVectorUnionVector(srcNodeInfo.getBitVector());
					}
				}
			}
		}
		return new BitVectorUnionVector(new BitVector());
	}

	@Override
	public boolean hasNodeTransferFunctions() {
		return false;
	}

	@Override
	public UnaryOperator<BitVectorVariable> getNodeTransferFunction(CGNode node) {
		return null;
	}

}
