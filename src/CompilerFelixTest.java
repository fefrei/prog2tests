package prog2.project4.tests.prog2tests;

import org.junit.Test;

import prog2.project4.tests.TestBase;

public class CompilerFelixTest extends TestBase {

	public final String VERSION = "1.0";

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("CompilerFelixTest", VERSION);
	}

	@Test
	public void doWhileScopeExecutionTest() {
		// loops don't open new scopes, and do-while-loops execute at least once
		assertResult("int scopetest(int x) { do { x = 42; } while (false); return x; }", 42, 5);
	}

}
