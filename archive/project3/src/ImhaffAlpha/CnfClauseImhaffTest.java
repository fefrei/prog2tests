package prog2.project3.tests;

// import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.CnfFactory;
import prog2.project3.cnf.Literal;
import prog2.project3.cnf.Variable;

public class CnfClauseImhaffTest {
	private Variable varA;
	private Variable varB;
	private Variable varC;
	private Literal litA;
	private Literal litNegB;
	private Literal litC;
	private Clause clause;

	@Before
	public void setUp() {
		varA = CnfFactory.createVariable("a");
		varB = CnfFactory.createVariable("b");
		varC = CnfFactory.createVariable("c");
		litA = CnfFactory.createPositiveLiteral(varA);
		litNegB = CnfFactory.createNegativeLiteral(varB);
		litC = CnfFactory.createPositiveLiteral(varC);
		// (a \/ !b \/ c)
		clause = CnfFactory.createClause(litA, litNegB, litC);
	}

	@Test
	public void testUnitClauseLiteral() {
		assertNull(clause + " has three unset variables.",
				clause.getUnitClauseLiteral());
		litA.chooseSatisfyingAssignment();
		assertNull(clause + " has two unset variables.",
				clause.getUnitClauseLiteral());
		litNegB.chooseSatisfyingAssignment();
		assertNull(clause + " is already determined via litA to"
				+ " TruthValue.TRUE.", clause.getUnitClauseLiteral());
	}

	@Test
	public void testPureLiteral() {
		varA.addDependentClausePos(clause);
		varA.addDependentClauseNeg(clause);
		varC.addDependentClausePos(clause);
		varC.addDependentClauseNeg(clause);

		assertEquals(litNegB, clause.getPureLiteral());
		varB.addDependentClauseNeg(clause);
		assertEquals(litNegB, clause.getPureLiteral());
		varB.addDependentClausePos(clause);
		assertEquals(null, clause.getPureLiteral());
	}
}
