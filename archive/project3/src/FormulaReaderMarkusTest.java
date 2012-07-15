package prog2.project3.tests;

/*
 * Benötigt: 
 * - Beispieldateien in satsolver/examples/
 * - Markus001.txt - Markus009.txt in satsolver/examples/Markus
 * 
 * EDIT: Moved them around to examples/Markus, and made pathnames independent.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import prog2.project3.propositional.FormulaReader;
import prog2.project3.propositional.IncorrectFormulaException;
import prog2.project3.propositional.PropositionalFormula;

import junit.framework.TestCase;

public class FormulaReaderMarkusTest extends TestCase {
	private static final String examplesPath = "examples" + File.separator + "Markus" + File.separator;
	
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
	private void checkCause(String input,
			IncorrectFormulaException.Cause expected) {
		try {
			FormulaReader.readFormulaFromString(input);
			fail("FormulaFromString didn't throw exception " + expected
					+ " for \"" + input + "\"");
		} catch (IncorrectFormulaException e) {
			assertEquals("Wrong exception cause: " + input, expected,
					e.getReason());
		}
	}

	private void checkCauseFile(String file,
			IncorrectFormulaException.Cause expected, String notFound) {
		try {
			readFileFormula(file, notFound);
			fail("FormulaFromString didn't throw exception " + expected
					+ " for " + file);
		} catch (IncorrectFormulaException e) {
			assertEquals("Wrong exception cause: " + file, expected,
					e.getReason());
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
			FormulaReader.readFormulaFromFile("this_file_hopefully_does_not_exist.verylongfileextension");
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
		final String fnf = "Die \"Markus00x\"-Dateien wurden fehlerhaft" +
				  " heruntergeladen. Bitte lasse das UpdateTool laufen," +
				  " oder erstelle ein Support-Ticket.";
		try {
			FormulaReader.readFormulaFromFile("examples" + File.separator
					+ "empty.txt");
			fail("empty.txt: Keine Exception");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IncorrectFormulaException e) {
			assertEquals("False exception cause: empty.txt", e.getReason(),
					IncorrectFormulaException.Cause.EMPTYFORMULA);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			assertEquals(
					"linebreakformula.txt",
					FormulaReader.readFormulaFromFile(
							"examples" + File.separator
									+ "linebreakformula.txt").toString(),
					"(a && b)");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IOException e) {
			fail("IOException");
		}

		try {
			assertEquals(
					"simple.txt",
					FormulaReader.readFormulaFromFile(
							"examples" + File.separator + "simple.txt")
							.toString(), "((! (hallo && welt)) || java)");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IOException e) {
			fail("IOException");
		}

		try {
			assertEquals(
					"simpleLinebreak.txt",
					FormulaReader
							.readFormulaFromFile(
									"examples" + File.separator
											+ "simpleLinebreak.txt").toString(),
					"((a && (! b)) || c)");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IOException e) {
			fail("IOException");
		}

		try {
			assertEquals(
					"simpleSpace.txt",
					FormulaReader.readFormulaFromFile(
							"examples" + File.separator + "simpleSpace.txt")
							.toString(), "(((! ((! a) && (! b))) || c) => d)");
		} catch (FileNotFoundException e) {
			fail(fnf);
		} catch (IOException e) {
			fail("IOException");
		}
	}

	@Test
	public void testReadFormulaFromFile_Markus() {
		// Eigene Tests, die gleichen Daten wie in testReadFormularFromString
		final String fnf = "Die \"Markus00x\"-Dateien wurden fehlerhaft" +
				  " heruntergeladen. Bitte lasse das UpdateTool laufen," +
				  " oder erstelle ein Support-Ticket.";

		checkCauseFile(examplesPath + "Markus001.txt",
				IncorrectFormulaException.Cause.MISSINGOPERAND, fnf);
		checkCauseFile(examplesPath + "Markus002.txt",
				IncorrectFormulaException.Cause.INCORRECTIDENTIFIER, fnf);
		checkCauseFile(examplesPath + "Markus003.txt",
				IncorrectFormulaException.Cause.MISSINGOPERATOR, fnf);
		checkCauseFile(examplesPath + "Markus004.txt",
				IncorrectFormulaException.Cause.INCORRECTIDENTIFIER, fnf);
		assertEquals(
				"Wrong result (!)",
				"(! (! a))",
				readFileFormula(examplesPath + "Markus005.txt", fnf));
		assertEquals(
				"Wrong result (=>/<=>)",
				"((a => b) <=> (b => c))",
				readFileFormula(examplesPath + "Markus006.txt", fnf));
		assertEquals(
				"Wrong result",
				"a",
				readFileFormula(examplesPath + "Markus007.txt", fnf));
		assertEquals(
				"Wrong result",
				"((((((a || b) || c) || d) && e) || f) && g)",
				readFileFormula(examplesPath + "Markus008.txt", fnf));
		assertEquals(
				"Wrong result",
				"(((a => b) <=> (a || b)) && ((A => B) <=> (A || B)))",
				readFileFormula(examplesPath + "Markus009.txt", fnf));
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool
				.doUpdateTest("FormulaReaderMarkusTest", "1.1.1");
	}
}
