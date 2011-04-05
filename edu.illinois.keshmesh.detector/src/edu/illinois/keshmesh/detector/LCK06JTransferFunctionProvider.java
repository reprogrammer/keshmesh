/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Iterator;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.BitVectorIdentity;
import com.ibm.wala.dataflow.graph.BitVectorKillAll;
import com.ibm.wala.dataflow.graph.BitVectorUnion;
import com.ibm.wala.dataflow.graph.BitVectorUnionVector;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixpoint.BitVectorVariable;
import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;

import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JTransferFunctionProvider implements ITransferFunctionProvider<CGNode, BitVectorVariable> {

	private final LCK06JBugDetector lck06jBugDetector;

	public LCK06JTransferFunctionProvider(LCK06JBugDetector lck06BugDetector) {
		this.lck06jBugDetector = lck06BugDetector;
	}

	@Override
	public AbstractMeetOperator<BitVectorVariable> getMeetOperator() {
		return BitVectorUnion.instance();
	}

	@Override
	public boolean hasEdgeTransferFunctions() {
		return true;
	}

	@Override
	public UnaryOperator<BitVectorVariable> getEdgeTransferFunction(CGNode src, CGNode dst) {
		if (!lck06jBugDetector.isSafeSynchronized(dst)) {
			CGNodeInfo dstNodeInfo = lck06jBugDetector.getCGNodeInfoMap().get(dst);
			Iterator<CallSiteReference> callSitesIterator = lck06jBugDetector.basicAnalysisData.callGraph.getPossibleSites(dst, src);
			IR dstIR = dst.getIR();
			while (callSitesIterator.hasNext()) {
				CallSiteReference callSiteReference = callSitesIterator.next();
				IntSet callInstructionIndices = dstIR.getCallInstructionIndices(callSiteReference);
				IntIterator instructionIndicesIterator = callInstructionIndices.intIterator();
				while (instructionIndicesIterator.hasNext()) {
					int invokeInstructionIndex = instructionIndicesIterator.next();
					InstructionInfo instructionInfo = new InstructionInfo(lck06jBugDetector.javaProject, dst, invokeInstructionIndex);
					if (!AnalysisUtils.isProtectedByAnySynchronizedBlock(dstNodeInfo.getSafeSynchronizedBlocks(), instructionInfo)) {
						return BitVectorIdentity.instance();
					}
				}
			}
		}
		return BitVectorKillAll.instance();
	}

	@Override
	public boolean hasNodeTransferFunctions() {
		return true;
	}

	@Override
	public UnaryOperator<BitVectorVariable> getNodeTransferFunction(CGNode node) {
		return new BitVectorUnionVector(lck06jBugDetector.getCGNodeInfoMap().get(node).getBitVector());
	}

}
