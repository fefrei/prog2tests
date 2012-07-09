package prog2.project3.tests;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.Cnf;
import prog2.project3.cnf.Literal;
import prog2.project3.cnf.TruthValue;
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
		// We want error messages like this one:

		// (Blah blah blah), therefore the cnf FOOBAR is not satisfied.

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
		// We want error messages like these ones:

		// The clause ASDF is not always fullfilled for the given map XYZZY

		// The clause ASDF evaluates to false

		Map<String, TruthValue> required = new LinkedHashMap<String, TruthValue>();

		for (Literal l : c.getLiterals()) {
			String name = l.getVariable().getName();
			Boolean bb = map.get(name);
			if (bb == null) {
				required.put(
						name,
						or(required.get(name),
								truthValue(!l.isNegatedLiteral())));
			} else if (bb ^ l.isNegatedLiteral()) {
				// There is a non-null boolean, and it is different from the
				// isNegated state.
				// => Literal fulfilled
				// => Clause fulfilled
				return null;
			}
		}

		boolean tautology = false;

		Iterator<Entry<String, TruthValue>> it = required.entrySet().iterator();

		while (it.hasNext()) {
			Entry<String, TruthValue> e = it.next();
			if (e.getValue() == TruthValue.UNDEFINED) {
				tautology = true;
				it.remove();
			}
		}

		if (tautology) {
			// The clause is not fulfilled by the current assignment, but there
			// were at least one pair like "toBe \/ ~toBe", so the clause
			// actually is a tautology
			return null;
		}

		// There was no tautology, and there is no reason to evaluate to true.
		// No I'm just trying to compose a helpful message.

		StringBuilder sb = new StringBuilder("The clause ");
		sb.append(TestUtilFelix.clauseToString(c));
		sb.append(" is not satisfied (");
		if (required.isEmpty()) {
			sb.append("no UNDEFINED variables have been found)");
		} else {
			sb.append("at least one assignment of ");
			sb.append(required.toString());
			sb.append(" would help)");
		}

		return sb.toString();
	}

	/**
	 * Handles null as "none", TRUE and FALSE as themselves and "UNDEFINED" as
	 * "both".
	 */
	public static final TruthValue or(TruthValue a, TruthValue b) {
		int aI = asInt(a), bI = asInt(b);
		return asTruthValue(aI | bI);
	}

	public static final int NONE = 0x00, TRUE = 0x01, FALSE = 0x10,
			BOTH = 0x11;

	public static final int asInt(TruthValue tv) {
		if (tv == null) {
			return NONE;
		}
		switch (tv) {
		case TRUE:
			return TRUE;
		case FALSE:
			return FALSE;
		case UNDEFINED:
			return BOTH;
		default:
			throw new IllegalStateException("Encountered unknown TruthValue: "
					+ tv);
		}
	}

	public static final TruthValue asTruthValue(int i) {
		switch (i) {
		case NONE:
			return null;
		case TRUE:
			return TruthValue.TRUE;
		case FALSE:
			return TruthValue.FALSE;
		case BOTH:
			return TruthValue.UNDEFINED;
		default:
			throw new IllegalStateException("Encountered illegal int: " + i);
		}
	}

	public static final String asString(TruthValue tv) {
		int i = asInt(tv);
		switch (i) {
		case NONE:
			return "none";
		case TRUE:
			return "true";
		case FALSE:
			return "false";
		case BOTH:
			return "any";
		default:
			throw new IllegalStateException("Encountered illegal int: " + i);
		}
	}

	public static final TruthValue truthValue(boolean b) {
		return b ? TruthValue.TRUE : TruthValue.FALSE;
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("SatisfyingAssignmentBewiedTest",
				"1.1");
	}
}
