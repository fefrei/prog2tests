package prog2.project3.tests;

import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import prog2.project3.cnf.Cnf;
import prog2.project3.dpll.DPLLAlgorithm;
import prog2.project3.propositional.FormulaReader;
import prog2.project3.propositional.PropositionalFormula;

public class IntegrationFelixTest {
	static final String VERSION = "1.0.3";

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("IntegrationFelixTest", VERSION);
	}

	/*
	 * The test data for this test is brought to you by Fabian Wobito
	 * https://code.google.com/p/prog2tests/issues/detail?id=12
	 */
	@Test
	public void testIntegrationSat1() {
		final int TESTS_COUNT = 132864;
		int alreadyPrinted = 0;
		int currentTest = 1;

		TestUtilFelix.printRunning("IntegrationFelixTest#testIntegrationSat1");
		
		List<String> testData = TestUtilFelix
				.parseDataFile("examples|Fabian|IntegrationFelixTestSat1.txt");
		List<String> results = new LinkedList<String>();
		if (testData.size() != TESTS_COUNT)
			fail("IntegrationFelixTestSat1.txt has wrong size or could not be read.\n"
					+ "You can simply delete it to fetch a new copy.\n"
					+ "If you cannot solve this problem, file a support ticket.");

		for (String item : testData) {
			alreadyPrinted = TestUtilFelix.updateProgressBar(alreadyPrinted, currentTest,
					TESTS_COUNT);

			PropositionalFormula formula = FormulaReader
					.readFormulaFromString(item);
			Cnf cnf = formula.getConjunctiveNormalForm();
			DPLLAlgorithm algo = new DPLLAlgorithm(cnf);
			if (!algo.isSatisfiable()) {
				results.add("You did not recognize that " + item
						+ " is satisfiable.");
			} else {
				results.add(null);
			}

			currentTest++;
		}

		TestUtilFelix.checkFailAndExplain(
				"IntegrationFelixTest#testIntegrationSat1", results);
	}

	/*
	 * The test data for this test is brought to you by Fabian Wobito
	 * https://code.google.com/p/prog2tests/issues/detail?id=12
	 */
	@Test
	public void testIntegrationUnSat1() {
		final int TESTS_COUNT = 19862;
		int alreadyPrinted = 0;
		int currentTest = 1;

		TestUtilFelix.printRunning("IntegrationFelixTest#testIntegrationUnSat1");

		List<String> testData = TestUtilFelix
				.parseDataFile("examples|Fabian|IntegrationFelixTestUnSat1.txt");
		List<String> results = new LinkedList<String>();
		if (testData.size() != TESTS_COUNT)
			fail("IntegrationFelixTestUnSat1.txt has wrong size or could not be read.\n"
					+ "You can simply delete it to fetch a new copy.\n"
					+ "If you cannot solve this problem, file a support ticket.");

		for (String item : testData) {
			alreadyPrinted = TestUtilFelix.updateProgressBar(alreadyPrinted, currentTest,
					TESTS_COUNT);

			PropositionalFormula formula = FormulaReader
					.readFormulaFromString(item);
			Cnf cnf = formula.getConjunctiveNormalForm();
			DPLLAlgorithm algo = new DPLLAlgorithm(cnf);
			if (algo.isSatisfiable()) {
				results.add("You did not recognize that " + item
						+ " is NOT satisfiable.");
			} else {
				results.add(null);
			}
			
			currentTest++;
		}

		TestUtilFelix.checkFailAndExplain(
				"IntegrationFelixTest#testIntegrationSat1", results);
	}

}
