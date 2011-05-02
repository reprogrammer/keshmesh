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
public class LCK01JFixInformation implements FixInformation {

	Set<String> typeNames;

	public LCK01JFixInformation(Set<String> typeNames) {
		super();
		this.typeNames = typeNames;
	}

	public Set<String> getTypeNames() {
		return typeNames;
	}

	@Override
	public FixInformation merge(FixInformation other) {
		if (!(other instanceof LCK01JFixInformation)) {
			throw new AssertionError("Only FixInformation's of the same types can be merged");
		}
		LCK01JFixInformation otherLCK01JFixInformation = (LCK01JFixInformation) other;
		Set<String> allTypeNames = new HashSet<String>(typeNames);
		allTypeNames.addAll(otherLCK01JFixInformation.getTypeNames());
		return new LCK01JFixInformation(allTypeNames);
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
		LCK01JFixInformation other = (LCK01JFixInformation) obj;
		if (typeNames == null) {
			if (other.typeNames != null)
				return false;
		} else if (!typeNames.equals(other.typeNames))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LCK01JFixInformation [typeNames=" + typeNames + "]";
	}

}
