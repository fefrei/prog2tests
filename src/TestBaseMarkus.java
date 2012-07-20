package prog2.project4.tests.prog2tests;

import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

import de.unisb.prog.mips.assembler.LabelAlreadyDefinedException;
import de.unisb.prog.mips.assembler.Reg;
import de.unisb.prog.mips.simulator.ProcessorState;
import de.unisb.prog.mips.simulator.Sys;
import prog2.project4.codegen.mips.MipsAsm;
import prog2.project4.driver.Pair;
import prog2.project4.driver.ParseEntity;
import prog2.project4.driver.Phase;
import prog2.project4.parser.ParserException;
import prog2.project4.tests.TestBase;
import prog2.project4.tree.NamingException;
import prog2.project4.tree.Scope;
import prog2.project4.tree.Tree;
import prog2.project4.tree.Type;
import prog2.project4.tree.TypingException;

//If you don't want that console output, go to TestBase.java, line 39
// and replace true with false. 

/**
 * 
 * @author Markus
 * 
 * 
 * 
 * 
 *         === FUNCTIONS ===
 * 
 *         static String CTS(/ * Text * /) Converts a comment into a string.
 *         Very good to put longer code parts in: getResult(CTS(/ * int
 *         f(boolean b){ return b ? 0 : 1; } * /), null);
 * 
 * 
 * 
 *         assertPrint(); asserts print statement to be working
 *         assertNamingException(prog, msg); assertNoNamingException(prog, msg);
 *         assertTypingException(prog, msg); assertNoTypingException(prog, msg);
 *         assertOutput(num, [msg], expected, output); asserts output[num]
 *         assertArrayEquals(msg, int[], int[]) like the original, but prints
 *         both arrays completely in error message
 * 
 * 
 * 
 * 
 *         getResult(String/Tree prog, int[] input); Executes prog with
 *         arguments input and returns the result getOutput(String/Tree prog,
 *         int[] input); Executes prog with arguments input and returns the
 *         print output Pair<Integer, int[]> getResultAndOutput(Phase phase,
 *         Tree prg, int[] input) Returns result and print output of a tree
 * 
 * 
 * 
 *         geterrhead([int n]) returns an default header for console output. n
 *         is the number of subfunctions you called. If you call geterrhead from
 *         a testroutine, use 1 (=default), use 2 in a subfunction, use 3 in a
 *         sub-sub-function, ... printCode() prints the current code. printAsm()
 *         prints the current asm output formatCode(String code) formats code
 *         (remove spaces at beginning of every line, but keep line indentation)
 * 
 * 
 */

