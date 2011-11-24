/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

/**
 * @author Mohsen Vakilian
 * 
 */
public class BugInstanceKey {

	BugPattern bugPattern;

	CodePosition bugPosition;

	public BugInstanceKey(BugPattern bugPattern, CodePosition bugPosition) {
		this.bugPattern = bugPattern;
		this.bugPosition = bugPosition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugPattern == null) ? 0 : bugPattern.hashCode());
		result = prime * result + ((bugPosition == null) ? 0 : bugPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BugInstanceKey other = (BugInstanceKey) obj;
		if (bugPattern == null) {
			if (other.bugPattern != null) {
				return false;
			}
		} else if (!bugPattern.equals(other.bugPattern)) {
			return false;
		}
		if (bugPosition == null) {
			if (other.bugPosition != null) {
				return false;
			}
		} else if (!bugPosition.equals(other.bugPosition)) {
			return false;
		}
		return true;
	}

}
