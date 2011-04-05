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
public class CodePosition {

	// [firstOffset, lastOffset)
	private int firstOffset, lastOffset;

	private int firstLine, lastLine;

	private IPath sourcePath;

	private String fullyQualifiedEnclosingClassName;

	public CodePosition(Position position, String fullyQualifiedEnclosingClassName) {
		this.firstOffset = position.getFirstOffset();
		this.lastOffset = position.getLastOffset();
		this.firstLine = position.getFirstLine();
		this.lastLine = position.getLastLine();
		this.sourcePath = Path.fromPortableString(position.getURL().getFile());
		this.fullyQualifiedEnclosingClassName = fullyQualifiedEnclosingClassName;
	}

	public CodePosition(int firstLine, int lastLine, IPath sourcePath, String fullyQualifiedClassName) {
		super();
		this.firstLine = firstLine;
		this.lastLine = lastLine;
		this.sourcePath = sourcePath;
		this.fullyQualifiedEnclosingClassName = fullyQualifiedClassName;
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

	public String getFullyQualifiedClassName() {
		return fullyQualifiedEnclosingClassName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + firstLine;
		result = prime * result + firstOffset;
		result = prime * result + ((fullyQualifiedEnclosingClassName == null) ? 0 : fullyQualifiedEnclosingClassName.hashCode());
		//		result = prime * result + lastLine;
		result = prime * result + lastOffset;
		result = prime * result + ((sourcePath == null) ? 0 : sourcePath.toPortableString().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (portableEquals(obj)) {
			CodePosition other = (CodePosition) obj;
			if (firstOffset != other.firstOffset) {
				return false;
			}
			if (lastOffset != other.lastOffset) {
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
		CodePosition other = (CodePosition) obj;
		if (firstLine != other.firstLine)
			return false;
		//		if (lastLine != other.lastLine)
		//			return false;
		if (sourcePath == null) {
			if (other.sourcePath != null)
				return false;
		} else if (!sourcePath.equals(other.sourcePath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return sourcePath.toPortableString() + " / " + fullyQualifiedEnclosingClassName + " @ [" + firstLine + "--" + lastLine + "(" + firstOffset + ", " + lastOffset + ")" + "]";
	}

}
