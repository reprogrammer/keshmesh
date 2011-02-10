/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.vna00j;

import org.junit.Before;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JTests {

	public static class VNA00JTest01 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("01", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/A>]";
		}

		@Override
		protected String getExpectedUnsafeInstructionsThatAccessUnprotectedFields() {
			return "{Node: < Application, Lp/A, <init>()V > Context: ReceiverInstanceContext<SITE_IN_NODE{< Application, Lp/A, main([Ljava/lang/String;)V >:NEW <Application,Lp/A>@0 in Everywhere}>=[], Node: < Application, Lp/A, increment()V > Context: ReceiverInstanceContext<SITE_IN_NODE{< Application, Lp/A, main([Ljava/lang/String;)V >:NEW <Application,Lp/A>@0 in Everywhere}>=[InstructionInfo [method=p.A.increment()V, ssaInstruction=5 = binaryop(add) 3 , 4, instructionIndex=4]], Node: < Application, Lp/A, m()V > Context: ReceiverInstanceContext<SITE_IN_NODE{< Application, Lp/A, main([Ljava/lang/String;)V >:NEW <Application,Lp/A>@0 in Everywhere}>=[InstructionInfo [method=p.A.m()V, ssaInstruction=5 = binaryop(add) 3 , 4, instructionIndex=4]], Node: < Application, Lp/A, main([Ljava/lang/String;)V > Context: Everywhere=[], Node: < Primordial, Ljava/lang/Object, <clinit>()V > Context: Everywhere=[], Node: < Primordial, Ljava/lang/Object, <init>()V > Context: ReceiverInstanceContext<SITE_IN_NODE{< Application, Lp/A, m()V >:NEW <Application,Ljava/lang/Object>@14 in ReceiverInstanceContext<SITE_IN_NODE{< Application, Lp/A, main([Ljava/lang/String;)V >:NEW <Application,Lp/A>@0 in Everywhere}>}>=[], Node: < Primordial, Ljava/lang/Object, <init>()V > Context: ReceiverInstanceContext<SITE_IN_NODE{< Application, Lp/A, main([Ljava/lang/String;)V >:NEW <Application,Lp/A>@0 in Everywhere}>=[], Node: < Primordial, Ljava/lang/Object, <init>()V > Context: ReceiverInstanceContext<SITE_IN_NODE{< Primordial, Ljava/lang/String, <clinit>()V >:NEW <Primordial,Ljava/lang/String$CaseInsensitiveComparator>@7 in Everywhere}>=[], Node: < Primordial, Ljava/lang/Object, <init>()V > Context: ReceiverInstanceContext<SITE_IN_NODE{synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V >:NEW <Application,[Ljava/lang/String>@1 in Everywhere}>=[], Node: < Primordial, Ljava/lang/Object, registerNatives()V > Context: Everywhere=[], Node: < Primordial, Ljava/lang/String$CaseInsensitiveComparator, <init>()V > Context: ReceiverInstanceContext<SITE_IN_NODE{< Primordial, Ljava/lang/String, <clinit>()V >:NEW <Primordial,Ljava/lang/String$CaseInsensitiveComparator>@7 in Everywhere}>=[], Node: < Primordial, Ljava/lang/String$CaseInsensitiveComparator, <init>(Ljava/lang/String$1;)V > Context: ReceiverInstanceContext<SITE_IN_NODE{< Primordial, Ljava/lang/String, <clinit>()V >:NEW <Primordial,Ljava/lang/String$CaseInsensitiveComparator>@7 in Everywhere}>=[], Node: < Primordial, Ljava/lang/String, <clinit>()V > Context: Everywhere=[], Node: synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V > Context: Everywhere=[], Node: synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeWorldClinit()V > Context: Everywhere=[]}";
		}

	}

	public static class VNA00JTest02 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("02", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/A>]";
		}

	}

	public static class VNA00JTest03 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("03", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/A>]";
		}

	}
}
