package edu.illinois.keshmesh.detector;

import java.util.Collection;

import com.ibm.wala.util.intset.BitVector;

public class CGNodeInfo {

	private final Collection<InstructionInfo> safeSynchronizedBlocks;
	private final BitVector bitVector;

	public CGNodeInfo(Collection<InstructionInfo> safeSynchronizedBlocks, BitVector bitVector) {
		this.safeSynchronizedBlocks = safeSynchronizedBlocks;
		this.bitVector = bitVector;
	}

	public Collection<InstructionInfo> getSafeSynchronizedBlocks() {
		return safeSynchronizedBlocks;
	}

	public BitVector getBitVector() {
		return bitVector;
	}

}
