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
public interface FixInformation {

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

	public abstract String toString();

	//FIXME: There is some code duplication among different overriders of this method that can be eliminated by use of an abstract class.
	public FixInformation merge(FixInformation other);

}
