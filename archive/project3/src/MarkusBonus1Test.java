package prog2.project3.tests;

import org.junit.Before;
import org.junit.Test;

import prog2.project3.propositional.FormulaReader;

import junit.framework.TestCase;

public class MarkusBonus1Test extends TestCase {
	@Before
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testBonus1_1() {
		// Einfache Sachen
		assertEquals("Wrong result", "a",
				FormulaReader.readFormulaFromString("a")
						.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "a || b", FormulaReader
				.readFormulaFromString("a b ||").toStringWithMinimalBrackets());
		assertEquals("Wrong result", "a && b", FormulaReader
				.readFormulaFromString("a b &&").toStringWithMinimalBrackets());
		assertEquals("Wrong result", "a => b", FormulaReader
				.readFormulaFromString("a b =>").toStringWithMinimalBrackets());
		assertEquals("Wrong result", "a <=> b", FormulaReader
				.readFormulaFromString("a b <=>").toStringWithMinimalBrackets());
		assertEquals("Wrong result", "! a", FormulaReader
				.readFormulaFromString("a !").toStringWithMinimalBrackets());
		assertEquals("Wrong result", "! ! a", FormulaReader
				.readFormulaFromString("a !     !")
				.toStringWithMinimalBrackets());
	}

	@Test
	public void testBonus1_2() {
		// Einzelner Operator
		// !
		assertEquals("Wrong result", "! ! a", FormulaReader
				.readFormulaFromString("a ! !").toStringWithMinimalBrackets());
		assertEquals("Wrong result", "a || ! ! ! b", FormulaReader
				.readFormulaFromString("a b ! ! ! ||")
				.toStringWithMinimalBrackets());
		// &&
		assertEquals("Wrong result", "a && b && c && d", FormulaReader
				.readFormulaFromString("a b && c d && &&")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "a && b && c && d", FormulaReader
				.readFormulaFromString("a b && c && d &&")
				.toStringWithMinimalBrackets());
		// ||
		assertEquals("Wrong result", "a || b || c || d", FormulaReader
				.readFormulaFromString("a b || c d || ||")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "a || b || c || d", FormulaReader
				.readFormulaFromString("a b || c || d ||")
				.toStringWithMinimalBrackets());
		// =>
		assertEquals("Wrong result", "(a => b) => c", FormulaReader
				.readFormulaFromString("a b => c =>")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "a => (b => c)", FormulaReader
				.readFormulaFromString("a b c => =>")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "(a => b) => (c => d)", FormulaReader
				.readFormulaFromString("a b => c d => =>")
				.toStringWithMinimalBrackets());
		// <=>
		assertEquals("Wrong result", "a <=> b <=> c <=> d", FormulaReader
				.readFormulaFromString("a b <=> c d <=> <=>")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "a <=> b <=> c <=> d", FormulaReader
				.readFormulaFromString("a b <=> c <=> d <=>")
				.toStringWithMinimalBrackets());

		// Operatorkombinationen
		// ! &&
		assertEquals("Wrong result", "! a && ! b", FormulaReader
				.readFormulaFromString("a ! b ! &&")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "! ! (a && b)", FormulaReader
				.readFormulaFromString("a b && ! !")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "! (a && ! b) && c", FormulaReader
				.readFormulaFromString("a b ! && ! c &&")
				.toStringWithMinimalBrackets());
		// ! ||
		assertEquals("Wrong result", "! a || ! b", FormulaReader
				.readFormulaFromString("a ! b ! ||")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "! ! (a || b)", FormulaReader
				.readFormulaFromString("a b || ! !")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "! (a || ! b) || c", FormulaReader
				.readFormulaFromString("a b ! || ! c ||")
				.toStringWithMinimalBrackets());
		// ! =>
		assertEquals("Wrong result", "! a => ! b", FormulaReader
				.readFormulaFromString("a ! b ! =>")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "! (a => b)", FormulaReader
				.readFormulaFromString("a b => !")
				.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"! ((! a => ! b) => ! c) => (d => ! e)",
				FormulaReader.readFormulaFromString(
						"a ! b ! => c ! => ! d e ! => =>")
						.toStringWithMinimalBrackets());
		// ! <=>
		assertEquals("Wrong result", "! a <=> ! b", FormulaReader
				.readFormulaFromString("a ! b ! <=>")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "! ! (a <=> b)", FormulaReader
				.readFormulaFromString("a b <=> ! !")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "! (a <=> ! b) <=> c", FormulaReader
				.readFormulaFromString("a b ! <=> ! c <=>")
				.toStringWithMinimalBrackets());
		// && ||
		assertEquals(
				"Wrong result",
				"a && b && c || a && b || d",
				FormulaReader.readFormulaFromString(
						"a b && c && a b && || d ||")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"(a || b || c) && (a || b) && d",
				FormulaReader.readFormulaFromString(
						"a b || c || a b || && d &&")
						.toStringWithMinimalBrackets());
		// && =>
		assertEquals("Wrong result", "((a => b) => c) && (a => b)",
				FormulaReader.readFormulaFromString("a b => c => a b => &&")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"(a && b && c => d && e) => f",
				FormulaReader.readFormulaFromString(
						"a b && c && d e && => f =>")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"a && b => (b && c => c && d)",
				FormulaReader.readFormulaFromString(
						"a b && b c && c d && => =>")
						.toStringWithMinimalBrackets());
		// && <=>
		assertEquals(
				"Wrong result",
				"a && b && c <=> a && b <=> d",
				FormulaReader.readFormulaFromString(
						"a b && c && a b && <=> d <=>")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"(a <=> b <=> c) && (a <=> b) && d",
				FormulaReader.readFormulaFromString(
						"a b <=> c <=> a b <=> && d &&")
						.toStringWithMinimalBrackets());
		// || =>
		assertEquals("Wrong result", "((a => b) => c) || (a => b)",
				FormulaReader.readFormulaFromString("a b => c => a b => ||")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"(a || b || c => d || e) => f",
				FormulaReader.readFormulaFromString(
						"a b || c || d e || => f =>")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"a || b => (b || c => c || d)",
				FormulaReader.readFormulaFromString(
						"a b || b c || c d || => =>")
						.toStringWithMinimalBrackets());
		// || <=>
		assertEquals(
				"Wrong result",
				"a || b || c <=> a || b <=> d",
				FormulaReader.readFormulaFromString(
						"a b || c || a b || <=> d <=>")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"(a <=> b <=> c) || (a <=> b) || d",
				FormulaReader.readFormulaFromString(
						"a b <=> c <=> a b <=> || d ||")
						.toStringWithMinimalBrackets());
		// => <=>
		assertEquals("Wrong result", "(a => b) => c <=> a => b", FormulaReader
				.readFormulaFromString("a b => c => a b => <=>")
				.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"((a <=> b <=> c) => (d <=> e)) => f",
				FormulaReader.readFormulaFromString(
						"a b <=> c <=> d e <=> => f =>")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"(a <=> b) => ((b <=> c) => (c <=> d))",
				FormulaReader.readFormulaFromString(
						"a b <=> b c <=> c d <=> => =>")
						.toStringWithMinimalBrackets());
	}

