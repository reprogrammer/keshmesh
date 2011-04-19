/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.ui.findbugs;

import java.lang.reflect.Field;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import edu.illinois.keshmesh.detector.Main;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.LCK02JFixInformation;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;
import edu.illinois.keshmesh.util.Logger;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.ClassContext;

/**
 * 
 * @author Mohsen Vakilian
 * @author Samira Tasharofi
 * 
 */
public class KeshmeshFindBugsDetector implements Detector {

	String projectName = "";

	@SuppressWarnings("serial")
	public class ProjectNotFoundException extends Exception {
		public ProjectNotFoundException(Throwable throwable) {
			super(throwable);
		}
	}

	private String getProjectName(AnalysisContext analysisContext) throws ProjectNotFoundException {
		try {
			Class<AnalysisContext> c = AnalysisContext.class;
			Field projectField;
			projectField = c.getDeclaredField("project");
			projectField.setAccessible(true);
			Project project = (Project) projectField.get(analysisContext);
			return project.getProjectName();
		} catch (SecurityException e) {
			throw new ProjectNotFoundException(e);
		} catch (NoSuchFieldException e) {
			throw new ProjectNotFoundException(e);
		} catch (IllegalArgumentException e) {
			throw new ProjectNotFoundException(e);
		} catch (IllegalAccessException e) {
			throw new ProjectNotFoundException(e);
		}
	}

	private IJavaProject getProject(String name) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		IProjectNature projectNature = project.getNature(JavaCore.NATURE_ID);
		if (projectNature == null)
			return null;
		return (IJavaProject) projectNature;
	}

	@Override
	public void visitClassContext(ClassContext classContext) {
		try {
			if (!getProjectName(classContext.getAnalysisContext()).equals(projectName)) {
				projectName = getProjectName(classContext.getAnalysisContext());
				IJavaProject javaProject = getProject(projectName);
				Logger.log("The java project under analysis is " + javaProject.getElementName());
				BugPatterns.enableAllBugPatterns();
				BugInstances bugInstances = Main.initAndPerformAnalysis(javaProject);
				for (edu.illinois.keshmesh.detector.bugs.BugInstance bugInstance : bugInstances) {
					Logger.log(bugInstance.getBugPosition().getFullyQualifiedClassName());
					SourceLineAnnotation sourceLineAnnotation = new SourceLineAnnotation(bugInstance.getBugPosition().getFullyQualifiedClassName(), bugInstance.getBugPosition().getSourcePath()
							.toString(), bugInstance.getBugPosition().getFirstLine(), bugInstance.getBugPosition().getLastLine(), bugInstance.getBugPosition().getFirstOffset(), bugInstance
							.getBugPosition().getLastOffset());
					if (bugInstance.getBugPattern().getName().equals("LCK02J")) {
						if (((LCK02JFixInformation) bugInstance.getFixInformation()).getTypeNames().size() == 1) {

							sourceLineAnnotation.setDescription(((LCK02JFixInformation) bugInstance.getFixInformation()).getTypeNames().iterator().next());

						} else {
							sourceLineAnnotation.setDescription("");

						}
					}
					bugReporter.reportBug(new BugInstance(this, getBugPatternName(bugInstance), HIGH_PRIORITY).addClass(classContext.getJavaClass()).addSourceLine(sourceLineAnnotation));
				}
			}
		} catch (ProjectNotFoundException e) {
			//FIXME: Log exceptions into the error log
			e.printStackTrace();
		} catch (CoreException e) {
			//FIXME: Log exceptions into the error log
			e.printStackTrace();
		} catch (WALAInitializationException e) {
			//FIXME: Log exceptions into the error log
			e.printStackTrace();
		}
	}

	private BugReporter bugReporter;

	private String getBugPatternName(edu.illinois.keshmesh.detector.bugs.BugInstance bugInstance) {
		return "KESHMESH_" + bugInstance.getBugPattern().getName();
	}

	@Override
	public void report() {
	}

	public KeshmeshFindBugsDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
		Logger.log("KeshmeshFindBugsDetector(BugReporter bugReporter)");
	}

}
