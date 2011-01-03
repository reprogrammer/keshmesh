/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import com.ibm.wala.analysis.reflection.JavaTypeContext;
import com.ibm.wala.analysis.typeInference.PointType;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 * 
 */
public class CustomReceiverInstanceContextSelector implements ContextSelector {

	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
		if (receiver == null) {
			return Everywhere.EVERYWHERE;
		} else if (AnalysisUtils.isJDKClass(callee.getDeclaringClass()) && !AnalysisUtils.isObjectGetClass(callee)) {
			//Provide a distinguishing context even when the receiver is null (e.g. in case of an invocation of a static method)
			//Note: new Random() and similar statements cause an infinite pointer analysis for contexts like CallerSiteContext(caller, site)
			PointType pointType = new PointType(receiver.getConcreteType());
			return new JavaTypeContext(pointType);
		} else {
			PointType pointType = new PointType(receiver.getConcreteType());
			return new JavaTypeContext(pointType);
			//			return new ReceiverInstanceContext(receiver);
		}
	}
}
