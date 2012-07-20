package prog2.project4.tests.prog2tests;

import static org.junit.Assert.*;
import org.junit.*;

import de.unisb.prog.mips.assembler.segments.Element;
import prog2.project4.driver.ParseEntity;
import prog2.project4.tree.TreeFactory;
import prog2.project4.tree.Type;

/**
 * 
 * @author Markus
 * 
 * 
 * 
 *         Intensive Operator testing - all unary and binary operators - checks
 *         all combinations of typing - checks results of all thinkable
 *         combinations - checks mixed expressions - also checks your register
 *         management, if you can handle big expressions and if you use illegal
 *         registers
 * 
 * 
 */

public class OperatorsMarkusTest extends TestBaseMarkus {
	public final String VERSION = "1.0";

	protected TreeFactory factory;

	@Before
	public void setUp() {
		factory = conf.getTreeFactory();
	}

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("OperatorsMarkusTest", VERSION);
	}

	@Test
	public void UnaryOperatorTyping() {
		// check for valid types
		subtest = 1;
		assertNoTypingExceptionExpr("+2", "Unary + must accept integer operand");
		assertNoTypingExceptionExpr("-0", "Unary - must accept integer operand");
		assertNoTypingExceptionExpr("!true", "Unary ! (not) must accept boolean operand");
		assertNoTypingExceptionExpr("!false", "Unary ! (not) must accept boolean operand");

		// check for invalid types
		subtest = 2;
		assertTypingExceptionExpr("+true", "Unary + mustn't accept boolean operand");
		assertTypingExceptionExpr("-false", "Unary - mustn't accept boolean operand");
		assertTypingExceptionExpr("!12", "Unary ! (not) mustn't accept integer operand");
		assertTypingExceptionExpr("!0",
				"Unary ! (not) mustn't accept integer operand, even if it's 0 or 1");

		// Check return types
		subtest = 3;
		assertExprType(Type.INT, "+1", "Unary + returns wrong type (int expected)");
		assertExprType(Type.INT, "-0", "Unary - returns wrong type (int expected)");
		assertExprType(Type.BOOL, "!true", "Unary ! (not) returns wrong type (bool expected)");
		assertExprType(Type.BOOL, "!false", "Unary ! (not) returns wrong type (bool expected)");
	}

	@Test
	public void BinaryOperatorTyping() {
		// checks every type combination (int/int, bool/int, int/bool,
		// bool/bool)
		subtest = 0;
		String[] arithmeticoperators = { "+", "-", "*", "/", "<<", ">>" };
		String[] compareoperators = { "<", "<=", ">", ">=" };
		for (int i = 0; i < 4; i++) {
			String a = (i % 2 == 0) ? "1" : "true";
			String b = (i / 2 == 0) ? "3" : "false";

			// Arithmetic (a o b) - exceptions if a or b are not INT
			for (String o : arithmeticoperators) {
				if (i == 0) {
					assertNoTypingExceptionExpr(a + " " + o + " " + b, "Binary " + o
							+ " should accept two ints");
					assertExprType(Type.INT, a + " " + o + " " + b, "Binary " + o
							+ " returns a wrong type!");
				} else
					assertTypingExceptionExpr(a + " " + o + " " + b, "Binary " + o
							+ " should only accept two ints");
			}

			// and/or : int/int=>int or bool/bool=>bool
			if (i == 0 || i == 3) {
				assertNoTypingExceptionExpr(a + " & " + b, "Binary & should accept two equal types");
				if (i == 0)
					assertExprType(Type.INT, a + " & " + b,
							"Binary & returns a wrong type for two ints!");
				else
					assertExprType(Type.BOOL, a + " & " + b,
							"Binary & returns a wrong type for two bools!");

				assertNoTypingExceptionExpr(a + " | " + b, "Binary | should accept two equal types");
				if (i == 0)
					assertExprType(Type.INT, a + " | " + b,
							"Binary | returns a wrong type for two ints!");
				else
					assertExprType(Type.BOOL, a + " | " + b,
							"Binary | returns a wrong type for two bools!");
			} else {
				assertTypingExceptionExpr(a + " & " + b, "Binary & shouldn't accept mixed types");
				assertTypingExceptionExpr(a + " | " + b, "Binary | shouldn't accept mixed types");
			}

			// == / != expect int/int or bool/bool and return bool
			if (i == 0 || i == 3) {
				assertNoTypingExceptionExpr(a + " == " + b, "== should accept two equal types");
				assertExprType(Type.BOOL, a + " == " + b, "== returns a wrong type for two ints!");

				assertNoTypingExceptionExpr(a + " != " + b, "!= should accept two equal types");
				assertExprType(Type.BOOL, a + " != " + b, "!= returns a wrong type for two ints!");
			} else {
				assertTypingExceptionExpr(a + " & " + b, "Binary & shouldn't accept mixed types");
				assertTypingExceptionExpr(a + " | " + b, "Binary | shouldn't accept mixed types");
			}

			// Compare-Operators expect int/int and return bool
			for (String o : compareoperators) {
				if (i == 0) {
					assertNoTypingExceptionExpr(a + " " + o + " " + b, o
							+ " should accept two ints");
					assertExprType(Type.BOOL, a + " " + o + " " + b, o + " must return bool");
				} else
					assertTypingExceptionExpr(a + " " + o + " " + b, o
							+ "should only accept two ints");
			}
		}

		// Special cases
		assertNoTypingExceptionExpr("1 / 0", "division by zero is valid and should return int!");
		assertNoTypingExceptionExpr("1 >> -1",
				"shift with negative amouth has nothing to do with types");
		assertNoTypingExceptionExpr("1 << -1",
				"shift with negative amouth has nothing to do with types");
	}

	@Test
	public void UnaryOperatorCompiler() {
		try {
			// SUBTEST 1 - unary -
			subtest = 1;
			code = "-1";
			assertResult(ParseEntity.EXPR, code, -1);
			code = "-0";
			assertResult(ParseEntity.EXPR, code, 0);
			code = "-----5";
			assertResult(ParseEntity.EXPR, code, -5);
			code = "----2";
			assertResult(ParseEntity.EXPR, code, 2);

			// SUBTEST 2 - unary +
			subtest = 2;
			code = "+1";
			assertResult(ParseEntity.EXPR, code, +1);
			code = "+0";
			assertResult(ParseEntity.EXPR, code, 0);
			code = "+(-1)";
			assertResult(ParseEntity.EXPR, code, -1);

			// SUBTEST 3 - unary !
			subtest = 3;
			code = "!false";
			assertResult(ParseEntity.EXPR, code, 1);
			code = "!true";
			assertResult(ParseEntity.EXPR, code, 0);
			code = "!!!!true";
			assertResult(ParseEntity.EXPR, code, 1);
			code = "!!!!false";
			assertResult(ParseEntity.EXPR, code, 0);
			code = "!!!!!true";
			assertResult(ParseEntity.EXPR, code, 0);

		} catch (AssertionError e) {
			System.err.println(geterrhead());
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}

	@Test
	public void BinaryOperatorCompiler() {
		try {
			// SUBTEST 1 - Testing all operators with combinations of some
			// numbers
			subtest = 1;
			String[] operators = { "*", "/", "+", "-", "&", "|", "<<", ">>", "==", "!=", "<=", "<",
					">=", ">" };
			int[] numbers = { -20, -3, -1, 0, 1, 2, 3, 18 };

			for (String o : operators) {
				for (int n1 : numbers)
					for (int n2 : numbers) {
						if (o.equals("/") && n2 == 0)
							continue; // no division by zero test
						if (n2 < 0 && (o.equals(">>") || o.equals("<<")))
							continue; // no negative shifting
						code = (n1 >= 0 ? n1 : "(" + n1 + ")") + " " + o + " "
								+ (n2 >= 0 ? n2 : "(" + n2 + ")"); // add () for
																	// negative
																	// numbers
						int eres = evaluateBinOp(n1, n2, o);
						int res = getResultExpr(code, null);
						if (res == eres)
							continue;
						if (o.equals(">>") && res == n1 >>> n2) {
							assertEquals(
									"I think you use logical right shift instead of arithmetic. >> has to return the SIGN-extended number, not the ZERO-extended. \nuse srav instead of srlv!\n",
									eres, res);
						}
						assertEquals("Binary " + o + " doesn't work correctly. ", eres, res);
					}
			}

			// SUBTEST 2 - Testing all boolean operators
			subtest = 2;
			String[] binaryoperators = { "|", "&", "==", "!=" };
			String[] bools = { "true", "false" };

			for (String o : binaryoperators) {
				for (String v1 : bools)
					for (String v2 : bools) {
						code = v1 + " " + o + " " + v2;
						int eres = evaluateBinOp(v1, v2, o);
						int res = getResultExpr(code, null);
						assertEquals("Binary " + o + " doesn't work for bools!", eres, res);
					}
			}

		} catch (AssertionError e) {
			System.err.println(geterrhead());
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}

	@Test
	public void OperatorMixedTests() {
		try {
			// SUBTEST 1 - Arithmetic
			subtest = 1;
			code = "1 + 2 * 3 + 4";
			assertResult(ParseEntity.EXPR, code, 11);
			code = "(1+2*2+3*3+4*4)/5";
			assertResult(ParseEntity.EXPR, code, 6);
			code = "2*(1---2 +++5)";
			assertResult(ParseEntity.EXPR, code, 8);
			code = "5 * -2";
			assertResult(ParseEntity.EXPR, code, -10);
			code = "-5 * -10";
			assertResult(ParseEntity.EXPR, code, 50);

			// SUBTEST 2 - Boolean
			subtest = 2;
			code = "(true & false) | (true & true) | !true";
			assertResult(ParseEntity.EXPR, code, 1);
			code = "(false == false) & (true == true) & (false == !true)";
			assertResult(ParseEntity.EXPR, code, 1);
			code = "(true != true) | (false != false)";
			assertResult(ParseEntity.EXPR, code, 0);

			// SUBTEST 3 - Mixed
			subtest = 3;
			code = "(3 == 1) & true";
			assertResult(ParseEntity.EXPR, code, 0);
			code = "(10 != 2) & (2 == 2)";
			assertResult(ParseEntity.EXPR, code, 1);
			code = "!(10 != 2)";
			assertResult(ParseEntity.EXPR, code, 0);

		} catch (AssertionError e) {
			System.err.println(geterrhead());
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}

	// register test

	@Test
	public void RegisterTest() {
		code = "";
		String code2 = "1";
		String ce = "";
		for (int i = 1; i < 36; i++) {
			code += "(" + i + " + ";
			ce += ")";
			code2 = "(" + code2 + ") + " + (i + 1);
		}
		code = code + 36 + ce;

		// SUBTEST 1
		// This test checks expression trees with more than 12 operatores to see
		// how you manage your registers
		// If you don't evaluate the bigger subtree first, you will run out of
		// registers here
		// This might produce every kind of error cause it's not specified what
		// should happen.
		// You might even get a "wrong result". In this cases, check how you
		// manage your registers.
		subtest = 1;
		try {
			// code is (1 + (2 + (3 + ...)))
			assertResult(ParseEntity.EXPR, code, 666);
			// code is (((1 + 2) + 3) ... 36)
			code = code2;
			assertResult(ParseEntity.EXPR, code, 666);
		} catch (Throwable e) {
			System.err.println(geterrhead());
			System.out
					.println("Your compiler doesn't really work for code with a lot of operators (35 here). \nCheck how you manage your registers. ");
			System.out
					.println("If you find a binary operator in a node, you first evaluate the node who needs more registers and then the other node. ");
			System.out.println("Check the slides: 11_codegen.pdf, last page. ");
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			printCode();
			printAsm();
			fail(e.getClass().getName() + ": " + e.getMessage());
			return;
		}

		// SUBTEST 2
		// Let's check a really big expression, formed in a binary tree with 512
		// leaves. You'll need the maximum amounth of 10 registers
		// code = (... (((1+2) + (3+4)) + ((5+6) + (7+8))) ... ((509+510) +
		// (511+512)) ...)
		// If you use all 12 allowed registers replace 512 with 2048 and 131328
		// with 2098176 to test your implementation with maximal size.
		subtest = 2;
		code = makeBigExpr(1, 512);
		try {
			assertResult(ParseEntity.EXPR, code, 131328);
		} catch (Throwable e) {
			System.err.println(geterrhead());
			System.out
					.println("Your compiler doesn't really work for code with a lot of operators (511 here). \nCheck how you manage your registers. ");
			System.out
					.println("If you find a binary operator in a node, you first evaluate the node who needs more registers and then the other node. ");
			System.out.println("Check the slides: 11_codegen.pdf, last page. ");
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			printCode();
			printAsm();
			fail(e.getClass().getName() + ": " + e.getMessage());
			return;
		}

		// SUBTEST 3
		// Search for illegal registers.
		// Forbidden: $sp, $gp, $fp, $a0-$a3, $k0, $k1, $at, $s0-$s7
		// $0 is allowed to use cause it's unwritalbe, $rt might be used by
		// assembler or expr test routines
		subtest = 3;
		for (Element e : asm.getText()) {
			String s = e.toString();
			if (s.matches(".*\\$[sgp]p.*") || s.matches(".*\\$a[0-3].*")
					|| s.matches(".*\\$k[01].*") || s.matches(".*\\$at.*")
					|| s.matches(".*\\$s[0-7].*")) {
				System.err.println(geterrhead());
				System.out.println("You use registers you aren't allowed to use: " + s);
				System.out.println("You might only use $v0, $v1 and $t0-$t9. ");
				printCode();
				printAsm();
				fail("You use registers you aren't allowed to use: " + s);
				return;
			}
		}

	}

	// HELPERS

	private int evaluateBinOp(int n1, int n2, String o) {
		if (o.equals("+"))
			return n1 + n2;
		if (o.equals("-"))
			return n1 - n2;
		if (o.equals("*"))
			return n1 * n2;
		if (o.equals("/"))
			return n1 / n2;
		if (o.equals(">>"))
			return n1 >> n2;
		if (o.equals("<<"))
			return n1 << n2;
		if (o.equals("&"))
			return n1 & n2;
		if (o.equals("|"))
			return n1 | n2;
		if (o.equals("=="))
			return n1 == n2 ? 1 : 0;
		if (o.equals("!="))
			return n1 != n2 ? 1 : 0;
		if (o.equals("<"))
			return n1 < n2 ? 1 : 0;
		if (o.equals("<="))
			return n1 <= n2 ? 1 : 0;
		if (o.equals(">"))
			return n1 > n2 ? 1 : 0;
		if (o.equals(">="))
			return n1 >= n2 ? 1 : 0;
		throw new UnsupportedOperationException(o + " not known!");
	}

	private int evaluateBinOp(String v1, String v2, String o) {
		if (o.equals("|"))
			return vtb(v1) | vtb(v2) ? 1 : 0;
		if (o.equals("&"))
			return vtb(v1) & vtb(v2) ? 1 : 0;
		if (o.equals("=="))
			return v1.equals(v2) ? 1 : 0;
		if (o.equals("!="))
			return v1.equals(v2) ? 0 : 1;
		throw new UnsupportedOperationException(o + " not known!");
	}

	// value (String) to boolean
	private boolean vtb(String v) {
		return v.equals("true");
	}

	// ( ((start + start+1) + (start+2 + start+3)) ... ((stop-3 + stop-2) +
	// (stop-1 + stop)) )
	private String makeBigExpr(int start, int stop) {
		if (start == stop)
			return Integer.toString(start);
		int mid = start + (stop - start + 1) / 2 - 1;
		return "(" + makeBigExpr(start, mid) + " + " + makeBigExpr(mid + 1, stop) + ")";
	}

}
