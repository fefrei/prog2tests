package prog2.project3.tests;

//import static prog2.project3.cnf.CnfFactory.*;
//import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import prog2.project3.propositional.FormulaReader;
import prog2.project3.propositional.IncorrectFormulaException;
import prog2.project3.propositional.IncorrectFormulaException.Cause;

public class FormulaReaderImhaffTest {

	@Test
	public void testNullPath() {
		try {
			FormulaReader.readFormulaFromFile(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
			// We expected that
		} catch (Exception ex) {
			fail("NullPointerException expected");
		}
	}

	@Test
	public void testEmptyPath() {
		try {
			FormulaReader.readFormulaFromFile("");
			fail("FileNotFoundException expected");
		} catch (FileNotFoundException ex) {
			// We expected that
		} catch (Exception ex) {
			fail("FileNotFoundException expected");
		}
	}

	@Test
	public void testIncorrectPath() {
		try {
			FormulaReader.readFormulaFromFile("jasbd78283b");
			fail("FileNotFoundException expected");
		} catch (FileNotFoundException ex) {
			// We expected that
		} catch (Exception ex) {
			fail("FileNotFoundException expected");
		}
	}

	@Test
	public void testCorrectPath() {
		try {
			FormulaReader.readFormulaFromFile("testCorrectPath.txt");
			// We expected that
			// -- as an alternative.
		} catch (IncorrectFormulaException e) {
			// We expected that
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unexpected exception, make sure that"
					+ " file exists and FormulaReader works", e);
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException", e);
		}
	}

	@Test
	public void testCorrectPathCorrectFormula() {
		try {
			FormulaReader.readFormulaFromFile("testCorrectFormula0.txt");
			FormulaReader.readFormulaFromFile("testCorrectFormula1.txt");
			FormulaReader.readFormulaFromFile("testCorrectFormula2.txt");
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Exception", e);
		}
	}

	@Test
	public void testWhitespaceFormula() {
		try {
			FormulaReader.readFormulaFromString(" ");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.EMPTYFORMULA, ex.getReason());
		}
	}

	@Test
	public void testIncorrectID() {
		try {
			FormulaReader.readFormulaFromString("1a");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.INCORRECTIDENTIFIER, ex.getReason());
		}

		try {
			FormulaReader.readFormulaFromString("1423");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.INCORRECTIDENTIFIER, ex.getReason());
		}

		try {
			FormulaReader.readFormulaFromString("a123");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.INCORRECTIDENTIFIER, ex.getReason());
		}

		try {
			FormulaReader.readFormulaFromString("a1b");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.INCORRECTIDENTIFIER, ex.getReason());
		}
	}

	@Test
	public void testCorrectID() {
		try {
			FormulaReader.readFormulaFromString("abc");
		} catch (IncorrectFormulaException ex) {
			fail("IncorrectFormulaException withreason " + ex.getReason()
					+ " was thrown");
		}
	}

	@Test
	public void testMissingOperand() {
		try {
			FormulaReader.readFormulaFromString("a &&");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.MISSINGOPERAND, ex.getReason());
		}

		try {
			FormulaReader.readFormulaFromString("!");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.MISSINGOPERAND, ex.getReason());
		}

		try {
			FormulaReader.readFormulaFromString("a =>");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.MISSINGOPERAND, ex.getReason());
		}

		try {
			FormulaReader.readFormulaFromString("a b && c || &&");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.MISSINGOPERAND, ex.getReason());
		}
	}

	@Test
	public void testMissingOperator() {
		try {
			FormulaReader.readFormulaFromString("a b");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.MISSINGOPERATOR, ex.getReason());
		}

		try {
			FormulaReader.readFormulaFromString("a b !");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.MISSINGOPERATOR, ex.getReason());
		}

		try {
			FormulaReader.readFormulaFromString("a ! b");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.MISSINGOPERATOR, ex.getReason());
		}

		try {
			FormulaReader.readFormulaFromString("a b c &&");
			fail("No exception has been thrown");
		} catch (IncorrectFormulaException ex) {
			assertEquals(Cause.MISSINGOPERATOR, ex.getReason());
		}
	}

	@Test
	public void testReadFormulaString() {
		assertEquals("(a || b)", FormulaReader.readFormulaFromString("a b ||")
				.toString());
		assertEquals("(a <=> b)", FormulaReader
				.readFormulaFromString("a b <=>").toString());
		assertEquals("((a && b) || c)",
				FormulaReader.readFormulaFromString("a b && c ||").toString());
		assertEquals("((a => b) => c)",
				FormulaReader.readFormulaFromString("a b => c =>").toString());
		assertEquals("(a => (b => c))",
				FormulaReader.readFormulaFromString("a b c => =>").toString());
		assertEquals("(! ((! a) || b))",
				FormulaReader.readFormulaFromString("a ! b || !").toString());
	}

	@Test
	public void testToStringWithMinimalBrackets() {
		// test associative
		assertEquals("! ! a", FormulaReader.readFormulaFromString("a ! !")
				.toStringWithMinimalBrackets());
		assertEquals("a && b && c",
				FormulaReader.readFormulaFromString("a b && c &&")
						.toStringWithMinimalBrackets());
		assertEquals("a || b || c",
				FormulaReader.readFormulaFromString("a b || c ||")
						.toStringWithMinimalBrackets());
		// Umm... No?!
		// assertEquals("a => b => c",
		// FormulaReader.readFormulaFromString("a b => c =>")
		// .toStringWithMinimalBrackets());
		assertEquals("a <=> b <=> c",
				FormulaReader.readFormulaFromString("a b <=> c <=>")
						.toStringWithMinimalBrackets());

		// test NEGATION stronger than CONJUNCTION
		assertEquals("! a && b", FormulaReader
				.readFormulaFromString("a ! b &&")
				.toStringWithMinimalBrackets());
		assertEquals("! (a && b)",
				FormulaReader.readFormulaFromString("a b && !")
						.toStringWithMinimalBrackets());

		// test CONJUNCTION stronger than DISJUNCTION
		assertEquals("a && b || c",
				FormulaReader.readFormulaFromString("a b && c ||")
						.toStringWithMinimalBrackets());
		assertEquals("(a || b) && c",
				FormulaReader.readFormulaFromString("a b || c &&")
						.toStringWithMinimalBrackets());

		// test DISJUNCTION stronger than IMPLICATION
		assertEquals("a || b => c",
				FormulaReader.readFormulaFromString("a b || c =>")
						.toStringWithMinimalBrackets());
		assertEquals("(a => b) || c",
				FormulaReader.readFormulaFromString("a b => c ||")
						.toStringWithMinimalBrackets());

		// test IMPLICATION stronger than EQUIVALENT
		assertEquals("a => b <=> c",
				FormulaReader.readFormulaFromString("a b => c <=>")
						.toStringWithMinimalBrackets());
		assertEquals("(a <=> b) => c",
				FormulaReader.readFormulaFromString("a b <=> c =>")
						.toStringWithMinimalBrackets());
	}
}
