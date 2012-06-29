package prog2.project3.tests;

// import static org.junit.Assert.*;
//import static prog2.project3.cnf.CnfFactory.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static prog2.project3.cnf.CnfFactory.createClause;
import static prog2.project3.cnf.CnfFactory.createCnfFormula;
import static prog2.project3.cnf.CnfFactory.createNegativeLiteral;
import static prog2.project3.cnf.CnfFactory.createPositiveLiteral;
import static prog2.project3.cnf.CnfFactory.createVariable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.Cnf;
import prog2.project3.cnf.Literal;
import prog2.project3.cnf.TruthValue;
import prog2.project3.cnf.Variable;
import prog2.project3.propositional.VariableNameGenerator;

public class CnfContentBewiedTest {
	// ===== TESTS themselves
	// Federal regulations require me to warn you that this next test
	// file... is looking pretty good.

	@Test
	public void testUniqueness() {
		String name = VariableNameGenerator.getVariableName();
		Variable v = createVariable(name);
		Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);
		Clause vC = createClause(vPosL, vNegL);

		if (v == createVariable(name)) {
			fail("You must not cache the results of CnfFactory"
					+ ".createVariable().\nIn other words: Always create a new"
					+ " Variable object in CnfFactory.createVariable()");
		}
		if (vPosL == createPositiveLiteral(v)
				|| vNegL == createNegativeLiteral(v)) {
			fail("You must not cache the results of CnfFactory"
					+ ".createXxxxtiveLiteral().\nIn other words: Always create a new"
					+ " Literal object in CnfFactory.createXxxxtiveLiteral()");
		}
		if (vC == createClause(vPosL, vNegL)) {
			// Shouldn't happen anyway
			fail("You must not cache the results of CnfFactory"
					+ ".createClause().\nWhy would you do that anyway?");
		}
		if (createCnfFormula(vC) == createCnfFormula(vC)) {
			// Shouldn't happen anyway
			fail("You must not cache the results of CnfFactory"
					+ ".createCnfFormula().\nWhy would you do that anyway?");
		}
	}

	@Test
	public void testVariableSpecification() {
		final Variable v = createVariable(VariableNameGenerator
				.getVariableName());
		final Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);
		final Clause vC = createClause(vPosL, vNegL);

		final ListeningBewiedClause c = new ListeningBewiedClause();

		assertEquals("Your Variable reports wrong parent clauses.",
				collect(vC), v.getParentClauses());

		v.addParentClause(c);
		if (!v.getParentClauses().contains(c)
				|| v.getParentClauses().size() != 2) {
			assertEquals("Your Variable didn't update it's parent clauses.",
					collect(vC, c), v);

		}

		assertSame("You must initialize your Variables as UNDEFINED.",
				TruthValue.UNDEFINED, v.getTruthValue());
		c.assertUpdates(0);

		v.setTruthValue(TruthValue.TRUE);
		assertSame("Your variable didn't update it's truthvalue.",
				TruthValue.TRUE, v.getTruthValue());
		c.assertUpdates(1);

		v.negateValue();
		assertSame("Your variable didn't negate it's truthvalue.",
				TruthValue.FALSE, v.getTruthValue());
		c.assertUpdates(2);

		v.setTruthValue(TruthValue.UNDEFINED);
		assertSame(TruthValue.UNDEFINED, v.getTruthValue());
		c.assertUpdates(3);

		if (v.isPureNegative() || v.isPurePositive()) {
			fail("Your variable (which has positive and negative dependent"
					+ " clauses) reported that it's pure.\n"
					+ "It isn't pure! This doesn't have anything to do whether"
					+ " you chose to do Bonusaufgabe2 or not.\n"
					+ "If you chose not to do Bonusaufgabe2, you should return"
					+ " false.");
		}

		Variable fresh = createVariable("fresh");

		try {
			fresh.getParentClauses().add(null);
			fail("You forgot to use Collections.unmodifiableSet() for the"
					+ " result of Variable.getParentClauses()");
		} catch (UnsupportedOperationException e) {
			// Expected this
		} catch (NullPointerException e) {
			fail("You're not meant to implement any Collections. Use java.util"
					+ ".Collections.unmodifiableWhatever() instead.");
		}

		if (!fresh.isPurePositive() || !fresh.isPureNegative()) {
			System.out.println("You chose not to do Bonusaufgabe2, or your"
					+ " implementation has gone horribly wrong (CCBT#tVS).\n"
					+ "That makes sad :-(\n");
			return;
		}

		// End of first part

		fresh.addDependentClauseNeg(c);
		if (fresh.isPurePositive()) {
			fail("Your variable with one negative dependent clause reported"
					+ " that it isPurePositive. It isn't!");
		}
		if (!fresh.isPureNegative()) {
			fail("Your variable with zero positive dependent clauses reported"
					+ " that it isn't PureNegative. But it is!");
		}

		fresh.addDependentClausePos(c);
		if (fresh.isPureNegative() || fresh.isPurePositive()) {
			fail("Your variable (which has positive and negative dependent"
					+ " clauses) reported that it's pure.\n"
					+ "Your implementation is broken. I can't imagine how you"
					+ " managed to get to this if-block.");
		}

		fresh.removeDependentClauseNeg(c);
		if (fresh.isPureNegative()) {
			fail("Your variable with one positive dependent clause reported"
					+ " that it isPureNegative after deleting the negative one."
					+ " It isn't!");
		}
		if (!fresh.isPurePositive()) {
			fail("Your variable with zero negative dependent clauses reported"
					+ " that it isn't PurePositive after deleting the negative"
					+ " one. But it is!");
		}

		fresh.removeDependentClausePos(c);
		if (!fresh.isPurePositive() || !fresh.isPureNegative()) {
			fail("Your implementation didn't recognize that after deleting"
					+ " every dependent clause, it should be 'fully' pure.");
		}
	}

	@Test
	public void testLiteralSpecification() {
		final Variable v = createVariable("foo");
		final Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);

		if (vPosL.isNegatedLiteral() || !vNegL.isNegatedLiteral()) {
			fail("Your Literals didn't implement isNegatedLiteral()"
					+ " the right way.");
		}

		assertSame(vPosL.getVariable(), v);
		assertSame(vNegL.getVariable(), v);
		{
			Variable phony = new CnfFactoryBewiedTest.PhonyBewiedVariable("asd");
			assertSame(createPositiveLiteral(phony).getVariable(), phony);
		}

		vPosL.chooseSatisfyingAssignment();
		assertSame("Positive Literal didn't set TruthValue correctly",
				TruthValue.TRUE, v.getTruthValue());
		assertSame("Literal doesn't forward call to getTruthValue",
				TruthValue.TRUE, vPosL.getTruthValue());
		assertSame("Negative literal forgot to invert truth value",
				TruthValue.FALSE, vNegL.getTruthValue());

		v.setTruthValue(TruthValue.UNDEFINED);
		assertSame("Literal doesn't forward call to getTruthValue",
				TruthValue.UNDEFINED, vPosL.getTruthValue());
		assertSame("Literal doesn't forward call to getTruthValue",
				TruthValue.UNDEFINED, vNegL.getTruthValue());

		vNegL.chooseSatisfyingAssignment();
		assertSame("Negative Literal didn't set TruthValue correctly",
				TruthValue.FALSE, v.getTruthValue());
		assertSame("Literal doesn't forward call to getTruthValue",
				TruthValue.FALSE, vPosL.getTruthValue());
		assertSame("Negative literal forgot to invert truth value",
				TruthValue.TRUE, vNegL.getTruthValue());

		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #0
				vPosL.chooseSatisfyingAssignment();
			}
		}, new Runnable() {
			public void run() {
				vNegL.chooseSatisfyingAssignment();
			}
		} };

		CnfFactoryBewiedTest.checkTestExceptions(tests,
				"CnfContentBewiedTest#testLiteralSpecification@ChooseInvalid",
				IllegalStateException.class);

		v.setTruthValue(TruthValue.UNDEFINED);
		assertSame(TruthValue.UNDEFINED, v.getTruthValue());

		if (!v.isPurePositive() || !v.isPureNegative()) {
			System.out.println("You chose not to do Bonusaufgabe2, or your"
					+ " implementation has gone horribly wrong (CCBT#tLS).\n"
					+ "That makes sad :-(\n");
			return;
		}

		final Clause c = new ListeningBewiedClause();

		v.addDependentClauseNeg(c);
		if (vPosL.isPure()) {
			fail("Your positive literal thinks that a variable with zero"
					+ " positive dependent clauses is pure. See"
					+ " https://forum.st.cs.uni-saarland.de/boards"
					+ "/viewthread?thread=1519 for the \"reason\".");
		}
		if (!vNegL.isPure()) {
			fail("Your negative literal thinks that a variable with zero"
					+ " positive dependent clauses isn't pure.\nMake sure your"
					+ " .isPure() implementation doesn't dependent on"
					+ " isNegatedLiteral()");
		}

		v.addDependentClausePos(c);
		if (vPosL.isPure() || vNegL.isPure()) {
			fail("Your positive literal thinks that a variable with one"
					+ " positive and one negative dependent clause is pure.");
		}

		v.removeDependentClauseNeg(c);
		if (!vPosL.isPure()) {
			fail("Your positive literal thinks that a variable with zero"
					+ " negative dependent clauses isn't pure. See"
					+ " https://forum.st.cs.uni-saarland.de/boards"
					+ "/viewthread?thread=1519 for the \"reason\".");
		}
		if (vNegL.isPure()) {
			fail("Your negative literal thinks that a variable with zero"
					+ " negative dependent clauses is pure.\nMake sure your"
					+ " .isPure() implementation is independent of"
					+ " isNegatedLiteral()");
		}

		v.removeDependentClausePos(c);
		if (!vPosL.isPure() || !vPosL.isPure()) {
			fail("Your literal this that a variable without any dependent"
					+ " clauses isn't pure.");
		}

		vNegL.addDependentClause(c);
		if (v.isPurePositive()) {
			fail("Your negative literal didn't add a negative dependent"
					+ " clause.\nIf you didn't pass the testVariable"
					+ "Specification(), then check there first.");
		}

		vNegL.removeDependentClause(c);
		vPosL.addDependentClause(c);
		if (v.isPureNegative()) {
			fail("Your negative literal didn't remove the negative dependent"
					+ " clause, or the positive literal didn't add one.\n"
					+ "If you didn't pass the testVariableSpecification(),"
					+ " then check there first.\nAnyway, you really shouldn't"
					+ " use two classes for literals.");
		}
	}

	@Test
	public void testClauseSpecification() {
		final Variable v = createVariable("subject");
		final Variable forPos = createVariable("forPos");
		final Variable forNeg = createVariable("forNeg");
		final Literal vLit = createPositiveLiteral(v);
		final boolean checkBonus2;

		if (!vLit.isPure()) {
			System.out.println("You chose not to do Bonusaufgabe2, or your"
					+ " implementation has gone horribly wrong (CCBT#tCVI).\n"
					+ "That makes sad :-(\nAttention: Checks for Bonus2 are"
					+ " disabled on testClauseVariableInteraction.\n\n");
			checkBonus2 = false;
		} else {
			checkBonus2 = true;
		}

		final Literal lPos = createPositiveLiteral(v);
		final Literal lNeg = createNegativeLiteral(v);
		final Clause cPos = createClause(lPos, createPositiveLiteral(forPos));
		final Clause cNeg = createClause(lNeg, createNegativeLiteral(forNeg));

		// Make sure that "forPos" and "forNeg" never get pure.
		checkUnitAndAddParent(false, forPos);
		checkUnitAndAddParent(false, forNeg);
		checkUnitAndAddParent(true, forPos);
		checkUnitAndAddParent(true, forNeg);

		// If it's pure, then it's wrong.
		// The state of checkBonus2 doesn't matter.
		assertFalse(
				"Your literal (still) thinks that it's pure after being used"
						+ " in a createClause statement. Make sure that Clause"
						+ " informs Variable about being there.\nIf you did not"
						+ " pass testVariableSpecification, check there first.",
				vLit.isPure());

		forPos.setTruthValue(TruthValue.TRUE);
		assertSame(forPos + " didn't update the truth value of " + cPos,
				TruthValue.TRUE, cPos.getLastTruthValue());
		assertNull(cPos + ", where .getTV = TV.TRUE, returns non-null"
				+ " unit-literal", cPos.getUnitClauseLiteral());
		assertNull(cNeg + ", where both literals are unset, returns"
				+ " non-null unit-literal", cNeg.getUnitClauseLiteral());

		if (checkBonus2) {
			assertTrue(v.isPureNegative());
			assertFalse(v.isPurePositive());
			assertSame(null, cPos.getPureLiteral());
			assertSame(lNeg, cNeg.getPureLiteral());
		}

		forPos.setTruthValue(TruthValue.FALSE);
		assertSame(forPos + " didn't update the truth value of " + cPos,
				TruthValue.UNDEFINED, cPos.getLastTruthValue());
		assertSame(cPos + ", where only " + lPos
				+ " is unset, returns wrong unit-literal", lPos,
				cPos.getUnitClauseLiteral());
		assertNull(cNeg + ", where both literals are unset, returns"
				+ " non-null unit-literal", cNeg.getUnitClauseLiteral());

		forPos.setTruthValue(TruthValue.UNDEFINED);
		forNeg.setTruthValue(TruthValue.TRUE);
		assertSame(forNeg + " didn't update the truth value of " + cNeg,
				TruthValue.UNDEFINED, cNeg.getLastTruthValue());
		assertSame(cNeg + ", where only " + lNeg
				+ " is unset, returns wrong unit-literal", lNeg,
				cNeg.getUnitClauseLiteral());
		assertNull(cPos + ", where both literals are unset, returns"
				+ " non-null unit-literal", cPos.getUnitClauseLiteral());

		if (checkBonus2) {
			forNeg.setTruthValue(TruthValue.FALSE);
			assertFalse(v.isPureNegative());
			assertTrue(v.isPurePositive());
			assertSame(lPos, cPos.getPureLiteral());
			assertSame(null, cNeg.getPureLiteral());
		}
	}

	@Test
	public void testCnfUnit() {
		assertUnitIsAny("a", "a");
		assertUnitIsAny("~b", "b");
		assertUnitIsAny("a|b|c", "a", "b", "c");
		assertUnitIsAny("a-b|~b-c|c|d-~e", "c");

		Cnf cnf = TestUtilFelix.parseCompactCnfString("a-c|~a-~b");
		cnf.getVariableForName("a").setTruthValue(TruthValue.TRUE);
		assertUnitIsAny(cnf, "b");
	}

	@Test
	public void testCnfPure() {
		assertPureIsAny("a", "a");
		assertPureIsAny("~b", "b");
		assertPureIsAny("a|b|c", "a", "b", "c");
		assertPureIsAny("a-b|b-~c|c-d|~d-a", "a", "b");

		Cnf cnf = TestUtilFelix.parseCompactCnfString("a-b|~a-~c|~b-c");
		cnf.getVariableForName("a").setTruthValue(TruthValue.TRUE);
		assertPureIsAny(cnf, "b");
	}

	@Test
	public void testCnfSpecification() {
		Cnf cnf = TestUtilFelix.parseCompactCnfString("a-b-~c|~a-b|d");
		assertVariables(cnf, collect("a", "b", "c", "d"));

		List<Variable> variables = new LinkedList<Variable>(cnf.getVariables());

		for (Variable v : variables) {
			v.setTruthValue(TruthValue.TRUE);
		}

		assertSame("After setting some variables, your cnf didn't update"
				+ " its truthvalue.", TruthValue.TRUE, cnf.getTruthValue());

		cnf.resetAllVariables();

		for (Variable v : variables) {
			assertSame("Your cnf.resetAllVariables() implementation forgot to"
					+ " reset the Variable v", TruthValue.UNDEFINED,
					v.getTruthValue());
		}
	}

	@Test
	public void testUnmodifiables() {
		final Variable v = createVariable("qwe");
		final Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);
		final Clause vC = createClause(vPosL, vNegL);
		final Cnf vCnf = createCnfFormula(vC);

		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #0
				v.getParentClauses().add(createClause(vPosL));
			}
		}, new Runnable() {
			public void run() {
				vC.getLiterals().add(
						createPositiveLiteral(createVariable("asd")));
			}
		}, new Runnable() {
			public void run() {
				vC.getVariables().add(createVariable("asd"));
			}
		}, new Runnable() {
			public void run() {
				vCnf.getClauses().add(createClause(vPosL));
			}
		}, new Runnable() {
			public void run() {
				vCnf.getVariables().add(createVariable("asd"));
			}
		} };

		CnfFactoryBewiedTest.checkTestExceptions(tests,
				"CnfContentBewiedTest#testUnmodifiables",
				UnsupportedOperationException.class);
	}

	@Test
	public void testNullPointerException() {
		final Variable v = createVariable("v");
		final Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);
		final Clause vC = createClause(vPosL, vNegL);
		final Cnf vCnf = createCnfFormula(vC);

		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #0
				v.addDependentClauseNeg(null);
			}
		}, new Runnable() {
			public void run() {
				v.addDependentClausePos(null);
			}
		}, new Runnable() {
			public void run() {
				v.removeDependentClauseNeg(null);
			}
		}, new Runnable() {
			public void run() {
				v.removeDependentClausePos(null);
			}
		}, new Runnable() {
			public void run() {
				v.setTruthValue(null);
			}
		}, new Runnable() {
			public void run() { // #5
				vPosL.addDependentClause(null);
			}
		}, new Runnable() {
			public void run() {
				vPosL.addParentClause(null);
			}
		}, new Runnable() {
			public void run() {
				vNegL.addDependentClause(null);
			}
		}, new Runnable() {
			public void run() {
				vNegL.addParentClause(null);
			}
		}, new Runnable() {
			public void run() {
				vNegL.removeDependentClause(null);
			}
		}, new Runnable() {
			public void run() {
				vCnf.getVariableForName(null);
			}
		} };
		CnfFactoryBewiedTest.checkTestExceptions(tests,
				"CnfContentBewiedTest#testNullPointerException",
				NullPointerException.class);
	}

	// ===== ASSERTS and other helpful methods to shorten the text

	public static final void assertVariables(Cnf cnf, Set<String> expected) {
		Collection<Variable> actual = cnf.getVariables();
		Set<String> names = new LinkedHashSet<String>();
		for (Variable v : actual) {
			if (!names.add(v.getName())) {
				fail("Your Cnf " + cnf + " reported to have a duplicate"
						+ " Variable with name " + v.getName());
				// This breaks further testing, therefore this is terminal.
			}
		}
		assertEquals("Your Cnf " + cnf + " reported to have variables of wrong"
				+ " names or a wrong amount thereof.", expected, names);
	}

	public static final void assertUnitIsAny(String compactCnfString,
			String... possibilities) {
		assertUnitIsAny(TestUtilFelix.parseCompactCnfString(compactCnfString),
				possibilities);
	}

	public static final void assertPureIsAny(String compactCnfString,
			String... possibilities) {
		assertPureIsAny(TestUtilFelix.parseCompactCnfString(compactCnfString),
				possibilities);
	}

	public static final void assertUnitIsAny(Cnf cnf, String... possibilities) {
		Literal unit = cnf.getUnitClauseLiteral();
		if (possibilities.length == 0) {
			assertNull("Your CNF " + cnf + " reported a unit-literal"
					+ " although there is none.", unit);
		}
		String possString = Arrays.toString(possibilities);
		assertNotNull("Your CNF " + cnf + " reported a unit-literal"
				+ " although it should be one of: " + possString, unit);
		String varname = unit.getVariable().getName();
		for (String poss : possibilities) {
			if (poss.equals(varname)) {
				return;
			}
		}
		fail("Your CNF " + cnf + " reported the unit-literal " + unit + " ('"
				+ varname + "'), whereas all the actual unit literals are: "
				+ possString);
	}

	public static final void assertPureIsAny(Cnf cnf, String... possibilities) {
		Literal pure = cnf.getPureLiteral();
		if (possibilities.length == 0) {
			assertNull("Your CNF " + cnf + " reported a pure-literal"
					+ " although there is none.", pure);
		}
		String possString = Arrays.toString(possibilities);
		assertNotNull("Your CNF " + cnf + " reported a pure-literal"
				+ " although it should be one of: " + possString, pure);
		String varname = pure.getVariable().getName();
		for (String poss : possibilities) {
			if (poss.equals(varname)) {
				return;
			}
		}
		fail("Your CNF " + cnf + " reported the pure-literal " + pure + " ('"
				+ varname + "'), whereas all the actual pure literals are: "
				+ Arrays.toString(possibilities));
	}

	// ===== COLLECTORS and wrappers

	public Clause clause(Variable[] vars, boolean... negative) {
		final int count = Math.min(vars.length, negative.length);
		Literal[] literals = new Literal[count];
		for (int i = 0; i < count; i++) {
			if (negative[i]) {
				literals[i] = createNegativeLiteral(vars[i]);
			} else {
				literals[i] = createPositiveLiteral(vars[i]);
			}
		}
		return createClause(literals);
	}

	public static final Literal createLiteral(boolean negated, String name) {
		if (negated) {
			return createNegativeLiteral(createVariable(name));
		}
		return createPositiveLiteral(createVariable(name));
	}

	public static final <T> Set<T> collect(T... given) {
		Set<T> ret = new HashSet<T>();
		for (T t : given) {
			ret.add(t);
		}
		return ret;
	}

	// ===== INTERNAL: You shouldn't need those anyway.

	public static class ListeningBewiedClause extends Clause {
		private int truthUpdates = 0;

		public ListeningBewiedClause() {
			super((Literal) null);
		}

		@Override
		public TruthValue getLastTruthValue() {
			return TruthValue.UNDEFINED;
		}

		@Override
		public Collection<Variable> getVariables() {
			fail("This method should not have been called.");
			throw new IllegalStateException();
		}

		@Override
		public Literal getUnitClauseLiteral() {
			fail("This method should not have been called.");
			throw new IllegalStateException();
		}

		@Override
		public void updateTruthValue() {
			truthUpdates++;
		}

		public void assertUpdates(int truthUpdates) {
			if (this.truthUpdates != truthUpdates) {
				fail("The parent clause should have been updated "
						+ truthUpdates
						+ " time(s) by now, but was only updated "
						+ this.truthUpdates + " time(s).");
			}
		}

		@Override
		public Literal getPureLiteral() {
			fail("This method should not have been called.");
			throw new IllegalStateException();
		}
	}

	// This methods has quite some side-effects, which may or may not be of
	// interest. Since "magical" side-effects are bad, this is "private"
	private final void checkUnitAndAddParent(boolean negated, Variable v) {
		Literal l = negated ? createPositiveLiteral(v)
				: createNegativeLiteral(v);
		Clause c = createClause(l);
		assertSame("Your clause " + c + " has exactly one, unset variable. It"
				+ " doesn't notice that it's therefore a unit-literal.", l,
				c.getUnitClauseLiteral());
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("CnfContentBewiedTest", "1.0");
	}
}
