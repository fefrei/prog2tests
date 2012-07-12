package prog2.project3.tests;

// import static prog2.project3.cnf.CnfFactory.*;
// import static org.junit.Assert.*;

import static org.junit.Assert.*;
import static prog2.project3.cnf.CnfFactory.createClause;
import static prog2.project3.cnf.CnfFactory.createCnfFormula;
import static prog2.project3.cnf.CnfFactory.createNegativeLiteral;
import static prog2.project3.cnf.CnfFactory.createPositiveLiteral;
import static prog2.project3.cnf.CnfFactory.createVariable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.Cnf;
import prog2.project3.cnf.Literal;
import prog2.project3.cnf.TruthValue;
import prog2.project3.cnf.Variable;

public class CnfFactoryBewiedTest {
	@Test
	public final void testNullSanity() {
		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #1
				createClause(((Literal[]) null));
			}
		}, new Runnable() {
			public void run() { // #2
				createClause((Literal) null);
			}
		}, new Runnable() {
			public void run() { // #3
				createClause((Collection<Literal>) null);
			}
		}, new Runnable() {
			public void run() { // #4
				createClause(Collections.singleton((Literal) null));
			}
		}, new Runnable() {
			public void run() { // #5
				createClause(Collections.singletonList((Literal) null));
			}
		}, new Runnable() {
			public void run() { // #6
				createCnfFormula(((Clause[]) null));
			}
		}, new Runnable() {
			public void run() { // #7
				createCnfFormula((Clause) null);
			}
		}, new Runnable() {
			public void run() { // #8
				createCnfFormula((Collection<Clause>) null);
			}
		}, new Runnable() {
			public void run() { // #9
				createCnfFormula(Collections.singleton((Clause) null));
			}
		}, new Runnable() {
			public void run() { // #10
				createCnfFormula(Collections.singletonList((Clause) null));
			}
		}, new Runnable() {
			public void run() { // #11
				createNegativeLiteral(null);
			}
		}, new Runnable() {
			public void run() { // #12
				createPositiveLiteral(null);
			}
		}, new Runnable() {
			public void run() { // #13
				createVariable(null);
			}
		}, };

		checkTestExceptions(tests, "CnfFactoryBewiedTest#testNullSanity",
				", since a method was called with NULL as argument",
				NullPointerException.class);
	}

	/**
	 * Tests some illegal names, and checks whether they are correctly
	 * identified as being illegal. Note that \u0666 is an arabic number, and
	 * Character.isDigit() returns true. The others should be obvious, I hope.
	 */
	@Test
	public final void testVariableNamesBad() {
		String[] tests = new String[] { "", " ", "'", "Ä", "aÄ", "a\u0666",
				"Ä9", "Fußknöchel", "x_1", "@", "[", "`", "{", "Adiòs", "7of9" };
		checkTestExceptions(tests, new VariableNameTester(),
				"CnfFactoryBewiedTest#testVariableNamesBad",
				", since such a name is NOT allowed",
				IllegalArgumentException.class);
	}

	/**
	 * Tests whether some actually perfectly fine variable names are accurately
	 * identified as being legal. Note how I also test the very start and
	 * beginning of the "two alphabet ranges", and that a name shouldn't start
	 * with a digit.
	 */
	@Test
	public final void testVariableNamesGood() {
		String[] tests = new String[] { "a1", "ZuFussGehen", "z9", "foobar",
				"A" };
		checkTestExceptions(tests, new VariableNameTester(),
				"CnfFactoryBewiedTest#testVariableNamesGood",
				", since such a name IS allowed", null);
	}

	/**
	 * Checks that bad clauses are correctly identified as such. A clause is
	 * "bad" if there are no literals, or there are two variable objects
	 * reachable that have a .equals name
	 */
	@Test
	public final void testCreateClauseBad() {
		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #1
				createClause();
			}
		}, new Runnable() {
			public void run() { // #2
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = spoof(a);
				createClause(a, a2);
			}
		}, new Runnable() {
			public void run() { // #3
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = new PhonyBewiedLiteral(spoof(a.getVariable()),
						false);
				createClause(a, a2);
			}
		}, new Runnable() {
			public void run() { // #4
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = spoof(a);
				createClause(a, a, a2);
			}
		}, new Runnable() {
			public void run() { // #5
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = new PhonyBewiedLiteral(spoof(a.getVariable()),
						false);
				Literal b = new PhonyBewiedLiteral("b", true);
				createClause(a, b, a2);
			}
		}, new Runnable() {
			public void run() { // #6
				createClause(new HashSet<Literal>());
			}
		}, new Runnable() {
			public void run() { // #7
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = spoof(a);
				createClause(collect(a, a2));
			}
		}, new Runnable() {
			public void run() { // #8
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = new PhonyBewiedLiteral(spoof(a.getVariable()),
						false);
				createClause(collect(a, a2));
			}
		}, new Runnable() {
			public void run() { // #9
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = spoof(a);
				createClause(collect(a, a, a2));
			}
		}, new Runnable() {
			public void run() { // #10
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = new PhonyBewiedLiteral(spoof(a.getVariable()),
						false);
				Literal b = new PhonyBewiedLiteral("b", true);
				createClause(collect(a, b, a2));
			}
		}, new Runnable() {
			public void run() { // #11
				String a = "a";
				String a2 = new String(a);
				assertFalse("The compiler optimized too heavily", a == a2);
				// Make sure that we are NOT using the SAME string,
				// but one that is EQUALS.
				Literal l = new PhonyBewiedLiteral(a, true);
				Literal l2 = new PhonyBewiedLiteral(a2, false);
				createClause(collect(l, l2));
				// I can't think of any other "special" cases that should fail.
			}
		} };
		checkTestExceptions(tests, "CnfFactoryBewiedTest#testCreateClauseBad",
				". You can lookup the details of a specific sub-test-number in"
						+ " lines 110 to 171 (CnfFactoryBewiedTest"
						+ "#testCreateClauseBad) for details",
				IllegalArgumentException.class);
	}

	/**
	 * Make sure that every combination that is perfectly fine is actually
	 * recognized, like two EQUAL literals, or multiple occurence of the SAME
	 * variable.
	 */
	@Test
	public final void testCreateClauseGood() {
		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #1
				Variable v = new PhonyBewiedVariable("a");
				Literal a = new PhonyBewiedLiteral(v, true);
				Literal a2 = new PhonyBewiedLiteral(v, true);
				createClause(a, a2);
			}
		}, new Runnable() {
			public void run() { // #2
				Variable v = new PhonyBewiedVariable("a");
				Literal a = new PhonyBewiedLiteral(v, true);
				Literal a2 = createNegativeLiteral(v);
				createClause(a, a2);
			}
		}, new Runnable() {
			public void run() { // #3
				Variable v = new PhonyBewiedVariable("a");
				Literal a = new PhonyBewiedLiteral(v, true);
				Literal a2 = createPositiveLiteral(v);
				createClause(a, a2);
			}
		}, new Runnable() {
			public void run() { // #4
				Variable v = new PhonyBewiedVariable("a");
				Literal a = new PhonyBewiedLiteral(v, true);
				Literal a2 = new PhonyBewiedLiteral(v, true);
				createClause(collect(a, a2));
			}
		}, new Runnable() {
			public void run() { // #5
				Variable v = new PhonyBewiedVariable("a");
				Literal a = new PhonyBewiedLiteral(v, true);
				Literal a2 = createNegativeLiteral(v);
				createClause(collect(a, a2));
			}
		}, new Runnable() {
			public void run() { // #6
				Variable v = new PhonyBewiedVariable("a");
				Literal a = new PhonyBewiedLiteral(v, true);
				Literal a2 = createPositiveLiteral(v);
				createClause(collect(a, a2));
				// I can't think of any other "special" cases
			}
		} };
		checkTestExceptions(tests, "CnfFactoryBewiedTest#testCreateClauseGood",
				", since there are no two (non-identical) variable instances"
						+ " who have an equal name", null);
	}

	/**
	 * Doesn't REALLY test anything, but prints a warning if the user is
	 * overriding the hashCode() fallback.
	 */
	@Test
	public final void testCreateVariable() {
		Variable a1 = createVariable("a"), a2 = createVariable("a");
		if (a1 == a2) {
			fail("createVariable() MUST return non-identical instances.");
		}
		if (a1.hashCode() == a2.hashCode() || a1.equals(a2)) {
			System.out.println("\nIf you use Object.hashCode() and"
					+ " Object.equals() for Variable, then everything will be"
					+ " okay, according to Tobias.\n"
					+ "Note that this doesn't mean your implementation is"
					+ " wrong. But think about this: Doing nothing is a"
					+ " granted win. Your implementation might or might not"
					+ " fail.\nWhat's easier?\n");
		}
	}

	// ===== Internals =====

	public static final <T> Set<T> collect(T... from) {
		Set<T> ret = new LinkedHashSet<T>();
		for (T t : from) {
			ret.add(t);
		}
		return ret;
	}

	public static final class VariableNameTester implements Tester<String> {
		@Override
		public void test(String input) {
			createVariable(input);
		}
	}

	public static final Cnf spoof(Cnf c) {
		Set<Clause> orig = c.getClauses();
		Set<Clause> spoofed = new HashSet<Clause>();
		for (Clause o : orig) {
			spoofed.add(spoof(o));
		}
		return new PhonyBewiedCnf(spoofed);
	}

	public static final Clause spoof(Clause c) {
		Set<Literal> orig = c.getLiterals();
		Set<Literal> spoofed = new HashSet<Literal>();
		for (Literal l : orig) {
			spoofed.add(spoof(l));
		}
		return new PhonyBewiedClause(spoofed);
	}

	public static final Literal spoof(Literal l) {
		return new PhonyBewiedLiteral(spoof(l.getVariable()),
				l.isNegatedLiteral());
	}

	public static final Variable spoof(Variable v) {
		return new PhonyBewiedVariable(v);
	}

	public static final void checkTestExceptions(Runnable[] tests,
			String method, String reason,
			Class<? extends RuntimeException> clazz) {
		List<String> reasons = new LinkedList<String>();
		List<Integer> indices = new LinkedList<Integer>();

		for (int i = 0; i < tests.length; i++) {
			String s = assertException(tests[i], reason, clazz);
			if (s != null) {
				reasons.add("#" + (i + 1) + ": " + s);
				indices.add(i + 1);
			}
		}

		if (!reasons.isEmpty()) {
			TestUtilFelix.failAndExplain(method, tests.length,
					indices.toArray(new Integer[indices.size()]),
					reasons.toArray(new String[reasons.size()]));
		}
	}

	public static final <T> void checkTestExceptions(T[] tests, Tester<T> t,
			String method, String reason,
			Class<? extends RuntimeException> clazz) {
		List<String> reasons = new LinkedList<String>();
		List<Integer> indices = new LinkedList<Integer>();

		for (int i = 0; i < tests.length; i++) {
			String s = assertException(t, tests[i], reason, clazz);
			if (s != null) {
				reasons.add("#" + (i + 1) + ": " + s);
				indices.add(i + 1);
			}
		}

		if (!reasons.isEmpty()) {
			TestUtilFelix.failAndExplain(method, tests.length,
					indices.toArray(new Integer[indices.size()]),
					reasons.toArray(new String[reasons.size()]));
		}
	}

	public static final String assertException(Runnable r, final String reason,
			Class<? extends RuntimeException> clazz) {
		try {
			r.run();
			if (clazz != null) {
				return "Expected " + clazz.getSimpleName() + " to be thrown"
						+ reason + ". You threw nothing.";
			} else {
				// Expected this
				return null;
			}
		} catch (AssertionFailedError e) {
			return e.getMessage();
		} catch (RuntimeException e) {
			if (clazz == null) {
				return "Expected a clean run" + reason + ", you threw "
						+ e.getClass().getSimpleName() + ": " + e.getMessage();
			} else if (clazz.isInstance(e)) {
				// Expected this
				return null;
			} else {
				return "Expected " + clazz.getSimpleName() + " to be thrown"
						+ reason + ". You threw "
						+ e.getClass().getSimpleName() + ": " + e.getMessage();
			}
		}
	}

	public static final <T> String assertException(final Tester<T> t,
			final T input, final String reason,
			final Class<? extends RuntimeException> clazz) {
		try {
			t.test(input);
			if (clazz != null) {
				return "Expected " + clazz.getSimpleName()
						+ " to be thrown for input \"" + input + "\"" + reason
						+ ". You threw nothing.";
			} else {
				// Expected this
				return null;
			}
		} catch (RuntimeException e) {
			if (clazz == null) {
				return "Expected a clean run" + reason + ", but for input "
						+ input + " you threw " + e.getClass().getSimpleName()
						+ ": " + e.getMessage();
			} else if (clazz.isInstance(e)) {
				// Expected this
				return null;
			} else {
				return "Expected " + clazz.getSimpleName() + " to be thrown"
						+ reason + ". You threw "
						+ e.getClass().getSimpleName() + ": " + e.getMessage();
			}
		}
	}

	public static interface Tester<T> {
		void test(T input);
	}

	@SuppressWarnings("serial")
	public static final class UnreasonableCallException extends
			RuntimeException {
	}

	// ===== Classes =====

	public static final class PhonyBewiedClause extends Clause {
		public PhonyBewiedClause(Collection<Literal> literals) {
			super(literals);
		}

		public PhonyBewiedClause(Literal... literals) {
			super(CnfFactoryBewiedTest.collect(literals));
		}

		@Override
		public final TruthValue getLastTruthValue() {
			return TruthValue.UNDEFINED;
		}

		@Override
		public final Collection<Variable> getVariables() {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}

		@Override
		public final Literal getUnitClauseLiteral() {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}

		@Override
		public final void updateTruthValue() {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}

		@Override
		public final Literal getPureLiteral() {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}
	}

	public static final class PhonyBewiedCnf extends Cnf {
		private final Set<Clause> clauses;

		public PhonyBewiedCnf(Set<Clause> clauses) {
			this.clauses = clauses;
		}

		public PhonyBewiedCnf(Clause... clauses) {
			this.clauses = CnfFactoryBewiedTest.collect(clauses);
		}

		@Override
		public final TruthValue getTruthValue() {
			return TruthValue.UNDEFINED;
		}

		@Override
		public final Set<Clause> getClauses() {
			return clauses;
		}

		@Override
		public final Collection<Variable> getVariables() {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}

		@Override
		public final void resetAllVariables() {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}

		@Override
		public final Variable getVariableForName(String name) {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}

		@Override
		public final Literal getUnitClauseLiteral() {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}

		@Override
		public final Literal getPureLiteral() {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}
	}

	public static final class PhonyBewiedLiteral extends Literal {
		private final Variable v;
		private final boolean negated;

		public PhonyBewiedLiteral(String name, boolean negated) {
			this.v = new PhonyBewiedVariable(name);
			this.negated = negated;
		}

		public PhonyBewiedLiteral(Variable v, boolean negated) {
			this.v = v;
			this.negated = negated;
		}

		@Override
		public final TruthValue getTruthValue() {
			return TruthValue.UNDEFINED;
		}

		@Override
		public final void chooseSatisfyingAssignment() {
			// IGNORED
		}

		@Override
		public final Variable getVariable() {
			return v;
		}

		@Override
		public final void addParentClause(Clause clause) {
			// IGNORED
		}

		@Override
		public final boolean isNegatedLiteral() {
			return negated;
		}

		@Override
		public final boolean isPure() {
			return false;
		}

		@Override
		public final void addDependentClause(Clause clause) {
			// IGNORED
		}

		@Override
		public final void removeDependentClause(Clause clause) {
			// IGNORED
		}
	}

	public static final class PhonyBewiedVariable implements Variable {
		private final String name;
		private final int hashCode;

		public PhonyBewiedVariable(String name) {
			this.name = name;
			hashCode = super.hashCode();
		}

		public PhonyBewiedVariable(Variable v) {
			this.name = v.getName();
			hashCode = v.hashCode();
		}

		@Override
		public final int hashCode() {
			return hashCode;
		}

		@Override
		public final String getName() {
			return name;
		}

		@Override
		public final void setTruthValue(TruthValue newValue) {
			// IGNORED
		}

		@Override
		public final TruthValue getTruthValue() {
			return TruthValue.UNDEFINED;
		}

		@Override
		public final void negateValue() {
			// IGNORED
		}

		@Override
		public final void addParentClause(Clause clause) {
			// IGNORED
		}

		@Override
		public final Set<Clause> getParentClauses() {
			// You shouldn't call this
			// => FAIL
			throw new UnreasonableCallException();
		}

		@Override
		public final void addDependentClausePos(Clause clause) {
			// IGNORED
		}

		@Override
		public final void removeDependentClausePos(Clause clause) {
			// IGNORED
		}

		@Override
		public final boolean isPurePositive() {
			return false;
		}

		@Override
		public final void addDependentClauseNeg(Clause clause) {
			// IGNORED
		}

		@Override
		public final void removeDependentClauseNeg(Clause clause) {
			// IGNORED
		}

		@Override
		public final boolean isPureNegative() {
			return false;
		}
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("CnfFactoryBewiedTest", "1.3.2");
	}
}
