package edu.illinois.keshmesh.detector.bugs;

public class Position {

	int firstOffset;

	int lastOffset;

	public Position(com.ibm.wala.cast.tree.CAstSourcePositionMap.Position position) {
		this.firstOffset = position.getFirstOffset();
		this.lastOffset = position.getLastOffset();
	}

	public Position(int firstOffset, int lastOffset) {
		super();
		this.firstOffset = firstOffset;
		this.lastOffset = lastOffset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + firstOffset;
		result = prime * result + lastOffset;
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
		Position other = (Position) obj;
		if (firstOffset != other.firstOffset)
			return false;
		if (lastOffset != other.lastOffset)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + firstOffset + ":" + lastOffset + "]";
	}
}
