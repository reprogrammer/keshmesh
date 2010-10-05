package edu.illinois.keshmesh.detector;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.ReceiverInstanceContext;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class CustomContextSelector implements ContextSelector {

	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
		if (receiver != null) {
			return new ReceiverInstanceContext(receiver);
		} else {
			//Provide a distinguishing context even when the receiver is null (e.g. in case of an invocation of a static method)
			//Note: new Random() and similar statements cause an infinite pointer analysis for contexts like CallerSiteContext(caller, site) 
			return caller.getContext();
		}
	}
}
