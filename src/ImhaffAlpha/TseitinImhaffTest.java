package prog2.project3.tests;

//import static prog2.project3.cnf.CnfFactory.*;
//import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;
import static prog2.project3.cnf.CnfFactory.createClause;
import static prog2.project3.cnf.CnfFactory.createCnfFormula;
import static prog2.project3.cnf.CnfFactory.createNegativeLiteral;
import static prog2.project3.cnf.CnfFactory.createPositiveLiteral;

import org.junit.Before;
import org.junit.Test;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.Cnf;
import prog2.project3.cnf.Literal;
import prog2.project3.cnf.Variable;
import prog2.project3.propositional.FormulaReader;
import prog2.project3.propositional.PropositionalFormula;
import prog2.project3.propositional.VariableNameGenerator;

public class TseitinImhaffTest {
	@Before
	public void setUp() {
		VariableNameGenerator.reset();
	}

	@Test
	public void testNegation() {
		String formulaString = "a !";
		PropositionalFormula formula = FormulaReader
				.readFormulaFromString(formulaString);
		Cnf cnf = formula.getConjunctiveNormalForm();

		assertEquals(2, cnf.getVariables().size());
		assertEquals(3, cnf.getClauses().size());

		Variable varX = cnf.getVariableForName("x1");
		Variable varA = cnf.getVariableForName("a");
		Literal litPosX = createPositiveLiteral(varX);
		Literal litNegX = createNegativeLiteral(varX);
		Literal litPosA = createPositiveLiteral(varA);
		Literal litNegA = createNegativeLiteral(varA);

		// x1
		Clause c0 = createClause(litPosX);
		// a v x1
		Clause c1 = createClause(litPosA, litPosX);
		// !a v !x1
		Clause c2 = createClause(litNegA, litNegX);
		assertEquals(createCnfFormula(c0, c1, c2), cnf);
	}

	@Test
	public void testConjunction() {
		String formulaString = "a b &&";
		PropositionalFormula formula = FormulaReader
				.readFormulaFromString(formulaString);
		Cnf cnf = formula.getConjunctiveNormalForm();

		assertEquals(3, cnf.getVariables().size());
		assertEquals(4, cnf.getClauses().size());

		Variable varX = cnf.getVariableForName("x1");
		Variable varA = cnf.getVariableForName("a");
		Variable varB = cnf.getVariableForName("b");
		Literal litPosX = createPositiveLiteral(varX);
		Literal litNegX = createNegativeLiteral(varX);
		Literal litPosA = createPositiveLiteral(varA);
		Literal litNegA = createNegativeLiteral(varA);
		Literal litPosB = createPositiveLiteral(varB);
		Literal litNegB = createNegativeLiteral(varB);

		// x1
		Clause c0 = createClause(litPosX);
		// !x1 v a
		Clause c1 = createClause(litNegX, litPosA);
		// !x1 v b
		Clause c2 = createClause(litNegX, litPosB);
		// !a v
		// !b v
		// x1
		Clause c3 = createClause(litNegA, litNegB, litPosX);
		assertEquals(createCnfFormula(c0, c1, c2, c3), cnf);
	}

	@Test
	public void testDisjunction() {
		String formulaString = "a b ||";
		PropositionalFormula formula = FormulaReader
				.readFormulaFromString(formulaString);
		Cnf cnf = formula.getConjunctiveNormalForm();

		assertEquals(3, cnf.getVariables().size());
		assertEquals(4, cnf.getClauses().size());

		Variable varX = cnf.getVariableForName("x1");
		Variable varA = cnf.getVariableForName("a");
		Variable varB = cnf.getVariableForName("b");
		Literal litPosX = createPositiveLiteral(varX);
		Literal litNegX = createNegativeLiteral(varX);
		Literal litPosA = createPositiveLiteral(varA);
		Literal litNegA = createNegativeLiteral(varA);
		Literal litPosB = createPositiveLiteral(varB);
		Literal litNegB = createNegativeLiteral(varB);

		// x1
		Clause c0 = createClause(litPosX);
		// !x1 v a v b
		Clause c1 = createClause(litNegX, litPosA, litPosB);
		// !a v x1
		Clause c2 = createClause(litNegA, litPosX);
		// !b v x1
		Clause c3 = createClause(litNegB, litPosX);
		assertEquals(createCnfFormula(c0, c1, c2, c3), cnf);
	}

