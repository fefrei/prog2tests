package prog2.project4.tests.prog2tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;

import prog2.project4.codegen.mips.MipsAsm;
import prog2.project4.codegen.mips.MipsCodeGenerator;
import prog2.project4.driver.Configuration;
import prog2.project4.driver.Driver;
import prog2.project4.driver.ParseEntity;
import prog2.project4.parser.ParserException;
import prog2.project4.tree.NamingException;
import prog2.project4.tree.Scope;
import prog2.project4.tree.Tree;
import prog2.project4.tree.TreeFactory;
import prog2.project4.tree.TypingException;
import de.unisb.prog.mips.assembler.LabelAlreadyDefinedException;
import de.unisb.prog.mips.assembler.LabelRef;
import de.unisb.prog.mips.assembler.Reg;
import de.unisb.prog.mips.insn.Instruction;
import de.unisb.prog.mips.simulator.ExceptionHandler;
import de.unisb.prog.mips.simulator.MemDumpFormatter;
import de.unisb.prog.mips.simulator.Memory;
import de.unisb.prog.mips.simulator.Processor;
import de.unisb.prog.mips.simulator.ProcessorState;
import de.unisb.prog.mips.simulator.ProcessorState.ExecutionState;
import de.unisb.prog.mips.simulator.Sys;
import de.unisb.prog.mips.simulator.SysCallHandler;

public class MassTestingBewied {

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("MassTestingBewied", "1.0.1");
	}
	
	// ===== CONSTANTS ====
	// You shouldn't need to touch those.

	// I have no idea what this does:
	private static final boolean DEBUG_DRIVER = true;

	// Prevent typos:
	private static final String TIMEOUT_ATTRIBUTE = "mass_timeout",
			OPTIMIZE_ATTRIBUTE = "optimize";

	// Amount of milliseconds before a given test is aborted.
	// I'd recommend around 20 seconds, that means 3 * 1000 milliseconds.
	// Don't worry, java does constant-folding.
	private static final int DEFAULT_TIMEOUT = 7 * 1000;

	// Shortcut that is more easily readable
	public static final int[] EMPTY = new int[] {};

	// ===== SOME TESTS, behavioural nature.
	// Feel free to write your own! :)

	// These tests are valid programs that test all the stages up to and
	// including code generation.
	// For "simpler" tests, see TreeBewiedTest

	// I tried to write tests that aren't too trivial.
	// So I hope that even your optimized code uses the intended opcodes.

	@Test
	public void testUnaryMinus() {
		String text = "int negativate(int a) { return -a; }";
		String scope = "(a)";
		Program p = new Program(text, scope);
		p.add(new Behavior(0, 0));
		p.add(new Behavior(50, -50));
		p.add(new Behavior(-42, 42));
		p.add(new Behavior(Integer.MAX_VALUE, -Integer.MAX_VALUE));
		p.add(new Behavior(Integer.MIN_VALUE, Integer.MIN_VALUE));
		runWrapped(p);
	}

	@Test
	public void testUnaryPlus() {
		String text = "int noOp(int a) { return +a; }";
		String scope = "(a)";
		Program p = new Program(text, scope);
		p.add(new Behavior(0, 0));
		p.add(new Behavior(50, 50));
		p.add(new Behavior(-42, -42));
		p.add(new Behavior(Integer.MAX_VALUE, Integer.MAX_VALUE));
		p.add(new Behavior(Integer.MIN_VALUE, Integer.MIN_VALUE));
		runWrapped(p);
	}

//	@Test
//	public void testNotNot1() {
//		String text = "int whoIsThere(int a) { return !a; }";
//		String scope = "(a)";
//		Program p = new Program(text, scope);
//		p.add(new Behavior(0, -1));
//		p.add(new Behavior(-1, 0));
//		p.add(new Behavior(50, -51));
//		p.add(new Behavior(-42, 41));
//		p.add(new Behavior(Integer.MAX_VALUE, Integer.MIN_VALUE));
//		p.add(new Behavior(Integer.MIN_VALUE, Integer.MAX_VALUE));
//		runWrapped(p);
//	}

	@Test
	public void testNotNot2() {
		String text = "boolean whoIsThere(boolean a) { return !a; }";
		String scope = "(a)";
		Program p = new Program(text, scope);
		p.add(new Behavior(0, 1));
		p.add(new Behavior(1, 0));
		runWrapped(p);
	}

	@Test
	public void testEquals() {
		runBinopTest("==", "int", "boolean", 3, 4, 0, /**/-3, 4, 0, /**/-1, 1, 0, /**/
				-30, -50, 0, /**/0, -5, 0, /**/Integer.MAX_VALUE, -1, 0, /**/
				-Integer.MAX_VALUE, 1, 0, /**/7, 7, 1, /**/Integer.MAX_VALUE, Integer.MAX_VALUE, 1, /**/
				-Integer.MAX_VALUE, -Integer.MAX_VALUE, 1);
		runBinopTest("==", "boolean", "boolean", 0, 0, 1, /**/0, 1, 0, /**/1, 0, 0, /**/
				1, 1, 1);
	}

	@Test
	public void testAnd() {
		// 0b0011 = 3
		// 0b0101 = 5
		// 0b0001 = 1
		runBinopTest("&", "int", "int", 3, 5, 1, /**/10, Integer.MAX_VALUE, 10, /**/
				10, 0, 0);
	}

	@Test
	public void testOr() {
		// 0b0011 = 3
		// 0b0101 = 5
		// 0b0111 = 7
		runBinopTest("|", "int", "int", 3, 5, 7, /**/10, Integer.MAX_VALUE, Integer.MAX_VALUE, /**/
				10, 0, 10);
	}

