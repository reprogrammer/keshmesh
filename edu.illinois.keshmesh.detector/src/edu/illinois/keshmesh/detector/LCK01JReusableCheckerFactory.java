/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

/**
 * 
 * @author Samira Tasharofi
 */

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

import edu.illinois.keshmesh.detector.util.AnalysisUtils;

public class LCK01JReusableCheckerFactory {
	private static final String JAVA_LANG_INTEGER = "java.lang.Integer";
	private static final String JAVA_LANG_STRING = "java.lang.String";
	private static final String JAVA_LANG_BOOLEAN = "java.lang.Boolean";
	private static final String JAVA_LANG_LONG = "java.lang.Long";
	private static final String JAVA_LANG_SHORT = "java.lang.Short";
	private static final String JAVA_LANG_FLOAT = "java.lang.Float";
	private static final String JAVA_LANG_DOUBLE = "java.lang.Double";
	private static final String JAVA_LANG_BYTE = "java.lang.Byte";

	public static LCK01JReusableChecker createReusableChecker(InstanceKey instanceKey) {
		String javaType = AnalysisUtils.walaTypeNameToJavaName(instanceKey.getConcreteType().getName());
		if (javaType.equals(JAVA_LANG_INTEGER))
			return new ReusableIntegerChecker(instanceKey);
		else if (javaType.equals(JAVA_LANG_BOOLEAN))
			return new ReusableBooleanChecker(instanceKey);
		else if (javaType.equals(JAVA_LANG_STRING))
			return new ReusableStringChecker(instanceKey);
		else if (javaType.equals(JAVA_LANG_LONG))
			return new ReusableLongChecker(instanceKey);
		else if (javaType.equals(JAVA_LANG_SHORT))
			return new ReusableShortChecker(instanceKey);
		else if (javaType.equals(JAVA_LANG_FLOAT))
			return new ReusableFloatChecker(instanceKey);
		else if (javaType.equals(JAVA_LANG_DOUBLE))
			return new ReusableDoubleChecker(instanceKey);
		else if (javaType.equals(JAVA_LANG_BYTE))
			return new ReusableByteChecker(instanceKey);
		else
			return new NullLCK01ReusableChecker();
	}
}
