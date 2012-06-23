package prog2.project3.tests;

import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

import prog2.project3.propositional.FormulaReader;
import prog2.project3.propositional.IncorrectFormulaException;
import prog2.project3.propositional.IncorrectFormulaException.Cause;

public class FormulaReaderBewiedTest extends TestCase {
	// ========== INTERESTING stuff ==========

	// I'm NOT testing any file I/O here.
	// I ONLY test the FromString method.
	// I simply assume/hope that FromFile and FromString do the same in your
	// implementation.

	/**
	 * Maximum count of errors to be printed per call to tryCombinations -- be
	 * prepared to see a lot more.
	 */
	private static final int MAX_ERRORS = 3;

	/**
	 * If you really don't wanna care about the bonus points, then set this to
	 * 'true':
	 */
	private static final boolean IGNORE_BONUS_ONE = false;
	
	// ========== MAGIC values. They are magic. ==========

	/**
	 * These are quite some extraordinary whitespace characters: See
	 * {@link #actualWhite} for an explanation.
	 * 
	 * Stolen from <a
	 * href="http://en.wikipedia.org/wiki/Whitespace_character">Wikipedia</a>.
	 */
	private static final String[] uberWhite = new String[] { "\u0085",
			"\u00A0", "\u1680", "\u180E", "\u2000", "\u2006", "\u200A",
			"\u2028", "\u2029", "\u202F", "\u205F", "\u3000" };
	/**
	 * Note that you are not supposed to use Character.isWhitespace(). The
	 * following is a regex that matches the ONLY "whitespace" that is allowed
	 * as such:
	 * 
	 * <pre>
	 * [ \t\n\x0B\f\r]
	 * </pre>
	 */
	private static final String[] actualWhite = new String[] { "\t", "\n",
			"\u000B", "\u000C", "\r", " " };
	/**
	 * These are some characters to ensure you aren"t using
	 * Character.isLowercase() or something.
	 */
	private static final String[] phony = new String[] { "Ö", "ä", "ß" };

	// ========== actual TESTS ==========

	@Test
	public void testDescriptionGood() {
		// Lines 29 and 30 in project description.
		assertResult("((a && b) || c)", "a  b && c ||");
		assertResult("((a && (b && (david && Putnam))) || c)",
				"a  b david Putnam && && && c ||");
	}

	@Test
	public void testDescriptionBad() {
		// Lines 32 thru 35 in project description.
		assertResult("ab && c ||", Cause.MISSINGOPERAND);
		// In my eyes, MISSINGOPERAND should be okay, too -- but don't bet on
		// it.
		assertResult("a b & & c ||", Cause.INCORRECTIDENTIFIER);
		assertResult("a b && c", Cause.MISSINGOPERATOR);
		assertResult("a123 b && c ||", Cause.INCORRECTIDENTIFIER);
	}

	@Test
	public void testDescriptionBonus() {
		assertBonusResult("! a || b", "a ! b ||");
		assertBonusResult("! ! a", "a ! !");
		assertBonusResult("a || b || c", "a b c || ||");
		assertBonusResult("a => (b => c)", "a b c => =>");
		assertBonusResult("(a => b) => c", "a b => c =>");
	}

	@Test
	public void testWhitespaceGood() {
		tryCombinations(expector("(! a)"), "a", actualWhite, "!");
		tryCombinations(expector(Cause.EMPTYFORMULA), actualWhite, actualWhite);
		tryCombinations(expector("(! a)"), actualWhite, actualWhite, "a !",
				actualWhite, actualWhite);
	}

	@Test
	public void testWhitespaceBad() {
		tryCombinations(expector(Cause.INCORRECTIDENTIFIER), phony, " !");
		tryCombinations(expector(Cause.INCORRECTIDENTIFIER), uberWhite, " !");
		tryCombinations(expector(Cause.INCORRECTIDENTIFIER), "as", phony, "d !");
		tryCombinations(expector(Cause.INCORRECTIDENTIFIER), "asd", uberWhite,
				"d &&");
	}

	@Test
	public void testFormulaGood() {
		assertResult("(a <=> (b => (c || (d && (! e)))))",
				"a b c d e ! && || => <=>");
		assertResult("(! (a && (b || (c => (d <=> e)))))",
				"a b c d e <=> => || && !");
	}