//	@Test
//	public void testNotEquals() {
//		runBinopTest("!=", "int", "int", 3, 4, 1, /**/-3, 4, 1, /**/-1, 1, 1, /**/
//				-30, -50, 1, /**/0, -5, 1, /**/Integer.MAX_VALUE, -1, 1, /**/
//				-Integer.MAX_VALUE, 1, 1, /**/7, 7, 0, /**/Integer.MAX_VALUE, Integer.MAX_VALUE, 0, /**/
//				-Integer.MAX_VALUE, -Integer.MAX_VALUE, 0);
//		runBinopTest("!=", "bool", "int", 0, 0, 0, /**/0, 1, 1, /**/1, 0, 1, /**/
//				1, 1, 0);
//	}

	@Test
	public void testAddition() {
		runBinopTest("+", "int", "int", 3, 4, 7, /**/-3, 4, 1, /**/-1, 1, 0, /**/
				-30, -50, -80, /**/0, -5, -5);
	}

	@Test
	public void testSubstraction() {
		runBinopTest("-", "int", "int", 3, 4, -1, /**/-3, 4, -7, /**/-1, 1, -2, /**/
				-30, -50, 20, /**/0, -5, 5);
	}

	@Test
	public void testMultiplication() {
		runBinopTest("*", "int", "int", 3, 4, 12, /**/-3, 4, -12, /**/-1, 1, -1, /**/
				-30, -50, 1500, /**/0, -5, 0);
	}

	@Test
	public void testDivision() {
		runBinopTest("/", "int", "int", 3, 4, 0, /**/-3, 4, 0, /**/-1, 1, -1, /**/
				-30, -50, 0, /**/0, -5, 0);
	}

	@Test
	public void testLessThan() {
		runBinopTest("<", "int", "boolean", 3, 4, 1, /**/4, 3, 0, /**/-3, 4, 1, /**/
				-1, 1, 1, /**/-30, -50, 0, /**/0, -5, 0, /**/
				-Integer.MAX_VALUE, Integer.MAX_VALUE, 1, /**/Integer.MAX_VALUE, -Integer.MAX_VALUE,
				0, /**/-7, -7, 0, /**/Integer.MAX_VALUE, Integer.MAX_VALUE, 0, /**/
				-Integer.MAX_VALUE, -Integer.MAX_VALUE, 0);
	}

	@Test
	public void testLessThanEquals() {
		runBinopTest("<=", "int", "boolean", 3, 4, 1, /**/4, 3, 0, /**/-3, 4, 1, /**/
				-1, 1, 1, /**/-30, -50, 0, /**/0, -5, 0, /**/
				-Integer.MAX_VALUE, Integer.MAX_VALUE, 1, /**/Integer.MAX_VALUE, -Integer.MAX_VALUE,
				0, /**/-7, -7, 1, /**/Integer.MAX_VALUE, Integer.MAX_VALUE, 1, /**/
				-Integer.MAX_VALUE, -Integer.MAX_VALUE, 1);
	}

	@Test
	public void testGreaterThan() {
		runBinopTest(">", "int", "boolean", 3, 4, 0, /**/4, 3, 1, /**/-3, 4, 0, /**/
				-1, 1, 0, /**/-30, -50, 1, /**/0, -5, 1, /**/
				-Integer.MAX_VALUE, Integer.MAX_VALUE, 0, /**/Integer.MAX_VALUE, -Integer.MAX_VALUE,
				1, /**/-7, -7, 0, /**/Integer.MAX_VALUE, Integer.MAX_VALUE, 0, /**/
				-Integer.MAX_VALUE, -Integer.MAX_VALUE, 0);
	}

