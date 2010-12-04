package edu.illinois.keshmesh.detector.util;

import com.ibm.wala.ide.ui.SWTTreeViewer;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.InferGraphRoots;
import com.ibm.wala.util.warnings.WalaException;

public class DisplayUtils {

	public static void displayGraph(Graph graph) throws WalaException {
		final SWTTreeViewer v = new SWTTreeViewer();
		v.setGraphInput(graph);
		v.setRootsInput(InferGraphRoots.inferRoots(graph));
		v.run();
		v.getApplicationWindow();
	}

}
