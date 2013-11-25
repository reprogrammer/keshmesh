/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.walaconfig;

import com.ibm.wala.analysis.reflection.JavaTypeContext;
import com.ibm.wala.analysis.typeInference.PointType;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.intset.EmptyIntSet;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.IntSetUtil;

import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 * 
 */
public class KObjectSensitiveContextSelector implements ContextSelector {

	private final int objectSensitivityLevel;

	public KObjectSensitiveContextSelector(int objectSensitivityLevel) {
		this.objectSensitivityLevel = objectSensitivityLevel;
	}

	public static final ContextKey RECEIVER_STRING = new ContextKey() {
		@Override
		public String toString() {
			return "RECEIVER_STRING_KEY";
		}
	};

	@Override
	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey[] actualParameters) {
		if (actualParameters == null || actualParameters.length == 0 || actualParameters[0] == null) {
			//Provide a distinguishing context even when the receiver is null (e.g. in case of an invocation of a static method)
			return caller.getContext();
		}

		InstanceKey receiver = actualParameters[0];

		if (AnalysisUtils.isLibraryClass(callee.getDeclaringClass()) || (AnalysisUtils.isJDKClass(callee.getDeclaringClass()) && !AnalysisUtils.isObjectGetClass(callee))) {
			//Note: new Random() and similar statements cause an infinite pointer analysis for contexts like CallerSiteContext(caller, site)
			PointType pointType = new PointType(receiver.getConcreteType());
			return new JavaTypeContext(pointType);
		} else {
			ReceiverString receiverString;
			if (!(caller.getContext() instanceof ReceiverStringContext)) {
				receiverString = new ReceiverString(receiver);
			} else {
				ReceiverString callerReceiverString = (ReceiverString) ((ReceiverStringContext) caller.getContext()).get(RECEIVER_STRING);
				receiverString = new ReceiverString(receiver, objectSensitivityLevel, callerReceiverString);
			}
			return new ReceiverStringContext(receiverString);
		}
	}

	private static final IntSet receiver = IntSetUtil.make(new int[] { 0 });

	@Override
	public IntSet getRelevantParameters(CGNode caller, CallSiteReference site) {
		if (site.isDispatch() || site.getDeclaredTarget().getNumberOfParameters() > 0) {
			return receiver;
		} else {
			return EmptyIntSet.instance;
		}
	}

}
