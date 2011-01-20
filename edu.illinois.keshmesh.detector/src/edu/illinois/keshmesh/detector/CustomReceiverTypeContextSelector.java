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
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class CustomReceiverTypeContextSelector implements ContextSelector {

	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
		if (receiver == null) {
			//Provide a distinguishing context even when the receiver is null (e.g. in case of an invocation of a static method)
			return caller.getContext();
		}
		PointType pointType = new PointType(receiver.getConcreteType());
		return new JavaTypeContext(pointType);
	}
}
