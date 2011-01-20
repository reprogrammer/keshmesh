/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.runtime.IPath;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.CodePosition;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class BugInstanceParser {
	private final static String BEGIN_MARKER = "/*["; //$NON-NLS-1$
	private final static String END_MARKER = "/*]*/"; //$NON-NLS-1$
	private final static String CLOSE_BEGIN_MARKER = "*/"; //$NON-NLS-1$
	private final static String SEPARATOR = ","; //$NON-NLS-1$

	Deque<NumberedBugInstance> stack;
	Set<NumberedBugInstance> numberedBugInstances;
	BugInstanceCreator bugInstanceCreator;
	IPath filePath;

	public BugInstanceParser(BugInstanceCreator bugInstanceCreator, IPath filePath) {
		this.stack = new LinkedList<NumberedBugInstance>();
		this.numberedBugInstances = new HashSet<NumberedBugInstance>();
		this.bugInstanceCreator = bugInstanceCreator;
		this.filePath = filePath;
	}

	//FIXME: This long method should be refactored.
	public Set<NumberedBugInstance> parseLine(String line, int currentLineNumber) {
		line = line.replaceAll("\\s", "");
		Set<NumberedBugInstance> numberedBugInstances = new HashSet<NumberedBugInstance>();
		int nextIndex = 0;

		while (true) {
			int beginMarkerIndex = line.indexOf(BEGIN_MARKER, nextIndex);
			int endMarkerIndex = line.indexOf(END_MARKER, nextIndex);

			if (beginMarkerIndex == -1 && endMarkerIndex == -1) {
				break;
			} else if (beginMarkerIndex != -1 && (beginMarkerIndex < endMarkerIndex || endMarkerIndex == -1)) {
				nextIndex = beginMarkerIndex + BEGIN_MARKER.length();
				String marker = line.substring(nextIndex, line.indexOf(CLOSE_BEGIN_MARKER, nextIndex));
				Deque<String> markerParts = new LinkedList<String>(Arrays.asList(marker.split(SEPARATOR)));
				String bugPatternName = markerParts.getFirst();
				markerParts.removeFirst();
				String bugInstanceNumber = markerParts.getFirst();
				markerParts.removeFirst();
				String[] replacements = markerParts.toArray(new String[markerParts.size()]);
				BugInstance testBugInstance = bugInstanceCreator.createTestBugInstance(BugPatterns.getBugPatternByName(bugPatternName), currentLineNumber, -1, filePath, replacements);
				NumberedBugInstance numberedBugInstance = new NumberedBugInstance(testBugInstance, bugInstanceNumber);
				stack.addLast(numberedBugInstance);
			} else if (endMarkerIndex != -1 && (endMarkerIndex < beginMarkerIndex || beginMarkerIndex == -1)) {
				nextIndex = endMarkerIndex + END_MARKER.length();
				//TODO: Maybe make a setter for BugPosition to return a new instance.
				NumberedBugInstance numberedBugInstance = stack.removeLast();
				BugInstance testBugInstance = numberedBugInstance.getBugInstance();
				CodePosition testBugPosition = testBugInstance.getBugPosition();
				BugInstance testBugInstanceWithLastLineNumber = new BugInstance(testBugInstance.getBugPattern(), new CodePosition(testBugPosition.getFirstLine(), currentLineNumber,
						testBugPosition.getSourcePath(), null), testBugInstance.getFixInformation());
				numberedBugInstances.add(new NumberedBugInstance(testBugInstanceWithLastLineNumber, numberedBugInstance.getNumber()));
			}
		}

		return numberedBugInstances;
	}

	public Set<NumberedBugInstance> parseExpectedBugInstances(Iterator<String> lineIterator) {
		Set<NumberedBugInstance> numberedBugInstances = new HashSet<NumberedBugInstance>();
		String line;
		int currentLineNumber = 0;
		while (lineIterator.hasNext()) {
			line = lineIterator.next();
			++currentLineNumber;
			numberedBugInstances.addAll(parseLine(line, currentLineNumber));
		}
		return numberedBugInstances;
	}

	public Set<NumberedBugInstance> parseExpectedBugInstances() throws IOException {
		return parseExpectedBugInstances(new FileLineIterator(filePath));
	}

	public static class FileLineIterator implements Iterator<String> {

		private BufferedReader in;
		private String nextLine = null;
		private boolean needToCheckHashNext = true;
		private boolean lastHasNext;

		public FileLineIterator(IPath path) throws FileNotFoundException {
			in = new BufferedReader(new FileReader(path.toFile()));
		}

		public boolean hasNextEagerly() {
			try {
				nextLine = in.readLine();
				return (nextLine != null);
			} catch (IOException e) {
				return false;
			}
		}

		@Override
		public boolean hasNext() {
			if (!needToCheckHashNext) {
				return lastHasNext;
			}
			lastHasNext = hasNextEagerly();
			needToCheckHashNext = false;
			return lastHasNext;
		}

		@Override
		public String next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			needToCheckHashNext = true;
			return nextLine;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
