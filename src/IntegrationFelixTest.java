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
	static final String VERSION = "1.1";

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
			alreadyPrinted = TestUtilFelix.updateProgressBar(alreadyPrinted,
					currentTest, TESTS_COUNT);

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

		TestUtilFelix
				.printRunning("IntegrationFelixTest#testIntegrationUnSat1");

		List<String> testData = TestUtilFelix
				.parseDataFile("examples|Fabian|IntegrationFelixTestUnSat1.txt");
		List<String> results = new LinkedList<String>();
		if (testData.size() != TESTS_COUNT)
			fail("IntegrationFelixTestUnSat1.txt has wrong size or could not be read.\n"
					+ "You can simply delete it to fetch a new copy.\n"
					+ "If you cannot solve this problem, file a support ticket.");

		for (String item : testData) {
			alreadyPrinted = TestUtilFelix.updateProgressBar(alreadyPrinted,
					currentTest, TESTS_COUNT);

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

	private int runDpllAndTrackIterations(DPLLAlgorithm algo) {
		int count = 1;

		while (!algo.iterate())
			count++;

		return count;
	}

	@Test
	public void testDpllPerformance1() {
		final int TESTS_COUNT = 467;
		int alreadyPrinted = 0;

		TestUtilFelix.printRunning("IntegrationFelixTest#testDpllPerformance1");

		List<String> testDataList = TestUtilFelix
				.parseDataFile("examples|Felix|DpllPerformanceFelixTestData1.txt");
		List<String> results = new LinkedList<String>();
		if (testDataList.size() != 3 * TESTS_COUNT)
			fail("DpllPerformanceFelixTestData1.txt.txt has wrong size or could not be read.\n"
					+ "You can simply delete it to fetch a new copy.\n"
					+ "If you cannot solve this problem, file a support ticket.");
		String[] testData = testDataList.toArray(new String[0]);

		for (int currentTest = 1; currentTest <= TESTS_COUNT; currentTest++) {
			alreadyPrinted = TestUtilFelix.updateProgressBar(alreadyPrinted,
					currentTest, TESTS_COUNT);

			PropositionalFormula formula = FormulaReader
					.readFormulaFromString(testData[(currentTest - 1) * 3]);
			boolean expectedSatisfiable = testData[(currentTest - 1) * 3 + 1]
					.equals("true");
			int maxAllowedSteps = Integer
					.valueOf(testData[(currentTest - 1) * 3 + 2]);
			Cnf cnf = formula.getConjunctiveNormalForm();
			DPLLAlgorithm algo = new DPLLAlgorithm(cnf);
			int steps = runDpllAndTrackIterations(algo);
			if (algo.isSatisfiable() != expectedSatisfiable) {
				results.add("We were checking this formula:\n"
						+ formula.toString() + "\n"
						+ "You said: satisfiable = " + algo.isSatisfiable()
						+ "\n" + "But I expected: satisfiable = "
						+ expectedSatisfiable);
			} else if (steps >= maxAllowedSteps) {
				results.add("We were checking this formula:\n"
						+ formula.toString()
						+ "\n"
						+ "You said: satisfiable = "
						+ algo.isSatisfiable()
						+ "\n"
						+ "That is correct! However, you needed "
						+ steps
						+ " steps to realize that.\n"
						+ "You should not need "
						+ maxAllowedSteps
						+ " or even more steps.\n"
						+ "Yes, even if you didn't implement the bonus-assignment 2.\n"
						+ "Make sure you correcty implemented Backtracking and Unit-Propagation.");
			} else {
				results.add(null);
			}

			currentTest++;
		}

		TestUtilFelix.checkFailAndExplain(
				"IntegrationFelixTest#testIntegrationSat1", results);
	}

}
