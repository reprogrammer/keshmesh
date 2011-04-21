/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JFixInformation implements FixInformation {

	@Override
	public FixInformation merge(FixInformation other) {
		if (!(other instanceof VNA00JFixInformation)) {
			throw new AssertionError("Only FixInformation's of the same types can be merged");
		}
		return this;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VNA00JFixInformation";
	}

}