	@Test
	public void testImplication() {
		String formulaString = "a b =>";
		PropositionalFormula formula = FormulaReader
				.readFormulaFromString(formulaString);
		Cnf cnf = formula.getConjunctiveNormalForm();

		assertEquals(3, cnf.getVariables().size());
		assertEquals(4, cnf.getClauses().size());

		Variable varX = cnf.getVariableForName("x1");
		Variable varA = cnf.getVariableForName("a");
		Variable varB = cnf.getVariableForName("b");
		Literal litPosX = createPositiveLiteral(varX);
		Literal litNegX = createNegativeLiteral(varX);
		Literal litPosA = createPositiveLiteral(varA);
		Literal litNegA = createNegativeLiteral(varA);
		Literal litPosB = createPositiveLiteral(varB);
		Literal litNegB = createNegativeLiteral(varB);

		// x1
		Clause c0 = createClause(litPosX);
		// !x1 v !a v b
		Clause c1 = createClause(litNegX, litNegA, litPosB);
		// a v x1
		Clause c2 = createClause(litPosA, litPosX);
		// !b v x1
		Clause c3 = createClause(litNegB, litPosX);
		assertEquals(createCnfFormula(c0, c1, c2, c3), cnf);
	}

	@Test
	public void testEquation() {
		String formulaString = "a b <=>";
		PropositionalFormula formula = FormulaReader
				.readFormulaFromString(formulaString);
		Cnf cnf = formula.getConjunctiveNormalForm();

		assertEquals(3, cnf.getVariables().size());
		assertEquals(5, cnf.getClauses().size());

		Variable varX = cnf.getVariableForName("x1");
		Variable varA = cnf.getVariableForName("a");
		Variable varB = cnf.getVariableForName("b");
		Literal litPosX = createPositiveLiteral(varX);
		Literal litNegX = createNegativeLiteral(varX);
		Literal litPosA = createPositiveLiteral(varA);
		Literal litNegA = createNegativeLiteral(varA);
		Literal litPosB = createPositiveLiteral(varB);
		Literal litNegB = createNegativeLiteral(varB);

		// x1
		Clause c0 = createClause(litPosX);
		// !x1 v !a v b
		Clause c1 = createClause(litNegX, litNegA, litPosB);
		// !x1 v !b v a
		Clause c2 = createClause(litNegX, litNegB, litPosA);
		// !x1 v !a v !b
		Clause c3 = createClause(litPosX, litNegA, litNegB);
		// x1 v a v b
		Clause c4 = createClause(litPosX, litPosA, litPosB);
		assertEquals(createCnfFormula(c0, c1, c2, c3, c4), cnf);
	}

	/**
	 * <pre>
	 * x2					^
	 * x2 <=> ( ! a <=> a )	^
	 * x1 <=> ! a
	 * </pre>
	 */
	@Test
	public void testExample1() {
		String formulaString = "a ! a <=>";
		PropositionalFormula formula = FormulaReader
				.readFormulaFromString(formulaString);
		Cnf cnf = formula.getConjunctiveNormalForm();

		// System.out.println("Ex 1 - !a <=> a :\t\t" + cnf);

		assertEquals(3, cnf.getVariables().size());
		assertEquals(7, cnf.getClauses().size());
	}

	/**
	 * <pre>
	 * x4					^
	 * x4 <=> ( a ^ x3 )	^
	 * x3 <=> ( x2 => d )	^
	 * x2 <=> ( x1 v c)		^
	 * x1 <=> ! b
	 * </pre>
	 */
	@Test
	public void testExample2() {
		String formulaString = "a b ! c || d => &&";
		PropositionalFormula formula = FormulaReader
				.readFormulaFromString(formulaString);
		Cnf cnf = formula.getConjunctiveNormalForm();

		// System.out.println("Ex 2 - a ^ ((!b v c) => d) :\t" + cnf);

		assertEquals(8, cnf.getVariables().size());
		assertEquals(12, cnf.getClauses().size());
	}
}
