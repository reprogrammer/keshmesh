/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.walaconfig;

import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 * 
 */
public class ReceiverString implements ContextItem {

	private final InstanceKey instances[];

	public ReceiverString(InstanceKey instanceKey) {
		this.instances = new InstanceKey[] { instanceKey };
	}

	ReceiverString(InstanceKey instanceKey, int max_length, ReceiverString base) {
		int instancesLength = Math.min(max_length, base.getCurrentLength() + 1);
		instances = new InstanceKey[instancesLength];
		instances[0] = instanceKey;
		System.arraycopy(base.instances, 0, instances, 1, Math.min(max_length - 1, base.getCurrentLength()));
	}

	private int getCurrentLength() {
		return instances.length;
	}

	public InstanceKey getReceiver() {
		return instances[0];
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("[");
		for (int i = 0; i < instances.length; i++) {
			str.append(" ").append(instances[i].toString());
		}
		str.append(" ]");
		return str.toString();
	}

	@Override
	public int hashCode() {
		int code = 11;
		for (int i = 0; i < instances.length; i++) {
			code *= instances[i].hashCode();
		}
		return code;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ReceiverString) {
			ReceiverString oc = (ReceiverString) o;
			if (oc.instances.length == instances.length) {
				for (int i = 0; i < instances.length; i++) {
					if (!(instances[i].equals(oc.instances[i]))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

}