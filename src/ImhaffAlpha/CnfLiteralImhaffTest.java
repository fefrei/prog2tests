package prog2.project3.tests;

// import static prog2.project3.cnf.CnfFactory.*;
// import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static prog2.project3.cnf.CnfFactory.createNegativeLiteral;
import static prog2.project3.cnf.CnfFactory.createPositiveLiteral;
import static prog2.project3.cnf.CnfFactory.createVariable;

import org.junit.Before;
import org.junit.Test;

import prog2.project3.cnf.CnfFactory;
import prog2.project3.cnf.Literal;
import prog2.project3.cnf.TruthValue;
import prog2.project3.cnf.Variable;

public class CnfLiteralImhaffTest {
	private Variable var;
	private Literal posLiteral, negLiteral;

	@Before
	public void setUp() {
		var = createVariable("variable");
		posLiteral = createPositiveLiteral(var);
		negLiteral = createNegativeLiteral(var);
	}

	@Test
	public void testConstructor() {
		try {
			createPositiveLiteral(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		try {
			createNegativeLiteral(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		assertTrue(posLiteral.isPure());
	}

	@Test
	public void testNegativeLiteralInit() {
		assertEquals(TruthValue.UNDEFINED, negLiteral.getTruthValue());

		assertEquals(var, negLiteral.getVariable());

		assertEquals(true, negLiteral.isNegatedLiteral());

		assertTrue(negLiteral.isPure());
	}

	@Test
	public void testNegativeLiteralAssignment() {
		negLiteral.chooseSatisfyingAssignment();
		assertEquals(TruthValue.FALSE, var.getTruthValue());
	}

	@Test
	public void testParentClause() {
		try {
			posLiteral.addParentClause(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		try {
			negLiteral.addParentClause(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		posLiteral.addParentClause(CnfFactory.createClause(posLiteral));
		negLiteral.addParentClause(CnfFactory.createClause(negLiteral));

		posLiteral.addDependentClause(CnfFactory.createClause(posLiteral));
		negLiteral.addDependentClause(CnfFactory.createClause(negLiteral));

		posLiteral.removeDependentClause(CnfFactory.createClause(posLiteral));
		negLiteral.removeDependentClause(CnfFactory.createClause(negLiteral));
	}

	@Test
	public void testPosLiteralIsPure() {
		assertTrue(posLiteral.isPure());
		var.addDependentClausePos(CnfFactory.createClause(posLiteral));
		assertTrue(posLiteral.isPure());
		var.addDependentClauseNeg(CnfFactory.createClause(posLiteral));
		assertFalse(posLiteral.isPure());
	}

	@Test
	public void testNegLiteralIsPure() {
		assertTrue(negLiteral.isPure());
		var.addDependentClauseNeg(CnfFactory.createClause(negLiteral));
		assertTrue(negLiteral.isPure());
		var.addDependentClausePos(CnfFactory.createClause(negLiteral));
		assertFalse(negLiteral.isPure());
	}
}
