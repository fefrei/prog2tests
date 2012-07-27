package prog2.project4.tests.prog2tests;

import static prog2.project4.tests.prog2tests.MassTestingBewied.*;

import org.junit.Test;

import prog2.project4.driver.ParseEntity;
import prog2.project4.tests.prog2tests.MassTestingBewied.Behavior;
import prog2.project4.tests.prog2tests.MassTestingBewied.Program;

public class AdvancedBewiedTests {
	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("AdvancedBewiedTests", "1.0");
	}

	// ===== TESTS

	@Test
	public void testManyArguments() {
		String text = "int testExample7(int a, int b, int c, int d, int e, int f) {"
				+ "int x = a + 100000*f; int y = 10*b+10000*e; int z = 100*c+1000*d;"
				+ " return x + y + z;}";
		String scope = "(a, b, c, d, e, f, x, y, z)";

		Program p = new Program(text, scope);
		p.add(new Behavior(new int[] { 0, 0, 0, 0, 0, 0 }, 0));
		p.add(new Behavior(new int[] { 1, 0, 0, 0, 0, 0 }, 1));
		p.add(new Behavior(new int[] { 0, 1, 0, 0, 0, 0 }, 10));
		p.add(new Behavior(new int[] { 0, 0, 1, 0, 0, 0 }, 100));
		p.add(new Behavior(new int[] { 0, 0, 0, 1, 0, 0 }, 1000));
		p.add(new Behavior(new int[] { 0, 0, 0, 0, 1, 0 }, 10000));
		p.add(new Behavior(new int[] { 0, 0, 0, 0, 0, 1 }, 100000));
		// This one is "testExample7()":
		p.add(new Behavior(new int[] { 6, 5, 4, 3, 2, 1 }, 123456));
		p.add(new Behavior(new int[] { 1, 2, 3, 4, 5, 6 }, 654321));
		/**
		 * <pre>
		 * ____11
		 * ___11
		 * __12
		 * _13
		 * 14
		 * 5
		 * ______
		 * 654321
		 * </pre>
		 */
		p.add(new Behavior(new int[] { 11, 11, 12, 13, 14, 5 }, 654321));
		/**
		 * <pre>
		 * ___123
		 * __123
		 * _123
		 * 123
		 * 23
		 * 3
		 * ______
		 * 666653
		 * </pre>
		 */
		p.add(new Behavior(new int[] { 123, 123, 123, 123, 23, 3 }, 666653));

		runWrapped(p);
	}

	@Test
	public void testManyOperands1() {
		String text = makeDeepRight(1, "+", 300);
		int expected = computeDeepAdd(1, 300);

		Program p = new Program(text, ParseEntity.EXPR, "()");
		p.add(new Behavior(EMPTY, EMPTY, expected));
		runWrapped(p);
	}

	@Test
	public void testManyOperands2() {
		String text = makeDeepLeft(-300, "+", -1);
		int expected = computeDeepAdd(-300, -1);

		Program p = new Program(text, ParseEntity.EXPR, "()");
		p.add(new Behavior(EMPTY, EMPTY, expected));
		runDirectly(p);
	}

	@Test
	public void testManyOperands3() {
		String text = makeDeepRight(1, "*", 12);
		text = "(1 * (1 * (" + text + ")))";
		int expected = 479001600;

		Program p = new Program(text, ParseEntity.EXPR, "()");
		p.add(new Behavior(EMPTY, EMPTY, expected));
		runWrapped(p);
	}

	// ===== UTILITIES

	public static final String makeDeepRight(int from, String operator, int to) {
		if (to < from) {
			throw new IllegalArgumentException();
		}
		StringBuilder sb = new StringBuilder();
		for (int i = from; i < to; i++) {
			sb.append('(');
			sb.append(String.valueOf(i));
			sb.append(' ');
			sb.append(operator);
			sb.append(' ');
		}
		sb.append(to);
		for (int i = from; i < to; i++) {
			sb.append(')');
		}
		return sb.toString();
	}

	public static final String makeDeepLeft(int from, String operator, int to) {
		if (to < from) {
			throw new IllegalArgumentException();
		}
		StringBuilder sb = new StringBuilder();
		for (int i = from + 1; i <= to; i++) {
			sb.append('(');
		}
		sb.append(from);
		for (int i = from + 1; i <= to; i++) {
			sb.append(' ');
			sb.append(operator);
			sb.append(' ');
			sb.append(String.valueOf(i));
			sb.append(')');
		}
		return sb.toString();
	}

	/**
	 * DO NOT USE
	 * 
	 * There's something broken. Above tests work, but if you try something like
	 * computeDeepAdd(-3, 3), something goes wrong.
	 */
	public static final int computeDeepAdd(int from, int to) {
		if (to < from) {
			throw new IllegalArgumentException();
		}
		boolean reverseSign = false;
		if (to <= 0) {
			reverseSign ^= true;
			int tmp = to;
			to = -from;
			from = -tmp;
		}
		if (from < 0) {
			if (-from <= to) {
				from = -from;
			} else {
				int tmp = to;
				to = -from;
				from = -tmp;
			}
		}
		if (to == from) {
			return 0;
		}
		int diff = to - from;
		int result = (diff + 1) * from + ((diff + 1) * diff) / 2;
		return reverseSign ? -result : result;
	}
}
