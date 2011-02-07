/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.transformer.core;

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
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.CodePosition;
import edu.illinois.keshmesh.detector.bugs.LCK02JFixInformation;
import edu.illinois.keshmesh.detector.util.CollectionUtils;
import edu.illinois.keshmesh.util.Logger;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK02JFixer extends Refactoring {

	CodePosition bugPosition;
	BugInstance bugInstance;
	LCK02JFixInformation fixInformation;

	public LCK02JFixer(CodePosition bugPosition) {
		super();
		this.bugPosition = bugPosition;
	}

	public LCK02JFixer(BugInstance bugInstance) {
		super();
		this.bugInstance = bugInstance;
		this.bugPosition = bugInstance.getBugPosition();
		this.fixInformation = (LCK02JFixInformation) bugInstance.getFixInformation();
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		return null;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		if (fixInformation.getTypeNames().size() == 1) {
			return RefactoringStatus.create(Status.OK_STATUS);
		} else {
			return RefactoringStatus.createFatalErrorStatus("More than one possible target classes were found: " + fixInformation.toString());
		}
	}

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
			ASTNode monitorNode = NodeFinder.perform(compilationUnit, bugPosition.getFirstOffset(), bugPosition.getLength());
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement) monitorNode;
			AST ast = synchronizedStatement.getAST();
			ASTRewrite rewriter = ASTRewrite.create(ast);

			ASTParser expressionParser = ASTParser.newParser(AST.JLS3);
			expressionParser.setKind(ASTParser.K_EXPRESSION);
			expressionParser.setSource(CollectionUtils.getTheOnlyElementOf(fixInformation.getTypeNames()).toCharArray());
			ASTNode astNode = expressionParser.createAST(progressMonitor);
			rewriter.set(synchronizedStatement, SynchronizedStatement.EXPRESSION_PROPERTY, astNode, null);
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
		} finally {
			textFileBufferManager.disconnect(bugPosition.getSourcePath(), LocationKind.LOCATION, progressMonitor);
		}
		return null;
	}

	@Override
	public String getName() {
		return "LCK02J Fixer";
	}

}
