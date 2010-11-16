/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;

import junit.framework.Assert;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public abstract class TestSetupHelper {

	/**
	 * Creates a new project in Eclipse and sets up its dependencies on JRE.
	 * 
	 * @param projectName
	 * @param baseProjectName
	 * @return
	 * @throws CoreException
	 */
	@SuppressWarnings("rawtypes")
	static IJavaProject createAndInitializeProject(String projectName, String baseProjectName) throws CoreException {
		IJavaProject project;
		project = JavaProjectHelper.createJavaProject(projectName, "bin");
		TestSetupHelper.addJREContainer(project);
		// set compiler options on projectOriginal
		Map options = project.getOptions(false);
		JavaProjectHelper.set15CompilerOptions(options);
		project.setOptions(options);

		return project;
	}

	/**
	 * Creates the specified package in the specified project if the package
	 * does not already exist else it simply returns the preexisting package.
	 * 
	 * @param containerProject
	 * @param packageName
	 * @return
	 * @throws CoreException
	 */
	static IPackageFragment createPackage(IJavaProject containerProject, String packageName) throws CoreException {
		boolean alreadyExists = false;
		IPackageFragmentRoot packageFragmentRoot = null;
		IPackageFragmentRoot[] allPackageFragmentRoots = containerProject.getAllPackageFragmentRoots();
		for (IPackageFragmentRoot root : allPackageFragmentRoots) {
			if (root.getElementName().equals(AbstractTestCase.CONTAINER)) {
				alreadyExists = true;
				packageFragmentRoot = root;
				break;
			}
		}
		if (!alreadyExists) {
			packageFragmentRoot = JavaProjectHelper.addSourceContainer(containerProject, AbstractTestCase.CONTAINER);
		}
		return (packageFragmentRoot.createPackageFragment(packageName, true, null));
	}

	public static String getFileContent(String fileName) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		StringBuilder sb = new StringBuilder();
		String str;
		while ((str = in.readLine()) != null) {
			sb.append(str);
			sb.append("\n");
		}
		in.close();
		return sb.toString();
	}

	public static String format(String contents) throws MalformedTreeException, BadLocationException {
		IDocument document = new Document();
		document.set(contents);
		CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(null);
		TextEdit textEdit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, contents, 0, contents.length(), 0, null);
		if (textEdit != null) {
			textEdit.apply(document);
		}
		return document.get();
	}

	static IJavaProject createAndInitializeProject(String suffix) throws CoreException {
		String projectName = "TestProject" + suffix + "-" + System.currentTimeMillis();
		return createAndInitializeProject(projectName, null);
	}

	/**
	 * Creates the class with the specified contents and name in the specified
	 * package and returns the compilation unit of the newly created class. If
	 * the class already exists no changes are made to it and the preexisting
	 * compilation unit is returned.
	 * 
	 * @param pack
	 *            the package in which the class will be created
	 * @param name
	 *            the name of the class e.g. A.java
	 * @param contents
	 *            the source code of the class.
	 * @return the compilation unit corresponding to the class with the
	 *         specified <code>name</code>
	 * @throws Exception
	 */
	public static ICompilationUnit createCU(IPackageFragment pack, String name, String contents) throws Exception {
		if (pack.getCompilationUnit(name).exists())
			return pack.getCompilationUnit(name);
		ICompilationUnit cu = pack.createCompilationUnit(name, contents, true, null);
		cu.save(null, true);
		return cu;
	}

	public static String join(String... pathElements) {
		StringBuilder sb = new StringBuilder();
		for (String pathElement : pathElements) {
			if (!pathElement.isEmpty()) {
				sb.append(pathElement);
				sb.append(File.separator);
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	protected static String getPathForOutputFile(String inputFileName, String bugInstanceNumber) {
		return inputFileName.replaceFirst("in", Matcher.quoteReplacement(join("out", bugInstanceNumber)));
	}

	protected static void compareFiles(String expectedFilePath, String actualFilePath) throws IOException {
		Assert.assertEquals(getFileContent(expectedFilePath), getFileContent(actualFilePath));
	}

	/**
	 * 
	 * @see Martin Aeschlimann, Dirk Bäumer, and Jerome Lanneluc. 2005. Java
	 *      Tool Smithing Extending the Eclipse Java Development Tools. In
	 *      EclipseCon, 1-51. http://www.eclipsecon.org/2005/presentations/
	 *      EclipseCON2005_Tutorial29.pdf.
	 */
	public static void addJREContainer(IJavaProject jproject) throws JavaModelException {
		IClasspathEntry jreCPEntry = JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER));
		JavaProjectHelper.addToClasspath(jproject, jreCPEntry);
	}

	/**
	 * Sets auto-building state for the test workspace.
	 * 
	 * @param state
	 *            The new auto building state
	 * @return The previous state
	 * @throws CoreException
	 *             Change failed
	 */
	public static boolean setAutoBuilding(boolean state) throws CoreException {
		// disable auto build
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription desc = workspace.getDescription();
		boolean result = desc.isAutoBuilding();
		desc.setAutoBuilding(state);
		workspace.setDescription(desc);
		return result;
	}

}