//	@Test
//	public void testGreaterThanEquals() {
//		runBinopTest(">=", "int", "boolean", 3, 4, 1, /**/4, 3, 0, /**/-3, 4, 1, /**/
//				-1, 1, 1, /**/-30, -50, 0, /**/0, -5, 0, /**/
//				-Integer.MAX_VALUE, Integer.MAX_VALUE, 1, /**/Integer.MAX_VALUE, -Integer.MAX_VALUE,
//				0, /**/-7, -7, 1, /**/Integer.MAX_VALUE, Integer.MAX_VALUE, 1, /**/
//				-Integer.MAX_VALUE, -Integer.MAX_VALUE, 1);
//	}

//	@Test
//	public void testShiftRight() {
//		// 0xFFFF8001 = -32767
//		// 0xFFFFFF80 = -32767>>8 = -128
//		// 0x00007FFF = +32767
//		// 0x0000007F = +32767>>8 = 127
//		runBinopTest(">>", "int", "int", 1, 1, 2, /**/0, 4, 0, /**/-32767, 8, -128, /**/32767, 8, 127, /**/
//				-1, 31, 1);
//	}

//	@Test
//	public void testShiftLeft() {
//		// 0xFFFF8001 = -32767
//		// 0x00007FFF = +32767
//		runBinopTest("<<", "int", "int", 3, 4, 0x30, /**/-3, 4, 0xFFFD0000, /**/
//				-1, 1, -2, /**/1, 1, 2, /**/0, 4, 0, /**/-32767, 8, 0xFF800100, /**/
//				32767, 8, 0x7FFF0000, /**/-1, 31, Integer.MIN_VALUE);
//	}

	@Test
	public void testStackedIf() {
		/**
		 * <pre>
		 * foo (int a) {
		 * int retVal = 0;
		 * if (a > 0) {
		 *              if (a > 5) {
		 *                      retVal = 10;
		 *              } else {
		 *                      retVal = 3;
		 *              }
		 * } else {
		 *              if (a < -5) {
		 *                      retVal = -10;
		 *              } else {
		 *                      retVal = -3;
		 *              }
		 * }
		 * return retVal;
		 * }
		 * 
		 * FunctionTree[IDENT:foo](
		 *              ListTree[LIST:null](
		 *                      ParamDeclTree[IDENT:a]{INT, #0}()),
		 *              ListTree[LIST:null](
		 *                      AssignTree[=:null](
		 *                              DeclTree[IDENT:retVal]{INT}(),
		 *                              IntLitTree[INT_LITERAL:0]()),
		 *                      TernaryTree[if:if](
		 *                              IntBoolBinopTree[>:null](
		 *                                      IdentUseTree[IDENT:a]{Defined in null}(),
		 *                                      IntLitTree[INT_LITERAL:0]()),
		 *                              ListTree[LIST:null](
		 *                                      TernaryTree[if:if](
		 *                                              IntBoolBinopTree[>:null](
		 *                                                      IdentUseTree[IDENT:a]{Defined in null}(),
		 *                                                      IntLitTree[INT_LITERAL:5]()),
		 *                                              ListTree[LIST:null](
		 *                                                      AssignTree[=:null](
		 *                                                              IdentUseTree[IDENT:retVal]{Defined in null}(),
		 *                                                              IntLitTree[INT_LITERAL:10]())),
		 *                                              ListTree[LIST:null](
		 *                                                      AssignTree[=:null](
		 *                                                              IdentUseTree[IDENT:retVal]{Defined in null}(),
		 *                                                              IntLitTree[INT_LITERAL:3]())))),
		 *                              ListTree[LIST:null](
		 *                                      TernaryTree[if:if](
		 *                                              IntBoolBinopTree[<:null](
		 *                                                      IdentUseTree[IDENT:a]{Defined in null}(),
		 *                                                      IntUnopTree[-:null](
		 *                                                              IntLitTree[INT_LITERAL:5]())),
		 *                                              ListTree[LIST:null](
		 *                                                      AssignTree[=:null](
		 *                                                              IdentUseTree[IDENT:retVal]{Defined in null}(),
		 *                                                              IntUnopTree[-:null](
		 *                                                                      IntLitTree[INT_LITERAL:10]()))),
		 *                                              ListTree[LIST:null](
		 *                                                      AssignTree[=:null](
		 *                                                              IdentUseTree[IDENT:retVal]{Defined in null}(),
		 *                                                              IntUnopTree[-:null](
		 *                                                                      IntLitTree[INT_LITERAL:3]())))))),
		 *                      IdentUseTree[IDENT:retVal]{Defined in null}()))
		 * </pre>
		 */

		String text = "int foo(int a) { int retVal = 0; if (a > 0) {"
				+ " if (a > 5) { retVal = 10; } else { retVal = 3; }" + " } else {"
				+ " if (a < -5) { retVal = -10; } else { retVal = -3; }" + " } return retVal; }";
		String scope = "(a, retVal (()()) (()()))";
		Program p = new Program(text, scope);
		p.add(new Behavior(50, 10));
		p.add(new Behavior(2, 3));
		p.add(new Behavior(5, 3));
		p.add(new Behavior(0, -3));
		p.add(new Behavior(-50, -10));
		p.add(new Behavior(-2, -3));
		runWrapped(p);
	}

	@Test
	public void testSimpleCond() {
		String text = "int maskAnswer(int a) { return (a == 42) ? 23 : a; }";
		String scope = "(a)";
		Program p = new Program(text, scope);
		p.add(new Behavior(0, 0));
		p.add(new Behavior(50, 50));
		p.add(new Behavior(42, 23));
		p.add(new Behavior(-50, -50));
		runWrapped(p);
	}

	@Test
	public void testArgumentWrite() {
		String text = "int getAnswer(int a, int b) {" + " b = 42 - a; return a + b; }";
		String scope = "(a, b)";
		Program p = new Program(text, scope);
		p.add(new Behavior(new int[] { 13, 42 }, 42));
		p.add(new Behavior(new int[] { 13, 41 }, 42));
		p.add(new Behavior(new int[] { 31, 42 }, 42));
		p.add(new Behavior(new int[] { -74561, 153 }, 42));
		p.add(new Behavior(new int[] { Integer.MAX_VALUE, 123 }, 42));
		runWrapped(p);
	}

	@Test
	public void testSpecialScoping() {
		String text = "int elaborateInc(int a) { int retVal = -1; "
				+ " { int a = a + 1; retVal = a; }  return retVal; }";
		String scope = "(a, retVal (a))";
		Program p = new Program(text, scope);
		p.add(new Behavior(12, 13));
		p.add(new Behavior(-1, 0));
		p.add(new Behavior(Integer.MAX_VALUE, Integer.MIN_VALUE));
		p.add(new Behavior(Integer.MIN_VALUE, -Integer.MAX_VALUE));
		runWrapped(p);
	}

	@Test
	public void testManyArguments() {
		String text = "int addAll(int a, int b, int c, int d, int e)"
				+ " { return a + b + c + d + e; }";
		String scope = "(a, b, c, d, e)";
		Program p = new Program(text, scope);
		p.add(new Behavior(new int[] { 0, 0, 0, 0, 0 }, EMPTY, 0));
		p.add(new Behavior(new int[] { 0, 0, 0, 0, 1 }, EMPTY, 1));
		p.add(new Behavior(new int[] { 1, 2, 3, 4, 5 }, EMPTY, 15));
		p.add(new Behavior(new int[] { -1, -2, -3, -4, -5 }, EMPTY, -15));
		p.add(new Behavior(new int[] { 99, 99, 99, 99, 99 }, EMPTY, 495));
		runWrapped(p);
	}

	@Test
	public void testStackedCond() {
		String text = "int maskAnswer(int a) { return ("
				+ "(a < 23) ? (a == 0) : (a == 42)) ? (a + 5) : a ; }";
		String scope = "(a)";
		Program p = new Program(text, scope);
		p.add(new Behavior(0, 5));
		p.add(new Behavior(5, 5));
		p.add(new Behavior(23, 23));
		p.add(new Behavior(42, 47));
		p.add(new Behavior(-42, -42));
		runWrapped(p);
	}

	@Test
	public void testStackedWhile() {
		/**
		 * <code>
		 * boolean foo(int a, int b) {
		 *      while (a > 0) {
		 *              int tmp = b;
		 *              while (tmp > 0) {
		 *                      print((a << 4) + tmp);
		 *                      tmp = tmp - 1;
		 *              }
		 *              a = a - 1;
		 *      }
		 *      return false;
		 * }
		 * </code>
		 */
		String text = "boolean foo(int a, int b) { while (a > 0) { int tmp = b; while (tmp > 0) { print((a << 4) + tmp); tmp = tmp - 1; } a = a - 1; } return false; }";
		String scope = "(a, b (tmp ()))";
		Program p = new Program(text, scope);
		p.add(new Behavior(new int[] { 0, -123 }, EMPTY, 0));
		p.add(new Behavior(new int[] { 321, -123 }, EMPTY, 0));
		p.add(new Behavior(new int[] { -321, 123 }, EMPTY, 0));
		p.add(new Behavior(new int[] { 1, 1 }, new int[] { 0x11 }, 0));
		p.add(new Behavior(new int[] { 3, 2 }, new int[] { 0x32, 0x31, 0x22, 0x21, 0x12, 0x11 }, 0));
		runWrapped(p);
	}

	@Test
	public void testStackedDoWhile() {
		String text = "boolean foo(int a, int b) { int bOrig = b; " + " do { b = bOrig;"
				+ " do { print ((a << 4) | b); b = b - 1; } while (b > 0);"
				+ " a = a - 1; } while (a > 0); return false; }";
		String scope = "(a, b, bOrig (()))";
		Program p = new Program(text, scope);
		p.add(new Behavior(new int[] { 0, 0 }, new int[] { 0 }, 0));
		p.add(new Behavior(new int[] { 2, 0 }, new int[] { 0x20, 0x10 }, 0));
		p.add(new Behavior(new int[] { 0, 1 }, new int[] { 1 }, 0));
		p.add(new Behavior(new int[] { 1, 1 }, new int[] { 0x11 }, 0));
		// 0b1111 1111 ____ = -1
		// 0b____ 0000 0001 = +1
		// 0b1111 1111 0001 = -15
		p.add(new Behavior(new int[] { -1, 1 }, new int[] { -15 }, 0));
		// 0b1111 1100 ____ = -4
		// 0b____ 0000 0001 = +1
		// 0b1111 1100 0001 = -128 + 64 + 1 = -103 + 40 = -63
		p.add(new Behavior(new int[] { -4, 1 }, new int[] { -63 }, 0));

		// 0b11111100____ = -4
		// 0b____10101111 = -128 + 32 + 8 + 4 + 2 + 1 = -96 + 15 = -81
		// 0b111111101111 = -17
		p.add(new Behavior(new int[] { -4, -81 }, new int[] { -17 }, 0));
		p.add(new Behavior(new int[] { 3, 2 }, new int[] { 0x32, 0x31, 0x22, 0x21, 0x12, 0x11 }, 0));
		runWrapped(p);
	}

	@Test
	public void testSimpleBlocks() {
		String text = "int foo(int a) { print(0); int b = a + 5;"
				+ " { int b = a + 3; print (b); } return b - 3; }";
		String scope = "(a, b (b))";
		Program p = new Program(text, scope);
		p.add(new Behavior(0, new int[] { 0, 3 }, 2));
		p.add(new Behavior(2, new int[] { 0, 5 }, 4));
		p.add(new Behavior(-5, new int[] { 0, -2 }, -3));
		runWrapped(p);
	}

	@Test
	public void testSimplePrint() {
		String text = "int doPrinting(int a) { print(0); print(a);"
				+ " print(a+1); print(a << 4); return a; }";
		String scope = "(a)";
		Program p = new Program(text, scope);
		p.add(new Behavior(0, new int[] { 0, 0, 1, 0 << 4 }, 0));
		p.add(new Behavior(1, new int[] { 0, 1, 2, 1 << 4 }, 1));
		p.add(new Behavior(4, new int[] { 0, 4, 5, 4 << 4 }, 4));
		p.add(new Behavior(-1, new int[] { 0, -1, 0, -1 << 4 }, -1));
		p.add(new Behavior(-4, new int[] { 0, -4, -3, -4 << 4 }, -4));
		runWrapped(p);
	}

	@Test
	public void testBlocks() {
		String text = "int foo(int a) { { int b = a + 3; print (b); } "
				+ " { int b = a + 2; print (b-1); }" + " int b = a + 1; return b - 3; }";
		String scope = "(a, b (b) (b))";
		Program p = new Program(text, scope);
		p.add(new Behavior(0, new int[] { 3, 1 }, -2));
		p.add(new Behavior(2, new int[] { 5, 3 }, 0));
		p.add(new Behavior(-5, new int[] { -2, -4 }, -7));
		runWrapped(p);
	}

	@Test
	public void testLimits() {
		// If you do constant folding: perfect!
		// If your program survives this test when optimizing: Even better! ;)
		String text = "boolean foo(int a) { int min = (1 << 31); int max = min - 1;"
				+ " print ( min ) ; print ( max ) ; "
				+ "return ( ( ( a > min ) & ( a < max ) ) & ( min < max ) ); }";
		// Scopes internally reorder their arguments.
		// They order the names lexically.
		String scope = "(a, max, min)";
		Program p = new Program(text, scope);
		p.add(new Behavior(0, new int[] { Integer.MIN_VALUE, Integer.MAX_VALUE }, 1));
		p.add(new Behavior(Short.MIN_VALUE, new int[] { Integer.MIN_VALUE, Integer.MAX_VALUE }, 1));
		p.add(new Behavior(Short.MAX_VALUE, new int[] { Integer.MIN_VALUE, Integer.MAX_VALUE }, 1));
		runWrapped(p);
	}

	@Test
	public void testBonus1() {
		String text = "int foo() { return -2147483648; }";
		String scope = "()";
		Program p = new Program(text, scope);
		p.add(new Behavior(EMPTY, EMPTY, Integer.MIN_VALUE));
		runWrapped(p);
	}

	@Test
	public void testOverflow() {
		String text = "int foo(int a) { return - ( 2147483647 ) + (-a); }";
		String scope = "(a)";
		Program p = new Program(text, scope);
		p.add(new Behavior(1, EMPTY, Integer.MIN_VALUE));
		p.add(new Behavior(-1, EMPTY, -2147483646));
		p.add(new Behavior(2, EMPTY, Integer.MAX_VALUE));
		runWrapped(p);
	}

	// ===== HELPERS and official interface
	// Fell free to use them :)

	public static final void runBinopTest(String operator, String inType, String outType,
			int... data) {
		if (data.length % 3 != 0) {
			throw new IllegalArgumentException("You have to supply triples of"
					+ " data: left, right, result.\nExample:"
					+ " runBinopTest(\"+\", 1, 2, 3, 9, 9, 18)");
		}

		String text = outType + " foo(" + inType + " a, " + inType + " b) { return a " + operator
				+ " b; }";
		String scope = "(a, b)";
		Program p = new Program(text, scope);
		for (int i = 0; i * 3 < data.length; i++) {
			final int i3 = i * 3;
			p.add(new Behavior(new int[] { data[i3], data[i3 + 1] }, EMPTY, data[i3 + 2]));
		}
		runWrapped(p);
	}

	public static final void runWrapped(Program p) {
		runWrapped(p, CompilerTestUpdateTool.getAttribute(TIMEOUT_ATTRIBUTE, DEFAULT_TIMEOUT));
	}

	public static final void runWrapped(Program p, int timeout) {
		// Make sure the timer doesn't die or does something unexpected
		Timer t = new Timer(false);
		StatefulRunnable sr = new StatefulRunnable(p);
		t.schedule(new TimeoutTask(Thread.currentThread(), sr, timeout), timeout);
		sr.run();
	}

	public static final class TimeoutTask extends TimerTask {
		private final Thread threadToKill;
		private final StatefulRunnable r;
		private final String description;

		public TimeoutTask(Thread t, StatefulRunnable r, String desc) {
			this.threadToKill = t;
			this.r = r;
			this.description = desc;
		}

		public TimeoutTask(Thread t, StatefulRunnable r, int timeout) {
			this(t, r, "Still running after " + timeout + " ms, killed (aborted).");
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			if (r.isRunning()) {
				threadToKill.stop(new TimeoutException(description));
			}
		}
	}

	public static class TimeoutException extends RuntimeException {
		private static final long serialVersionUID = -1745473460350102343L;

		public TimeoutException() {
			super();
		}

		public TimeoutException(String message, Throwable cause) {
			super(message, cause);
		}

		public TimeoutException(String message) {
			super(message);
		}

		public TimeoutException(Throwable cause) {
			super(cause);
		}
	}

	public static final class StatefulRunnable implements Runnable {
		private final Runnable backing;
		private volatile boolean running = false;

		public StatefulRunnable(Runnable backing) {
			this.backing = backing;
		}

		@Override
		public void run() {
			synchronized (this) {
				running = true;
			}
			try {
				backing.run();
			} finally {
				synchronized (this) {
					running = false;
				}
			}
		}

		public boolean isRunning() {
			synchronized (this) {
				return running;
			}
		}
	}

	public static final class Behavior {
		public final int[] input, expectedOutput;
		public final int expectedResult;

		public Behavior(int[] input, int[] output, int result) {
			this.input = input;
			this.expectedOutput = output;
			this.expectedResult = result;
		}

		public Behavior(int input, int[] output, int result) {
			this.input = new int[] { input };
			this.expectedOutput = output;
			this.expectedResult = result;
		}

		public Behavior(int input, int result) {
			this.input = new int[] { input };
			this.expectedOutput = EMPTY;
			this.expectedResult = result;
		}

		public Behavior(int[] input, int result) {
			this.input = input;
			this.expectedOutput = EMPTY;
			this.expectedResult = result;
		}
	}

	private static final String USER_START = "__start",
			BEWIED_CALLER_PREFIX = "__bewied_call_wrapper.";

	/**
	 * Very much of these classes are copied from TestBase, Driver, and similar.
	 * Thank you for supplying us with such a great testing environment :D
	 * 
	 * Unluckily, I have to modify it in certain ways java doesn't allow, and
	 * therefore COPY instead of REUSE it. Sigh.
	 */
	public static final class Program extends Driver implements Runnable {
		private final String text, expectedScope;
		private final ArrayList<Behavior> behaviors = new ArrayList<MassTestingBewied.Behavior>();
		private final ParseEntity ent;

		private MipsAsm asm = null;
		private Tree programTree = null;
		private Reg resultReg;

		public Program(String text, ParseEntity ent, String expectedScope) {
			super(getConfiguration(), DEBUG_DRIVER);
			this.text = text;
			this.ent = ent;
			this.expectedScope = expectedScope;

			checkIntegrity();

			parse();
		}

		public void add(Behavior b) {
			behaviors.add(b);
		}

		public Program(String text, String expectedScope) {
			this(text, ParseEntity.PRG, expectedScope);
		}

		private final void checkIntegrity() {
			if (behaviors.size() == 0) {
				return;
			}
			final int length = behaviors.get(0).input.length;
			for (int i = 1; i < behaviors.size(); i++) {
				if (length != behaviors.get(i).input.length) {
					throw new IllegalArgumentException("The input of the first"
							+ " Behavior has length " + length + ", the " + i
							+ "th input has length " + behaviors.get(i).input.length);
				}
			}
		}

		@Override
		public final void run() {
			checkIntegrity();

			assertNoException();

			prepareAsm();

			if (DEBUG_DRIVER) {
				try {
					asm.append(System.out);
					System.out.println("Result will be expected in " + resultReg);
				} catch (IOException e) {
					// This shouldn't happen anyway
				}
			}

			for (int i = 0; i < behaviors.size(); i++) {
				appendCaller(i);
			}

			asm.prepare();

			for (int i = 0; i < behaviors.size(); i++) {
				try {
					runCaller(i);
				} catch (Throwable e) {
					throw new RuntimeException("During behavioral test #" + i
							+ " (starting with #0) an error occurred.\n" + "Input was "
							+ Arrays.toString(behaviors.get(i).input) + " ("
							+ behaviors.get(i).input.length + " arguments)", e);
				}
			}
		}

		private final void parse() {
			try {
				programTree = parse(ent, text);
			} catch (ParserException e) {
				throw new RuntimeException("Parse failed unexpectedly", e);
			}
		}

		public void assertNoException() {
			try {
				checkScopesNamesTypes();
			} catch (NamingException e) {
				throw new RuntimeException("Analyze names failed unexpectedly", e);
			} catch (TypingException e) {
				throw new RuntimeException("Compute type failed unexpectedly", e);
			}
		}

		public void assertNamingException() {
			try {
				checkScopesNamesTypes();
				fail("Expected NamingException to be thrown. you threw nothing.");
			} catch (NamingException e) {
				// Expected
			} catch (TypingException e) {
				throw new RuntimeException("Compute type failed unexpectedly", e);
			}
		}

		public void assertTypingException() {
			try {
				checkScopesNamesTypes();
				fail("Expected TypingException to be thrown. you threw nothing.");
			} catch (NamingException e) {
				throw new RuntimeException("Analyze names failed unexpectedly", e);
			} catch (TypingException e) {
				// expected
			}
		}

		private final void checkScopesNamesTypes() throws NamingException, TypingException {
			// This is copied from TestBase.java.
			// Thank you for supplying this code :D

			// But why, oh why couldn't you just write another constructor?
			// One that allows me to spoof the Configuration object?

			Scope actualScope = new Scope();
			programTree.analyzeNames(actualScope);

			if (expectedScope != null) {
				StringBuilder sb = new StringBuilder();
				try {
					actualScope.print(sb);
				} catch (IOException e) {
					fail("IO exception. This should never happen");
					// Correct, and thanks for thinking of that :)
				}
				assertEquals("Scope differed ", expectedScope, sb.toString());
			}

			programTree.computeType();
		}

		private final void prepareAsm() {
			asm = new MipsAsm();
			try {
				asm.getText().label(USER_START);
			} catch (LabelAlreadyDefinedException e) {
				throw new IllegalStateException("The label __start may not be defined otherwise");
			}
			resultReg = generate(programTree, asm);
		}

		private final void appendCaller(final int bNr) {
			final Behavior b = behaviors.get(bNr);
			final String labelName = BEWIED_CALLER_PREFIX + bNr;
			try {
				asm.getText().label(labelName);
			} catch (LabelAlreadyDefinedException e) {
				throw new IllegalStateException("startup code label " + labelName
						+ " already defined");
			}

			// Treat the arguments correctly
			// First four go into a0...a3 the others on the stack
			for (int i = 0; i < Math.min(b.input.length, 4); i++) {
				Reg r = Reg.values()[Reg.a0.ordinal() + i];
				asm.li(r, b.input[i]);
			}
			if (b.input.length > 4) {
				asm.subiu(Reg.sp, Reg.sp, 4 * (b.input.length - 4));
			}
			for (int i = b.input.length - 1, j = 0; i >= 4; i--, j++) {
				asm.li(Reg.t0, b.input[i]);
				asm.sw(Reg.t0, Reg.sp, j * 4);
			}

			// jump to the generated code
			LabelRef target = asm.createRef(USER_START);
			asm.jal(target);

			// break into the driver
			asm.brk();
		}

		private final void runCaller(final int bNr) {
			final Behavior b = behaviors.get(bNr);
			final String labelName = BEWIED_CALLER_PREFIX + bNr;
			final OutputExpector expector = new OutputExpector(b.expectedOutput);
			final Sys sys = new Sys(1024, EXEPTION_HANDLER, expector);
			final Processor proc = sys.getProcessor();

			sys.load(asm, labelName);

			proc.state = ExecutionState.RUNNING;
			proc.setIgnoreBreaks(false);
			proc.run(true);

			expector.assertComplete();
			assertEquals("Results differed ", b.expectedResult, proc.gp[resultReg.ordinal()]);
		}
	}

	/**
	 * Copied exactly from Driver.java.
	 * 
	 * I could have saved a lot of copy pasta if you just wouldn't make those
	 * fields private.
	 */
	public static final ExceptionHandler EXEPTION_HANDLER = new ExceptionHandler() {
		private void handle(String msg, ProcessorState state, Memory mem, int addr) {
			try {
				System.err.format("processor exception: %s at %08x\n", msg, addr);
				System.err.println("memory dump:");
				mem.dump(System.out, addr - 16, 32, MemDumpFormatter.DATA);
				System.err.println("disassembly:");
				mem.dump(System.out, addr - 16, 32, MemDumpFormatter.DISASM);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void unalignedMemory(ProcessorState state, Memory mem, int addr) {
			handle("unaligned memory", state, mem, addr);
		}

		@Override
		public void overflow(ProcessorState state, Memory mem, int addr) {
			handle("integer overflow", state, mem, addr);
		}

		@Override
		public void illegalInstruction(ProcessorState state, Memory mem, int addr) {
			handle("unaligned memory", state, mem, addr);
		}

		@Override
		public void breakpoint(ProcessorState state, Memory mem) {
			state.state = ExecutionState.HALTED;
		}
	};

	public static final class OutputExpector implements SysCallHandler {
		private final int[] expectedOutput;
		private int passed = 0;

		public OutputExpector(int[] expectedOutput) {
			this.expectedOutput = expectedOutput;
		}

		@Override
		public void syscall(final ProcessorState state, final Memory mem) {
			final int insn = mem.load(state.pc, de.unisb.prog.mips.simulator.Type.WORD);
			final int rs = Instruction.FIELD_RS.extract(insn);
			final int rt = Instruction.FIELD_RT.extract(insn);
			final int rd = Instruction.FIELD_RD.extract(insn);
			if (rt != 0 || rd != 0) {
				fail("Invalid syscall -- how did you do that? o.O");
			}
			int actual = state.gp[rs];
			if (passed >= expectedOutput.length) {
				fail("Tried to print " + actual + ".\nExpected " + expectedOutput.length
						+ " outputs, but" + " not more (failed on " + (expectedOutput.length + 1)
						+ "th syscall, every call before" + " this one was correct).");
			}
			int expected = expectedOutput[passed];
			if (expected != actual) {
				fail("Expected " + (passed + 1) + "th (starting with" + " 1) syscall to print "
						+ expected + ", not " + actual + ".\nEvery syscall before"
						+ " this one was correct, but aborting now.");
			}
			// It took me one whole day to notice that the following line had
			// been missing:
			passed += 1;
		}

		public final void assertComplete() {
			if (passed != expectedOutput.length) {
				fail("Execution stopped prematurely -- " + passed
						+ " syscalls have been issued, all of them correct," + " but "
						+ expectedOutput.length + " were expected.");
			}
		}
	}

	// ===== Configuration, and optimization =====
	// You don't need to understand HOW this works.
	// It basically does NOTHING SPECIAL, unless you edited updateTool.cfg,
	// and inserted the line "optimize=true" -- in that case, the following code
	// detects this line, and ensures that only your "optimized" code is run.
	// This way you can easily test your optimization for correctness.

	private static Configuration config = null;

	public static final Configuration getConfiguration() {
		if (config == null) {
			config = buildConfiguration(CompilerTestUpdateTool.getAttribute(OPTIMIZE_ATTRIBUTE,
					false));
		}

		return config;
	}

	public static final Configuration buildConfiguration(boolean alwaysOptimize) {
		if (alwaysOptimize) {
			return new AlwaysOptimizingConfiguration(Configuration.INSTANCE);
		} else {
			return Configuration.INSTANCE;
		}
	}

	private static final class AlwaysOptimizingConfiguration implements Configuration {
		private final Configuration backing;

		public AlwaysOptimizingConfiguration(Configuration backing) {
			this.backing = backing;
		}

		@Override
		public TreeFactory getTreeFactory() {
			return backing.getTreeFactory();
		}

		@Override
		public MipsCodeGenerator getCodeGenerator(MipsAsm asm, boolean optimize) {
			return backing.getCodeGenerator(asm, true);
		}
	}
}
