/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.transformer.core;

import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import edu.illinois.keshmesh.detector.Logger;
import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.CodePosition;
import edu.illinois.keshmesh.detector.bugs.LCK03JBugPattern;
import edu.illinois.keshmesh.detector.bugs.LCK03JFixInformation;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
public class LCK03JFixer extends Refactoring {

	final static String SYNC_COMMAND = "synchronized";

	CodePosition bugPosition;
	BugInstance bugInstance;
	LCK03JFixInformation fixInformation;

	public LCK03JFixer(CodePosition bugPosition) {
		super();
		this.bugPosition = bugPosition;
	}

	public LCK03JFixer(BugInstance bugInstance) {
		super();
		this.bugInstance = bugInstance;
		this.bugPosition = bugInstance.getBugPosition();
		this.fixInformation = (LCK03JFixInformation) bugInstance.getFixInformation();
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		return null;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		if (fixInformation.getTypeNames().size() == 1) {
			if (fixInformation.isLock())
				return RefactoringStatus.create(Status.OK_STATUS);
			else
				return RefactoringStatus.createFatalErrorStatus("Cannot fix the synchronized statement on Condition: " + fixInformation.toString());
		} else {
			return RefactoringStatus.createFatalErrorStatus("Multiple types are found: " + fixInformation.toString());
		}
	}

	/**
	 * FIXME:
	 * 
	 * Break the method into smaller ones.
	 * 
	 * Rethrow the exception as CoreException
	 * 
	 * Preserve comments.
	 * 
	 * Preserve formatting.
	 * 
	 */
	@Override
	public Change createChange(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		ITextFileBufferManager textFileBufferManager = null;
		try {
			//Retrieving the Document out of IPath
			textFileBufferManager = FileBuffers.getTextFileBufferManager();
			textFileBufferManager.connect(bugPosition.getSourcePath(), LocationKind.LOCATION, progressMonitor);
			ITextFileBuffer textFileBuffer = textFileBufferManager.getTextFileBuffer(bugPosition.getSourcePath(), LocationKind.IFILE);
			IDocument document = textFileBuffer.getDocument();
			try {
				Logger.log(document.get(bugPosition.getFirstOffset(), bugPosition.getLength()));
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
			// Parsing the Document
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(document.get().toCharArray());
			parser.setResolveBindings(true);
			CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(progressMonitor);

			// Retrieving the begin and end indexes of synchronized command
			int bugLineOffset = document.getLineInformation(bugPosition.getFirstLine() - 1).getOffset();
			int bugLineLength = document.getLineInformation(bugPosition.getFirstLine() - 1).getLength();
			String bugLine = document.get(bugLineOffset, bugLineLength);
			int syncCommandBeginIndex = getSynchronizedCommandBeginIndex(bugLine);
			int syncCommandLastIndex = getSynchronizedCommandLastIndex(bugLine, syncCommandBeginIndex);

			// Extracting the synchronized command expression
			String bugLineAfterSync = bugLine.substring(syncCommandBeginIndex + SYNC_COMMAND.length());
			int openParenthesisIndex = bugLineAfterSync.indexOf('(') + syncCommandBeginIndex + SYNC_COMMAND.length();
			String synchExpression = bugLine.substring(openParenthesisIndex + 1, syncCommandLastIndex);

			// Computing the begin and end offset of the synchronized command
			int syncCommandBeginOffset = bugLineOffset + syncCommandBeginIndex;
			int syncCommandLastOffset = bugLineOffset + syncCommandLastIndex;
			// Getting the synchronized command AST node 
			ASTNode monitorNode = NodeFinder.perform(compilationUnit, syncCommandBeginOffset, syncCommandLastOffset - syncCommandBeginOffset + 1);
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement) monitorNode;
			List synchBodyStatements = synchronizedStatement.getBody().statements();

			// Creating a "local variable assignment" statement and a try/catch/final block to be replaced at monitor node place
			//AST ast = synchronizedStatement.getAST();
			ASTRewrite rewriter = ASTRewrite.create(compilationUnit.getAST());
			String localVarNameForLock = "tempLock";
			String localVarAssignment = LCK03JBugPattern.LOCK + " " + localVarNameForLock + " = " + synchExpression + ";\n";
			String tryFinalBlockStatements = "try {\n" + localVarNameForLock + ".lock();\n";
			for (Object statement : synchBodyStatements) {
				tryFinalBlockStatements += statement;
			}
			tryFinalBlockStatements += "} finally {\n" + localVarNameForLock + ".unlock();\n}";

			// Rewriting the monitor node
			ASTNode astNode = rewriter.createStringPlaceholder(localVarAssignment + tryFinalBlockStatements, TryStatement.TRY_STATEMENT);
			rewriter.replace(synchronizedStatement, astNode, null);
			TextEdit textEdit = rewriter.rewriteAST(document, null);
			UndoEdit undoEdit = textEdit.apply(document);

			//Committing changes to the source file
			textFileBuffer.commit(progressMonitor, true);
		} catch (BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to fix LCK03J.", e));
		} finally {
			textFileBufferManager.disconnect(bugPosition.getSourcePath(), LocationKind.LOCATION, progressMonitor);
		}
		return null;
	}

	private int getSynchronizedCommandBeginIndex(String syncCommandLine) {
		int syncBeginIndex = syncCommandLine.indexOf(SYNC_COMMAND);

		// There is a possibility of having synchronized word within comments
		// Skipping the comments before the synchronized command
		int startCommentIndex = syncCommandLine.indexOf("/*");
		int endCommentIndex = syncCommandLine.indexOf("*/");
		String temp_synchCommandLine = syncCommandLine;
		int temp_syncIndex = syncBeginIndex;
		int temp_beginIndex = 0;
		while (startCommentIndex >= 0 && endCommentIndex > 0 && temp_synchCommandLine.length() > 0 && temp_syncIndex > startCommentIndex) {
			temp_beginIndex += (endCommentIndex + 2);
			temp_synchCommandLine = temp_synchCommandLine.substring(endCommentIndex + 2);
			temp_syncIndex = temp_synchCommandLine.indexOf(SYNC_COMMAND);
			startCommentIndex = temp_synchCommandLine.indexOf("/*");
			endCommentIndex = temp_synchCommandLine.indexOf("*/");
			syncBeginIndex = temp_beginIndex + temp_syncIndex;
			Logger.log(Integer.toString(syncBeginIndex));
		}
		return syncBeginIndex;
	}

	private int getSynchronizedCommandLastIndex(String syncCommandLine, int beginIndex) {
		String syncCommandLineAfterSyncWord = syncCommandLine.substring(beginIndex + SYNC_COMMAND.length());
		int openParenthesisIndex = syncCommandLineAfterSyncWord.indexOf('(') + beginIndex + SYNC_COMMAND.length();
		int index = openParenthesisIndex;
		int pcounter = 1;
		while (pcounter != 0 && index < syncCommandLine.length()) {
			index++;
			if (syncCommandLine.charAt(index) == ')') {
				pcounter--;
			} else if (syncCommandLine.charAt(index) == '(') {
				pcounter++;
			}
		}
		return index;
	}

	@Override
	public String getName() {
		return "LCK03J Fixer";
	}

}
