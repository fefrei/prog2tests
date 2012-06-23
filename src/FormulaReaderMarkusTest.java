package prog2.project3.tests;

/*
 * Benötigt: 
 * - Beispieldateien in satsolver/examples/
 * - Markus001.txt - Markus009.txt in satsolver/prog2/project3/tests/
 *   (selbes Verzeichnis wie *Test.java)
 */

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import prog2.project3.propositional.FormulaReader;
import prog2.project3.propositional.IncorrectFormulaException;
import prog2.project3.propositional.PropositionalFormula;

import junit.framework.TestCase;

public class MarkusFormulaReaderTest extends TestCase {
	@Before
	protected void setUp() throws Exception {
		super.setUp();
	}

	private String readFileFormula(String file, String notFound) {
		try {
			PropositionalFormula p = null;
			try {
				p = FormulaReader.readFormulaFromFile(file);
			} catch (FileNotFoundException e) {
				fail(notFound);
			}
			return p.toString();
		} catch (IOException e) {
			fail("Check read permissions of file " + file);
			throw new IllegalStateException();
		}
	}

	// Überprüft Fehlermeldungen
	private void checkCause(String input, IncorrectFormulaException.Cause expected) {
		try {
			FormulaReader.readFormulaFromString(input);
			fail("FormulaFromString didn't throw exception "+expected+" for \"" + input+"\"");
		} catch (IncorrectFormulaException e) {
			assertEquals("Wrong exception cause: " + input, expected, e.getReason());
		}
	}

	private void checkCauseFile(String file, IncorrectFormulaException.Cause expected,
			String notFound) {
		try {
			readFileFormula(file, notFound);
			fail("FormulaFromString didn't throw exception "+expected+" for " + file);
		} catch (IncorrectFormulaException e) {
			assertEquals("Wrong exception cause: " + file, expected, e.getReason());
		}
	}

	@Test
	public void testReadFormulaFromString() {
		// Beispiele aus der Beschreibung
		assertEquals("Wrong result", "((a && B) || c)", FormulaReader
				.readFormulaFromString(" a B   &&		 c  || 	").toString());
		assertEquals(
				"Wrong result",
				"((a && (b && (dAvid && Putnam))) || c)",
				FormulaReader.readFormulaFromString(
						"	a b  dAvid  Putnam && && && c ||").toString());

		// Beispiele für Fehlermeldungen aus der Beschreibung
		checkCause("", IncorrectFormulaException.Cause.EMPTYFORMULA);
		checkCause("  	   	", IncorrectFormulaException.Cause.EMPTYFORMULA);
		checkCause("ab && c ||", IncorrectFormulaException.Cause.MISSINGOPERAND);
		checkCause("a b & & c ||",
				IncorrectFormulaException.Cause.INCORRECTIDENTIFIER);
		checkCause("a b && c", IncorrectFormulaException.Cause.MISSINGOPERATOR);
		checkCause("a123 b && c ||",
				IncorrectFormulaException.Cause.INCORRECTIDENTIFIER);

		// Eigene Beispiele
		assertEquals("Wrong result (!)", "(! (! a))", FormulaReader
				.readFormulaFromString("a ! !").toString());
		assertEquals("Wrong result (=>/<=>)", "((a => b) <=> (b => c))",
				FormulaReader.readFormulaFromString("a b => b c => <=>")
						.toString());
		assertEquals("Wrong result", "a",
				FormulaReader.readFormulaFromString("a").toString());
		assertEquals(
				"Wrong result",
				"((((((a || b) || c) || d) && e) || f) && g)",
				FormulaReader.readFormulaFromString(
						"a b || c || d || e && f || g &&").toString());
		assertEquals(
				"Wrong result",
				"(((a => b) <=> (a || b)) && ((A => B) <=> (A || B)))",
				FormulaReader.readFormulaFromString(
						"a b => a b || <=> A B => A B || <=> &&").toString());
	}

