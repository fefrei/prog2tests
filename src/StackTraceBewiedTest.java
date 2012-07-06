package prog2.project3.tests;

//import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import prog2.project3.cnf.Cnf;
import prog2.project3.cnf.TruthValue;
import prog2.project3.dpll.Choice;
import prog2.project3.dpll.DPLLAlgorithm;
import prog2.project3.dpll.StackEntry;

public class StackTraceBewiedTest {
	// ===== TESTS themselves -- pretty boring, huh?

	/**
	 * Getestet wird, in dieser Reihenfolge:
	 * 
	 * <pre>
	 * a ! ! !
	 * a ! a &&
	 * k k ! =>
	 * a ! ! ! ! !
	 * c c ! && a ! => a <=>
	 * c c ! || a ! => a <=>
	 * a b && ! a ! b && ! &&
	 * a ! b => ! a ! a ! <=> ! && !
	 * a b ! <=> b c <=> c a <=> && &&
	 * a ! a ! || a ! a && ! &&
	 * a a a ! a || ! ! ! ! <=> ! &&
	 * a b b ! && b && b && &&
	 * b b a b => ! ! ! <=> ! ! ! ! &&
	 * b b ! ! b && b a || ! && &&
	 * b a a b ! a || || ! <=> ! ! &&
	 * a b ! ! b || a => ! ! ! &&
	 * a b ! ! ! ! b a <=> ! || ! &&
	 * a a ! b a ! b ! => ! => ! && &&
	 * a b ! ! a || b a ! && c && <=> &&
	 * a d ! ! b ! c || && d => ! ! ! &&
	 * "a b => b c => <=>
	 * z z ! && f <=>\n\tf ! w <=>\n\tcb sb <=>\n\tf cb <=> !\n\t&& && &&
	 * a
	 * a|~b", "a|~a
	 * ~c-b|~d-c|~b-d|~b-~c|a
	 * ~a-~x-~b|a-x-b|a-b|~a-~b
	 * a-~b|a-~b-c|a-~b-c-~d
	 * a-b|a-~b-c|~c-e-~e-a|b-c-d|~a-~b-d
	 * </pre>
	 */
	@Test
	public void testDpllStacktrace() {
		// Für weitere Test-files, eröffnet einfach ein Ticket mit einer
		// entsprechenden CNF und/oder der UPN-Darstellung :)
		runFelixwrappedSingleFile("testDpllStacktrace",
				"examples|Ben|ExtendedDpllStacktrace.txt");
	}

	// ===== OFFICIAL interface and methods. Feel free to use them!

	public static boolean VERBOSE_PARSING = false, VERBOSE_TESTING = false;

	public static final void runFelixwrappedSingleFile(String name,
			String pathToFile) {
		runFelixwrappedSuite(name, parseStacktrace(pathToFile));
	}

	public static final void runFelixwrappedFiles(String name, String prefix,
			String... filenames) {
		List<TestInstance> tests = new LinkedList<TestInstance>();
		for (String filename : filenames) {
			tests.addAll(parseStacktrace(prefix + filename));
		}
		runFelixwrappedSuite(name, tests);
	}

	public static final void runFelixwrappedSuite(String name,
			List<TestInstance> tests) {
		List<String> explanations = new LinkedList<String>();
		for (TestInstance test : tests) {
			try {
				test.runAll();
				explanations.add(null);
			} catch (StackMismatchException e) {
				explanations.add(e.getMessage());
			}
		}
		TestUtilFelix.checkFailAndExplain(name, explanations);
	}

	public static final void runFailfastSuite(List<TestInstance> tests) {
		for (TestInstance test : tests) {
			test.runAll();
		}
	}

	public static final List<TestInstance> parseStacktrace(String pathToFile) {
		return parseStacktrace(TestUtilFelix.parseDataFile(pathToFile));
	}

	public static final List<TestInstance> parseStacktrace(List<String> lines) {
		List<TestInstance> ret = new LinkedList<TestInstance>();
		Iterator<String> it = lines.iterator();
		while (it.hasNext()) {
			ret.add(parseStacktrace(it));
		}
		return ret;
	}

