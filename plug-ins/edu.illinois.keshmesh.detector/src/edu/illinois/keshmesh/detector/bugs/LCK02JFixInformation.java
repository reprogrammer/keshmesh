/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK02JFixInformation implements FixInformation {

	Set<String> typeNames;

	public LCK02JFixInformation(Set<String> typeNames) {
		this.typeNames = typeNames;
	}

	public Set<String> getTypeNames() {
		return typeNames;
	}

	@Override
	public FixInformation merge(FixInformation other) {
		if (!(other instanceof LCK02JFixInformation)) {
			throw new AssertionError("Only FixInformation's of the same types can be merged");
		}
		LCK02JFixInformation otherLCK02JFixInformation = (LCK02JFixInformation) other;
		Set<String> allTypeNames = new HashSet<String>(typeNames);
		allTypeNames.addAll(otherLCK02JFixInformation.getTypeNames());
		return new LCK02JFixInformation(allTypeNames);
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
		LCK02JFixInformation other = (LCK02JFixInformation) obj;
		if (typeNames == null) {
			if (other.typeNames != null)
				return false;
		} else if (!typeNames.equals(other.typeNames))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LCK02JFixInformation [typeNames=" + typeNames + "]";
	}

}