	@Test
	public void testReadFormulaFromFile_Sanity() {
		// Exceptions
		// IOException fehlt, da schlecht reproduzierbar. Diese sollte aber
		// automatisch beim Lesen geworfen werden.
		try {
			FormulaReader.readFormulaFromFile("does_not_exist.file");
			fail("FileNotFoundException fehlt.");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			fail("IOException");
		}
		try {
			FormulaReader.readFormulaFromFile(".");
			fail("FileNotFoundException fehlt, wenn Verzeichnis übergeben. ");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			fail("IOException");
		}
		try {
			FormulaReader.readFormulaFromFile(null);
			fail("NullPointerException fehlt.");
		} catch (NullPointerException e) {
		} catch (IOException e) {
			fail("IOException");
		}
	}

	@Test
	public void testReadFormulaFromFile_Examples() {
		// Die Beispiele lesen
		final String fnf = "Der \"examples\"-Ordner muss im Hauptverzeichnis des Projekts (\"satsolver\") liegen";
		try {
			FormulaReader.readFormulaFromFile("examples/empty.txt");
			fail("empty.txt: Keine Exception");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IncorrectFormulaException e) {
			assertEquals("False exception cause: empty.txt", e.getReason(),
					IncorrectFormulaException.Cause.EMPTYFORMULA);
		} catch (IOException e) {
			fail("IOException");
		}

		try {
			assertEquals("linebreakformula.txt", FormulaReader
					.readFormulaFromFile("examples/linebreakformula.txt")
					.toString(), "(a && b)");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IOException e) {
			fail("IOException");
		}

		try {
			assertEquals("simple.txt",
					FormulaReader.readFormulaFromFile("examples/simple.txt")
							.toString(), "((! (hallo && welt)) || java)");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IOException e) {
			fail("IOException");
		}

		try {
			assertEquals("simpleLinebreak.txt", FormulaReader
					.readFormulaFromFile("examples/simpleLinebreak.txt")
					.toString(), "((a && (! b)) || c)");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IOException e) {
			fail("IOException");
		}

		try {
			assertEquals(
					"simpleSpace.txt",
					FormulaReader.readFormulaFromFile(
							"examples/simpleSpace.txt").toString(),
					"(((! ((! a) && (! b))) || c) => d)");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IOException e) {
			fail("IOException");
		}
	}

	@Test
	public void testReadFormulaFromFile_Markus() {
		// Eigene Tests, die gleichen Daten wie in testReadFormularFromString
		final String fnf = "Die \"Markus00x\"-Dateien müssen im test-Verzeichnis (\"satsolver/prog2/project3/tests\") liegen";

		checkCauseFile("prog2/project3/tests/Markus001.txt",
				IncorrectFormulaException.Cause.MISSINGOPERAND, fnf);
		checkCauseFile("prog2/project3/tests/Markus002.txt",
				IncorrectFormulaException.Cause.INCORRECTIDENTIFIER, fnf);
		checkCauseFile("prog2/project3/tests/Markus003.txt",
				IncorrectFormulaException.Cause.MISSINGOPERATOR, fnf);
		checkCauseFile("prog2/project3/tests/Markus004.txt",
				IncorrectFormulaException.Cause.INCORRECTIDENTIFIER, fnf);
		assertEquals("Wrong result (!)", "(! (! a))",
				readFileFormula("prog2/project3/tests/Markus005.txt", fnf));
		assertEquals("Wrong result (=>/<=>)", "((a => b) <=> (b => c))",
				readFileFormula("prog2/project3/tests/Markus006.txt", fnf));
		assertEquals("Wrong result", "a",
				readFileFormula("prog2/project3/tests/Markus007.txt", fnf));
		assertEquals("Wrong result",
				"((((((a || b) || c) || d) && e) || f) && g)",
				readFileFormula("prog2/project3/tests/Markus008.txt", fnf));
		assertEquals("Wrong result",
				"(((a => b) <=> (a || b)) && ((A => B) <=> (A || B)))",
				readFileFormula("prog2/project3/tests/Markus009.txt", fnf));
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("FormulaReaderMarkusTest", "1.0.2");
	}
}