	public static final TestInstance parseStacktrace(Iterator<String> it) {
		Map<Integer, MyState> states;
		Cnf c;
		try {
			c = TestUtilFelix.parseCompactCnfString(it.next());
			it.next(); // "-1", later the "minCount"
			int stateCount = Integer.valueOf(it.next());
			if (VERBOSE_PARSING) {
				System.out.print("Parsing " + stateCount + " states ... ");
			}
			states = new LinkedHashMap<Integer, MyState>();
			for (int i = 0; i < stateCount; i++) {
				it.next(); // "====="
				final int ID = Integer.valueOf(it.next());
				final MyState state = new MyState(ID);
				states.put(ID, state);
				final int stackDepth = Integer.valueOf(it.next());
				for (int j = 0; j < stackDepth; j++) {
					state.stack.add(new StackEntryExpectation(it.next()));
				}
				final int nextCount = Integer.valueOf(it.next());
				for (int j = 0; j < nextCount; j++) {
					state.nextStateIDs.add(Integer.valueOf(it.next()));
				}
			}
		} catch (NoSuchElementException e) {
			if (VERBOSE_PARSING) {
				System.out.println("FAILED");
			}
			throw new StacktraceFormatException(e);
		} catch (NumberFormatException e) {
			if (VERBOSE_PARSING) {
				System.out.println("FAILED");
			}
			throw new StacktraceFormatException(e);
		}
		for (MyState state : states.values()) {
			for (Integer id : state.nextStateIDs) {
				MyState found = states.get(id);
				if (found == null) {
					if (VERBOSE_PARSING) {
						System.out.println("FAILED");
					}
					throw new StacktraceFormatException();
				}
				state.nextStates.add(found);
			}
		}
		MyState root = states.get(0);
		if (root == null) {
			if (VERBOSE_PARSING) {
				System.out.println("FAILED");
			}
			throw new StacktraceFormatException();
		}
		if (VERBOSE_PARSING) {
			System.out.println("COMPLETE:");

		}
		return new TestInstance(c, root);
	}

	// ===== "INSTANCE" of a test case

	public static final class TestInstance {
		private final DPLLAlgorithm d;
		private final Cnf cnf;
		private MyState currentState;

		public TestInstance(Cnf cnf, MyState currentState) {
			this.cnf = cnf;
			this.currentState = currentState;
			d = new DPLLAlgorithm(cnf);
		}

		public boolean runStep() throws StackMismatchException {
			boolean finished = d.iterate();
			if (finished ^ currentState.nextStates.isEmpty()) {
				throw new StackMismatchException(cnf, currentState, d);
			}
			if (finished) {
				if (VERBOSE_TESTING) {
					System.out.println("Completed");
				}
				return false;
			}
			List<StackEntry> stack = d.getStack();
			for (MyState next : currentState.nextStates) {
				List<StackEntryExpectation> expected = next.stack;
				if (matches(expected, stack)) {
					currentState = next;
					if (VERBOSE_TESTING) {
						StringBuilder sb = new StringBuilder();
						sb.append("Went to #");
						sb.append(String.valueOf(next.ID));
						sb.append(", ");
						appendString(sb, next.stack);
						System.out.println(sb.toString());
					}
					return true;
				}
			}
			throw new StackMismatchException(cnf, currentState, d);
		}

		public void runAll() throws StackMismatchException {
			while (runStep())
				;
		}
	}

	// ===== UNDER THE HODD -- technical details ahead.

	@SuppressWarnings("serial")
	private static final class StacktraceFormatException extends
			IllegalArgumentException {
		public StacktraceFormatException() {
			super();
		}

		public StacktraceFormatException(Throwable cause) {
			super(cause);
		}
	}

	@SuppressWarnings("serial")
	public static final class StackMismatchException extends
			AssertionFailedError {
		public final Cnf cnf;
		public final MyState oldState;
		public final DPLLAlgorithm dpllAlgo;

		public StackMismatchException(Cnf cnf, MyState oldState,
				DPLLAlgorithm dpllAlgo) {
			super(buildMessage(cnf, oldState, dpllAlgo));
			this.cnf = cnf;
			this.oldState = oldState;
			this.dpllAlgo = dpllAlgo;
		}
	}

