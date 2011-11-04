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
		protected String getExpectedUnprotectedInstructionsThatMayAccessUnsafelySharedFields() {
			return "{Node: < Application, Lp/A, <init>()V > Context: ReceiverStringContext: [ SITE_IN_NODE{< Application, Lp/A, main([Ljava/lang/String;)V >:NEW <Application,Lp/A>@0 in ReceiverStringContext: [ SITE_IN_NODE{synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V >:NEW <Application,[Ljava/lang/String>@1 in Everywhere} ]} SITE_IN_NODE{synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V >:NEW <Application,[Ljava/lang/String>@1 in Everywhere} ]=[], Node: < Application, Lp/A, increment()V > Context: ReceiverStringContext: [ SITE_IN_NODE{< Application, Lp/A, main([Ljava/lang/String;)V >:NEW <Application,Lp/A>@0 in ReceiverStringContext: [ SITE_IN_NODE{synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V >:NEW <Application,[Ljava/lang/String>@1 in Everywhere} ]} SITE_IN_NODE{< Application, Lp/A, main([Ljava/lang/String;)V >:NEW <Application,Lp/A>@0 in ReceiverStringContext: [ SITE_IN_NODE{synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V >:NEW <Application,[Ljava/lang/String>@1 in Everywhere} ]} ]=[InstructionInfo [method=p.A.increment()V, ssaInstruction=3 = getfield < Application, Lp/A, counter, <Primordial,I> > 1, instructionIndex=2], InstructionInfo [method=p.A.increment()V, ssaInstruction=putfield 1 = 5 < Application, Lp/A, counter, <Primordial,I> >, instructionIndex=5]], Node: < Application, Lp/A, m()V > Context: ReceiverStringContext: [ SITE_IN_NODE{< Application, Lp/A, main([Ljava/lang/String;)V >:NEW <Application,Lp/A>@0 in ReceiverStringContext: [ SITE_IN_NODE{synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V >:NEW <Application,[Ljava/lang/String>@1 in Everywhere} ]} SITE_IN_NODE{synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V >:NEW <Application,[Ljava/lang/String>@1 in Everywhere} ]=[InstructionInfo [method=p.A.m()V, ssaInstruction=3 = getfield < Application, Lp/A, counter, <Primordial,I> > 1, instructionIndex=2], InstructionInfo [method=p.A.m()V, ssaInstruction=putfield 1 = 5 < Application, Lp/A, counter, <Primordial,I> >, instructionIndex=5]], Node: < Application, Lp/A, main([Ljava/lang/String;)V > Context: ReceiverStringContext: [ SITE_IN_NODE{synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V >:NEW <Application,[Ljava/lang/String>@1 in Everywhere} ]=[], Node: < Primordial, Ljava/lang/Object, <clinit>()V > Context: Everywhere=[], Node: < Primordial, Ljava/lang/Object, <init>()V > Context: JavaTypeContext<point: <Application,Lp/A>>=[], Node: < Primordial, Ljava/lang/Object, <init>()V > Context: JavaTypeContext<point: <Primordial,Ljava/lang/Object>>=[], Node: < Primordial, Ljava/lang/Object, <init>()V > Context: JavaTypeContext<point: <Primordial,Ljava/lang/String$CaseInsensitiveComparator>>=[], Node: < Primordial, Ljava/lang/Object, <init>()V > Context: JavaTypeContext<point: <Primordial,[Ljava/lang/String>>=[], Node: < Primordial, Ljava/lang/Object, registerNatives()V > Context: Everywhere=[], Node: < Primordial, Ljava/lang/String$CaseInsensitiveComparator, <init>()V > Context: JavaTypeContext<point: <Primordial,Ljava/lang/String$CaseInsensitiveComparator>>=[], Node: < Primordial, Ljava/lang/String$CaseInsensitiveComparator, <init>(Ljava/lang/String$1;)V > Context: JavaTypeContext<point: <Primordial,Ljava/lang/String$CaseInsensitiveComparator>>=[], Node: < Primordial, Ljava/lang/String, <clinit>()V > Context: Everywhere=[], Node: synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeRootMethod()V > Context: Everywhere=[], Node: synthetic < Primordial, Lcom/ibm/wala/FakeRootClass, fakeWorldClinit()V > Context: Everywhere=[]}";
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

	public static class VNA00JTest04 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("04", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/A$1>, <Application,Lp/A$2>, <Application,Lp/A>]";
		}

	}

	public static class VNA00JTest05 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("05", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/A>]";
		}

	}

	public static class VNA00JTest06 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("06", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/A>]";
		}

	}

	public static class VNA00JTest07 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("07", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/A>]";
		}

	}

	public static class VNA00JTest08 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("08", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[]";
		}

	}

	public static class VNA00JTest09 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("09", "ControlledStop.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/ControlledStop>]";
		}

	}

	public static class VNA00JTest10 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("10", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/A>, <Application,Lp/ThreadSafeD>]";
		}

	}
}
