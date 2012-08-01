package prog2.project4.tests.prog2tests;

import org.junit.Test;

import prog2.project4.driver.Configuration;
import prog2.project4.tests.TestBase;
import prog2.project4.tree.Tree;
import prog2.project4.tree.TreeFactory;

public class CompilerFelixTest extends TestBase {

	public final String VERSION = "1.1";

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("CompilerFelixTest", VERSION);
	}

	@Test
	public void doWhileScopeExecutionTest() {
		// loops don't open new scopes, and do-while-loops execute at least once
		assertResult("int scopetest(int x) { do { x = 42; } while (false); return x; }", 42, 5);
	}
	
	@Test
	public void setOperandMeanTest() {
		// Yes, this has to work. Sorry.
		// See https://code.google.com/p/prog2tests/issues/detail?id=43
		TreeFactory factory = Configuration.INSTANCE.getTreeFactory();
		Tree listTree = factory.makeList();
		Tree innerTree = factory.makeList();
		listTree.setOperand(50, innerTree);
		assert(listTree.getOperand(50) == innerTree);
		assert(listTree.getOperand(42) == null); // was never set, must be null
	}

}
