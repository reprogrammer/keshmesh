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

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.ClassContext;

/**
 * 
 * @author Mohsen Vakilian
 * @author Samira Tasharofi
 * 
 */
public class LCK02JFindBugsDetector implements Detector {

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
			System.out.println("CustomDetector.visitClassContext(ClassContext)");
			String projectName = getProjectName(classContext.getAnalysisContext());
			IJavaProject javaProject = getProject(projectName);
			System.out.println("The java project under analyais is " + javaProject.getElementName());
			bugReporter.reportBug(new BugInstance(this, "KESHMESH_LCK02J", HIGH_PRIORITY).addClass(classContext.getJavaClass()));
		} catch (ProjectNotFoundException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private BugReporter bugReporter;

	@Override
	public void report() {
	}

	public LCK02JFindBugsDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
		System.out.println("LCK02JFindBugsDetector(BugReporter bugReporter)");
	}

}
