package edu.illinois.keshmesh.detector.bugs;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class BugInstance {

	BugPattern bugPattern;

	BugPosition bugPosition;

	public BugInstance(BugPattern bugPattern, BugPosition bugPosition) {
		this.bugPattern = bugPattern;
		this.bugPosition = bugPosition;
	}

	public BugPattern getBugPattern() {
		return bugPattern;
	}

	public BugPosition getBugPosition() {
		return bugPosition;
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
		} else if (!bugPosition.equals(other.bugPosition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return bugPattern + " @ " + bugPosition;
	}
}
