/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.walaconfig;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.ReceiverInstanceContext;

/**
 * 
 * FIXME: Rename the class.
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 * 
 */
public class CustomReceiverInstanceContextSelector implements ContextSelector {

	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
		if (receiver == null) {
			return caller.getContext();
		} else {
			return new ReceiverInstanceContext(receiver);
		}
	}

}
