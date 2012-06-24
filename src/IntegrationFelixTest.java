package prog2.project3.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import prog2.project3.cnf.Cnf;
import prog2.project3.dpll.DPLLAlgorithm;
import prog2.project3.propositional.FormulaReader;
import prog2.project3.propositional.PropositionalFormula;

public class IntegrationFelixTest {
	static final String VERSION = "1.0";

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("IntegrationFelixTest", VERSION);
	}

	/*
	 * The test data for this test is broughtto you by Fabian Wobito
	 * https://code.google.com/p/prog2tests/issues/detail?id=12
	 */
	@Test
	public void testIntegrationSat1() {
		List<String> testData = TestUtilFelix
				.parseDataFile("examples|Felix|IntegrationFelixTestSat1.txt");
		if (testData.size() != 132864)
			fail("IntegrationFelixTestSat1.txt has wrong size or could not be read.\n"
					+ "You can simply delete it to fetch a new copy.\n"
					+ "If you cannot solve this problem, file a support ticket.");
		
		for (String item : testData) {
			PropositionalFormula formula = FormulaReader
					.readFormulaFromString(item);
			Cnf cnf = formula.getConjunctiveNormalForm();
			DPLLAlgorithm algo = new DPLLAlgorithm(cnf);
			if(!algo.isSatisfiable()) {
				fail("You did not recognize that " + item + " is satisfiable.");
			}
		}
	}
	
	/*
	 * The test data for this test is broughtto you by Fabian Wobito
	 * https://code.google.com/p/prog2tests/issues/detail?id=12
	 */
	@Test
	public void testIntegrationUnSat1() {
		List<String> testData = TestUtilFelix
				.parseDataFile("examples|Felix|IntegrationFelixTestUnSat1.txt");
		if (testData.size() != 19862)
			fail("IntegrationFelixTestUnSat1.txt has wrong size or could not be read.\n"
					+ "You can simply delete it to fetch a new copy.\n"
					+ "If you cannot solve this problem, file a support ticket.");
		
		for (String item : testData) {
			PropositionalFormula formula = FormulaReader
					.readFormulaFromString(item);
			Cnf cnf = formula.getConjunctiveNormalForm();
			DPLLAlgorithm algo = new DPLLAlgorithm(cnf);
			if(algo.isSatisfiable()) {
				fail("You did not recognize that " + item + " is not satisfiable.");
			}
		}
	}

}
