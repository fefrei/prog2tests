package prog2.project3.tests;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import prog2.project3.propositional.FormulaReader;
import prog2.project3.propositional.PropositionalFormula;

public class Bonus1FelixTest {
	static final String VERSION = "1.0";

	// @Test
	// public void testGenerateTestData() {
	// String[] easyTests = { "a", "b", "a !", "a a &&", "a b &&", "a a ||",
	// "a b ||", "a a =>", "a b =>", "a a <=>", "a b <=>",
	// "a a ! <=>", "a b ! <=>" };
	//
	// for (String formulaString : easyTests) {
	// System.out.println(formulaString);
	// PropositionalFormula formula = FormulaReader
	// .readFormulaFromString(formulaString);
	// System.out.println(formula.toString());
	// System.out.println(formula.toStringWithMinimalBrackets());
	// }
	//
	// for (int n = 1; n < 10; n++) {
	// for (int i = 1; i < 10; i++) {
	// String formulaString = TestUtilFelix.generateRandomFormula(n);
	// System.out.println(formulaString);
	// PropositionalFormula formula = FormulaReader
	// .readFormulaFromString(formulaString);
	// System.out.println(formula.toString());
	// System.out.println(formula.toStringWithMinimalBrackets());
	// }
	// }
	// }

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("Bonus1FelixTest", VERSION);
	}

	@Test
	public void testVerifyBonus1Data1() {
		List<String> testData = TestUtilFelix
				.parseDataFile("examples|Felix|Bonus1FelixTestData1.txt");
		if (testData.size() != 94 * 3)
			fail("Bonus1FelixTestData1.txt has wrong size or could not be read.\n"
					+ "You can simply delete it to fetch a new copy.\n"
					+ "If you cannot solve this problem, file a support ticket.");

		List<String> results = new LinkedList<String>();

		for (int testID = 1; testID <= 94; testID++) {
			String formulaString = testData.get((testID - 1) * 3);
			String expectedFormulaOutString = testData
					.get((testID - 1) * 3 + 1);
			String expectedMinimalBracesString = testData
					.get((testID - 1) * 3 + 2);

			PropositionalFormula formula = FormulaReader
					.readFormulaFromString(formulaString);

			String formulaOutString = formula.toString();
			String minimalBracesString = formula.toStringWithMinimalBrackets();

			if (minimalBracesString == "")
				fail("You did not implement toStringWithMinimalBrackets.");

			if (!expectedFormulaOutString.equals(formulaOutString)) {
				results.add("I called FormulaReader.readFormulaFromString with this argument:\n"
						+ formulaString
						+ "\nYou parsed that to a formula where toString returns this:\n"
						+ formulaOutString
						+ "\nHowever, the expected result is:\n"
						+ expectedFormulaOutString
						+ "\nPlease, check FormulaReader.readFormulaFromString and PropositionalFormula.toString for errors.");
				continue;
			}
			if (!expectedMinimalBracesString.equals(minimalBracesString)) {
				results.add("I called FormulaReader.readFormulaFromString with this argument:\n"
						+ formulaString
						+ "\nYou correctly parsed that to a formula where toString returns this:\n"
						+ formulaOutString
						+ "\nThen, I called PropositionalFormula.toStringWithMinimalBrackets on that. You returned:\n"
						+ minimalBracesString
						+ "\nHowever, the expected result is:\n"
						+ expectedMinimalBracesString
						+ "\nPlease, check PropositionalFormula.toStringWithMinimalBrackets for errors.");
				continue;
			}
			results.add(null);
		}

		TestUtilFelix.checkFailAndExplain(
				"Bonus1FelixTest#testVerifyBonus1Data1", results);
	}
}
