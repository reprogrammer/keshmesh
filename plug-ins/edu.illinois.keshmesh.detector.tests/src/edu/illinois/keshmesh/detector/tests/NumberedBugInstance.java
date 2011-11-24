/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests;

import edu.illinois.keshmesh.detector.bugs.BugInstance;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class NumberedBugInstance {

	private BugInstance bugInstance;
	private String number;

	public NumberedBugInstance(BugInstance bugInstance, String number) {
		super();
		this.bugInstance = bugInstance;
		this.number = number;
	}

	public BugInstance getBugInstance() {
		return bugInstance;
	}

	public String getNumber() {
		return number;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugInstance == null) ? 0 : bugInstance.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
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
		NumberedBugInstance other = (NumberedBugInstance) obj;
		if (bugInstance == null) {
			if (other.bugInstance != null)
				return false;
		} else if (!bugInstance.portableEquals(other.bugInstance))
			return false;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NumberedBugInstance [bugInstance=" + bugInstance + ", number=" + number + "]";
	}

}