	@Test
	public void testBonus1_3() {
		// Verschiedenes
		assertEquals("Wrong result", "! a || b", FormulaReader
				.readFormulaFromString("a ! b ||")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "A && B || C", FormulaReader
				.readFormulaFromString("A B && C ||")
				.toStringWithMinimalBrackets());
		assertEquals("Wrong result", "(A || B) && C", FormulaReader
				.readFormulaFromString("A B || C &&")
				.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"((a || b || c || d) && e || f) && g",
				FormulaReader.readFormulaFromString(
						"a b || c || d || e && f || g &&")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"(a => b <=> a || b) && (A => B <=> A || B)",
				FormulaReader.readFormulaFromString(
						"a b => a b || <=> A B => A B || <=> &&")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"! a && ! b || c && d => d || e <=> a || b => c",
				FormulaReader.readFormulaFromString(
						"a ! b ! && c d && || d e || => a b || c => <=>")
						.toStringWithMinimalBrackets());
		assertEquals(
				"Wrong result",
				"! ((c || d) && (d || e) && ((a <=> b) || (a => b)))",
				FormulaReader.readFormulaFromString(
						"c d || d e || a b <=> a b => || && && !")
						.toStringWithMinimalBrackets());
		// assertEquals("Wrong result","",FormulaReader.readFormulaFromString("").toStringWithMinimalBrackets());
	}


	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("MarkusBonus1Test", "1.0");
	}
}
