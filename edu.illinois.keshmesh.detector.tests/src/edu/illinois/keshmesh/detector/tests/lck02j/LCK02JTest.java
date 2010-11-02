/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck02j;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;

import edu.illinois.keshmesh.detector.ConcurrencyBugsDetector;
import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;
import edu.illinois.keshmesh.detector.tests.AbstractTestCase;
import edu.illinois.keshmesh.detector.tests.Activator;
import edu.illinois.keshmesh.transformer.core.LCK02JFixer;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
abstract public class LCK02JTest extends AbstractTestCase {

	protected IPath targetTestClassPath = null;
	protected BugInstances bugInstances;

	public void setupProjectAndAnalyze(String testClass) throws Exception {
		addTestClass(testClass);
		findBugs();
	}

	public void addTestClass(String testClass) throws Exception {
		setUpProject();
		Path testClassPath = new Path(testClass);
		File test1File = Activator.getDefault().getFileInPlugin(testClassPath);
		String javaText = format(getFileContent(test1File.getAbsolutePath()));
		ICompilationUnit compilationUnit = createCU(packageP, testClassPath.lastSegment(), javaText);
		targetTestClassPath = compilationUnit.getResource().getLocation();
	}

	public void findBugs() throws WALAInitializationException {
		bugInstances = ConcurrencyBugsDetector.initAndPerformAnalysis(javaProject);
		System.out.println(bugInstances);
	}

	public void findAndFixBugs(BugInstance bugInstance) {
		LCK02JFixer fixer = new LCK02JFixer(bugInstance);
		try {
			if (fixer.checkInitialConditions(new NullProgressMonitor()).isOK()) {
				fixer.createChange(new NullProgressMonitor());
			}
		} catch (OperationCanceledException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
