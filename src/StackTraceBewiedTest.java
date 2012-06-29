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

	@Test
	public void testDpllStacktrace() {
		// Für weitere Test-files, eröffnet einfach ein Ticket mit einer
		// entsprechenden CNF und/oder der UPN-Darstellung :)
		runFelixwrappedSuite("testDpllStacktrace",
				parseStacktrace("examples|Ben|SampleDpllStacktrace.txt"));
	}

	// ===== OFFICIAL interface and methods. Feel free to use them!

	public static boolean VERBOSE_PARSING = false;

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
		System.out.print("Running " + tests.size() + " stacktrace tests ... ");
		for (TestInstance test : tests) {
			try {
				test.runAll();
				explanations.add(null);
			} catch (StackMismatchException e) {
				explanations.add(e.getMessage());
			}
		}
		TestUtilFelix.checkFailAndExplain(name, explanations);
		System.out.println("PASS");
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
			System.out.println("COMPLETE");
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
				return true;
			}
			List<StackEntry> stack = d.getStack();
			for (MyState next : currentState.nextStates) {
				List<StackEntryExpectation> expected = next.stack;
				if (matches(expected, stack)) {
					currentState = next;
					return false;
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
			super("While testing your behavior on the cnf " + cnf
					+ ", you hung in state#" + oldState.ID
					+ " and your stack was " + dpllAlgo.getStack()
					+ ". You had the choice between these further states: "
					+ oldState.nextStateIDs);
			this.cnf = cnf;
			this.oldState = oldState;
			this.dpllAlgo = dpllAlgo;
		}
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
		SatSolverTestUpdateTool.doUpdateTest("StackTraceBewiedTest", "1.0");
	}
}
