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
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.intset.EmptyIntSet;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.IntSetUtil;

/**
 * 
 * See com.ibm.wala.ipa.callgraph.propagation.ReceiverTypeContextSelector
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class CustomReceiverTypeContextSelector implements ContextSelector {

	@Override
	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey[] actualParameters) {
		if (actualParameters == null || actualParameters.length == 0 || actualParameters[0] == null) {
			//Provide a distinguishing context even when the receiver is null (e.g. in case of an invocation of a static method)
			return caller.getContext();
		}
		PointType pointType = new PointType(actualParameters[0].getConcreteType());
		return new JavaTypeContext(pointType);
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
