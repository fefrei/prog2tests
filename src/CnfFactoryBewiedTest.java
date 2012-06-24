package prog2.project3.tests;

// import static prog2.project3.cnf.CnfFactory.*;
import static org.junit.Assert.fail;
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
import prog2.project3.cnf.Variable;

public class CnfFactoryBewiedTest {
	@Test
	public final void testNullSanity() {
		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() {
				createClause(((Literal[]) null)); // #0
			}
		}, new Runnable() {
			public void run() {
				createClause((Literal) null);
			}
		}, new Runnable() {
			public void run() {
				createClause((Collection<Literal>) null);
			}
		}, new Runnable() {
			public void run() {
				createClause(Collections.singleton((Literal) null));
			}
		}, new Runnable() {
			public void run() {
				createClause(Collections.singletonList((Literal) null));
			}
		}, new Runnable() {
			public void run() {
				createCnfFormula(((Clause[]) null)); // #5
			}
		}, new Runnable() {
			public void run() {
				createCnfFormula((Clause) null);
			}
		}, new Runnable() {
			public void run() {
				createCnfFormula((Collection<Clause>) null);
			}
		}, new Runnable() {
			public void run() {
				createCnfFormula(Collections.singleton((Clause) null));
			}
		}, new Runnable() {
			public void run() {
				createCnfFormula(Collections.singletonList((Clause) null));
			}
		}, new Runnable() {
			public void run() {
				createNegativeLiteral(null); // #10
			}
		}, new Runnable() {
			public void run() {
				createPositiveLiteral(null);
			}
		}, new Runnable() {
			public void run() {
				createVariable(null);
			}
		}, };

		checkTestExceptions(tests, "CnfFactoryBewiedTest#testNullSanity",
				NullPointerException.class);
	}

	@Test
	public final void testVariableNamesBad() {
		String[] tests = new String[] { "", " ", "'", "Ä", "aÄ", "a\u0666",
				"Ä9", "Fußknöchel", "x_1", "@", "[", "`", "{", "Adiòs", "7of9" };
		checkTestExceptions(tests, new VariableNameTester(),
				"CnfFactoryBewiedTest#testVariableNamesBad",
				IllegalArgumentException.class);
	}

	@Test
	public final void testVariableNamesGood() {
		String[] tests = new String[] { "a1", "ZuFussGehen", "z9", "foobar",
				"A" };
		checkTestExceptions(tests, new VariableNameTester(),
				"CnfFactoryBewiedTest#testVariableNamesGood", NoException.class);
	}

	@Test
	public final void testCreateClauseBad() {
		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #0
				createClause();
			}
		}, new Runnable() {
			public void run() {
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = spoof(a);
				createClause(a, a2);
			}
		}, new Runnable() {
			public void run() {
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = new PhonyBewiedLiteral(spoof(a.getVariable()), false);
				createClause(a, a2);
			}
		}, new Runnable() {
			public void run() {
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = spoof(a);
				createClause(a, a, a2);
			}
		}, new Runnable() {
			public void run() {
				Literal a = new PhonyBewiedLiteral("a", true);
				Literal a2 = new PhonyBewiedLiteral(spoof(a.getVariable()), false);
				Literal b = new PhonyBewiedLiteral("b", true);
				createClause(a, b, a2);
				// I can't think of any other "special" cases that should fail.
			}
		} };
		checkTestExceptions(tests, "CnfFactoryBewiedTest#testCreateClauseBad",
				IllegalArgumentException.class);
	}

	@Test
	public final void testCreateClauseGood() {
		Runnable[] tests = new Runnable[] { new Runnable() {
			public void run() { // #0
				Variable v = new PhonyBewiedVariable("a");
				Literal a = new PhonyBewiedLiteral(v, true);
				Literal a2 = new PhonyBewiedLiteral(v, true);
				createClause(a, a2);
				throw new NoException();
			}
		}, new Runnable() {
			public void run() {
				Variable v = new PhonyBewiedVariable("a");
				Literal a = new PhonyBewiedLiteral(v, true);
				Literal a2 = createNegativeLiteral(v);
				createClause(a, a2);
				throw new NoException();
			}
		}, new Runnable() {
			public void run() {
				Variable v = new PhonyBewiedVariable("a");
				Literal a = new PhonyBewiedLiteral(v, true);
				Literal a2 = createPositiveLiteral(v);
				createClause(a, a2);
				throw new NoException();
				// I can't think of any other "special" cases
			}
		} };
		checkTestExceptions(tests, "CnfFactoryBewiedTest#testCreateClauseGood",
				NoException.class);
	}
	
	@Test
	public final void testCreateVariable() {
		Variable a1 = createVariable("a"), a2 = createVariable("a");
		if (a1 == a2) {
			fail("createVariable() MUST return non-identical instances.");
		}
		if (a1.hashCode() == a2.hashCode() || a1.equals(a2)) {
			System.out.println("\nIf you use Object.hashCode() and" +
					  " Object.equals() for Variable, then everything will be" +
					  " okay, according to Tobias.\n" +
					  "Note that this doesn't mean your implementation is" +
					  " wrong. But think about this: Doing nothing is a" +
					  " granted win. Your implementation might or might not" +
					  " fail.\nWhat's easier?\n");
		}
	}
	
	// ===== Internals =====
	
	public static final <T> Set<T> collect(T[] from) {
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
			throw new NoException();
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
		return new PhonyBewiedLiteral(spoof(l.getVariable()), l.isNegatedLiteral());
	}
	
	public static final Variable spoof(Variable v) {
		return new PhonyBewiedVariable(v);
	}

	@SuppressWarnings("serial")
	public static final class NoException extends RuntimeException {
	}

	protected static final void checkTestExceptions(Runnable[] tests,
			String method, Class<? extends RuntimeException> clazz) {
		List<String> reasons = new LinkedList<String>();
		List<Integer> indices = new LinkedList<Integer>();

		for (int i = 0; i < tests.length; i++) {
			String s = assertException(tests[i], clazz);
			if (s != null) {
				reasons.add("#"+i+": "+s);
				indices.add(i);
			}
		}

		if (!reasons.isEmpty()) {
			TestUtilFelix.failAndExplain(method, tests.length,
					indices.toArray(new Integer[indices.size()]),
					reasons.toArray(new String[reasons.size()]));
		}
	}

	protected static final <T> void checkTestExceptions(T[] tests, Tester<T> t,
			String method, Class<? extends RuntimeException> clazz) {
		List<String> reasons = new LinkedList<String>();
		List<Integer> indices = new LinkedList<Integer>();

		for (int i = 0; i < tests.length; i++) {
			String s = assertException(t, tests[i], clazz);
			if (s != null) {
				reasons.add(s);
				indices.add(i);
			}
		}

		if (!reasons.isEmpty()) {
			TestUtilFelix.failAndExplain(method, tests.length,
					indices.toArray(new Integer[indices.size()]),
					reasons.toArray(new String[reasons.size()]));
		}
	}

	public static final String assertException(Runnable r,
			Class<? extends RuntimeException> clazz) {
		try {
			r.run();
			return "Expected " + clazz.getSimpleName()
					+ " to be thrown. You threw nothing.";
		} catch (AssertionFailedError e) {
			return e.getMessage();
		} catch (RuntimeException e) {
			if (clazz.isInstance(e)) {
				// Expected this
				return null;
			} else {
				return "Expected " + clazz.getSimpleName()
						+ " to be thrown. You threw "
						+ e.getClass().getSimpleName() + ": " + e.getMessage();
			}
		}
	}

	public static final <T> String assertException(Tester<T> t, T input,
			Class<? extends RuntimeException> clazz) {
		try {
			t.test(input);
			return "Expected " + clazz.getSimpleName()
					+ " to be thrown for input \"" + input
					+ "\". You threw nothing.";
		} catch (RuntimeException e) {
			if (clazz.isInstance(e)) {
				// Expected this
				return null;
			} else {
				return "Expected " + clazz.getSimpleName()
						+ " to be thrown. You threw "
						+ e.getClass().getSimpleName() + ": " + e.getMessage();
			}
		}
	}

	public static interface Tester<T> {
		void test(T input);
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("CnfFactoryBewiedTest", "1.0");
	}
}
