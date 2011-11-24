/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class BugInstances implements Collection<BugInstance> {

	Map<BugInstanceKey, BugInstance> bugInstances;

	public BugInstances() {
		bugInstances = new HashMap<BugInstanceKey, BugInstance>();
	}

	@Override
	public boolean add(BugInstance bugInstance) {
		BugInstanceKey key = bugInstance.getKey();
		BugInstance mergedBugInstance = bugInstance.merge(bugInstances.get(key));
		BugInstance oldBugInstance = bugInstances.put(key, mergedBugInstance);
		return !mergedBugInstance.equals(oldBugInstance);
	}

	@Override
	public boolean addAll(Collection<? extends BugInstance> bugInstances) {
		boolean changed = false;
		// FIXME: I dont't know why the foreach loop version doesn't work correctly. It might have something to do with generics and wildcards.
		//		for (BugInstance bugInstance : bugInstances) {
		//			changed = changed || add(bugInstance);
		//		}
		Iterator<? extends BugInstance> iter = bugInstances.iterator();
		while (iter.hasNext()) {
			BugInstance nextBugInstance = iter.next();
			boolean added = add(nextBugInstance);
			changed = changed || added;
		}
		return changed;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	public BugInstance portableFind(BugInstance testBugInstance) {
		for (BugInstance bugInstance : bugInstances.values()) {
			if (bugInstance.portableEquals(testBugInstance))
				return bugInstance;
		}
		return null;
	}

	@Override
	public boolean contains(Object object) {
		throw new UnsupportedOperationException();
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
		return bugInstances.values().iterator();
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
		return bugInstances.values().size();
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
		return bugInstances.values().toString();
	}

}
