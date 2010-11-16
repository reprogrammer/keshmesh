/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.ui.findbugs;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
public class LCK02JFindBugsDetector implements Detector {

	@Override
	public void visitClassContext(ClassContext classContext) {
		System.out.println("class context .................");
		System.out.println("context = " + classContext);

		bugReporter.reportBug(new BugInstance(this, "KESHMESH_LCK02J", HIGH_PRIORITY).addClass(classContext.getJavaClass()));
	}

	private BugReporter bugReporter;

	public LCK02JFindBugsDetector(BugReporter bugReporter) {
		//super(bugReporter);
		this.bugReporter = bugReporter;
		System.out.println("!!!!!!!!!!!!!!!!!!!!!Keshmesh Plugin ................");
	}

	@Override
	public void report() {
	}

}
