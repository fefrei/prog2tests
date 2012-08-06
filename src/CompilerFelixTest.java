package prog2.project4.tests.prog2tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import prog2.project4.driver.Configuration;
import prog2.project4.driver.ParseEntity;
import prog2.project4.parser.ParserException;
import prog2.project4.tests.TestBase;
import prog2.project4.tree.Tree;
import prog2.project4.tree.TreeFactory;

public class CompilerFelixTest extends TestBase {

	public final String VERSION = "1.3.1";

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

		// was never set, must be null
		assertTrue(listTree.getOperand(42) == null);
	}

	/**
	 * Tests some edge cases for scopes
	 */
	@Test
	public void scopeIncompleteDeclarationTest() {
		final String prog = "int f(int a) { if (a == 0) int b = 5; else int c = 5; c = c + 5; return c; }";

		assertResult(prog, 10, 42);
		assertResult(prog, 10, -42);
	}

	/**
	 * Tests that you don't allow double declaration
	 * 
	 * @throws ParserException
	 */
	@Test
	public void scopeDoubleDeclarationTest() throws ParserException {
		assertNameException(ParseEntity.PRG, "int f(int x) { int a = 0; int a = 1; return a; };");
	}

	/**
	 * Tests that you handle multiple scopes
	 */
	@Test
	public void scopeNestedDeclaration() {
		assertResult("int f(int x) { int a = 5; { int a = 10; } return a; }", 5, 42);
	}

	/**
	 * Tests that you can't be tricked into an infinite loop
	 */
	@Test
	public void doContinueWhileFalseTest() {
		assertResult("int f(int x) { do { continue; } while(false); return 5; }", 5, 42);
	}
}
