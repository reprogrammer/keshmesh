/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class BugInstances implements Collection<BugInstance> {

	Set<BugInstance> bugInstances;

	public BugInstances() {
		bugInstances = new HashSet<BugInstance>();
	}

	@Override
	public boolean add(BugInstance bugInstance) {
		return bugInstances.add(bugInstance);
	}

	@Override
	public boolean addAll(Collection<? extends BugInstance> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object object) {
		for (BugInstance bugInstance : bugInstances) {
			if (bugInstance.portableEquals(object))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<BugInstance> iterator() {
		return bugInstances.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return bugInstances.size();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return bugInstances.toString();
	}

}
