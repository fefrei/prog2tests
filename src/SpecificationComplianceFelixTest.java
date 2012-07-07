package prog2.project3.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.Cnf;
import prog2.project3.cnf.Literal;
import prog2.project3.cnf.TruthValue;
import prog2.project3.cnf.Variable;
import prog2.project3.dpll.Choice;
import prog2.project3.dpll.DPLLAlgorithm;
import prog2.project3.dpll.StackEntry;

public class SpecificationComplianceFelixTest {
	static final String VERSION = "1.3";

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("SpecificationComplianceFelixTest", VERSION);
	}

	@Test
	/**
	 * Tests if the DPLL assigns variables in lexical order.
	 */
	public void testDpllLexicalOrdering() {
		// this cnf has no pure or unit literals
		Cnf cnf = TestUtilFelix.parseCompactCnfString("b-a-c|~b-~c-~a");
		DPLLAlgorithm algo = new DPLLAlgorithm(cnf);

		// this _must_ set a to true
		algo.iterate();

		List<StackEntry> stack = algo.getStack();
		assertEquals("Your stack had the wrong amount of entries after one iteration.", 1,
				stack.size());

		StackEntry entry = stack.get(0);
		assertEquals("You should have guessed a variable, but your stack says IMPLIED.",
				Choice.CHOSEN, entry.choice);

		assertEquals(
				"You should have set the first variable to TRUE, because the specification says "
						+ "(line 210) that you should try TRUE first.", TruthValue.TRUE,
				entry.variable.getTruthValue());

		assertEquals(
				"You should habe chosen the variable \"a\" first (lexical order, see line 209 in specification).",
				"a", entry.variable.getName());
	}

	@Test
	/**
	 * Tests if the DPLL assigns variables in lexical order, even if
	 * Cnf.getVariables can't be trusted.
	 */
	public void testDpllLexicalOrderingSpoofedCnf() {
		final Cnf realCnf = TestUtilFelix.parseCompactCnfString("b-a-c|~b-~c-~a");

		final Cnf spoofedCnf = new Cnf() {

			@Override
			public TruthValue getTruthValue() {
				return realCnf.getTruthValue();
			}

			@Override
			public Set<Clause> getClauses() {
				return realCnf.getClauses();
			}

			@Override
			public Collection<Variable> getVariables() {
				Collection<Variable> res = new LinkedList<Variable>();
				res.add(realCnf.getVariableForName("b"));
				res.add(realCnf.getVariableForName("a"));
				res.add(realCnf.getVariableForName("c"));

				return res;
			}

			@Override
			public void resetAllVariables() {
				realCnf.resetAllVariables();
			}

			@Override
			public Variable getVariableForName(String name) {
				return realCnf.getVariableForName(name);
			}

			@Override
			public Literal getUnitClauseLiteral() {
				return realCnf.getUnitClauseLiteral();
			}

			@Override
			public Literal getPureLiteral() {
				return realCnf.getPureLiteral();
			}

		};

		DPLLAlgorithm algo = new DPLLAlgorithm(spoofedCnf);

		// this _must_ set a to true
		algo.iterate();

		List<StackEntry> stack = algo.getStack();
		assertEquals("Your stack had the wrong amount of entries after one iteration.", 1,
				stack.size());

		StackEntry entry = stack.get(0);
		assertEquals("You should have guessed a variable, but your stack says IMPLIED.",
				Choice.CHOSEN, entry.choice);

		assertEquals(
				"You should have set the first variable to TRUE, because the specification says "
						+ "(line 210) that you should try TRUE first.", TruthValue.TRUE,
				entry.variable.getTruthValue());

		assertEquals(
				"You should habe chosen the variable \"a\" first (lexical order, see line 209 in specification).\n"
						+ "If you passed SpecificationComplianceFelixTest#testDpllLexicalOrdering, "
						+ "but failed this test, see https://forum.st.cs.uni-saarland.de/"
						+ "boards/viewthread?thread=1511", "a", entry.variable.getName());
	}

	@Test
	/**
	 * Tests if Unit-Clauses always have a Truth-Value of UNDEFINED
	 */
	public void testUnitClauseOnlyIfUndefined() {
		Cnf cnf = TestUtilFelix.parseCompactCnfString("a-b");
		cnf.getVariableForName("a").setTruthValue(TruthValue.TRUE);

		if (cnf.getTruthValue() != TruthValue.TRUE) {
			fail("You did not detect that a clause has the TruthValue TRUE although "
					+ "a literal ist TRUE.\n" + "The truth value you gave was: "
					+ cnf.getTruthValue());
		}

		if (cnf.getUnitClauseLiteral() != null) {
			System.out
					.println("\nFAIL: SpecificationComplianceFelixTest#testUnitClauseOnlyIfUndefined\n"
							+ "You found a unit clause in (a \\/ b) although a is already set to TRUE.\n"
							+ "Therefore, the clause has the TruthValue TRUE and should not be a Unit clause.\n"
							+ "See documentation, line 147." + "\n");
			fail("Your implementation of Unit-Clauses is wrong. See the console.");
		}
	}

	@Test
	/**
	 * Tests if you really read the specification for Bonus2.
	 */
	public void testBonus2SpecificationCompliance() {
		Cnf cnf = TestUtilFelix.parseCompactCnfString("a-b");
		cnf.getVariableForName("a").setTruthValue(TruthValue.TRUE);
		cnf.getVariableForName("b").setTruthValue(TruthValue.UNDEFINED);

		if (cnf.getPureLiteral() == null) {
			System.out.println("\nYou did not implement Bonus2 correctly.\n"
					+ "I called cnf.getPureLiteral() for this Cnf: "
					+ TestUtilFelix.cnfToString(cnf) + "\n"
					+ "(a is set to TRUE, b is UNDEFINED)\n"
					+ "You did not return a pure literal.\n"
					+ "But in fact, 'b' is a pure literal in that clause.\n"
					+ "I know that it does not make much sense to say that a clause "
					+ "with truth value TRUE has a pure literal, "
					+ "but the specification wants it this way.\n"
					+ "See this thread for more information: "
					+ "https://forum.st.cs.uni-saarland.de/boards/viewthread?thread=1535\n");
			fail("You did not implement Bonus2 correctly. See the console for more information.");
		}
	}

	@Test
	/**
	 * Tests whether we can trick your implementation of DPLL into throwing
	 * an NullPointerException by calling getSatisfyingAssignment after using iterate
	 * 
	 * This test is brought to you by Christian Faber.
	 */
	public void testDpllGetAssignmentAfterIterate() {
		Cnf forumula = TestUtilFelix.parseCompactCnfString("a|~a-b");
		DPLLAlgorithm dpll = new DPLLAlgorithm(forumula);
		Variable varA = forumula.getVariableForName("a");

		dpll.iterate();
		dpll.iterate();

		if (varA.getTruthValue() == TruthValue.TRUE && forumula.getTruthValue() == TruthValue.TRUE) {
			try {
				dpll.getSatisfyingAssignment().get("a").booleanValue();
			} catch (NullPointerException e) {

				/*
				 * Ja das testen die wirklich! Das Problem ist, dass nach
				 * iterate der wert dpll.satisfied nicht gesetzt wird wodurch
				 * eure formel unter Umständen noch einmal duchläuft ohne den
				 * Stack zu leeren!
				 */

				fail("You returned Null when asked for the SatisfiyingAssignment of a formula even though it is True (even by your calculation)! Tip: Debug!");
			}
		}
	}
}
