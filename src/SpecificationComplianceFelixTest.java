package prog2.project3.tests;

import static org.junit.Assert.assertEquals;

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
	static final String VERSION = "1.0";

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("SpecificationComplianceFelixTest", VERSION);
	}

	@Test
	/*
	 * Tests if the DPLL assigns variables in lexical order.
	 */
	public void testDpllLexicalOrdering() {
		// this cnf has no pure or unit literals
		Cnf cnf = TestUtilFelix.parseCompactCnfString("b-a-c|~b-~c-~a");
		DPLLAlgorithm algo = new DPLLAlgorithm(cnf);

		// this _must_ set a to true
		algo.iterate();

		List<StackEntry> stack = algo.getStack();
		assertEquals(
				"You stack had the wrong amount of entries after one iteration.",
				1, stack.size());

		StackEntry entry = stack.get(0);
		assertEquals(
				"You should have guessed a variable, but your stack says IMPLIED.",
				Choice.CHOSEN, entry.choice);

		assertEquals(
				"You should have set the first variable to TRUE, because the specification says (line 210) that you should try TRUE first.",
				TruthValue.TRUE, cnf.getVariableForName("a").getTruthValue());

		assertEquals(
				"You should habe chosen the variable \"a\" first (lexical order, see line 209 in specification).",
				"a", entry.variable.getName());
	}

	@Test
	/*
	 * Tests if the DPLL assigns variables in lexical order, even if
	 * Cnf.getVariables can't be trusted.
	 */
	public void testDpllLexicalOrderingSpoofedCnf() {
		final Cnf realCnf = TestUtilFelix
				.parseCompactCnfString("b-a-c|~b-~c-~a");

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
				Variable[] realVariables = realCnf.getVariables().toArray(
						new Variable[0]);

				assertEquals(
						"Your getVariables returned a wrong amount of variables.",
						3, realVariables.length);

				Collection<Variable> res = new LinkedList<Variable>();
				res.add(realVariables[1]); // be evil, change the order a bit
				res.add(realVariables[0]); // be evil, change the order a bit
				res.add(realVariables[2]);

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
		assertEquals(
				"You stack had the wrong amount of entries after one iteration.",
				1, stack.size());

		StackEntry entry = stack.get(0);
		assertEquals(
				"You should have guessed a variable, but your stack says IMPLIED.",
				Choice.CHOSEN, entry.choice);

		assertEquals(
				"You should have set the first variable to TRUE, because the specification " +
				"says (line 210) that you should try TRUE first.",
				TruthValue.TRUE, spoofedCnf.getVariableForName("a")
						.getTruthValue());

		assertEquals(
				"You should habe chosen the variable \"a\" first (lexical order, see line 209 in specification).\n"
						+ "If you passed SpecificationComplianceFelixTest#testDpllLexicalOrdering, " +
						"but failed this test, see https://forum.st.cs.uni-saarland.de/" +
						"boards/viewthread?thread=1511",
				"a", entry.variable.getName());
	}
}
