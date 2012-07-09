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
import prog2.project3.dpll.DPLLAlgorithm;
import prog2.project3.propositional.VariableNameGenerator;

public class CnfContentBewiedTest {
	private static final boolean STRICT = true;

	// ===== TESTS themselves
	// Federal regulations require me to warn you that this next test
	// file... is looking pretty good. Finally.

	// SPEC: comments mark lines, that talk about the specification
	// According to Tobias, there's something not okay.

	@Test
	// SPEC: Analyzation Complete
	public void testUniqueness() {
		String name = VariableNameGenerator.getVariableName();
		Variable v = createVariable(name);
		Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);
		Clause vC = createClause(vPosL, vNegL);

		// SPEC: Granted
		// a) CnfClausePublicTest#testClauseIllegal already tests this.
		// b) 22. June 2012, 22:57:58 Tobias_Frey: Zu der Factory; die Factory
		// merkt sich gar nichts und gibt jedes Mal ein neues Objekt zurück.
		if (v == createVariable(name)) {
			fail("You must not cache the results of CnfFactory"
					+ ".createVariable().\nIn other words: Always create a new"
					+ " Variable object in CnfFactory.createVariable()");
		}
		// SPEC: Not required
		// Because: Assumption on specification.
		// => Do not test.
		if (STRICT) {
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
	}

	@Test
	// SPEC: Analyzation Complete
	public void testVariableSpecification() {
		final Variable v = createVariable(VariableNameGenerator
				.getVariableName());
		final Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);
		final Clause vC = createClause(vPosL, vNegL);

		final ListeningBewiedClause c = new ListeningBewiedClause();

		// SPEC: Granted.
		assertEquals("Your Variable reports wrong parent clauses.",
				collect(vC), v.getParentClauses());

		// SPEC: Granted.
		v.addParentClause(c);
		if (!v.getParentClauses().contains(c)
				|| v.getParentClauses().size() != 2) {
			assertEquals("Your Variable didn't update it's parent clauses.",
					collect(vC, c), v);

		}

		// SPEC: Granted.
		// https://forum.st.cs.uni-saarland.de/boards/viewthread?thread=1531&lastpage=yes#8509
		assertSame("You must initialize your Variables as UNDEFINED, see h"
				+ "ttps://forum.st.cs.uni-saarland.de/boards/viewthread?th"
				+ "read=1531&lastpage=yes#8509 for details.",
				TruthValue.UNDEFINED, v.getTruthValue());
		c.reset();

		v.setTruthValue(TruthValue.TRUE);
		// SPEC: Granted.
		assertSame("Your variable didn't update it's truthvalue.",
				TruthValue.TRUE, v.getTruthValue());
		// SPEC: Granted.
		c.assertUpdates();

		v.negateValue();
		// SPEC: Granted.
		assertSame("Your variable didn't negate it's truthvalue.",
				TruthValue.FALSE, v.getTruthValue());
		// SPEC: Granted.
		c.assertUpdates();

		v.setTruthValue(TruthValue.UNDEFINED);
		// SPEC: Granted.
		assertSame(TruthValue.UNDEFINED, v.getTruthValue());
		// SPEC: Granted.
		c.assertUpdates();

		// SPEC: Granted.
		if (v.isPureNegative() || v.isPurePositive()) {
			fail("Your variable (which has positive and negative dependent"
					+ " clauses) reported that it's pure.\n"
					+ "It isn't pure! This doesn't have anything to do whether"
					+ " you chose to do Bonusaufgabe2 or not.\n"
					+ "If you chose not to do Bonusaufgabe2, you should return"
					+ " false.");
		}

		Variable fresh = createVariable("fresh");

		// SPEC: Granted.
		if (!fresh.isPurePositive() || !fresh.isPureNegative()) {
			System.out.println("You chose not to do Bonusaufgabe2, or your"
					+ " implementation has gone horribly wrong"
					+ " (in testVariableSpecification()).\n"
					+ "That makes me sad :-(\n");
			return;
		}

		// End of first part

		fresh.addDependentClauseNeg(c);
		// SPEC: Granted.
		if (fresh.isPurePositive()) {
			fail("Your variable with one negative dependent clause reported"
					+ " that it isPurePositive. It isn't!");
		}
		// SPEC: Granted.
		if (!fresh.isPureNegative()) {
			fail("Your variable with zero positive dependent clauses reported"
					+ " that it isn't PureNegative. But it is!");
		}

