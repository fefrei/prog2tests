package prog2.project4.tests.prog2tests;

import org.junit.Test;

import prog2.project4.driver.ParseEntity;
import prog2.project4.tests.TestBase;

/**
 * @author Andreas Schmidt
 */
public class CompilerAndreasTest extends TestBase {
	/*
	 * These are old Nightlies that have been used for testing. Now you're able
	 * to run these tests during the day, too :)
	 * 
	 * Or, like the author stated it: Macht die Nacht zum Tag.
	 */

	@Test
	public void testNightlyTypeExceptions() throws Exception {
		// 555 ist not a boolean
		assertTypeException(ParseEntity.PRG,
				"int test() { boolean x = 555; return 0; }"); // 5
		// You can't do "+" on two booleans.
		assertTypeException(ParseEntity.PRG,
				"int test() { boolean x = true + true; return 0; }"); // 7
		// 555 is not a boolean
		assertTypeException(ParseEntity.PRG,
				"int test() { int i = 555 ? 1 : 0; return 0; }"); // 8
		// "1&0" and "1>0" have different types.
		// (Note that the type of "i" would be considered later)
		assertTypeException(ParseEntity.PRG,
				"int test() { int i = 1 > 0 ? 1 & 0: 1 > 0; return 0; }"); // 11
	}

	@Test
	public void testDoWhile() {
		assertResult(
				"int doWhile() { int i = 0; do { i = i + 1; } while (false); return i; }",
				1);
		assertResult(
				"int doWhile() { int i = 0; int b = -5; do { b = -b; } while (false); return b; }",
				5);
	}

	@Test
	public void testFibIter() {
		assertResult(
				"int fib(int x) { int f = 0; int l = 1; while(x > 0) { int tmp = l; l = l + f; f = tmp; print(f); x = x - 1; } return f; }",
				13, 7);
	}

	@Test
	public void testBreak() {
		assertResult(
				"int brk() { int i = 0; while(true) { if (i == 10) { break; } i = i + 1; } return i; }",
				10);
	}

	@Test
	public void testContinue() {
		assertResult(
				"int brk() { int i = 0; int b = 0; while (i < 10) { i = i + 1; if (i > 3) { continue; } b = b + 1; } return b; }",
				3);
	}

	@Test
	public void testDocumentationExample5() {
		assertResult(
				"int pow(int x, int y) { int result = 1; while (y > 0) { result = result * x; y = y - 1; } return result; }",
				32, 2, 5);
	}

	@Test
	public void testBinaryOperations() {
		assertResult("boolean f() { return 10 == 5; }", 0);
		assertResult("boolean f() { return 10 != 5; }", 1);
		assertResult("boolean f() { return 10 == 10; }", 1);
		assertResult("boolean f() { return 10 == 11; }", 0);
		assertResult("boolean f() { return 10 > 5; }", 1);
		assertResult("boolean f() { return 5 > 10; }", 0);
		assertResult("boolean f() { return 10 < 5; }", 0);
		assertResult("boolean f() { return 5 < 10; }", 1);
		assertResult("int f() { return 4 >> 1; }", 2);
		assertResult("int f() { return 4 << 1; }", 8);
		assertResult("boolean f() { return 1 >= 1; }", 1);
		assertResult("boolean f() { return 1 < 1; }", 0);
	}

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("CompilerAndreasTest", "1.0");
	}
}