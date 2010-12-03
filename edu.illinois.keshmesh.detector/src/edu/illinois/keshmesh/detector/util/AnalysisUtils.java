package edu.illinois.keshmesh.detector.util;

import java.util.Collection;

import edu.illinois.keshmesh.detector.InstructionInfo;

public class AnalysisUtils {

	public static boolean isProtectedByAnySynchronizedBlock(Collection<InstructionInfo> safeSynchronizedBlocks, InstructionInfo instruction) {
		for (InstructionInfo safeSynchronizedBlock : safeSynchronizedBlocks) {
			if (instruction.isInside(safeSynchronizedBlock)) {
				return true;
			}
		}
		return false;
	}

}
