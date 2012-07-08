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
		runFelixwrappedSingleFile(ITERATIONS, "testDpllStacktrace",
				"examples|Ben|CorrectDpllStacktrace.txt");
	}

	// ===== OFFICIAL interface and methods. Feel free to use them!

	public static boolean VERBOSE_PARSING = false,
			VERY_VERBOSE_PARSING = false, VERBOSE_TESTING = false,
			VERY_VERBOSE_TESTING = false, FAIL_INTENTIONALLY = false;
	public static int ITERATIONS = 100;

	public static final void runFelixwrappedSingleFile(int iterations,
			String name, String pathToFile) {
		runFelixwrappedSuite(iterations, name, parseStacktrace(pathToFile));
	}

	public static final void runFelixwrappedFiles(int iterations, String name,
			String prefix, String... filenames) {
		List<TestInstance> tests = new LinkedList<TestInstance>();
		for (String filename : filenames) {
			tests.addAll(parseStacktrace(prefix + filename));
		}
		runFelixwrappedSuite(iterations, name, tests);
	}

	public static final void runFelixwrappedSuite(int iterations, String name,
			List<TestInstance> tests) {
		if (iterations == 0) {
			return;
		}
		final int TESTS_PER_ITERATION = tests.size();
		final int TESTS_COUNT = TESTS_PER_ITERATION * iterations;
		final boolean PROGRESS_BAR;

		List<Integer> failedTests = new LinkedList<Integer>();
		List<String> failureMessages = new LinkedList<String>();
		int alreadyPrinted = 0;
		int currentTest = 1;

		if (VERBOSE_TESTING || VERY_VERBOSE_TESTING) {
			System.out.println("Disabling progress bar due to verbosity level");
			PROGRESS_BAR = false;
		} else {
			PROGRESS_BAR = true;
		}

		TestUtilFelix.printRunning(name);

		for (int i = 0; i < iterations; i++) {
			if (VERBOSE_TESTING) {
				System.out.println("========== Iteration #" + i);
			}
			int testIdx = 0;
			for (TestInstance test : tests) {
				try {
					test.runAll();
				} catch (StackMismatchException e) {
					failedTests.add(testIdx + 1);
					failureMessages.add("Test " + (testIdx + 1)
							+ " failed with this message:\n" + e.getMessage());
				}
				if (PROGRESS_BAR) {
					alreadyPrinted = TestUtilFelix.updateProgressBar(
							alreadyPrinted, currentTest, TESTS_COUNT);
				}
				currentTest++;
			}

			if (!failedTests.isEmpty()) {
				// It's most improbable that this occurs only in the last
				// iteration.
				System.out.println("X");
				TestUtilFelix.failAndExplain(name, TESTS_PER_ITERATION,
						failedTests, failureMessages);
			}
		}
	}

	public static final void runFailfastSuite(List<TestInstance> tests) {
		for (TestInstance test : tests) {
			test.runAll();
		}
	}

	public static final List<TestInstance> parseStacktrace(String pathToFile) {
		long start = System.currentTimeMillis();
		final List<TestInstance> ret = parseStacktrace(TestUtilFelix
				.parseDataFile(pathToFile));
		if (VERBOSE_PARSING) {
			System.out.println("Took " + (System.currentTimeMillis() - start)
					+ "ms for parsing.");
		}
		return ret;
	}

	public static final List<TestInstance> parseStacktrace(List<String> lines) {
		List<TestInstance> ret = new LinkedList<TestInstance>();
		Iterator<String> it = lines.iterator();
		int counter = 0;
		while (it.hasNext()) {
			ret.add(parseStacktrace(it, ++counter));
		}
		return ret;
	}

	public static final TestInstance parseStacktrace(Iterator<String> it,
			int testcase) {
		Map<Integer, MyState> states;
		Cnf c;
		try {
			c = TestUtilFelix.parseCompactCnfString(it.next());
			it.next(); // "-1", later the "minCount"
			int stateCount = Integer.valueOf(it.next());
			if (VERY_VERBOSE_PARSING) {
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
			if (VERY_VERBOSE_PARSING) {
				System.out.println("FAILED");
			}
			throw new StacktraceFormatException(e);
		} catch (NumberFormatException e) {
			if (VERY_VERBOSE_PARSING) {
				System.out.println("FAILED");
			}
			throw new StacktraceFormatException(e);
		}
		for (MyState state : states.values()) {
			for (Integer id : state.nextStateIDs) {
				MyState found = states.get(id);
				if (found == null) {
					if (VERY_VERBOSE_PARSING) {
						System.out.println("FAILED");
					}
					throw new StacktraceFormatException();
				}
				state.nextStates.add(found);
			}
		}
		MyState root = states.get(0);
		if (root == null) {
			if (VERY_VERBOSE_PARSING) {
				System.out.println("FAILED");
			}
			throw new StacktraceFormatException();
		}
		if (VERY_VERBOSE_PARSING) {
			System.out.println("COMPLETE");

		}
		return new TestInstance(c, root, testcase);
	}

	// ===== "INSTANCE" of a test case

	public static final class TestInstance {
		private final MyState root;
		private final int testcase;
		private final String compact;

		private Cnf cnf;
		private DPLLAlgorithm d;
		private MyState currentState;

		public TestInstance(Cnf cnf, MyState root, int testcase) {
			this.root = currentState = root;
			this.cnf = cnf;
			compact = TestUtilFelix.cnfToCompactString(cnf);
			this.testcase = testcase;
			d = new DPLLAlgorithm(cnf);
		}

		public boolean runStep() throws StackMismatchException {
			boolean finished = d.iterate();
			if (finished ^ currentState.nextStates.isEmpty()) {
				throw new StackMismatchException(cnf, currentState, d);
			}
			if (finished) {
				if (VERBOSE_TESTING) {
					System.out
							.println("<===== Completed testcase #" + testcase);
				}
				return false;
			}
			List<StackEntry> stack = d.getStack();
			if (FAIL_INTENTIONALLY && currentState.ID > 30) {
				stack = new LinkedList<StackEntry>();
			}
			for (MyState next : currentState.nextStates) {
				List<StackEntryExpectation> expected = next.stack;
				if (matches(expected, stack)) {
					currentState = next;
					if (VERY_VERBOSE_TESTING) {
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
			if (VERBOSE_TESTING) {
				System.out.println("<===== FAILED after state #"
						+ currentState.ID + " in testcase #" + testcase);
			}
			throw new StackMismatchException(cnf, currentState, d);
		}

		public void runAll() throws StackMismatchException {
			if (VERBOSE_TESTING) {
				System.out.println("=====> Started testcase #" + testcase);
			}
			while (runStep())
				;
			// Don't need to reset in case of error.
			reset();
		}

		public void reset() {
			currentState = root;
			cnf = TestUtilFelix.parseCompactCnfString(compact);
			d = new DPLLAlgorithm(cnf);
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
		sb.append(TestUtilFelix.cnfToString(cnf));
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
		sb.append("\nInstead of any of the above, your next stack was:\n");
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
		sb.append(e.choice.toString().charAt(0));
		sb.append(':');
		sb.append(e.variable.getName());
		sb.append('=');
		sb.append(e.variable.getTruthValue().toString().charAt(0));
		sb.append(')');
	}

	public static final void appendString(StackEntryExpectation e,
			StringBuilder sb) {
		sb.append('(');
		sb.append(e.c.toString().charAt(0));
		sb.append(':');
		sb.append(e.varname);
		sb.append('=');
		sb.append(e.tv.toString().charAt(0));
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
			boolean ret = e.choice == c && e.variable.getTruthValue() == tv
					&& e.variable.getName().equals(varname);
			// System.out.println(e+"matches "+this+": "+ret);
			return ret;
		}
	}

	public static final boolean matches(List<StackEntryExpectation> expected,
			List<StackEntry> actual) {
		if (expected.size() != actual.size()) {
			// System.out.println(expected+" has a different size than "+actual);
			return false;
		}
		Iterator<StackEntryExpectation> exp = expected.iterator();
		Iterator<StackEntry> act = actual.iterator();
		while (exp.hasNext()) {
			if (!act.hasNext()) {
				// System.out.println("FAIL!!!!!");
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
		SatSolverTestUpdateTool.doUpdateTest("StackTraceBewiedTest", "1.3.1");
	}
}
