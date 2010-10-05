package edu.illinois.keshmesh.detector.bugs;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class BugInstance {

	BugPattern bugPattern;

	Position position;

	public BugInstance(BugPattern bugPattern, Position position) {
		this.bugPattern = bugPattern;
		this.position = position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugPattern == null) ? 0 : bugPattern.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
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
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return bugPattern + " @" + position;
	}
}
