/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.util.intset.BitVector;
import com.ibm.wala.util.intset.MutableMapping;
import com.ibm.wala.util.intset.OrdinalSetMapping;

import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;
import edu.illinois.keshmesh.util.Logger;
import edu.illinois.keshmesh.util.Modes;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JBugDetector extends BugPatternDetector {

	VNA00JIntermediateResults intermediateResults = new VNA00JIntermediateResults();

	private final OrdinalSetMapping<InstructionInfo> globalValues = MutableMapping.make();

	private final Map<CGNode, CGNodeInfo> cgNodeInfoMap = new HashMap<CGNode, CGNodeInfo>();

	@Override
	public IntermediateResults getIntermediateResults() {
		return intermediateResults;
	}

	@Override
	public BugInstances performAnalysis(IJavaProject javaProject, BasicAnalysisData basicAnalysisData) {
		this.javaProject = javaProject;
		this.basicAnalysisData = basicAnalysisData;
		Iterator<CGNode> cgNodesIter = this.basicAnalysisData.callGraph.iterator();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			Logger.log("CGNode: " + cgNode.getIR());
		}
		BugInstances bugInstances = new BugInstances();
		Collection<IClass> threadSafeClasses = getThreadSafeClasses();
		intermediateResults.setThreadSafeClasses(threadSafeClasses);

		populateMapOfunsafeInstructionsThatAccessUnprotectedFields();

		return bugInstances;
	}

	private Collection<IClass> getThreadSafeClasses() {
		Collection<IClass> threadSafeClasses = new HashSet<IClass>();
		Iterator<CGNode> cgNodesIter = basicAnalysisData.callGraph.iterator();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			if (isThreadSafe(cgNode)) {
				threadSafeClasses.add(cgNode.getMethod().getDeclaringClass());
			}
		}
		return threadSafeClasses;
	}

	private boolean isThreadSafe(CGNode cgNode) {
		IClass declaringClass = cgNode.getMethod().getDeclaringClass();
		if (implementsRunnableInterface(declaringClass) || extendsThreadClass(declaringClass)) {
			return true;
		} else {
			return AnalysisUtils.contains(javaProject, cgNode, new InstructionFilter() {
				@Override
				public boolean accept(InstructionInfo instructionInfo) {
					return instructionInfo.getInstruction() instanceof SSAMonitorInstruction;
				}
			});
		}
	}

	private boolean extendsThreadClass(IClass klass) {
		IClass superclass = klass.getSuperclass();
		if (superclass == null) {
			return false;
		}
		if (isThreadClass(superclass)) {
			return true;
		}
		return extendsThreadClass(superclass);
	}

	private boolean implementsRunnableInterface(IClass klass) {
		for (IClass implementedInterface : klass.getAllImplementedInterfaces()) {
			if (isRunnableInterface(implementedInterface)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRunnableInterface(IClass interfaceClass) {
		return AnalysisUtils.getEnclosingNonanonymousClassName(interfaceClass.getName()).equals("java.lang.Runnable");
	}

	private boolean isThreadClass(IClass klass) {
		return AnalysisUtils.getEnclosingNonanonymousClassName(klass.getName()).equals("java.lang.Thread");
	}

	/**
	 * 
	 * This method is based on
	 * LCK06JBugDetector#populateSynchronizedBlocksForNode.
	 * 
	 * @param synchronizedBlocks
	 * @param cgNode
	 * @param synchronizedBlockKind
	 */
	private void populateSynchronizedBlocksForNode(Collection<InstructionInfo> synchronizedBlocks, final CGNode cgNode) {
		AnalysisUtils.collect(javaProject, synchronizedBlocks, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				SSAInstruction instruction = instructionInfo.getInstruction();
				if (AnalysisUtils.isMonitorEnter(instruction)) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * This method is based on
	 * LCK06JBugDetector#populateUnsafeModifyingStaticFieldsInstructionsMap.
	 */
	private void populateMapOfunsafeInstructionsThatAccessUnprotectedFields() {
		Map<CGNode, Collection<InstructionInfo>> intermediateMapOfUnsafeInstructions = null;

		if (!Modes.isInProductionMode()) {
			intermediateMapOfUnsafeInstructions = new HashMap<CGNode, Collection<InstructionInfo>>();
		}

		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			BitVector bitVector = new BitVector();
			Collection<InstructionInfo> synchronizedBlocks = new HashSet<InstructionInfo>();
			Collection<InstructionInfo> unsafeInstructionsThatAccessUnprotectedFields = new HashSet<InstructionInfo>();
			if (canContainUnsafeAccesses(cgNode.getMethod())) {
				Collection<InstructionInfo> instructionsThatAccessUnprotectedFields = getInstructionsThatAccessUnprotectedFields(cgNode);
				populateSynchronizedBlocksForNode(synchronizedBlocks, cgNode);
				for (InstructionInfo instructionThatAccessesUnprotectedFields : instructionsThatAccessUnprotectedFields) {
					if (!AnalysisUtils.isProtectedByAnySynchronizedBlock(synchronizedBlocks, instructionThatAccessesUnprotectedFields)) {
						unsafeInstructionsThatAccessUnprotectedFields.add(instructionThatAccessesUnprotectedFields);
					}
				}
				for (InstructionInfo instructionThatAccessesUnprotectedFields : instructionsThatAccessUnprotectedFields) {
					Logger.log("MODIFY: " + instructionThatAccessesUnprotectedFields);
				}
				for (InstructionInfo unsafeInstruction : unsafeInstructionsThatAccessUnprotectedFields) {
					bitVector.set(globalValues.add(unsafeInstruction));
					Logger.log("UNSAFE INSTRUCTION: " + unsafeInstruction);
				}
			}
			cgNodeInfoMap.put(cgNode, new CGNodeInfo(synchronizedBlocks, bitVector));

			if (!Modes.isInProductionMode() && intermediateMapOfUnsafeInstructions != null) {
				intermediateMapOfUnsafeInstructions.put(cgNode, unsafeInstructionsThatAccessUnprotectedFields);
			}
		}

		if (!Modes.isInProductionMode()) {
			intermediateResults.setUnsafeInstructionsThatAccessUnprotectedFields(intermediateMapOfUnsafeInstructions);
		}
	}

	private boolean canContainUnsafeAccesses(IMethod method) {
		return !method.isSynchronized() && !isIgnoredClass(method.getDeclaringClass());
	}

	/**
	 * 
	 * See LCK06JBugDetector#isIgnoredClass
	 * 
	 * @param klass
	 * @return
	 */
	private boolean isIgnoredClass(IClass klass) {
		return AnalysisUtils.isJDKClass(klass);
	}

	/**
	 * 
	 * See LCK06JBugDetector#getModifyingStaticFieldsInstructions
	 * 
	 * @param cgNode
	 * @return
	 */
	private Collection<InstructionInfo> getInstructionsThatAccessUnprotectedFields(CGNode cgNode) {
		Collection<InstructionInfo> instructionsThatAccessUnprotectedFields = new HashSet<InstructionInfo>();
		IR ir = cgNode.getIR();
		if (ir == null) {
			return instructionsThatAccessUnprotectedFields;
		}
		final DefUse defUse = new DefUse(ir);

		AnalysisUtils.collect(javaProject, instructionsThatAccessUnprotectedFields, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				return doesAccessUnprotectedField(defUse, instructionInfo.getInstruction());
			}
		});
		return instructionsThatAccessUnprotectedFields;
	}

	/**
	 * 
	 * See LCK06JBugDetector#canModifyStaticField.
	 * 
	 * @param defUse
	 * @param ssaInstruction
	 * @return
	 */
	private boolean doesAccessUnprotectedField(final DefUse defUse, SSAInstruction ssaInstruction) {
		for (int i = 0; i < ssaInstruction.getNumberOfUses(); i++) {
			SSAInstruction defInstruction = defUse.getDef(ssaInstruction.getUse(i));
			if (defInstruction instanceof SSAGetInstruction && !isProtected((SSAFieldAccessInstruction) defInstruction)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param fieldAccessInstruction
	 * @return true if the changes to the given field are visible to other
	 *         threads.
	 */
	private boolean isProtected(SSAFieldAccessInstruction fieldAccessInstruction) {
		return isFinal(fieldAccessInstruction) || isVolatile(fieldAccessInstruction);
	}

	/**
	 * 
	 * See LCK06JBugDetector#isStaticNonFinal.
	 * 
	 * @param fieldAccessInstruction
	 * @return
	 */
	private boolean isFinal(SSAFieldAccessInstruction fieldAccessInstruction) {
		IField accessedField = basicAnalysisData.classHierarchy.resolveField(fieldAccessInstruction.getDeclaredField());
		return accessedField.isFinal();
	}

	private boolean isVolatile(SSAFieldAccessInstruction fieldAccessInstruction) {
		IField accessedField = basicAnalysisData.classHierarchy.resolveField(fieldAccessInstruction.getDeclaredField());
		return accessedField.isVolatile();
	}
}
