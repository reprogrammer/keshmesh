/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
public class LCK03JFixInformation implements FixInformation {

	private Set<String> typeNames;
	private boolean isLock;

	public LCK03JFixInformation(Set<String> typeNames, boolean isLock) {
		this(typeNames);
		this.isLock = isLock;
	}

	public LCK03JFixInformation(Set<String> typeNames) {
		this.typeNames = typeNames;
	}

	public Set<String> getTypeNames() {
		return typeNames;
	}

	public boolean isLock() {
		return isLock;
	}

	@Override
	public FixInformation merge(FixInformation other) {
		if (!(other instanceof LCK03JFixInformation)) {
			throw new AssertionError("Only FixInformation's of the same types can be merged");
		}
		LCK03JFixInformation otherLCK03JFixInformation = (LCK03JFixInformation) other;
		Set<String> allTypeNames = new HashSet<String>(typeNames);
		allTypeNames.addAll(otherLCK03JFixInformation.getTypeNames());
		return new LCK03JFixInformation(allTypeNames);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typeNames == null) ? 0 : typeNames.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LCK03JFixInformation other = (LCK03JFixInformation) obj;
		if (typeNames == null) {
			if (other.typeNames != null)
				return false;
		} else if (!typeNames.equals(other.typeNames))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LCK03JFixInformation [typeNames=" + typeNames + "]";
	}

}
