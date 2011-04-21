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
public class BugInstance {

	BugPattern bugPattern;

	CodePosition bugPosition;

	FixInformation fixInformation;

	public BugInstance(BugPattern bugPattern, CodePosition bugPosition, FixInformation fixInformation) {
		this.bugPattern = bugPattern;
		this.bugPosition = bugPosition;
		this.fixInformation = fixInformation;
	}

	public BugPattern getBugPattern() {
		return bugPattern;
	}

	public CodePosition getBugPosition() {
		return bugPosition;
	}

	public FixInformation getFixInformation() {
		return fixInformation;
	}

	public BugInstanceKey getKey() {
		return new BugInstanceKey(bugPattern, bugPosition);
	}

	public BugInstance merge(BugInstance other) {
		if (other == null) {
			return this;
		}
		if (!getKey().equals(other.getKey())) {
			throw new AssertionError("Only bug instances with the same keys can be merged.");
		}
		return new BugInstance(bugPattern, bugPosition, fixInformation.merge(other.getFixInformation()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugPattern == null) ? 0 : bugPattern.hashCode());
		result = prime * result + ((bugPosition == null) ? 0 : bugPosition.hashCode());
		result = prime * result + ((fixInformation == null) ? 0 : fixInformation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (portableEquals(obj)) {
			BugInstance other = (BugInstance) obj;
			if (bugPosition == null) {
				if (other.bugPosition != null) {
					return false;
				}
			} else if (!bugPosition.equals(other.bugPosition)) {
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean portableEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BugInstance other = (BugInstance) obj;
		if (bugPattern == null) {
			if (other.bugPattern != null)
				return false;
		} else if (!bugPattern.equals(other.bugPattern))
			return false;
		if (bugPosition == null) {
			if (other.bugPosition != null)
				return false;
		} else if (!bugPosition.portableEquals(other.bugPosition))
			return false;
		if (fixInformation == null) {
			if (other.fixInformation != null)
				return false;
		} else if (!fixInformation.equals(other.fixInformation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return bugPattern + " @ " + bugPosition + " : " + fixInformation;
	}

}
