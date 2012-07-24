package prog2.project4.tests.prog2tests;

import org.junit.Test;

import prog2.project4.tests.prog2tests.MassTestingBewied.Program;

public class TreeBewiedTest {
	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("TreeBewiedTest", "1.0.1");
	}
	
	// ===== TESTS
	// Thanks to the static import, all the utilities are hidden in
	// MassTestingBewied.
	// Therefore, this file only consists of a big list of tests :)

	@Test
	public void testBasicNamingBad() {
		// "Bad" == "Should throw an exception"
		assertNamingException("int foo(int a) { return b; }");
		assertNamingException("int foo(int a) { { int b = 42; } return b; }");
		assertNamingException("int foo(int a) { { int b = b; int c = 42; } return a; }");
		assertNamingException("int foo(int a) { int a = 42; return a; }");
	}

	@Test
	public void testExoticGood() {
		// "Good" == "Shouldn't throw an exception ever"
		assertNoException("int foo(int a) { return a; }", "(a)");
		assertNoException("int foo(int a) { { int b = 42; } return a; }", "(a (b))");
		assertNoException("int foo(int a) { int c = -1; { int b = a; c = b + 3; } return c; }", "(a, c (b))");
		assertNoException("int foo(int a) { a = 42; return a; }", "(a)");
		assertNoException("int foo(int a) { { int a = 42; } return a; }", "(a (a))");
	}

	@Test
	public void testBasicTypingBad() {
		// "Bad" == "Should throw an exception"
		assertTypingException("int foo(int a) { return true; }", "(a)");
		assertTypingException("int foo(int a) { return false; }", "(a)");
		assertTypingException("boolean foo(int a) { return 42; }", "(a)");
		assertTypingException("boolean foo(int a) { return -7; }", "(a)");
		assertTypingException("boolean foo(int a) { return !a; }", "(a)");
		assertTypingException("int foo(int a) { return a > 4; }", "(a)");
		assertTypingException("boolean foo(boolean a) { return a > 4; }", "(a)");
		assertTypingException("boolean foo(int a) {"
				+ " return (a ? false : true) ; }", "(a)");
		assertTypingException("boolean foo(boolean a) {"
				+ " return (a ? 23 : true) ; }", "(a)");
		assertTypingException("int foo(int a) { return a << true ; }", "(a)");
		assertTypingException("int foo(int a) { return a + true ; }", "(a)");
		assertTypingException("int foo(int a) { return a | true ; }", "(a)");
		assertTypingException("int foo(int a) { return !a ; }", "(a)");
		assertTypingException("boolean foo(int a) { return -a ; }", "(a)");
		assertTypingException("boolean foo(int a) { return +a ; }", "(a)");
	}

	@Test
	public void testExoticTypingBad() {
		// "Bad" == "Should throw an exception"
		assertTypingException("int foo(int a) {"
				+ " while (a & 3) { } return 42; }", "(a ())");
		assertTypingException("int foo(int a) {"
				+ " do { } while (a & 3); return 42; }", "(a ())");
		assertTypingException("int foo(int a) {"
				+ " if (a & 3) {} return 42; }", "(a ())");
		assertTypingException("int foo(int a) {"
				+ " return !a; }", "(a)");
		assertTypingException("boolean foo(int a) {"
				+ " return !a; }", "(a)");
		assertTypingException("int foo(int a) {"
				+ " { boolean a = a > 42; boolean b = a < 23; }"
				+ " return 42; }", "(a (a, b))");
	}

	// ===== HELPERS
	// I lied to you. Sorry.
	// Here, have a wrapper:

	/**
	 * Asserts that a NamingException will be risen during analyzeNames().
	 * 
	 * @param text
	 *            The "source code", like "int foo() { return 42; }"
	 */
	public static final void assertNamingException(String text) {
		new Program(text, null).assertNamingException();
	}

	/**
	 * Asserts that a TypingException will be risen during computeTypes(), and
	 * that analyzeNames() works properly, and the given Scope will be reached.
	 * 
	 * @param text
	 *            The "source code", like "int foo() { return 42; }"
	 * @param scope
	 *            The expected Scope. <code>null</code> if undefined or
	 *            arbitrary.
	 */
	public static final void assertTypingException(String text, String scope) {
		new Program(text, scope).assertTypingException();
	}

	/**
	 * Asserts that no Exception will be risen during computeTypes() or
	 * analyzeNames(), and the given Scope will be reached.
	 * 
	 * @param text
	 *            The "source code", like "int foo() { return 42; }"
	 * @param scope
	 *            The expected Scope. <code>null</code> if undefined or
	 *            arbitrary.
	 */
	public static final void assertNoException(String text, String scope) {
		new Program(text, scope).assertNoException();
	}
}
