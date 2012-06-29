package prog2.project3.tests;

//import static prog2.project3.cnf.CnfFactory.*;
//import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static prog2.project3.cnf.CnfFactory.createVariable;

import org.junit.Before;
import org.junit.Test;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.CnfFactory;
import prog2.project3.cnf.TruthValue;
import prog2.project3.cnf.Variable;

public class CnfVariableImhaffTest {
	private Variable var;
	private Clause c;

	@Before
	public void setUp() {
		var = CnfFactory.createVariable("halloWelt");
		c = CnfFactory.createClause(CnfFactory.createPositiveLiteral(var));
	}

	@Test
	public void testConstructor() {
		try {
			createVariable(null);
			fail("NullPointerException expected");
		} catch (NullPointerException ex) {
			// that's what we expected
		}
	}

	@Test
	public void testInit() {
		assertEquals("halloWelt", var.getName());
		assertEquals(TruthValue.UNDEFINED, var.getTruthValue());
		assertEquals(0, var.getParentClauses().size());
		assertEquals(true, var.isPurePositive());
		assertEquals(true, var.isPureNegative());
	}

	@Test
	public void testSetTruthValue() {
		try {
			var.setTruthValue(null);
			fail("no exception has been thrown");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		var.setTruthValue(TruthValue.TRUE);
		assertEquals(TruthValue.TRUE, var.getTruthValue());
		var.setTruthValue(TruthValue.FALSE);
		assertEquals(TruthValue.FALSE, var.getTruthValue());
		var.setTruthValue(TruthValue.UNDEFINED);
		assertEquals(TruthValue.UNDEFINED, var.getTruthValue());
	}

	@Test
	public void testNegateValue() {
		var.setTruthValue(TruthValue.UNDEFINED);
		var.negateValue();
		assertEquals(TruthValue.UNDEFINED, var.getTruthValue());

		var.setTruthValue(TruthValue.TRUE);
		var.negateValue();
		assertEquals(TruthValue.FALSE, var.getTruthValue());

		var.setTruthValue(TruthValue.FALSE);
		var.negateValue();
		assertEquals(TruthValue.TRUE, var.getTruthValue());
	}

	@Test
	public void testParentClause() {
		try {
			var.addParentClause(null);
			fail("no exception has been thrown");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		var.addParentClause(c);
		assertEquals(1, var.getParentClauses().size());
		assertTrue(var.getParentClauses().contains(c));
	}

	@Test
	public void testDependentClause() {
		try {
			var.addDependentClausePos(null);
			fail("no exception has been thrown");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		var.addDependentClausePos(c);

		try {
			var.addDependentClauseNeg(null);
			fail("no exception has been thrown");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		var.addDependentClauseNeg(c);

		try {
			var.removeDependentClausePos(null);
			fail("no exception has been thrown");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		var.removeDependentClausePos(c);

		try {
			var.removeDependentClauseNeg(null);
			fail("no exception has been thrown");
		} catch (NullPointerException ex) {
			// that's what we expected
		}

		var.removeDependentClauseNeg(c);
	}

	@Test
	public void testIsPurePositive() {
		var.addDependentClausePos(c);
		assertTrue(var.isPurePositive());
		assertFalse(var.isPureNegative());

		// fail("need to implement initial state test");
	}

	@Test
	public void testIsPureNegative() {
		var.addDependentClauseNeg(c);
		assertTrue(var.isPureNegative());
		assertFalse(var.isPurePositive());

		// fail("need to implement initial state test");
	}

}
