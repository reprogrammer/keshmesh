/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.ui.popup.actions;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.illinois.keshmesh.detector.ConcurrencyBugsDetector;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK02JAction implements IObjectActionDelegate {

	protected ISelection currentSelection;

	private Shell shell;

	/**
	 * Constructor for Action1.
	 */
	public LCK02JAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IJavaProject selectedJavaProject = (IJavaProject) ((IStructuredSelection) currentSelection).getFirstElement();

		MessageDialog.openInformation(shell, "Ui", selectedJavaProject.getElementName());
		try {
			ConcurrencyBugsDetector.initAndPerformAnalysis(selectedJavaProject);
		} catch (WALAInitializationException e) {
			//FIXME: Put it in the error log
			e.printStackTrace();
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		currentSelection = selection;
	}

}
