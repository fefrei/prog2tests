package prog2.project3.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.Cnf;
import prog2.project3.cnf.CnfFactory;
import prog2.project3.cnf.Literal;
import prog2.project3.cnf.TruthValue;
import prog2.project3.cnf.Variable;

public class CnfFactoryMarkusTest {
	static final String VERSION = "1.0";

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("CnfFactoryMarkusTest", VERSION);
	}

	private static String truthToString(TruthValue t) {
		if (t == TruthValue.TRUE)
			return "1";
		if (t == TruthValue.FALSE)
			return "0";
		return "u";
	}

	/**
	 * Testet, ob bei jeder möglichen Änderung einer Variablen die
	 * updateTruthValue-Methode der Clause aufgerufen wird. Testet außerdem für
	 * alle Kombinationen aus drei Werten, ob der Wahrheitswert, den die
	 * Updatemethode von Clause zurückliefert korrekt ist.
	 */
	@Test
	public void testClauseUpdate() {
		Variable va = CnfFactory.createVariable("a");
		// Neu initialisierte Variable muss UNDEFINED sein
		assertEquals("Neue Variable", TruthValue.UNDEFINED, va.getTruthValue());

		Literal l1 = CnfFactory.createPositiveLiteral(va);
		Literal l2 = CnfFactory.createNegativeLiteral(va);
		Clause c1 = CnfFactory.createClause(l1);
		Clause c2 = CnfFactory.createClause(l2);
		// Neue Clause muss undefined sein
		assertEquals("Clause (a=undefined)", TruthValue.UNDEFINED,
				c1.getLastTruthValue());
		assertEquals("Clause (~a=undefined)", TruthValue.UNDEFINED,
				c2.getLastTruthValue());

		// setTruthValue muss Clause.updateTruthValue aufrufen
		va.setTruthValue(TruthValue.TRUE);
		assertEquals("Clause nach setTruthValue nicht geupdated!",
				TruthValue.TRUE, c1.getLastTruthValue());
		assertEquals("Clause nach setTruthValue nicht geupdated!",
				TruthValue.FALSE, c2.getLastTruthValue());

		// Literal.getTruthValue
		assertEquals("Literal.getTruthValue liefert falsches Ergebnis",
				TruthValue.TRUE, l1.getTruthValue());
		assertEquals(
				"Literal.getTruthValue liefert falsches Ergebnis für negative Literale",
				TruthValue.FALSE, l2.getTruthValue());

		// chooseSatisfyingAssignment muss Clause.updateTruthValue aufrufen
		va.setTruthValue(TruthValue.UNDEFINED);
		l2.chooseSatisfyingAssignment();
		assertEquals(
				"Clause (a) nach Literal.chooseSatisfyingAssignment nicht geupdated oder falsches Ergebnis!",
				TruthValue.FALSE, c1.getLastTruthValue());
		assertEquals(
				"Clause (~a) nach Literal.setchooseSatisfyingAssignment nicht geupdated oder falsches Ergebnis!",
				TruthValue.TRUE, c2.getLastTruthValue());

		// negateValue muss Clause.updateTruthValue aufrufen
		va.negateValue();
		assertEquals(
				"Clause (a) nach Variable.negateValue nicht geupdated oder falsches Ergebnis!",
				TruthValue.TRUE, c1.getLastTruthValue());
		assertEquals(
				"Clause (~a) nach Variable.negateValue nicht geupdated oder falsches Ergebnis!",
				TruthValue.FALSE, c2.getLastTruthValue());

		// -- Teste die Updatemethode selbst --
		Variable vb = CnfFactory.createVariable("b"), vc = CnfFactory
				.createVariable("c");
		Clause cl = CnfFactory.createClause(
				CnfFactory.createPositiveLiteral(va),
				CnfFactory.createPositiveLiteral(vb),
				CnfFactory.createPositiveLiteral(vc));
		TruthValue r;
		for (TruthValue a : TruthValue.values()) {
			va.setTruthValue(a);
			for (TruthValue b : TruthValue.values()) {
				vb.setTruthValue(b);
				for (TruthValue c : TruthValue.values()) {
					vc.setTruthValue(c);
					// Ergebnis berechnen
					if (a == TruthValue.TRUE || b == TruthValue.TRUE
							|| c == TruthValue.TRUE)
						r = TruthValue.TRUE;
					else if (a == TruthValue.UNDEFINED
							|| b == TruthValue.UNDEFINED
							|| c == TruthValue.UNDEFINED)
						r = TruthValue.UNDEFINED;
					else
						r = TruthValue.FALSE;
					assertEquals("Clause (a,b,c): a=" + truthToString(a)
							+ ", b=" + truthToString(b) + ", c="
							+ truthToString(c) + ".", cl.getLastTruthValue(), r);
				}
			}
		}
	}

	/**
	 * Testet CNF.resetAllVariables und CNF.getTruthValue mit allen möglichen
	 * Kombinationen aus drei Variablen.
	 */
	@Test
	public void testCnfTruthValue() {
		// Variablen a,b,c
		Variable va = CnfFactory.createVariable("a"), vb = CnfFactory
				.createVariable("b"), vc = CnfFactory.createVariable("c");
		// Clauses c1,c2,c3 = (a), (b), (c)
		Clause c1 = CnfFactory.createClause(CnfFactory
				.createPositiveLiteral(va)), c2 = CnfFactory
				.createClause(CnfFactory.createPositiveLiteral(vb)), c3 = CnfFactory
				.createClause(CnfFactory.createPositiveLiteral(vc));
		// CNF: c1 & c2 & c3 = a & b & c
		Cnf formula = CnfFactory.createCnfFormula(c1, c2, c3);

		// Teste resetAllVariables
		va.setTruthValue(TruthValue.TRUE);
		vb.setTruthValue(TruthValue.FALSE);
		vc.setTruthValue(TruthValue.UNDEFINED);
		formula.resetAllVariables();
		assertEquals("Ergebnis von CNF.resetAllVariables()",
				TruthValue.UNDEFINED, va.getTruthValue());
		assertEquals("Ergebnis von CNF.resetAllVariables()",
				TruthValue.UNDEFINED, vb.getTruthValue());
		assertEquals("Ergebnis von CNF.resetAllVariables()",
				TruthValue.UNDEFINED, vc.getTruthValue());

		// Teste alle Kombinationen für CNF.getTruthValue()
		TruthValue r;
		for (TruthValue a : TruthValue.values()) {
			va.setTruthValue(a);
			for (TruthValue b : TruthValue.values()) {
				vb.setTruthValue(b);
				for (TruthValue c : TruthValue.values()) {
					vc.setTruthValue(c);
					// Ergebnis berechnen
					if (a == TruthValue.TRUE && b == TruthValue.TRUE
							&& c == TruthValue.TRUE)
						r = TruthValue.TRUE;
					else if (a == TruthValue.FALSE || b == TruthValue.FALSE
							|| c == TruthValue.FALSE)
						r = TruthValue.FALSE;
					else
						r = TruthValue.UNDEFINED;
					assertEquals("Cnf (a,b,c): a=" + truthToString(a) + ", b="
							+ truthToString(b) + ", c=" + truthToString(c)
							+ ".", formula.getTruthValue(), r);
				}
			}
		}
	}

}
