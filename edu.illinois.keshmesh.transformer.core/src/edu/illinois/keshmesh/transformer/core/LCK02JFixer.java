package edu.illinois.keshmesh.transformer.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class LCK02JFixer extends Refactoring {

	int selectionStart;
	int selectionLength;
	String className;

	public LCK02JFixer(int selectionStart, int selectionLength, String className) {
		super();
		this.selectionStart = selectionStart;
		this.selectionLength = selectionLength;
		this.className = className;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		return null;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		return null;
	}

	@Override
	public String getName() {
		return "LCK02J Fixer";
	}

}
