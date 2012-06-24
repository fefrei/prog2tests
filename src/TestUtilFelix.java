package prog2.project3.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

import prog2.project3.cnf.Clause;
import prog2.project3.cnf.Cnf;
import prog2.project3.cnf.Literal;

public class TestUtilFelix {
	static final String VERSION = "1.1";

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("TestUtilFelix", VERSION);
	}

	private static String combineStrings(List<String> coll, String sep) {
		// sorts all strings, then adds them together, inserting separators
		StringBuilder buf = new StringBuilder();

		java.util.Collections.sort(coll);

		for (String item : coll) {
			if (buf.length() > 0)
				buf.append(sep);
			buf.append(item);
		}

		return buf.toString();
	}

	/*
	 * Returns a string representation of a Literal object.
	 */
	public static String literalToString(Literal lit) {
		return lit.isNegatedLiteral() ? "~" + lit.getVariable().getName() : lit
				.getVariable().getName();
	}

	/*
	 * Returns a string representation of a Clause object.
	 */
	public static String clauseToString(Clause clause) {
		Collection<Literal> literals = clause.getLiterals();
		List<String> literalStrings = new LinkedList<String>();

		for (Literal item : literals) {
			literalStrings.add(literalToString(item));
		}

		return "(" + combineStrings(literalStrings, " \\/ ") + ")";
	}

	/*
	 * Returns a string representation of a Cnf object.
	 */
	public static String cnfToString(Cnf cnf) {
		Collection<Clause> clauses = cnf.getClauses();
		List<String> clauseStrings = new LinkedList<String>();

		for (Clause item : clauses) {
			clauseStrings.add(clauseToString(item));
		}

		return combineStrings(clauseStrings, " /\\ ");
	}

	private static String getVar(int id) {
		return ((char) (97 + id)) + " ";
	}

	private static void appendBinaryOperator(StringBuilder buf, Random rnd) {
		switch (rnd.nextInt(4)) {
		case 0:
			buf.append("&& ");
			break;
		case 1:
			buf.append("|| ");
			break;
		case 2:
			buf.append("=> ");
			break;
		default:
			buf.append("<=> ");
			break;
		}
	}

	/*
	 * Generates a valid formula.
	 * Length sets an upper limit to how complex the formula will be.
	 */
	public static String generateRandomFormula(int length) {
		int remainingLength = length * 3;

		if (length < 1 || length > 26)
			throw new IllegalArgumentException(
					"TestUtilFelix.generateRandomFormula: length invalid");

		StringBuilder buf = new StringBuilder();

		Random rnd = new Random();

		buf.append(getVar(rnd.nextInt(length)));
		buf.append(getVar(rnd.nextInt(length)));
		int depth = 2;

		while (depth > 1) {
			// all operators available
			switch (rnd.nextInt(4)) {
			case 0:
				if (remainingLength <= 0)
					continue; // no more variables to get to an end
				buf.append(getVar(rnd.nextInt(length)));
				depth++;
				break;
			case 1:
				buf.append("! ");
				break;
			default:
				if (depth < 2)
					continue; // too few opearands
				if (depth == 2 && remainingLength > 0)
					continue; // we don't want to stop that soon
				appendBinaryOperator(buf, rnd);
				depth -= 1;
				break;
			}
			remainingLength--;
		}

		return buf.toString();
	}

	/*
	 * Fails and prints the given list and messages.
	 * Only pronts a reasonable amount of messages.
	 */
	public static void failAndExplain(String testName, int testCount,
			Integer[] failedTests, String[] failureMessages) {

		System.out.println("_________________________________________________");
		System.out.println("FAILURE: You failed the test " + testName + ".");
		System.out.println("This test has " + testCount + " subtests.");
		System.out.println("Your failed " + failedTests.length
				+ " subtest(s) and passed " + (testCount - failedTests.length)
				+ " subtest(s).\n");
		if (failedTests.length > testCount * 0.2) {
			System.out
					.println("This indicates that there is a general problem "
							+ "with your implementation.\n");
		} else {
			System.out.println("This indicates that there are some "
					+ "edge-cases you did not think of.\n");
		}

		System.out
				.println("The following information may or may not be helpful. "
						+ "If you don't understand it, file a support ticket here: "
						+ "https://code.google.com/p/prog2tests/issues/entry?template=support\n");

		System.out.print("You failed the following subtest(s):");
		for (int i = 0; i < Math.min(failedTests.length, 50); i++)
			System.out.print(" " + failedTests[i]);
		if (failedTests.length > 50)
			System.out.print(" (and " + (failedTests.length - 50)
					+ "more subtest(s)");
		System.out.println(".\n");

		if (failureMessages.length > 0) {
			System.out.println("Some subtests left a message for you:");
			for (int i = 0; i < Math.min(failedTests.length, 3); i++) {
				System.out.println("\nMessage " + (i + 1)
						+ ": ______________________________________");
				System.out.println(failureMessages[i]);
			}
			if (failedTests.length > 3) {
				System.out
						.println("\nInformation: ____________________________________");
				System.out.print((failureMessages.length - 3)
						+ " more message(s) were not printed.");
			}
		}

		System.out.println("\nEnd of failure message for test " + testName);
		System.out.println("_________________________________________________");
		System.out.println("\n");

		fail("You failed " + failedTests.length
				+ " subtest(s) of this test.\nSee the console for details.");
	}

	/*
	 * Fails and prints the given list and messages.
	 * Only pronts a reasonable amount of messages.
	 */
	public static void failAndExplain(String testName, int testCount,
			List<Integer> failedTests, List<String> failureMessages) {
		failAndExplain(testName, testCount,
				failedTests.toArray(new Integer[0]),
				failureMessages.toArray(new String[0]));
	}

	/*
	 * Fails if results contains a string that is not null.
	 * Prints beautiful error messages.
	 */
	public static void checkFailAndExplain(String testName, String[] results) {
		int testCount = results.length;

		List<Integer> failedTests = new LinkedList<Integer>();
		List<String> failureMessages = new LinkedList<String>();

		for (int i = 0; i < testCount; i++) {
			if (results[i] != null) {
				failedTests.add(i + 1);
				failureMessages.add(results[i]);
			}
		}

		if (failedTests.size() > 0) {
			failAndExplain(testName, testCount, failedTests, failureMessages);
		}
	}

	/*
	 * Fails if results contains a string that is not null.
	 * Prints beautiful error messages.
	 */
	public static void checkFailAndExplain(String testName, List<String> results) {
		checkFailAndExplain(testName, results.toArray(new String[0]));
	}

	/*
	 * reads a file, returning a string list
	 */
	public static List<String> parseDataFile(String pathToFile) {
		pathToFile = pathToFile.replace('|', File.separatorChar);
		File file = new File(pathToFile);

		if (!file.canRead())
			throw new RuntimeException(
					"A test file was not found. Try running the update tool again.\n"
							+ "If that doesn't help, file a ticket.");

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			List<String> lines = new LinkedList<String>();

			String lastLine = reader.readLine();
			while (lastLine != null) {
				lines.add(lastLine);
				lastLine = reader.readLine();
			}

			return lines;
		} catch (Exception e) {
			throw new RuntimeException(
					"A test file could not be read. Try running the update tool again.\n"
							+ "If that doesn't help, file a ticket.");
		}
	}
}
