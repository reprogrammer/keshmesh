/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import java.util.Collection;

import com.ibm.wala.classLoader.IField;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JFixInformation implements FixInformation {

	Collection<IField> unsafeStaticFields;

	public LCK06JFixInformation(Collection<IField> unsafeStaticFields) {
		this.unsafeStaticFields = unsafeStaticFields;
	}

	//TODO: remove this after fixing LCK06JTest
	public LCK06JFixInformation() {
	}

	public Collection<IField> getStaticFieldNames() {
		return this.unsafeStaticFields;
	}

	@Override
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
		return "LCK06JFixInformation";
	}

}
