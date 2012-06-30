package prog2.project3.tests;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.Cnf;
import prog2.project3.cnf.Literal;
import prog2.project3.dpll.DPLLAlgorithm;
import prog2.project3.propositional.FormulaReader;

public class SatisfyingAssignmentBewiedTest {
	// ===== TEST - Exactly what it says on the tin

	@Test
	public void testStandardSatisfiable() {
		String[] upns = new String[] { "a ! !", "a ! ! !", "a ! a <=> a ||",
				"a c <=> c ! b a <=> && &&" };
		String[] cnfs = new String[] { "a-~b|~a-b|c-a" };

		assertSatisfiable(
				"SatisfyingAssignmentBewiedTest#testStandardSatisfiable",
				convert(upns, cnfs));
	}

	@Test
	public void testStandardUnsatisfiable() {
		String[] upns = new String[] { "a ! a &&",
				"a b <=> b c <=> &&      a c ! && a ! c && ||      &&",
				"a b ! && c d ! && &&      a ! b || c ! d || ||      &&" };
		String[] cnfs = new String[] { "a-b|~a-~b|a-c|~a-~c|b-c|~b-~c" };

		assertUnsatisfiable(
				"SatisfyingAssignmentBewiedTest#testStandardUnatisfiable",
				convert(upns, cnfs));
	}

	// ===== HELPERS - Feel free to use them

	public static final List<Cnf> convert(String[] upns, String[] cnfs) {
		List<Cnf> ret = new LinkedList<Cnf>();
		for (String upn : upns) {
			ret.add(FormulaReader.readFormulaFromString(upn)
					.getConjunctiveNormalForm());
		}
		for (String cnf : cnfs) {
			ret.add(TestUtilFelix.parseCompactCnfString(cnf));
		}
		return ret;
	}

	public static final void assertSatisfiable(String name, Collection<Cnf> cnfs) {
		List<String> results = new LinkedList<String>();
		for (Cnf c : cnfs) {
			Map<String, Boolean> map = new DPLLAlgorithm(c)
					.getSatisfyingAssignment();
			if (map == null) {
				results.add("Your DPLLAlgorithm thought that " + c
						+ " is unsolvable.\n"
						+ "Please make sure you pass IntegrationFelixTest.");
			} else {
				results.add(checkSatisfied(c, map));
			}
		}
		TestUtilFelix.checkFailAndExplain(name, results);
	}

	public static final void assertUnsatisfiable(String name,
			Collection<Cnf> cnfs) {
		List<String> results = new LinkedList<String>();
		for (Cnf c : cnfs) {
			Map<String, Boolean> map = new DPLLAlgorithm(c)
					.getSatisfyingAssignment();
			if (map != null) {
				results.add("Your DPLLAlgorithm thought that " + c
						+ " is solvable by " + map + ".\n"
						+ "Please make sure you pass IntegrationFelixTest.");
			} else {
				results.add(null);
			}
		}
		TestUtilFelix.checkFailAndExplain(name, results);
	}

	public static final String checkSatisfied(Cnf cnf, Map<String, Boolean> map) {
		// We want error messages like these one:

		// Mapping for variable XYZ is missing (variable occurs in clause ASDF),
		// therefore the cnf FOOBAR is not satisfied.

		// The clause ASDF evaluates to false, therefore the cnf FOOBAR is not
		// satisfied.

		for (Clause c : cnf.getClauses()) {
			String msg = checkSatisfied(c, map);
			if (msg != null) {
				return msg + ", therefore the cnf "
						+ TestUtilFelix.cnfToString(cnf) + " is not satisfied.";
			}
		}

		return null;
	}

	public static final String checkSatisfied(Clause c, Map<String, Boolean> map) {
		// We want error messages like these one:

		// Mapping for variable XYZ is missing (variable occurs in clause ASDF)

		// The clause ASDF evaluates to false

		boolean fulfilled = false;

		for (Literal l : c.getLiterals()) {
			String name = l.getVariable().getName();
			Boolean bb = map.get(name);
			if (bb == null) {
				return "Mapping for variable " + name
						+ " is missing (variable occurs in clause "
						+ TestUtilFelix.clauseToString(c) + ")";
			}
			if (bb ^ l.isNegatedLiteral()) {
				fulfilled = true;
			}
		}
		if (!fulfilled) {
			return "The clause " + TestUtilFelix.clauseToString(c)
					+ " evaluates to false";
		}
		return null;
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("SatisfyingAssignmentBewiedTest",
				"1.0");
	}
}