		fresh.addDependentClausePos(c);
		// SPEC: Granted.
		if (fresh.isPureNegative() || fresh.isPurePositive()) {
			fail("Your variable (which has positive and negative dependent"
					+ " clauses) reported that it's pure.\n"
					+ "Your implementation is broken. I can't imagine how you"
					+ " managed to get to this if-block.");
		}

		fresh.removeDependentClauseNeg(c);
		// SPEC: Granted.
		if (fresh.isPureNegative()) {
			fail("Your variable with one positive dependent clause reported"
					+ " that it isPureNegative after deleting the negative one."
					+ " It isn't!");
		}
		// SPEC: Granted.
		if (!fresh.isPurePositive()) {
			fail("Your variable with zero negative dependent clauses reported"
					+ " that it isn't PurePositive after deleting the negative"
					+ " one. But it is!");
		}

		fresh.removeDependentClausePos(c);
		// SPEC: Granted.
		if (!fresh.isPurePositive() || !fresh.isPureNegative()) {
			fail("Your implementation didn't recognize that after deleting"
					+ " every dependent clause, it should be 'fully' pure.");
		}
	}

	@Test
	// SPEC: Analyzation Complete
	public void testLiteralSpecification() {
		final Variable v = createVariable("foo");
		final Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);

		v.setTruthValue(TruthValue.UNDEFINED);

		// SPEC: Granted.
		if (vPosL.isNegatedLiteral() || !vNegL.isNegatedLiteral()) {
			fail("Your Literals didn't implement isNegatedLiteral()"
					+ " the right way.");
		}

		// SPEC: Granted.
		assertSame(vPosL.getVariable(), v);
		// SPEC: Granted.
		assertSame(vNegL.getVariable(), v);
		{
			Variable phony = new CnfFactoryBewiedTest.PhonyBewiedVariable("asd");
			// SPEC: Granted.
			assertSame(createPositiveLiteral(phony).getVariable(), phony);
		}

		vPosL.chooseSatisfyingAssignment();
		// SPEC: Granted.
		assertSame("Positive Literal didn't set TruthValue correctly",
				TruthValue.TRUE, v.getTruthValue());
		// SPEC: Granted.
		assertSame("Literal doesn't forward call to getTruthValue",
				TruthValue.TRUE, vPosL.getTruthValue());
		// SPEC: Granted.
		assertSame("Negative literal forgot to invert truth value",
				TruthValue.FALSE, vNegL.getTruthValue());

		v.setTruthValue(TruthValue.UNDEFINED);
		// SPEC: Granted.
		assertSame("Literal doesn't forward call to getTruthValue",
				TruthValue.UNDEFINED, vPosL.getTruthValue());
		// SPEC: Granted.
		assertSame("Literal doesn't forward call to getTruthValue",
				TruthValue.UNDEFINED, vNegL.getTruthValue());

		vNegL.chooseSatisfyingAssignment();
		// SPEC: Granted.
		assertSame("Negative Literal didn't set TruthValue correctly",
				TruthValue.FALSE, v.getTruthValue());
		// SPEC: Granted.
		assertSame("Literal doesn't forward call to getTruthValue",
				TruthValue.FALSE, vPosL.getTruthValue());
		// SPEC: Granted.
		assertSame("Negative literal forgot to invert truth value",
				TruthValue.TRUE, vNegL.getTruthValue());

		// SPEC: Granted.
		try {
			vPosL.chooseSatisfyingAssignment();
			fail("A literal may not choose a new satisfying assignment when"
					+ " the variable is something different than UNDEFINED"
					+ " (in this case, it is FALSE).");
		} catch (IllegalStateException e) {
			// Expected this
		}
		// SPEC: Granted.
		try {
			vNegL.chooseSatisfyingAssignment();
			fail("A literal may not choose a new satisfying assignment when"
					+ " the variable is something different than UNDEFINED"
					+ " (in this case, it is FALSE).\nThis seems to only"
					+ " happen with your negative literal.");
		} catch (IllegalStateException e) {
			// Expected this
		}

		v.setTruthValue(TruthValue.UNDEFINED);
		// SPEC: Granted.
		assertSame(TruthValue.UNDEFINED, v.getTruthValue());

		// SPEC: Granted.
		if (!v.isPurePositive() || !v.isPureNegative()) {
			System.out
					.println("You chose not to do Bonusaufgabe2, or your"
							+ " implementation has gone horribly wrong (in testLiteralSpecification).\n"
							+ "That makes me sad :-(\n");
			return;
		}

		final Clause c = new ListeningBewiedClause();

		v.addDependentClauseNeg(c);
		// SPEC: Granted
		// Discussion with Plavix on 27. June:
		// (17:56:32) bewied: Außerdem: Testet Literal.isPure() in Abhängigkeit
		// von isNegated()?
		// (18:00:35) Plavix: wuerde sinn machen
		if (vPosL.isPure()) {
			fail("Your positive literal thinks that a variable with zero"
					+ " positive dependent clauses is pure. See"
					+ " https://forum.st.cs.uni-saarland.de/boards"
					+ "/viewthread?thread=1519 for the \"reason\".");
		}
		// SPEC: Granted
		// (see above)
		if (!vNegL.isPure()) {
			fail("Your negative literal thinks that a variable with zero"
					+ " positive dependent clauses isn't pure.\nMake sure your"
					+ " .isPure() implementation doesn't dependent on"
					+ " isNegatedLiteral()");
		}

		v.addDependentClausePos(c);
		// SPEC: Granted
		if (vPosL.isPure() || vNegL.isPure()) {
			fail("Your positive literal thinks that a variable with one"
					+ " positive and one negative dependent clause is pure.");
		}

		v.removeDependentClauseNeg(c);
		// SPEC: Granted
		// (see above)
		if (!vPosL.isPure()) {
			fail("Your positive literal thinks that a variable with zero"
					+ " negative dependent clauses isn't pure. See"
					+ " https://forum.st.cs.uni-saarland.de/boards"
					+ "/viewthread?thread=1519 for the \"reason\".");
		}
		// SPEC: Granted
		// (see above)
		if (vNegL.isPure()) {
			fail("Your negative literal thinks that a variable with zero"
					+ " negative dependent clauses is pure.\nMake sure your"
					+ " .isPure() implementation is independent of"
					+ " isNegatedLiteral()");
		}

		v.removeDependentClausePos(c);
		// SPEC: Granted
		if (!vPosL.isPure() || !vPosL.isPure()) {
			fail("Your literal this that a variable without any dependent"
					+ " clauses isn't pure.");
		}

		vNegL.addDependentClause(c);
		// SPEC: Granted
		if (v.isPurePositive()) {
			fail("Your negative literal didn't add a negative dependent"
					+ " clause.\nIf you didn't pass the testVariable"
					+ "Specification(), then check there first.");
		}

		vNegL.removeDependentClause(c);
		vPosL.addDependentClause(c);
		// SPEC: Granted
		if (v.isPureNegative()) {
			fail("Your negative literal didn't remove the negative dependent"
					+ " clause, or the positive literal didn't add one.\n"
					+ "If you didn't pass the testVariableSpecification(),"
					+ " then check there first.\nAnyway, you really shouldn't"
					+ " use two classes for literals.");
		}
	}

	@Test
	// SPEC: Analyzation Complete
	public void testClauseTruthValue() {
		final Variable a = createVariable("a");
		final Variable b = createVariable("b");
		final Literal aPos = createPositiveLiteral(a);
		final Literal bNeg = createNegativeLiteral(b);
		final Clause clauseA = createClause(aPos);
		final Clause clauseA_NB = createClause(aPos, bNeg);

		a.setTruthValue(TruthValue.TRUE);
		b.setTruthValue(TruthValue.UNDEFINED);
		// SPEC: Granted
		assertSame("Your Variable.setTruthValue() didn't correctly update the"
				+ " clause (a), where a is TRUE.", TruthValue.TRUE,
				clauseA.getLastTruthValue());
		// SPEC: Granted
		assertSame("Your Variable.setTruthValue() didn't correctly update the"
				+ " clause (a \\/ ~b), where a is TRUE and b is UNDEFINED.",
				TruthValue.TRUE, clauseA_NB.getLastTruthValue());

		b.setTruthValue(TruthValue.TRUE);
		// SPEC: Granted
		assertSame("The clause (a \\/ ~b), where a and b are TRUE, should be"
				+ " (still) TRUE.", TruthValue.TRUE,
				clauseA_NB.getLastTruthValue());

		a.setTruthValue(TruthValue.FALSE);
		// SPEC: Granted
		assertSame("Your Variable.setTruthValue() didn't correctly update the"
				+ " clause (a), where a is FALSE.", TruthValue.FALSE,
				clauseA.getLastTruthValue());
		// SPEC: Granted
		assertSame("Your Variable.setTruthValue() didn't correctly update the"
				+ " clause (a \\/ ~b), where a is FALSE and b is TRUE.",
				TruthValue.FALSE, clauseA_NB.getLastTruthValue());

		b.setTruthValue(TruthValue.UNDEFINED);
		// SPEC: Granted
		assertSame("Your Variable.setTruthValue() didn't correctly update the"
				+ " clause (a \\/ ~b), where a is FALSE and b is UNDEFINED.",
				TruthValue.UNDEFINED, clauseA_NB.getLastTruthValue());
	}

	@Test
	// SPEC: Analyzation Complete
	public void testClauseSpecification() {
		final Variable subject = createVariable("subject");
		final Variable forPos = createVariable("forPos");
		final Variable forNeg = createVariable("forNeg");
		final Literal subjectPosLit = createPositiveLiteral(subject);
		final boolean checkBonus2;

		subject.setTruthValue(TruthValue.UNDEFINED);
		forPos.setTruthValue(TruthValue.UNDEFINED);
		forNeg.setTruthValue(TruthValue.UNDEFINED);

		// SPEC: Granted
		if (!subjectPosLit.isPure()) {
			System.out.println("You chose not to do Bonusaufgabe2, or your"
					+ " implementation has gone horribly wrong"
					+ " (in testClauseSpecification).\n"
					+ "That makes me sad :-(\n");
			checkBonus2 = false;
		} else {
			checkBonus2 = true;
		}

		final Literal subjectNegLit = createNegativeLiteral(subject);
		final Clause clausePos = createClause(subjectPosLit,
				createPositiveLiteral(forPos));
		final Clause clauseNeg = createClause(subjectNegLit,
				createNegativeLiteral(forNeg));

		// Make sure that "forPos" and "forNeg" never get pure.
		// This creates clauses that contain the given Variable.
		// SPEC: See checkUnitAndAddParent()
		checkUnitAndAddParent(false, forPos);
		checkUnitAndAddParent(false, forNeg);
		checkUnitAndAddParent(true, forPos);
		checkUnitAndAddParent(true, forNeg);

		// If it's pure, then it's wrong.
		// The state of checkBonus2 doesn't matter.
		// SPEC: Granted
		assertFalse("Your literal (still) thinks that it's pure after being"
				+ " used in a createClause statement. Make sure that Clause"
				+ " informs Variable about being there.\nIf you did not pass"
				+ " testVariableSpecification, check there first.\n"
				+ "Also note that isPure() says: return false if you don't"
				+ " want to implement it.", subjectPosLit.isPure());

		// We are testing with the following clauses "existing":
		// forPos
		// ~forPos
		// forNeg
		// ~forNeg
		// subject, forPos
		// ~subject, ~forNeg
		// The creation of these clauses should have had the effect that neither
		// forPos nor forNeg ever get "pure".

		forPos.setTruthValue(TruthValue.TRUE);
		// clausePos = (subject \/ forPos), forPos = true, subject = undef
		// => no unit here
		// clauseNeg = (~subject \/ forNeg), forNeg = undef, subject = undef
		// => no unit here
		// => subject is pure negative

		// SPEC: Granted
		assertSame(forPos + " didn't update the truth value of " + clausePos,
				TruthValue.TRUE, clausePos.getLastTruthValue());
		// SPEC: Granted
		assertNull(clausePos + ", where .getTV = TV.TRUE, returns non-null"
				+ " unit-literal", clausePos.getUnitClauseLiteral());
		// SPEC: Granted
		assertNull(clauseNeg + ", where both literals are unset, returns"
				+ " non-null unit-literal", clauseNeg.getUnitClauseLiteral());

		if (checkBonus2) {
			// SPEC: Granted
			// https://forum.st.cs.uni-saarland.de/boards/viewthread?thread=1529&lastpage=yes#8510
			assertTrue("subject is UNDEFINED and forPos is TRUE, therefore"
					+ " (subject \\/ forPos) shouldn't hinder the variable"
					+ " from becoming pure negative. See https://forum.st."
					+ "cs.uni-saarland.de/boards/viewthread?thread=1529&la"
					+ "stpage=yes#8510 for details.", subject.isPureNegative());
			// SPEC: Granted
			assertFalse("There is a clause (~subject \\/ ~forNeg), and both"
					+ " are TruthValue.UNDEFINED. Ergo: subject is NOT pure"
					+ " positive", subject.isPurePositive());
			// SPEC: Not required
			if (STRICT) {
				assertNull(
						"(subject \\/ forPos) evaluates to TRUE, since forPos"
								+ " already is set. For best optimization, skip clauses"
								+ " that already have a truthvalue assigned.",
						clausePos.getPureLiteral());
			}
			// SPEC: Granted
			assertSame("There is a clause (~subject \\/ ~forNeg), and both"
					+ " are TruthValue.UNDEFINED. Ergo: subject is NOT a"
					+ " pure literal", subject, clauseNeg.getPureLiteral()
					.getVariable());
		}

		forPos.setTruthValue(TruthValue.FALSE);
		// SPEC: Granted
		assertSame(forPos + " didn't update the truth value of " + clausePos,
				TruthValue.UNDEFINED, clausePos.getLastTruthValue());
		// SPEC: Granted
		assertSame(clausePos + ", where only subject is unset, returns wrong"
				+ " unit-literal", subjectPosLit,
				clausePos.getUnitClauseLiteral());
		// SPEC: Granted
		assertNull(clauseNeg + ", where both literals are unset, returns"
				+ " non-null unit-literal", clauseNeg.getUnitClauseLiteral());

		forPos.setTruthValue(TruthValue.UNDEFINED);
		forNeg.setTruthValue(TruthValue.TRUE);
		// SPEC: Granted
		assertSame(forNeg + " didn't update the truth value of " + clauseNeg,
				TruthValue.UNDEFINED, clauseNeg.getLastTruthValue());
		// SPEC: Granted
		assertSame(clauseNeg + ", where only subject is unset, returns a"
				+ " wrong unit-literal", subjectNegLit,
				clauseNeg.getUnitClauseLiteral());
		// SPEC: Granted
		assertNull(clausePos + ", where both literals are unset, returns"
				+ " non-null unit-literal", clausePos.getUnitClauseLiteral());

		if (checkBonus2) {
			// SPEC: Granted
			assertFalse("Although forNeg is TRUE, subject still isn't"
					+ " pureNegative, since (~subject \\/ ~forNeg) still isn't"
					+ " determined. Your implementation thought otherwise.",
					subject.isPureNegative());
			// SPEC: Granted
			assertFalse("(~subject \\/ ~forNeg), where forNeg is TRUE, should"
					+ " prevent subject from becoming purePositive",
					subject.isPurePositive());
			forNeg.setTruthValue(TruthValue.FALSE);
			// SPEC: Granted
			assertFalse("There is (subject \\/ forPos), and neither of the"
					+ " variables is set. Therefore, subject cannot be"
					+ " pureNegative.", subject.isPureNegative());
			// SPEC: Granted
			assertTrue("(~subject \\/ ~forNeg), where forNeg is FALSE,"
					+ " shouldn't prevent subject from becoming"
					+ " purePositive", subject.isPurePositive());
			// SPEC: Not required
			if (STRICT) {
				assertNull("(~subject \\/ ~forNeg) evaluates to TRUE, since"
						+ " forNeg already is set to FALSE. For best"
						+ " optimization, skip clauses that already have a"
						+ " truthvalue assigned.", clauseNeg.getPureLiteral());
			}
			// SPEC: Granted
			assertSame("subject is now purePositive, so it should be returned"
					+ " by getPureLiteral.", subjectPosLit,
					clausePos.getPureLiteral());
		}
	}

	@Test
	// SPEC: Analyzation Complete
	public void testCnfUnit() {
		assertUnitIsAny("a", "a");
		assertUnitIsAny("~b", "b");
		assertUnitIsAny("a|b|c", "a", "b", "c");
		assertUnitIsAny("a-b|~b-c|c|d-~e", "c");
		assertUnitIsAny("a-b|b-~c");
		assertUnitIsAny("~a-~b-~c-~d-~e");

		Cnf cnf = TestUtilFelix.parseCompactCnfString("a-c|~a-~b");
		cnf.getVariableForName("a").setTruthValue(TruthValue.TRUE);
		// SPEC: Granted
		assertUnitIsAny(cnf, "b");
	}

	@Test
	// SPEC: Analyzation Complete
	public void testCnfPure() {
		if (!createVariable("foobar").isPureNegative()) {
			System.out.println("You chose not to do Bonusaufgabe2, or your"
					+ " implementation has gone horribly wrong"
					+ " (in testCnfPure).\n" + "That makes me sad :-(\n");
		}

		assertPureIsAny("a", "a");
		assertPureIsAny("~b", "b");
		assertPureIsAny("a|b|c", "a", "b", "c");
		assertPureIsAny("a-b|b-~c|c-d|~d-a", "a", "b");
		assertPureIsAny("aa-~aa");
		assertPureIsAny("aa|~aa");

		Cnf cnf = TestUtilFelix.parseCompactCnfString("a-b|~a-~c|~b-c");
		cnf.getVariableForName("a").setTruthValue(TruthValue.TRUE);
		// SPEC: Granted, see:
		// https://forum.st.cs.uni-saarland.de/boards/viewthread?thread=1529&lastpage=yes#8510
		assertPureIsAny(cnf, "b");
	}

	@Test
	// SPEC: Analyzation Complete
	public void testCnfSpecification() {
		Cnf cnf = TestUtilFelix.parseCompactCnfString("a-b-~c|~a-b|d");
		// SPEC: Granted
		assertVariables(cnf, collect("a", "b", "c", "d"));

		List<Variable> variables = new LinkedList<Variable>(cnf.getVariables());

		for (Variable v : variables) {
			v.setTruthValue(TruthValue.TRUE);
		}

		// SPEC: Granted
		assertSame("After setting some variables, your cnf didn't update"
				+ " its truthvalue.", TruthValue.TRUE, cnf.getTruthValue());

		cnf.resetAllVariables();

		for (Variable v : variables) {
			// SPEC: Granted (each)
			assertSame("Your cnf.resetAllVariables() implementation forgot to"
					+ " reset the Variable v", TruthValue.UNDEFINED,
					v.getTruthValue());
		}
	}

	@Test
	// SPEC: Analyzation Complete
	public void testUnmodifiables() {
		final Variable v = createVariable("qwe");
		final Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);
		final Clause vC = createClause(vPosL, vNegL);
		final Cnf vCnf = createCnfFormula(vC);

		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #1
				// SPEC: Not required
				if (STRICT) {
					v.getParentClauses().add(createClause(vPosL));
				}
			}
		}, new Runnable() {
			public void run() { // #2
				// SPEC: Granted.
				vC.getLiterals().add(
						createPositiveLiteral(createVariable("asd")));
			}
		}, new Runnable() {
				// SPEC: Granted.
			public void run() { // #3
				vC.getVariables().add(createVariable("asd"));
			}
		}, new Runnable() {
				// SPEC: Granted.
			public void run() { // #4
				vCnf.getClauses().add(createClause(vPosL));
			}
		}, new Runnable() {
			public void run() { // #5
				// SPEC: Not required
				// => DO NOT TEST
				if (STRICT) {
					vCnf.getVariables().add(createVariable("asd"));
				}
			}
		} };

		CnfFactoryBewiedTest.checkTestExceptions(tests,
				"CnfContentBewiedTest#testUnmodifiables",
				". You can lookup details of a specific sub-test-number in "
						+ "CnfContentBewiedTest#testUnmodifiables for details",
				UnsupportedOperationException.class);
	}

	@Test
	// SPEC: Analyzation Complete
	public void testNullPointerException() {
		final Variable v = createVariable("v");
		final Literal vPosL = createPositiveLiteral(v), vNegL = createNegativeLiteral(v);
		final Clause vC = createClause(vPosL, vNegL);
		final Cnf vCnf = createCnfFormula(vC);

		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #1
				// SPEC: Granted.
				v.addDependentClauseNeg(null);
			}
		}, new Runnable() {
			public void run() { // #2
				// SPEC: Granted.
				v.addDependentClausePos(null);
			}
		}, new Runnable() {
			public void run() { // #3
				// SPEC: Granted.
				v.removeDependentClauseNeg(null);
			}
		}, new Runnable() {
			public void run() { // #4
				// SPEC: Granted.
				v.removeDependentClausePos(null);
			}
		}, new Runnable() {
			public void run() { // #5
				// SPEC: Granted.
				v.setTruthValue(null);
			}
		}, new Runnable() {
			public void run() { // #6
				// SPEC: Granted.
				vPosL.addDependentClause(null);
			}
		}, new Runnable() {
			public void run() { // #7
				// SPEC: Granted.
				vPosL.addParentClause(null);
			}
		}, new Runnable() {
			public void run() { // #8
				// SPEC: Granted.
				vNegL.addDependentClause(null);
			}
		}, new Runnable() {
			public void run() { // #9
				// SPEC: Granted.
				vNegL.addParentClause(null);
			}
		}, new Runnable() {
			public void run() { // #10
				// SPEC: Granted.
				vNegL.removeDependentClause(null);
			}
		}, new Runnable() {
			public void run() { // #11
				// SPEC: Granted.
				vCnf.getVariableForName(null);
			}
		}, new Runnable() {
			public void run() { // #12
				// SPEC: Granted.
				new DPLLAlgorithm(null);
			}
		}, new Runnable() {
			public void run() { // #13
				// SPEC: Granted.
				v.addParentClause(null);
			}
		} };
		CnfFactoryBewiedTest.checkTestExceptions(tests,
				"CnfContentBewiedTest#testNullPointerException",
				". You can lookup details of a specific sub-test-number in "
						+ "CnfContentBewiedTest#testNullPointerException"
						+ " for details", NullPointerException.class);
	}

	// ===== ASSERTS and other helpful methods to shorten the text

	/**
	 * Checks that a given cnf contains exactly the variables whose name is
	 * given.<br>
	 * Note that this is a rough integrity test, and especially does not check
	 * for cnf correctness.
	 * 
	 * @param expected
	 *            The expected set (literally) of variable names which may
	 *            occur.
	 * @param cnf
	 *            The Cnf whose variables should be checked
	 */
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
		} else {
			String possString = Arrays.toString(possibilities);
			assertNotNull("Your CNF " + cnf + " reported no unit-literal"
					+ " instead of one of: " + possString, unit);
			String varname = unit.getVariable().getName();
			for (String poss : possibilities) {
				if (poss.equals(varname)) {
					return;
				}
			}
			fail("Your CNF " + cnf + " reported the unit-literal " + unit
					+ " ('" + varname
					+ "'), whereas all the actual unit literals are: "
					+ possString);
		}
	}

	public static final void assertPureIsAny(Cnf cnf, String... possibilities) {
		Literal pure = cnf.getPureLiteral();
		if (possibilities.length == 0) {
			assertNull("Your CNF " + cnf + " reported a pure-literal"
					+ " although there is none.", pure);
		} else {
			String possString = Arrays.toString(possibilities);
			assertNotNull("Your CNF " + cnf + " reported no pure-literal"
					+ " instead of one of: " + possString, pure);
			String varname = pure.getVariable().getName();
			for (String poss : possibilities) {
				if (poss.equals(varname)) {
					return;
				}
			}
			fail("Your CNF " + cnf + " reported the pure-literal " + pure
					+ " ('" + varname
					+ "'), whereas all the actual pure literals are: "
					+ Arrays.toString(possibilities));
		}
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

	private static class ListeningBewiedClause extends Clause {
		private int truthUpdates = 0;

		public ListeningBewiedClause() {
			super(createPositiveLiteral(createVariable(VariableNameGenerator
					.getVariableName())));
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

		public void assertUpdates() {
			assertTrue("The parent clause should have been updated at least"
					+ " once by now, but was only updated " + this.truthUpdates
					+ " time(s).", truthUpdates > 0);
		}

		public void reset() {
			truthUpdates = 0;
		}

		@Override
		public Literal getPureLiteral() {
			fail("This method should not have been called.");
			throw new IllegalStateException();
		}
	}

	/**
	 * This methods creates a clause from this variable, negating it if wanted.
	 * Since this kind of "magical" side-effects are bad, this is "private"
	 * 
	 * @param negated
	 *            Whether the variable should be wrapped in a negated literal
	 *            (or, if false, positive literal)
	 * @param v
	 *            The Variable instance to be hooked
	 */
	private final void checkUnitAndAddParent(boolean negated, Variable v) {
		Literal l = negated ? createPositiveLiteral(v)
				: createNegativeLiteral(v);
		Clause c = createClause(l);
		// SPEC: Granted
		assertSame("Your clause " + c + " has exactly one, unset variable ("
				+ v + "). But it doesn't notice that it's a unit-literal.", l,
				c.getUnitClauseLiteral());
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("CnfContentBewiedTest", "1.3.3");
	}
}
