package prog2.project3.tests;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import prog2.project3.cnf.Cnf;
import prog2.project3.propositional.FormulaReader;
import prog2.project3.propositional.PropositionalFormula;

public class TseitinFelixTest {
	static final String VERSION = "1.3";

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
	// Cnf cnf = formula.getConjunctiveNormalForm();
	// System.out.println(TestUtilFelix.cnfToString(cnf));
	// }
	//
	// for (int n = 1; n < 10; n++) {
	// for (int i = 1; i < 10; i++) {
	// String formulaString = TestUtilFelix.generateRandomFormula(n);
	// System.out.println(formulaString);
	// PropositionalFormula formula = FormulaReader
	// .readFormulaFromString(formulaString);
	// System.out.println(formula.toString());
	// Cnf cnf = formula.getConjunctiveNormalForm();
	// System.out.println(TestUtilFelix.cnfToString(cnf));
	// }
	// }
	// }

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("TseitinFelixTest", VERSION);
	}

	@Test
	public void testVerifyTseitinData1() {
		List<String> testData = TestUtilFelix
				.parseDataFile("examples|Felix|TseitinFelixTestData1.txt");
		if (testData.size() != 94 * 3)
			fail("TseitinFelixTestData1.txt has wrong size or could not be read.\n"
					+ "You can simply delete it to fetch a new copy.\n"
					+ "If you cannot solve this problem, file a support ticket.");

		List<String> results = new LinkedList<String>();

		for (int testID = 1; testID <= 94; testID++) {
			String formulaString = testData.get((testID - 1) * 3);
			String expectedFormulaOutString = testData
					.get((testID - 1) * 3 + 1);
			String expectedCnfString = testData.get((testID - 1) * 3 + 2);

			PropositionalFormula formula = FormulaReader
					.readFormulaFromString(formulaString);
			Cnf cnf = formula.getConjunctiveNormalForm();

			String formulaOutString = formula.toString();
			String cnfString = TestUtilFelix.cnfToString(cnf);

			if (!expectedFormulaOutString.equals(formulaOutString)) {
				results
						.add("Test "
								+ testID
								+ " called FormulaReader.readFormulaFromString with this argument:\n"
								+ formulaString
								+ "\nYou parsed that to a formula where toString returns this:\n"
								+ formulaOutString
								+ "\nHowever, the expected result is:\n"
								+ expectedFormulaOutString
								+ "\nPlease, check FormulaReader.readFormulaFromString and PropositionalFormula.toString for errors.");
				continue;
			}
			if (!expectedCnfString.equals(cnfString)) {
				results
						.add("Test "
								+ testID
								+ " called FormulaReader.readFormulaFromString with this argument:\n"
								+ formulaString
								+ "\nYou correctly parsed that to a formula where toString returns this:\n"
								+ formulaOutString
								+ "\nThen, I called PropositionalFormula.getConjunctiveNormalForm on that. You returned:\n"
								+ cnfString
								+ "\nHowever, the expected result is:\n"
								+ expectedCnfString
								+ "\nPlease, check PropositionalFormula.getConjunctiveNormalForm for errors.");
				continue;
			}
			results.add(null);
		}

		TestUtilFelix.checkFailAndExplain(
				"TseitinFelixTest#testVerifyTseitinData1",
				results.toArray(new String[0]));
	}
}
