package edu.illinois.keshmesh.detector.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public abstract class AbstractTestCase {

	protected IJavaProject javaProject;

	protected IPackageFragmentRoot fragmentRoot;

	protected IPackageFragment packageP;

	protected static final String CONTAINER = "src";
	protected static final String PACKAGE_NAME = "p";

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

	public void setUpProject(String testID) throws Exception {
		javaProject = createAndInitializeProject(testID);
		//Should be called after the projects are created
		JavaProjectHelper.setAutoBuilding(false);
		fragmentRoot = JavaProjectHelper.addSourceContainer(javaProject, CONTAINER);
		packageP = fragmentRoot.createPackageFragment(PACKAGE_NAME, true, null);
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
	protected IPackageFragment createPackage(IJavaProject containerProject, String packageName) throws CoreException {
		boolean alreadyExists = false;
		IPackageFragmentRoot packageFragmentRoot = null;
		IPackageFragmentRoot[] allPackageFragmentRoots = containerProject.getAllPackageFragmentRoots();
		for (IPackageFragmentRoot root : allPackageFragmentRoots) {
			if (root.getElementName().equals(CONTAINER)) {
				alreadyExists = true;
				packageFragmentRoot = root;
				break;
			}
		}
		if (!alreadyExists) {
			packageFragmentRoot = JavaProjectHelper.addSourceContainer(containerProject, CONTAINER);
		}
		return (packageFragmentRoot.createPackageFragment(packageName, true, null));
	}

	/**
	 * Creates a new project in Eclipse and sets up its dependencies on JRE.
	 * 
	 * @param projectName
	 * @param baseProjectName
	 * @return
	 * @throws CoreException
	 */
	protected IJavaProject createAndInitializeProject(String projectName, String baseProjectName) throws CoreException {
		IJavaProject project;
		project = JavaProjectHelper.createJavaProject(projectName, "bin");
		JavaProjectHelper.addJREContainer(project);
		// set compiler options on projectOriginal
		Map options = project.getOptions(false);
		JavaProjectHelper.set15CompilerOptions(options);
		project.setOptions(options);

		return project;
	}

	protected IJavaProject createAndInitializeProject(String suffix) throws CoreException {
		String projectName = "TestProject" + suffix + "-" + System.currentTimeMillis();
		return createAndInitializeProject(projectName, null);
	}

	//	@After
	public void tearDown() throws Exception {
		JavaProjectHelper.performDummySearch();
		JavaProjectHelper.delete(javaProject);
	}

	protected String format(String contents) {
		IDocument document = new Document();
		document.set(contents);
		CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(null);
		TextEdit textEdit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, contents, 0, contents.length(), 0, null);
		if (textEdit != null)
			try {
				textEdit.apply(document);
			} catch (MalformedTreeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return document.get();
	}

	//	public static void performDummySearch() throws JavaModelException {
	//		new SearchEngine().searchAllTypeNames(null,
	//				"A".toCharArray(), // make sure we search a concrete name. This
	//				// is faster according to Kent
	//				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE, IJavaSearchConstants.CLASS, SearchEngine.createJavaSearchScope(new IJavaElement[0]), new Requestor(),
	//				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
	//	}

	protected static String getFileContent(String fileName) throws IOException {
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

	private static class Requestor extends TypeNameRequestor {
	}

}
