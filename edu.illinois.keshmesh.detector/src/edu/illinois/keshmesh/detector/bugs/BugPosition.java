/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class BugPosition {

	// [firstOffset, lastOffset)
	private int firstOffset, lastOffset;

	private int firstLine, lastLine;

	private IPath sourcePath;

	public BugPosition(Position position) {
		this.firstOffset = position.getFirstOffset();
		this.lastOffset = position.getLastOffset();
		this.firstLine = position.getFirstLine();
		this.lastLine = position.getLastLine();
		this.sourcePath = Path.fromPortableString(position.getURL().getFile());
	}

	public BugPosition(int firstLine, int lastLine, IPath sourcePath) {
		super();
		this.firstLine = firstLine;
		this.lastLine = lastLine;
		this.sourcePath = sourcePath;
	}

	public int getFirstOffset() {
		return firstOffset;
	}

	public int getLastOffset() {
		return lastOffset;
	}

	public int getFirstLine() {
		return firstLine;
	}

	public int getLastLine() {
		return lastLine;
	}

	public int getLength() {
		return lastOffset - firstOffset;
	}

	public IPath getSourcePath() {
		return sourcePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + firstLine;
		result = prime * result + firstOffset;
		result = prime * result + lastLine;
		result = prime * result + lastOffset;
		result = prime * result + ((sourcePath == null) ? 0 : sourcePath.toPortableString().hashCode());
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
		BugPosition other = (BugPosition) obj;
		if (firstLine != other.firstLine)
			return false;
		if (firstOffset != other.firstOffset)
			return false;
		if (lastLine != other.lastLine)
			return false;
		if (lastOffset != other.lastOffset)
			return false;
		if (sourcePath == null) {
			if (other.sourcePath != null)
				return false;
		} else if (!sourcePath.equals(other.sourcePath))
			return false;
		return true;
	}

	public boolean portableEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BugPosition other = (BugPosition) obj;
		if (firstLine != other.firstLine)
			return false;
		if (lastLine != other.lastLine)
			return false;
		if (sourcePath == null) {
			if (other.sourcePath != null)
				return false;
		} else if (!sourcePath.equals(other.sourcePath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return sourcePath.toPortableString() + " @ [" + firstLine + "--" + lastLine + "(" + firstOffset + ", " + lastOffset + ")" + "]";
	}

}
