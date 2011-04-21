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
 * @author Samira Tasharofi
 * 
 */

public class LCK06JFixInformation implements FixInformation {

	Set<String> unsafeStaticFieldNames;

	public LCK06JFixInformation(Set<String> unsafeStaticFieldNames) {
		this.unsafeStaticFieldNames = unsafeStaticFieldNames;
	}

	public Set<String> getStaticFieldNames() {
		return this.unsafeStaticFieldNames;
	}

	@Override
	public FixInformation merge(FixInformation other) {
		if (!(other instanceof LCK06JFixInformation)) {
			throw new AssertionError("Only FixInformation's of the same types can be merged");
		}
		LCK06JFixInformation otherLCK06JFixInformation = (LCK06JFixInformation) other;
		Set<String> allUnsafeStaticFieldNames = new HashSet<String>(unsafeStaticFieldNames);
		allUnsafeStaticFieldNames.addAll(otherLCK06JFixInformation.getStaticFieldNames());
		return new LCK02JFixInformation(allUnsafeStaticFieldNames);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unsafeStaticFieldNames == null) ? 0 : unsafeStaticFieldNames.hashCode());
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
		LCK06JFixInformation other = (LCK06JFixInformation) obj;
		if (unsafeStaticFieldNames == null) {
			if (other.unsafeStaticFieldNames != null)
				return false;
		} else if (!unsafeStaticFieldNames.equals(other.unsafeStaticFieldNames))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LCK06JFixInformation [unsafeStaticFieldNames=" + unsafeStaticFieldNames + "]";
	}

}
