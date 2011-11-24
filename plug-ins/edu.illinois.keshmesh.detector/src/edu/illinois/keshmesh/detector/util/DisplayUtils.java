/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.util;

import com.ibm.wala.ide.ui.SWTTreeViewer;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.InferGraphRoots;

public class DisplayUtils {

	private static final boolean ENABLED = false;

	public static void displayGraph(Graph graph) throws WalaException {
		if (ENABLED) {
			final SWTTreeViewer v = new SWTTreeViewer();
			v.setGraphInput(graph);
			v.setRootsInput(InferGraphRoots.inferRoots(graph));
			v.run();
			v.getApplicationWindow();
		}
	}

}