public class TestBaseMarkus extends TestBase {
	public final String VERSION = "1.0.0";

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("TestBaseMarkus", VERSION);
	}

	/** Checks if the version file exists on the server. If not, return false. **/
	public boolean updatePossible(String test) {
		String url = "https://prog2tests.googlecode.com/svn/update/" + test + ".version.txt";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new URL(url).openStream()));
			reader.close();
		} catch (IOException e) {
			if (e instanceof FileNotFoundException)
				return false;
			throw new RuntimeException("Error while checking " + test);
		}
		return true;
	}

	// For printing
	protected int subtest = 0;
	protected String code = null;
	protected MipsAsm asm = null;

	/**
	 * Gives back an error header. If you use this from a subfunction like an
	 * assert, increase n.
	 **/
	protected String geterrhead() {
		return geterrhead(2);
	}

	/**
	 * Gives back an error header. If you use this from a subfunction like an
	 * assert, increase n.
	 **/
	protected String geterrhead(int n) {
		StackTraceElement element = new RuntimeException().getStackTrace()[n];
		return "\n-------------------------------------------------------------------------\n\n"
				+ "Error in " + element.getMethodName() + " (Subtest " + subtest + ")";
	}

	/** prints the actual code **/
	protected void printCode() {
		System.out.println("-- PROGRAMMCODE --\n" + formatCode(code));
	}

	/** formats given code (adjusts spaces at beginning) **/
	protected String formatCode(String code) {
		String[] lines = code.split("\n");
		if (lines.length == 1)
			return "\"" + code.trim() + "\"";
		int crop = -1;
		int i = 0;
		while (i < lines.length) {
			if (crop == -1) {
				if (lines[i].matches("\\s+")) {
					lines[i] = "";
				} else if (!lines[i].isEmpty()) {
					char[] search = lines[i].toCharArray();
					int j = 0;
					do {
						if (java.lang.Character.isWhitespace(search[j]))
							j++;
						else {
							crop = j;
							lines[i] = lines[i].substring(crop);
							break;
						}
					} while (j <= search.length);
				}
			} else {
				if (lines[i].length() > crop) {
					if (lines[i].substring(0, crop).matches("\\s*")) {
						lines[i] = lines[i].substring(crop);
					} else {
						crop = -1;
						i--;
					}
				}
			}
			i++;
		}
		StringBuilder sb = new StringBuilder();
		boolean start = false;
		for (String s : lines) {
			if (!s.isEmpty())
				start = true;
			if (start)
				sb.append(s.replaceAll("\\t", "    ") + "\n");
		}
		return sb.toString();
	}

	protected void printAsm() {
		if (asm == null)
			return;
		try {
			asm.append(System.out);
		} catch (IOException e) {
			System.out.println("Asm couldn't be printed cause of " + e.getMessage());
		}
	}

	/**
	 * Takes a comment (/ * * /) and turns everything inside the comment to a
	 * string that is returned from CTS()
	 **/
	public static String CTS() {
		StackTraceElement element = new RuntimeException().getStackTrace()[1];
		String name = System.getProperty("user.dir") + "/"
				+ element.getClassName().replace('.', '/') + ".java";
		try {
			InputStream in = new FileInputStream(new File(name));
			String s = convertStreamToString(in, element.getLineNumber());
			return s.substring(s.indexOf("/*") + 2, s.indexOf("*/"));
		} catch (FileNotFoundException e) {
			throw new UnsupportedOperationException(".java not found.");
		}
	}

	// From http://www.kodejava.org/examples/266.html
	private static String convertStreamToString(InputStream is, int lineNum) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		int i = 1;
		try {
			while ((line = reader.readLine()) != null) {
				if (i++ >= lineNum)
					sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	// ASSERTS
	/** Fails with message <code>msg</code> if a naming exception is thrown **/
	protected void assertNoNamingException(String prog, String msg) {
		Tree prg;
		try {
			prg = parse(ParseEntity.PRG, prog);
		} catch (ParserException e1) {
			System.err.println(e1.getMessage());
			return;
		}
		try {
			typeCheck(prg);
		} catch (NamingException e) {
			System.err.println(geterrhead(2));
			System.out.println(msg);
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			System.out.println(formatCode(prog));
			fail(msg);
			return;
		} catch (TypingException e) {
		}
	}

	/** Fails with message <code>msg</code> if no naming exception is thrown **/
	protected void assertNamingException(String prog, String msg) {
		Tree prg;
		try {
			prg = parse(ParseEntity.PRG, prog);
		} catch (ParserException e1) {
			System.err.println(e1.getMessage());
			return;
		}
		try {
			typeCheck(prg);
		} catch (NamingException e) {
			return;
		} catch (TypingException e) {
		}
		System.err.println(geterrhead(2));
		System.out.println(msg);
		System.out.println(formatCode(prog));
		fail(msg);
	}

	/** Fails with message <code>msg</code> if a typing exception is thrown **/
	protected void assertNoTypingException(String prog, String msg) {
		Tree prg;
		try {
			prg = parse(ParseEntity.PRG, prog);
		} catch (ParserException e1) {
			System.err.println(e1.getMessage());
			return;
		}
		try {
			typeCheck(prg);
		} catch (NamingException e) {
		} catch (TypingException e) {
			System.err.println(geterrhead(2));
			System.out.println(msg);
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			System.out.println(formatCode(prog));
			fail(msg);
			return;
		}
	}

	/** Fails with message <code>msg</code> if no typing exception is thrown **/
	protected void assertTypingException(String prog, String msg) {
		Tree prg;
		try {
			prg = parse(ParseEntity.PRG, prog);
		} catch (ParserException e1) {
			System.err.println(e1.getMessage());
			return;
		}
		try {
			typeCheck(prg);
		} catch (NamingException e) {
		} catch (TypingException e) {
			return;
		}
		System.err.println(geterrhead(2));
		System.out.println(msg);
		System.out.println(formatCode(prog));
		fail(msg);
	}

	/** Fails with message <code>msg</code> if a typing exception is thrown **/
	protected void assertNoTypingExceptionExpr(String expr, String msg) {
		Tree prg;
		try {
			prg = parse(ParseEntity.EXPR, expr);
		} catch (ParserException e1) {
			System.err.println(e1.getMessage());
			return;
		}
		try {
			typeCheck(prg);
		} catch (NamingException e) {
			return;
		} catch (TypingException e) {
			System.err.println(geterrhead(2));
			System.out.println(msg);
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			System.out.println("Code: " + formatCode(expr));
			fail(msg);
			return;
		}
	}

	/** Fails with message <code>msg</code> if no typing exception is thrown **/
	protected void assertTypingExceptionExpr(String expr, String msg) {
		Tree prg;
		try {
			prg = parse(ParseEntity.EXPR, expr);
		} catch (ParserException e1) {
			System.err.println(e1.getMessage());
			return;
		}
		try {
			typeCheck(prg);
		} catch (NamingException e) {
		} catch (TypingException e) {
			return;
		}
		System.err.println(geterrhead(2));
		System.out.println(msg);
		System.out.println("Code: " + formatCode(expr));
		fail(msg);
	}

	/** Fails with message <code>msg</code> if a typing exception is thrown **/
	protected void assertExprType(Type type, String expr, String msg) {
		Tree prg;
		Type t = null;
		try {
			prg = parse(ParseEntity.EXPR, expr);
		} catch (ParserException e1) {
			System.err.println(e1.getMessage());
			return;
		}
		try {
			t = typeCheck(prg);
			assertEquals(type, t);
		} catch (NamingException e) {
			return;
		} catch (TypingException e) {
			System.err.println(geterrhead(2));
			// System.out.println(msg);
			System.out.println("You threw a not expected Typing Exception: \n" + e.getMessage());
			System.out.println("Code: " + formatCode(expr));
			fail(msg);
			return;
		} catch (AssertionError e) {
			System.err.println(geterrhead(2));
			System.out.println(msg);
			System.out.println("Exception: " + e.getMessage());
			System.out.println("Code: " + formatCode(expr));
			fail(msg);
			return;
		}
	}

	protected void assertArrayEquals(String msg, int[] expected, int[] result) {
		try {
			org.junit.Assert.assertArrayEquals(expected, result);
		} catch (AssertionError e) {
			throw new AssertionError(msg + "\nexpected: " + Arrays.toString(expected)
					+ "\nbut was:  " + Arrays.toString(result));
		}
	}

	// i'm caching assertprint for performance reasons
	String printmsg = null; // print already checked?

	// null = no
	// "" = yes, it's okay
	// <msg> = failed with message <msg>

	/** Asserts print statement to be working correctly **/
	protected void assertPrint() {
		if (printmsg != null) {
			if (printmsg.isEmpty())
				return;
			fail(printmsg);
			return;
		}
		Tree t;
		try {
			t = parse(ParseEntity.PRG, CTS(/*
											 * int printtester(){ int i = 4;
											 * boolean b = false; print(i);
											 * print(b); return 0; }
											 */));
		} catch (ParserException e2) {
			printmsg = "This test expects print to work correctly. print throws parser exceptions";
			fail(printmsg);
			return;
		}

		Scope scope = new Scope();
		try {
			t.analyzeNames(scope);
		} catch (NamingException e) {
			printmsg = "This test expects print to work correctly. print throws naming exceptions";
			fail(printmsg);
			return;
		}
		try {
			t.computeType();
		} catch (TypingException e1) {
			printmsg = "This test expects print to work correctly. print throws typing exceptions";
			fail(printmsg);
			return;
		}

		MipsAsm asm = new MipsAsm();
		try {
			asm.getText().label("__start");
		} catch (LabelAlreadyDefinedException e) {
			throw new IllegalStateException("The label __start may not be defined otherwise");
		}
		generate(t, asm);
		addCallerCode(asm, new int[0], "__start");
		Pair<Sys, int[]> res = execute2(asm);
		int[] output = res.snd();
		if (output.length < 2) {
			printmsg = "This test expects print to work correctly. Make sure print works for both integer and boolean. ";
			fail(printmsg);
		} else if (output[0] != 4 || output[1] != 0) {
			printmsg = "This test expects print to work correctly. Make sure print returns the right results";
			fail(printmsg);
		} else
			printmsg = "";
	}

	/**
	 * asserts that print output nr. <code>num</code> exists and is equal to
	 * <code>expected</code>
	 **/
	protected static void assertOutput(int num, int expected, int[] output) {
		assertOutput(num, "Wrong output nr. " + num + ". output=" + Arrays.toString(output),
				expected, output);
	}

	/**
	 * asserts that print output nr. <code>num</code> exists and is equal to
	 * <code>expected</code>, otherwise fail with <code>msg</code>
	 **/
	protected static void assertOutput(int num, String msg, int expected, int[] output) {
		if (output.length <= num)
			throw new AssertionError("Too few outputs. At least " + num + " expected, you have "
					+ output.length);
		assertEquals(msg, expected, output[num]);
	}

	// EVALUTATORS
	/**
	 * Executes prg and returns his result.
	 * 
	 * @param input
	 *            the parameters for the given function
	 **/
	protected int getResult(String prg, int[] input) {
		return getResultAndOutput(Phase.END, parseprg(prg), input).fst();
	}

	/**
	 * Executes prg and returns his print output.
	 * 
	 * @param input
	 *            the parameters for the given function
	 **/
	protected int[] getOutput(String prg, int[] input) {
		return getResultAndOutput(Phase.END, parseprg(prg), input).snd();
	}

	/**
	 * Executes expr and returns his result.
	 * 
	 * @param input
	 *            the parameters for the given function
	 **/
	protected int getResultExpr(String expr, int[] input) {
		return getResultAndOutput(Phase.END, parseprg(ParseEntity.EXPR, expr), input).fst();
	}

	/**
	 * Executes expr and returns his print output.
	 * 
	 * @param input
	 *            the parameters for the given function
	 **/
	protected int[] getOutputExpr(String expr, int[] input) {
		return getResultAndOutput(Phase.END, parseprg(ParseEntity.EXPR, expr), input).snd();
	}

	/**
	 * Executes prg and returns his result.
	 * 
	 * @param input
	 *            the parameters for the given function
	 **/
	protected int getResult(Tree prg, int[] input) {
		return getResultAndOutput(Phase.END, prg, input).fst();
	}

	/**
	 * Executes prg and returns his output.
	 * 
	 * @param input
	 *            the parameters for the given function
	 **/
	protected int[] getOutput(Tree prg, int[] input) {
		return getResultAndOutput(Phase.END, prg, input).snd();
	}

	protected Tree parseprg(String program) {
		return parseprg(ParseEntity.PRG, program);
	}

	protected Tree parseprg(ParseEntity ent, String program) {
		try {
			return parse(ent, program);
		} catch (ParserException e) {
			fail("parser exception thrown: " + e);
			return null;
		}
	}

	/**
	 * Executes and returns result and print-outputs of a given tree
	 * 
	 * @param phase
	 *            use Phase.END for complete run
	 * @param input
	 *            the arguments for the function
	 **/
	protected Pair<Integer, int[]> getResultAndOutput(Phase phase, Tree prg, int[] input) {
		if (input == null)
			input = new int[0];
		if (phase == Phase.BUILD_TREE)
			return null;

		Scope scope = new Scope();
		try {
			prg.analyzeNames(scope);
		} catch (NamingException e) {
			throw new AssertionError("naming exception thrown: " + e);
		}

		if (phase == Phase.ANALYZE_NAMES)
			return null;

		try {
			prg.computeType();
		} catch (TypingException e) {
			throw new AssertionError("typing exception thrown: " + e);
		}

		if (phase == Phase.COMPUTE_TYPES)
			return null;

		asm = new MipsAsm();
		try {
			asm.getText().label("__start");
		} catch (LabelAlreadyDefinedException e) {
			throw new IllegalStateException("The label __start may not be defined otherwise");
		}
		Reg reg = generate(prg, asm);

		if (phase == Phase.GEN_CODE)
			return null;

		addCallerCode(asm, input, "__start");
		Pair<Sys, int[]> res = execute2(asm);
		ProcessorState state = res.fst().getProcessor();
		int[] output = res.snd();
		int result = state.gp[reg.ordinal()];
		return new Pair<Integer, int[]>(result, output);
	}

	@Override
	protected void check(Phase phase, Tree prg, int[] input, int[] expOutput, Integer expResult,
			String scopeLayout) {
		if (phase == Phase.BUILD_TREE)
			return;

		Scope scope = new Scope();
		try {
			prg.analyzeNames(scope);
		} catch (NamingException e) {
			fail("naming exception thrown: " + e);
			return;
		}

		if (phase == Phase.ANALYZE_NAMES)
			return;

		try {
			prg.computeType();
		} catch (TypingException e) {
			fail("typing exception thrown: " + e);
			return;
		}

		if (scopeLayout != null) {
			StringBuilder sb = new StringBuilder();
			try {
				scope.print(sb);
			} catch (IOException e) {
				fail("IO exception. This should never happen");
			}
			assertEquals("scope mismatch ", scopeLayout, sb.toString());
		}

		if (phase == Phase.COMPUTE_TYPES)
			return;

		asm = new MipsAsm();
		try {
			asm.getText().label("__start");
		} catch (LabelAlreadyDefinedException e) {
			throw new IllegalStateException("The label __start may not be defined otherwise");
		}
		Reg reg = generate(prg, asm);

		if (phase == Phase.GEN_CODE)
			return;

		addCallerCode(asm, input, "__start");
		if (debug) {
			try {
				asm.append(System.out);
			} catch (IOException e) {
			}
		}
		Pair<Sys, int[]> res = execute2(asm);
		ProcessorState state = res.fst().getProcessor();
		int[] output = res.snd();

		if (expResult != null)
			assertEquals("results differ.", expResult.intValue(), state.gp[reg.ordinal()]);
		if (expOutput != null)
			assertArrayEquals("outputs differ.", expOutput, output);
	}
}