	@Test
	public void testFormulaBonus() {
		assertBonusResult("a <=> b => c || d && ! e",
				"a b c d e ! && || => <=>");
		assertBonusResult("! (a && (b || (c => (d <=> e))))",
				"a b c d e <=> => || && !");
		assertBonusResult("! ! ! a", "a ! ! !");
		assertBonusResult("! ! ! a && b", "a ! ! ! b &&");
		assertBonusResult("a && b && c || d || e", "a b c && && d || e ||");
		assertBonusResult("a && b && c || d || e", "a b c && && d e || ||");
		assertBonusResult("a && b && c || d || e", "a b && c && d || e ||");
		assertBonusResult("a && b && c || d || e", "a b && c && d e || ||");
	}

	// ========== BACKGROUND methods and helpers ==========

	private static final void tryCombinations(Tester t, Object... data) {
		String[][] strings = new String[data.length][];
		for (int i = 0; i < data.length; i++) {
			if (data[i] instanceof String) {
				strings[i] = new String[] { (String) data[i] };
			} else if (data[i] instanceof String[]) {
				strings[i] = (String[]) data[i];
			} else {
				System.out.println("Wrong input, silly: " + data[i]
						+ " at index " + i);
			}
		}
		tryCombinations(strings, t);
	}

	private static final void tryCombinations(String[][] strings, Tester t) {
		int[] indices = new int[strings.length];
		int counter = 0;
		int errCounter = 0;
		do {
			{
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < strings.length; i++) {
					sb.append(strings[i][indices[i]]);
				}
				try {
					t.test(sb.toString());
				} catch (RuntimeException e) {
					System.out.println("While trying combination #" + counter
							+ " " + Arrays.toString(indices)
							+ " encountered the following exception:");
					System.out.println(e.getMessage());
					System.out.println("StringBuilder contains >>>"
							+ sb.toString() + "<<<\n\n");
					if (++errCounter > MAX_ERRORS) {
						throw new RuntimeException(
								"See console for more information on fail #"
										+ counter + " and previous.", e);
					}
				}
			}

			for (int i = strings.length - 1; i >= 0; i--) {
				if (indices[i] < strings[i].length - 1) {
					indices[i]++;
					break;
				} else {
					indices[i] = 0;
					if (i == 0) {
						counter = -1;
					}
				}
			}
			counter++;
		} while (counter > 0);
	}

	private static final Tester expector(final String s) {
		return new Tester() {
			@Override
			public void test(String input) {
				assertResult(s, input);
			}
		};
	}

	private static final Tester expector(final Cause... causes) {
		return new Tester() {
			@Override
			public void test(String input) {
				assertResult(input, causes);
			}
		};
	}

	private static final Tester expectorBonus(final String s) {
		return new Tester() {
			@Override
			public void test(String input) {
				assertBonusResult(s, input);
			}
		};
	}

	private static final void assertResult(String expected, String input) {
		assertEquals(expected, FormulaReader.readFormulaFromString(input)
				.toString());
	}

	private static final void assertBonusResult(String expected, String input) {
		if (IGNORE_BONUS_ONE) {
			return;
		}
		assertEquals(expected, FormulaReader.readFormulaFromString(input)
				.toStringWithMinimalBrackets());
	}

	private static final void assertResult(String input, Cause... expected) {
		try {
			String result = FormulaReader.readFormulaFromString(input)
					.toString();
			fail("Expected IncorrectFormulaException to be thrown. You returned "
					+ result + " instead.");
		} catch (IncorrectFormulaException e) {
			Cause actual = e.getReason();
			for (Cause c : expected) {
				if (c == actual) {
					return;
				}
			}
			fail("Expected any of " + Arrays.toString(expected)
					+ " to be thrown, you threw " + actual + " instead.");
		} catch (Exception e) {
			fail("Expected IncorrectFormulaException to be thrown. You threw "
					+ e);
		}
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool
				.doUpdateTest("FormulaReaderBewiedTest", "1.0");
	}

	private static interface Tester {
		void test(String input);
	}
}
