package prog2.project4.tests.prog2tests;

import static org.junit.Assert.*;

import org.junit.Test;

import prog2.project4.driver.Configuration;
import prog2.project4.tests.TestBase;
import prog2.project4.tree.Tree;
import prog2.project4.tree.TreeFactory;

public class CompilerFelixTest extends TestBase {

	public final String VERSION = "1.2.1";

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("CompilerFelixTest", VERSION);
	}

	/**
	 * Tests if while-blocks don't open new scopes, and the body of
	 * do-while-loops executes at least once.
	 */
	@Test
	public void doWhileScopeExecutionTest() {
		assertResult("int scopetest(int x) { do { x = 42; } while (false); return x; }", 42, 5);
		assertResult("int scopetest(int x) { do { int x = 42; } while (false); return x; }", 5, 5);
	}

	/**
	 * Tests if your implemented setOperand and getOperand according to the
	 * specification (or how Tobias reads the specification)
	 */
	@Test
	public void setOperandMeanTest() {
		// Yes, this has to work. Sorry.
		// See https://code.google.com/p/prog2tests/issues/detail?id=43
		TreeFactory factory = Configuration.INSTANCE.getTreeFactory();
		Tree listTree = factory.makeList();
		Tree innerTree = factory.makeList();
		listTree.setOperand(50, innerTree);
		assertTrue(listTree.getOperand(50) == innerTree);
		assertTrue(listTree.getOperand(42) == null); // was never set, must be null
	}

}
