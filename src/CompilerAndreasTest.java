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

	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testNightlyTypeExceptions() throws Exception {
		// 555 ist not a boolean
		assertTypeException(ParseEntity.PRG, "int test() { boolean x = 555; return 0; }"); // 5
		// You can't do "+" on two booleans.
		assertTypeException(ParseEntity.PRG, "int test() { boolean x = true + true; return 0; }"); // 7
		// 555 is not a boolean
		assertTypeException(ParseEntity.PRG, "int test() { int i = 555 ? 1 : 0; return 0; }"); // 8
		// "1&0" and "1>0" have different types.
		// (Note that the type of "i" would be considered later)
		assertTypeException(ParseEntity.PRG,
				"int test() { int i = 1 > 0 ? 1 & 0: 1 > 0; return 0; }"); // 11
	}

	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testDoWhile() {
		// Assure that regardless of the loop-condition you have to execute the
		// body at least one time.
		assertResult("int doWhile() { int i = 0; do { i = i + 1; } while (false); return i; }", 1);
		assertResult(
				"int doWhile() { int i = 0; int b = -5; do { b = -b; } while (false); return b; }",
				5);
		// Assures you execute the body only once, when the condition is not
		// satisfied. Perhaps you have to change the MIPS code you generate for
		// do while loops.
		assertResult("int doWhileContinue() { do { continue; } while(false);  return 5;}", 5);

		// Using a break statement in a do while loop should also cancel the
		// execution.
		assertResult(
				"int doAndBreakWhile() { int i = 0; do { i = i + 1; if (i == 5) { break; } } while (true); return i; }",
				5);

		// Using a continue statement in a do while loop should also cancel the
		// current loop execution... and divide your faculty by one factor.
		assertResult(
				"int doAndContinueWhile(int n) { int i = 0; int s = 1; do { i = i + 1; if (i == 3) { continue; } s = s * i; } while (i < n); return s; }",
				40, 5);
	}

	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testReturnParam() {
		// Assures that you can also return parameter values.
		assertResult("int id(int x) { return x; }", 5, 5);
	}

	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testNestedIfElse() {
		String program = "int f(int x) { int res = -10; if(x == 5) { res = 0; } else { if(x < 5) { res = -1;} else { res = 1; } } return res; }";
		assertResult(program, -1, 3);
		assertResult(program, 0, 5);
		assertResult(program, 1, 10);
	}

	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testAlgorithmIntegration() {
		// Just a simple use case, where MIPS should calculate Fibonacci number
		// 7.
		assertResult(
				"int fib(int x) { int f = 0; int l = 1; while(x > 0) { int tmp = l; l = l + f; f = tmp; x = x - 1; } return f; }",
				13, 7);
		// GCD uses loops, conditions, breaks, ... so you can see how all
		// features interact.
		String gcdProgram = "int gcd(int x, int y) { int res = 0; if(x < 0) { x = -x; } if(y < 0) { y = -y; } while (true)	{ if(x == y) { res = x; break; } if(y == 0) { res = x; break; } if(x == 0) { res = y; break; } if (x > y) { x = x - y; } else { if (y > x) { y = y - x; } } } return res; }";
		assertResult(gcdProgram, 3, 3, 9);
		assertResult(gcdProgram, 5, 25, 10);
		assertResult(gcdProgram, 7, 21, 49);

		assertResult(
				"int fac(int x) { int res = 1; while(x > 0) { res = res * x; x = x - 1; } return res; }",
				479001600, 12);
	}

	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testBreak() {
		// Break should terminate a loop.
		assertResult(
				"int brk() { int i = 0; while(true) { if (i == 10) { break; } i = i + 1; } return i; }",
				10);
		// A break does terminate excactly one loop.
		assertResult(
				"int doubleBreak() { int i = 0; while(i < 10) { int j = 0; while(j < 10) { if (j == 3) { break; } j = j + 1;} i = i + 1; } return i; }",
				10);
	}

	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testContinue() {
		// The contiune should stop you from increasing the result value (b).
		assertResult(
				"int goOn() { int i = 0; int b = 0; while (i < 10) { i = i + 1; if (i > 3) { continue; } b = b + 1; } return b; }",
				3);
	}

	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testDocumentationExample5() {
		// An execution of the documentation's fifth example, that is not
		// included in the public tests.
		assertResult(
				"int pow(int x, int y) { int result = 1; while (y > 0) { result = result * x; y = y - 1; } return result; }",
				32, 2, 5);
	}

	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testBinaryOperations() {
		/*
		 * If you pass BinOpBulkTest the following tests are irrelevant for you.
		 * If not you can use these tests to write against cases that you can
		 * read/understand easier than the cryptic BulkTest.
		 */
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

	/**
	 * Corrected in version 1.2. Thanks to Daniel Steine for reporting this.
	 * https://code.google.com/p/prog2tests/issues/detail?id=47
	 */
	@Test(timeout = TestBase.DEFAULT_TIMEOUT)
	public void testNightly6_Bonus1() throws Exception {
		// Test corrected by Daniel Steines - thank you!
		assertTypeException(ParseEntity.PRG, "int test() {return 2147483648;}");
		assertResult(ParseEntity.PRG, "int test() {return -2147483648;}", -2147483648);
		assertResult(ParseEntity.PRG, "int test() {return 2147483647;}", 2147483647);
	}

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("CompilerAndreasTest", "1.3");
	}

}
