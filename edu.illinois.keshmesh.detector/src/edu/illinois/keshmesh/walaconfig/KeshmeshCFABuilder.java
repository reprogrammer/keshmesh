/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.walaconfig;

import com.ibm.wala.cast.java.ipa.callgraph.AstJavaZeroOneContainerCFABuilder;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class KeshmeshCFABuilder extends AstJavaZeroOneContainerCFABuilder {

	public KeshmeshCFABuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache, ContextSelector appContextSelector, SSAContextInterpreter appContextInterpreter) {
		super(cha, options, cache, appContextSelector, appContextInterpreter);
	}

	@Override
	protected ZeroXInstanceKeys makeInstanceKeys(IClassHierarchy cha, AnalysisOptions options, SSAContextInterpreter contextInterpreter) {
		ZeroXInstanceKeys zik = new ZeroXInstanceKeys(options, cha, contextInterpreter, ZeroXInstanceKeys.ALLOCATIONS | ZeroXInstanceKeys.SMUSH_MANY | ZeroXInstanceKeys.SMUSH_THROWABLES);
		return zik;
	}

}
