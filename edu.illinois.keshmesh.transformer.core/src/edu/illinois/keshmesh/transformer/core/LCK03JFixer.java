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
import org.eclipse.text.edits.MalformedTreeException;
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
		if (fixInformation.getInstanceTypes().size() == 1) {
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

			//Rewriting the AST
			int bugLineOffset = document.getLineInformation(bugPosition.getFirstLine() - 1).getOffset();
			int bugLineLength = document.getLineInformation(bugPosition.getFirstLine() - 1).getLength();
			String bugLineContent = document.get(bugLineOffset, bugLineLength);
			String syncCommand = "synchronized";

			int syncIndex = bugLineContent.indexOf(syncCommand);
			int start_comment_index = bugLineContent.indexOf("/*");
			int end_comment_index = bugLineContent.indexOf("*/");
			String temp_bugLineContent = bugLineContent;
			int temp_syncIndex = syncIndex;
			int temp_beginIndex = 0;

			// there is a possibility of having synchronized word within comments
			while (start_comment_index >= 0 && end_comment_index > 0 && temp_bugLineContent.length() > 0 && temp_syncIndex > start_comment_index) {
				temp_beginIndex += (end_comment_index + 2);
				temp_bugLineContent = temp_bugLineContent.substring(end_comment_index + 2);
				temp_syncIndex = temp_bugLineContent.indexOf(syncCommand);
				start_comment_index = temp_bugLineContent.indexOf("/*");
				end_comment_index = temp_bugLineContent.indexOf("*/");
				syncIndex = temp_beginIndex + temp_syncIndex;
				Logger.log(Integer.toString(syncIndex));
			}

			String bugLineContentAfterSynch = bugLineContent.substring(syncIndex + syncCommand.length());
			int openParenthesisIndex = bugLineContentAfterSynch.indexOf('(') + syncIndex + syncCommand.length();
			int myFirstOffset = bugLineOffset + syncIndex;
			int index = openParenthesisIndex;
			int pcounter = 1;
			while (pcounter != 0 && index < bugLineLength) {
				index++;
				if (bugLineContent.charAt(index) == ')') {
					pcounter--;
				} else if (bugLineContent.charAt(index) == '(') {
					pcounter++;
				}
			}

			String synchExpression = bugLineContent.substring(openParenthesisIndex + 1, index);

			int myLastOffset = bugLineOffset + index;
			ASTNode monitorNode = NodeFinder.perform(compilationUnit, myFirstOffset, myLastOffset - myFirstOffset + 1);
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement) monitorNode;
			List synchBodyStatements = synchronizedStatement.getBody().statements();

			//Creating a try node to be replaced by monitor node
			AST ast = synchronizedStatement.getAST();
			ASTRewrite rewriter = ASTRewrite.create(ast);
			String localVarNameForLock = "tempLock";
			String localVarAssignment = LCK03JBugPattern.LOCK + " " + localVarNameForLock + " = " + synchExpression + ";\n";
			String tryFinalBlockStatements = "try {\n" + localVarNameForLock + ".lock();\n";
			for (Object statement : synchBodyStatements) {
				tryFinalBlockStatements += statement;
			}
			tryFinalBlockStatements += "} finally {\n" + localVarNameForLock + ".unlock();\n}";
			//Rewriting the monitor node
			ASTNode astNode = rewriter.createStringPlaceholder(localVarAssignment + tryFinalBlockStatements, TryStatement.TRY_STATEMENT);
			rewriter.replace(synchronizedStatement, astNode, null);
			TextEdit textEdit = rewriter.rewriteAST(document, null);
			try {
				UndoEdit undoEdit = textEdit.apply(document);
			} catch (MalformedTreeException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			//Committing changes to the source file
			textFileBuffer.commit(progressMonitor, true);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			textFileBufferManager.disconnect(bugPosition.getSourcePath(), LocationKind.LOCATION, progressMonitor);
		}
		return null;
	}

	@Override
	public String getName() {
		return "LCK03J Fixer";
	}

}
