/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */

/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

/**
 * 
 * @author Samira Tasharofi
 */

import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.NormalAllocationInNode;

abstract class LCK01JReusableChecker {
	InstanceKey instanceKey;

	abstract boolean isReusable();

	boolean isAllocationInMethod(String className, String methodName) {
		if (instanceKey instanceof NormalAllocationInNode) {
			NormalAllocationInNode normalAllocationInNode = (NormalAllocationInNode) instanceKey;
			return (normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString().equals(className) && normalAllocationInNode.getNode().getMethod().getName().toString()
					.equals(methodName));
		} else
			return false;
	}

	boolean isAllocationInStaticInitializerOfClass(String className) {
		if (instanceKey instanceof NormalAllocationInNode) {
			NormalAllocationInNode normalAllocationInNode = (NormalAllocationInNode) instanceKey;
			return normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString().equals(className) && normalAllocationInNode.getNode().getMethod().isClinit();
		} else
			return false;
	}
}

class NullLCK01ReusableChecker extends LCK01JReusableChecker {

	@Override
	boolean isReusable() {
		return false;
	}

}

class ReusableStringChecker extends LCK01JReusableChecker {
	public ReusableStringChecker(InstanceKey instanceKey) {
		this.instanceKey = instanceKey;
	}

	public boolean isReusable() {
		if (instanceKey instanceof NormalAllocationInNode) {
			return isAllocationInMethod("Ljava/lang/String", "intern");
		} else
			return (instanceKey instanceof ConcreteTypeKey);
	}
}

class ReusableBooleanChecker extends LCK01JReusableChecker {
	public ReusableBooleanChecker(InstanceKey instanceKey) {
		this.instanceKey = instanceKey;
	}

	public boolean isReusable() {
		return isAllocationInStaticInitializerOfClass("Ljava/lang/Boolean");
	}
}

class ReusableIntegerChecker extends LCK01JReusableChecker {
	public ReusableIntegerChecker(InstanceKey instanceKey) {
		this.instanceKey = instanceKey;
	}

	public boolean isReusable() {
		return isAllocationInMethod("Ljava/lang/Integer", "valueOf");
	}
}

class ReusableLongChecker extends LCK01JReusableChecker {
	public ReusableLongChecker(InstanceKey instanceKey) {
		this.instanceKey = instanceKey;
	}

	public boolean isReusable() {
		return (isAllocationInStaticInitializerOfClass("Ljava/lang/Long$LongCache") || isAllocationInMethod("Ljava/lang/Long", "valueOf"));
	}
}

class ReusableShortChecker extends LCK01JReusableChecker {
	public ReusableShortChecker(InstanceKey instanceKey) {
		this.instanceKey = instanceKey;
	}

	public boolean isReusable() {
		return (isAllocationInStaticInitializerOfClass("Ljava/lang/Short$ShortCache") || isAllocationInMethod("Ljava/lang/Short", "valueOf"));
	}
}

class ReusableFloatChecker extends LCK01JReusableChecker {
	public ReusableFloatChecker(InstanceKey instanceKey) {
		this.instanceKey = instanceKey;
	}

	public boolean isReusable() {
		return isAllocationInMethod("Ljava/lang/Float", "valueOf");
	}
}

class ReusableDoubleChecker extends LCK01JReusableChecker {
	public ReusableDoubleChecker(InstanceKey instanceKey) {
		this.instanceKey = instanceKey;
	}

	public boolean isReusable() {
		return isAllocationInMethod("Ljava/lang/Double", "valueOf");
	}
}

class ReusableByteChecker extends LCK01JReusableChecker {
	public ReusableByteChecker(InstanceKey instanceKey) {
		this.instanceKey = instanceKey;
	}

	public boolean isReusable() {
		return isAllocationInStaticInitializerOfClass("Ljava/lang/Byte$ByteCache");
	}
}
