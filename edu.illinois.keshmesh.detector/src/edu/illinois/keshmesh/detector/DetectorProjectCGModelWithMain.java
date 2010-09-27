package edu.illinois.keshmesh.detector;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;

import edu.illinois.keshmesh.detector.wala.WalaProjectCGModelWithMain;

public class DetectorProjectCGModelWithMain extends WalaProjectCGModelWithMain {

	public DetectorProjectCGModelWithMain(IJavaProject project, String exclusionsFile) throws IOException, CoreException {
		super(project, exclusionsFile);
	}

	public PointerAnalysis getPointerAnalysis() {
		return engine.getPointerAnalysis();
	}

}
