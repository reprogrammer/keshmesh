/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import com.ibm.wala.classLoader.IField;

import edu.illinois.keshmesh.util.Modes;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JIntermediateResults implements IntermediateResults {

	private IField[] staticFields;

	public void setStaticFields(Set<IField> staticFields) {
		if (!Modes.isInProductionMode() && this.staticFields == null) {
			this.staticFields = setToSortedArray(staticFields);
		}
	}

	private IField[] setToSortedArray(Set<IField> staticFields) {
		IField[] sortedArray = staticFields.toArray(new IField[] {});
		Arrays.sort(sortedArray, new Comparator<IField>() {

			@Override
			public int compare(IField o1, IField o2) {
				return o1.toString().compareTo(o2.toString());
			}

		});
		return sortedArray;
	}

	public String getStaticFields() {
		return Arrays.toString(staticFields);
	}

}