	public static final String buildMessage(Cnf cnf, MyState oldState,
			DPLLAlgorithm dpllAlgo) {
		StringBuilder sb = new StringBuilder();
		sb.append("While testing your behavior on the cnf ");
		sb.append(cnf.toString());
		sb.append(":\nYou hung after state #");
		sb.append(String.valueOf(oldState.ID));
		sb.append(" and your (correct!) stack of the previous iteration was ");
		appendString(sb, oldState.stack);
		sb.append(".\nYou had the choice between these further states:");
		for (MyState nextState : oldState.nextStates) {
			sb.append('\n');
			sb.append('#');
			sb.append(String.valueOf(nextState.ID));
			sb.append(':');
			sb.append(' ');
			appendString(sb, nextState.stack);
		}
		sb.append("\nInstead of any of the previous, your next stack was:\n");
		appendString(dpllAlgo.getStack(), sb);
		return sb.toString();
	}

	public static final void appendString(List<StackEntry> list,
			StringBuilder sb) {
		sb.append('[');
		Iterator<StackEntry> it = list.iterator();

		if (it.hasNext()) {
			appendString(it.next(), sb);
		}
		while (it.hasNext()) {
			sb.append(',');
			sb.append(' ');
			appendString(it.next(), sb);
		}
		sb.append(']');
	}

	public static final void appendString(StringBuilder sb,
			List<StackEntryExpectation> list) {
		sb.append('[');
		Iterator<StackEntryExpectation> it = list.iterator();

		if (it.hasNext()) {
			appendString(it.next(), sb);
		}
		while (it.hasNext()) {
			sb.append(',');
			sb.append(' ');
			appendString(it.next(), sb);
		}
		sb.append(']');
	}

	public static final void appendString(StackEntry e, StringBuilder sb) {
		sb.append('(');
		sb.append(e.variable);
		sb.append(',');
		sb.append(' ');
		sb.append(e.choice);
		sb.append(')');
	}

	public static final void appendString(StackEntryExpectation e,
			StringBuilder sb) {
		sb.append('(');
		sb.append(e.varname);
		sb.append('[');
		sb.append('=');
		sb.append(e.tv);
		sb.append(']');
		sb.append(',');
		sb.append(' ');
		sb.append(e.c);
		sb.append(')');
	}

	public static final class MyState {
		public final List<Integer> nextStateIDs = new LinkedList<Integer>();
		public final List<MyState> nextStates = new LinkedList<MyState>();
		public final List<StackEntryExpectation> stack = new LinkedList<StackEntryExpectation>();
		public final int ID;

		public MyState(int iD) {
			ID = iD;
		}
	}

	public static final class StackEntryExpectation {
		private final Choice c;
		private final String varname;
		private final TruthValue tv;

		public StackEntryExpectation(String s) {
			varname = s.substring(2);
			final char choiceChar = s.charAt(0);
			switch (choiceChar) {
			case 'I':
				c = Choice.IMPLIED;
				break;
			case 'C':
				c = Choice.CHOSEN;
				break;
			default:
				throw new StacktraceFormatException();
			}
			final char stateChar = s.charAt(1);
			switch (stateChar) {
			case 'T':
				tv = TruthValue.TRUE;
				break;
			// case 'U':
			// tv = TruthValue.UNDEFINED;
			// break;
			case 'F':
				tv = TruthValue.FALSE;
				break;
			default:
				throw new StacktraceFormatException();
			}
		}

		public boolean matches(StackEntry e) {
			return e.choice == c && e.variable.getTruthValue() == tv
					&& e.variable.getName().equals(varname);
		}
	}

	public static final boolean matches(List<StackEntryExpectation> expected,
			List<StackEntry> actual) {
		if (expected.size() != actual.size()) {
			return false;
		}
		Iterator<StackEntryExpectation> exp = expected.iterator();
		Iterator<StackEntry> act = actual.iterator();
		while (exp.hasNext()) {
			if (!act.hasNext()) {
				return false;
			}
			if (!exp.next().matches(act.next())) {
				return false;
			}
		}
		return true;
	}

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("StackTraceBewiedTest", "1.2.1");
	}
}
