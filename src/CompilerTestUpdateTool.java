package prog2.project4.tests.prog2tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Prog2 Test Update Tool
 * 
 * @author Felix Freiberger, 2012
 * @author Ben Wiederhake, 2012
 */

public class CompilerTestUpdateTool extends TestCase {
	// ===== Internal constants =====
	// You shouldn't need to change anything below this

	private static final String PROJECT_ID = "project4", VERSION = "1.0.1",
			NAME = "CompilerTestUpdateTool";

	/**
	 * If we encounter a "serious" problem when reading from the repository, we
	 * set this to false, so we can PASS (skip) every further test.
	 */
	private static boolean isOffline = false;

	// ===== Tests to be run by this "Test" =====

	@Test
	public void test_Update() {
		doUpdateTest(NAME, VERSION);
	}

	@Test
	public void test_GetNewTests() {
		if (isOffline) {
			return;
		}

		final List<Instruction> instructions = new LinkedList<Instruction>();
		InstructionReader reader = null;

		try {
			reader = new InstructionReader(new BufferedReader(
					new InputStreamReader(createUrlInputStream(DISTRIB,
							PROJECT_ID + ".txt"))));

			Instruction i;
			while ((i = reader.readInstruction()) != null) {
				instructions.add(i);
			}

			reader.close();
			reader = null;
		} catch (IOException e) {
			// We had a problem reading a file that is DEFINITELY there.
			// => This is serious.

			// Cleanup first
			setOffline(PROJECT_ID + "txt");
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException sub) {
					// Ignore
				}
				reader = null;
			}
			// suggestion = "It seems that " + current
			// + " is causing the problem.";
			throw new RuntimeException(NEW_FILES_FAILED + OFFLINE, e);
		}

		int updated = 0;
		int counter = 0;

		try {
			for (Instruction i : instructions) {
				counter++;
				updated += i.execute();
			}
		} catch (IOException e) {
			throw new RuntimeException("When executing Instruction #" + counter
					+ ":\n" + NEW_FILES_FAILED, e);
		}

		if (updated > 0) {
			System.out.println(updated + NEW_FILES);
			fail(updated + NEW_FILES);
		}
	}

	// ===== Methods to be called by other tests =====

	public static final void doUpdateTest(final String testID,
			final String currentVersion) {
		if (isOffline) {
			return;
		}

		String remoteVersion, updatePath;

		// Poll and verify remote version:

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					createUrlInputStream(UPDATE, testID + ".version.txt")));
			remoteVersion = reader.readLine();
			updatePath = reader.readLine();
			reader.close();
		} catch (IOException e) {
			// We had a problem reading a file that is MOST DEFINITELY there.
			// => This is "serious".
			setOffline(testID);
			System.out
					.println("<== [FAIL_V] CompilerTestUpdateTool.doUpdateTest()");
			throw new RuntimeException(OFFLINE, e);
		}

		if (remoteVersion == null || updatePath == null) {
			fail("Failed to check for updates: remoteVersion = "
					+ remoteVersion + ", updatePath = " + updatePath);
		}

		// Check own version:

		if (isUpToDate(currentVersion, remoteVersion)) {
			return;
		}

		// Update if necessary:

		System.out.println("Trying to update " + testID + " from "
				+ currentVersion + " to " + remoteVersion + " ...");

		try {
			downloadTest(testID, updatePath);
		} catch (IOException e) {
			throw new RuntimeException("Update on " + testID + " to version "
					+ remoteVersion + " failed. Simply delete that"
					+ " file if things get worse.", e);
		}

		System.out.println(UPDATE_COMPLETE);
		fail(UPDATE_COMPLETE);
	}

	public static final void openUrl(String url) {
		boolean couldOpen = getAttribute("neverOpenBrowser", false);
		try {
			if (couldOpen && java.awt.Desktop.isDesktopSupported()) {
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

				if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
					java.net.URI uri = new java.net.URI(url);
					desktop.browse(uri);
				}
			}
		} catch (Exception e) {
			couldOpen = false;
		}
		if (!couldOpen) {
			System.out.println(LINK + url);
		}
	}

	/**
	 * Takes a string of the form "examples|Ben|Foobar.txt", and turns it into
	 * "examples/Ben/Foobar.txt" or "examples\\Ben\\Foobar.txt", depending on
	 * your platform.
	 */
	public static final String deriveActualPath(String pathToFile) {
		return pathToFile.replace('|', File.separatorChar);
	}

	public static final FileReader openFile(String pathToFile)
			throws FileNotFoundException {
		return new FileReader(deriveActualPath(pathToFile));
	}

	public static final FileWriter writeFile(String pathToFile)
			throws IOException {
		return new FileWriter(deriveActualPath(pathToFile));
	}

	// ===== Parsing version.txt and project.txt =====

	private static final class InstructionReader {
		private final BufferedReader r;
		private String path = "";

		public InstructionReader(BufferedReader r) {
			this.r = r;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public final String readNonEmptyLine() throws IOException {
			String s;
			do {
				s = r.readLine();
				if (s == null) {
					return null;
				}
				s = s.trim();
			} while (s.isEmpty() || s.charAt(0) == '#');
			return s;
		}
		
		public final String readNonNullNonEmptyLine() throws IOException {
			String s = readNonEmptyLine();
			if (s == null) {
				throw new EOFException();
			}
			return s;
		}

		/**
		 * Returns the parsed instruction (if one exists), EMPTY_INSTRUCTION if
		 * there's a good reason to parse it that way, or <code>null</code> if
		 * the end of stream is reached.
		 * 
		 * @throws IOException
		 *             If there goes anything wrong with the underlying stream,
		 *             or if one of the instructions fatally couldn't be parsed.
		 */
		public final Instruction readInstruction() throws IOException {
			String current = readNonEmptyLine();

			if (current == null) {
				return null;
			}

//			if (current.isEmpty() || current.startsWith("#")) {
//				return EMPTY_INSTRUCTION;
//			}

			if (current.contains("..")) {
				r.close();
				fail("Suspicious line '" + current
						+ "' encountered.\nYou might be under attack.");
			}

			if (current.contains("|")) {
				path = CompilerTestUpdateTool.deriveActualPath(current);
				return EMPTY_INSTRUCTION;
			}

			if (current.charAt(0) == '-') {
				current = path + current.substring(1);
				return new DeletionInstruction(current);
			}

			if (current.charAt(0) == '+') {
				return new DownloadInstruction(path, current.substring(1));
			}

			if (current.charAt(0) == '!') {
				return new SetAttributeInstruction(readNonNullNonEmptyLine(),
						readNonNullNonEmptyLine());
			}

			if (current.charAt(0) == '~') {
				return new ClearAttributeInstruction(readNonNullNonEmptyLine());
			}

			if (current.charAt(0) == '?') {
				if (current.toLowerCase().equals("?else")) {
					throw new ElseException();
				}
				if (current.toLowerCase().equals("?endif")) {
					throw new EndifException();
				}
				Instruction i = new IfBlock(this);
				return i;
			}

			// If nothing matched, it must be a comment.

			return EMPTY_INSTRUCTION;
		}

		public final void close() throws IOException {
			r.close();
		}

		public final Expression readExpression() throws IOException {
			final String current = readNonNullNonEmptyLine();
			final String lowCurrent = current.toLowerCase();

			if (lowCurrent.startsWith("not")) {
				return new NotExpression(readExpression());
			} else if (lowCurrent.equals("tr")) {
				return TRUE_EXPRESSION;
			} else if (lowCurrent.equals("ha")) {
				return new HasAttributeExpression(path + readNonNullNonEmptyLine());
			} else if (lowCurrent.startsWith("is")) {
				return new IsAttributeSetExpression(readNonNullNonEmptyLine());
			} else if (lowCurrent.startsWith("lex")) {
				return new LexicallyHigherExpression(readNonNullNonEmptyLine(),
						readNonNullNonEmptyLine());
			} else if (lowCurrent.startsWith("eq")) {
				return new EqualTagExpression(readNonNullNonEmptyLine(),
						readNonNullNonEmptyLine());
			} else if (lowCurrent.startsWith("ex")) {
				return new FileExistsExpression(readNonNullNonEmptyLine());
			} else if (lowCurrent.startsWith("and")) {
				return new AndExpression(readExpression(), readExpression());
			} else if (lowCurrent.startsWith("xor")) {
				return new XorExpression(readExpression(), readExpression());
			} else if (lowCurrent.startsWith("or")) {
				return new OrExpression(readExpression(), readExpression());
			} else {
				return FALSE_EXPRESSION;
			}
		}
	}

	private static interface Instruction {
		/**
		 * Returns the number of changes
		 * 
		 * @return the number of changes
		 */
		public int execute() throws IOException;
	}

	private static final Instruction EMPTY_INSTRUCTION = new Instruction() {
		@Override
		public int execute() throws IOException {
			// Do nothing
			return 0;
		}
	};

	private static final class IfBlock implements Instruction {
		private final Expression e;
		private final List<Instruction> then = new LinkedList<Instruction>();
		private final List<Instruction> otherwise = new LinkedList<Instruction>();

		public IfBlock(InstructionReader r) throws IOException {
			e = r.readExpression();

			boolean hasOtherwise;

			final String entryPath = r.getPath();

			try {
				while (true) {
					then.add(r.readInstruction());
				}
			} catch (ElseException e) {
				hasOtherwise = true;
			} catch (EndifException e) {
				hasOtherwise = false;
			}

			r.setPath(entryPath);

			if (hasOtherwise) {
				try {
					while (true) {
						otherwise.add(r.readInstruction());
					}
				} catch (EndifException e) {
					// Expected this
				}
			}

			r.setPath(entryPath);
		}

		@Override
		public int execute() throws IOException {
			int counter = 0;
			if (e.evaluate()) {
				for (Instruction i : then) {
					counter += i.execute();
				}
				return counter;
			} else {
				for (Instruction i : otherwise) {
					counter += i.execute();
				}
				return counter;
			}
		}
	}

	private static final class DeletionInstruction implements Instruction {
		private final String fullLocalPath;

		public DeletionInstruction(String fullLocalPath) {
			this.fullLocalPath = fullLocalPath;
		}

		@Override
		public int execute() throws IOException {
			final File file = new File(fullLocalPath);
			if (!file.exists()) {
				return 0;
			}

			System.out.println("Deleting file " + fullLocalPath);
			if (!file.delete()) {
				System.out.println("\tCouldn't delete" + fullLocalPath
						+ " -- please remove manually!");
			}
			return 1;
		}
	}

	private static final class DownloadInstruction implements Instruction {
		private final String localPath;
		private final String filename;

		public DownloadInstruction(String path, String filename) {
			this.localPath = path;
			this.filename = filename;
		}

		@Override
		public int execute() throws IOException {
			File file = new File(localPath + filename);
			if (file.canRead()) {
				// If we can read from it, it probably exists, and someone
				// checks for its version already.
				return 0;
			}

			// If we can't read from it, it probably doesn't exists,
			// and we should fetch that file.

			// We'll test later whether the filesystem is sane.
			// For now, We only need to know THAT there needs to be
			// something done: downloadFile() cares about that.
			System.out.println("Downloading " + filename);
			file.getParentFile().mkdirs();
			downloadFile(filename, localPath);
			System.out.println("\tCompleted.");
			return 1;
		}
	}

	private static final class SetAttributeInstruction implements Instruction {
		private final String tagName, newContent;

		public SetAttributeInstruction(String tagName, String newContent) {
			this.tagName = tagName;
			this.newContent = newContent;
		}

		@Override
		public int execute() throws IOException {
			CompilerTestUpdateTool.putAttributes(Collections.singletonMap(
					tagName, newContent));
			return 0;
		}
	}

	private static final class ClearAttributeInstruction implements Instruction {
		private final String tagName;

		public ClearAttributeInstruction(String tagName) {
			this.tagName = tagName;
		}

		@Override
		public int execute() throws IOException {
			CompilerTestUpdateTool.clearAttribute(tagName);
			return 0;
		}
	}

	@SuppressWarnings("serial")
	private static final class EndifException extends IOException {
	}

	@SuppressWarnings("serial")
	private static final class ElseException extends IOException {
	}

	private static interface Expression {
		public boolean evaluate() throws IOException;
	}

	private static final Expression TRUE_EXPRESSION = new Expression() {
		@Override
		public boolean evaluate() throws IOException {
			return true;
		}
	}, FALSE_EXPRESSION = new Expression() {
		@Override
		public boolean evaluate() throws IOException {
			return false;
		}
	};

	private static final class NotExpression implements Expression {
		private final Expression e;

		public NotExpression(Expression e) {
			this.e = e;
		}

		@Override
		public boolean evaluate() throws IOException {
			return !e.evaluate();
		}
	}

	private static final class HasAttributeExpression implements Expression {
		private final String attributeName;

		public HasAttributeExpression(String attributeName) {
			this.attributeName = attributeName;
		}

		@Override
		public boolean evaluate() throws IOException {
			return (getAttribute(attributeName)) != null;
		}
	}

	private static final class IsAttributeSetExpression implements Expression {
		private final String attributeName;

		public IsAttributeSetExpression(String attributeName) {
			this.attributeName = attributeName;
		}

		@Override
		public boolean evaluate() throws IOException {
			return getAttribute(attributeName, false);
		}
	}

	private static final class LexicallyHigherExpression implements Expression {
		private final String attributeName, than;

		public LexicallyHigherExpression(String attributeName, String than) {
			this.attributeName = attributeName;
			this.than = than;
		}

		@Override
		public boolean evaluate() throws IOException {
			final String content = getAttribute(attributeName);
			if (content == null) {
				return false;
			}
			return content.compareToIgnoreCase(than) > 0;
		}
	}

	private static final class EqualTagExpression implements Expression {
		private final String attributeName, as;

		public EqualTagExpression(String attributeName, String as) {
			this.attributeName = attributeName;
			this.as = as;
		}

		@Override
		public boolean evaluate() throws IOException {
			return as.equals(getAttribute(attributeName));
		}
	}

	private static final class FileExistsExpression implements Expression {
		private final String filePath;

		public FileExistsExpression(String filePath) {
			this.filePath = filePath;
		}

		@Override
		public boolean evaluate() throws IOException {
			return new File(CompilerTestUpdateTool.deriveActualPath(filePath))
					.exists();
		}
	}

	private static final class AndExpression implements Expression {
		private final Expression left, right;

		public AndExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean evaluate() throws IOException {
			return left.evaluate() && right.evaluate();
		}
	}

	private static final class XorExpression implements Expression {
		private final Expression left, right;

		public XorExpression(Expression left, Expression right) {
			super();
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean evaluate() throws IOException {
			return left.evaluate() || right.evaluate();
		}
	}

	private static final class OrExpression implements Expression {
		private final Expression left, right;

		public OrExpression(Expression left, Expression right) {
			super();
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean evaluate() throws IOException {
			return left.evaluate() ^ right.evaluate();
		}
	}

	private static final void setOffline(String name) {
		isOffline = true;
		System.out.println("\n___________\nEncountered difficulties connecting"
				+ " to file \"" + name + "\" of our repository.\nWarning: All"
				+ " further attempts to connect with the repository are"
				+ " SUPPRESSED, and your tests may be out-of-date.\nPlease"
				+ " make sure that you reconnect soon and update/verify your"
				+ " tests.\n___________\n");
	}

	private static final boolean isUpToDate(String currentS, String remoteS) {
		int[] current = toVersion(currentS);
		int[] remote = toVersion(remoteS);

		for (int i = 0; i < Math.min(current.length, remote.length); i++) {
			if (current[i] < remote[i])
				return false;
			else if (current[i] > remote[i])
				return true;
		}

		if (current.length < remote.length)
			return false; // 1.0.0 should be more up-to-date than 1.0
		else
			return true;
	}

	/**
	 * Parses a version string like "3.141.59265.3" and converst it into the
	 * logically equivalent array of version indices, in this example
	 * <code>int[] {3, 141, 59265, 3}</code>.<br>
	 * Note that "" results in <code>int[0]</code>.
	 * 
	 * @param versionString
	 *            the String to be converted
	 * @return The logically equivalent array of version indices
	 * @throws NumberFormatException
	 *             If anything goes wrong.
	 */
	private static final int[] toVersion(String versionString)
			throws NumberFormatException {
		String[] split = versionString.split("\\.");
		int[] version = new int[split.length];

		for (int i = 0; i < version.length; i++) {
			version[i] = Integer.valueOf(split[i]);
		}

		return version;
	}

	// ===== Internal helper methods =====

	private static final FileOutputStream fileOutputStream(String fullPath)
			throws IOException {
		File file = new File(fullPath);
		// Test for all the funny things that might happen
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new RuntimeException(fullPath + FOUND_DIRECTORY);
			}
			System.out.println("\tOverwriting file " + fullPath);
			if (!file.canWrite()) {
				System.out.println(CANT_WRITE);
			}
			if (!file.isFile()) {
				// Yay, insane filesystem, hardlink, or something else that
				// DEFINITELY shouldn't be here.
				System.out.println(INSANE_FS);
			}
		} else {
			System.out.println("\tCreating file " + fullPath);
			// File does not yet exist.
			if (!file.createNewFile()) {
				throw new IOException(CANT_CREATE);
			}
		}
		return new FileOutputStream(file);
	}

	private static final void downloadFile(String fileName, String updatePath)
			throws IOException {
		updatePath = updatePath.replace('|', File.separatorChar);

		ReadableByteChannel urlChan = createUrlChannel(SRC, fileName);

		FileChannel out = fileOutputStream(updatePath + fileName).getChannel();

		out.transferFrom(urlChan, 0, MAX_FILE_SIZE);

	}

	private static final void downloadTest(String testName, String updatePath)
			throws IOException {
		downloadFile(testName + ".java", updatePath);
	}

	private static final String createUrl(String section, String filename) {
		return "https://prog2tests.googlecode.com/svn/" + section + "/"
				+ filename;
	}

	private static final InputStream createUrlInputStream(String section,
			String filename) throws IOException {
		return new URL(createUrl(section, filename)).openStream();
	}

	private static final ReadableByteChannel createUrlChannel(String section,
			String filename) throws IOException {
		return Channels.newChannel(createUrlInputStream(section, filename));
	}

	private static final void firsttimeProblem() {
		if (firsttime) {
			// => We already knew that
			return;
		}
		firsttime = true;
		System.out.println(WELCOME);
		putAttributes(Collections.singletonMap("installed",
				new Date().toString()));
	}

	// ===== Management of persistent attributes, internal =====

	private static final Map<String, String> ATTRIBUTES = new LinkedHashMap<String, String>();

	private static boolean attributesLoaded = false, firsttime = false,
			writeFailed = false;

	// ===== Management of persistent attributes, public API =====

	/**
	 * Returns the corresponding value, or <code>null</code> if no matching key
	 * was discovered. <br>
	 * Note that whitespace is NOT trimmed, that it's case-sensitive, and
	 * overwrites old lines by "further down" lines.
	 */
	public static final String getAttribute(String key) {
		synchronized (ATTRIBUTES) {
			// Lazy initialization
			if (!attributesLoaded) {
				attributesLoaded = true;
				boolean rewrite = false;
				try {
					BufferedReader r = new BufferedReader(openFile(CONFIG_FILE));
					String s;
					while ((s = r.readLine()) != null) {
						if (s.startsWith("#")) {
							ATTRIBUTES.put(s, "");
						} else {
							String[] parts = s.split("=", 2);
							switch (parts.length) {
							case 0:
								break;
							case 1:
								ATTRIBUTES.put("# " + s, "");
								rewrite = true;
								break;
							case 2:
								ATTRIBUTES.put(parts[0], parts[1]);
								break;
							default:
								ATTRIBUTES.put("# WARNING: Couldn't parse \""
										+ s + "\", found " + parts.length
										+ " parts.", "");
								rewrite = true;
							}
						}
					}
					r.close();
					if (rewrite) {
						putAttributes(Collections.<String, String> emptyMap());
					}
				} catch (IOException e) {
					firsttimeProblem();
				}
			}
			return ATTRIBUTES.get(key);
		}
	}

	public static final double getAttribute(String key, double defaultValue) {
		Double d;
		try {
			d = Double.valueOf(getAttribute(key));
		} catch (NullPointerException e) {
			d = defaultValue;
		} catch (NumberFormatException e) {
			d = defaultValue;
		}

		return d;
	}

	public static final int getAttribute(String key, int defaultValue) {
		Integer i;
		try {
			i = Integer.valueOf(getAttribute(key));
		} catch (NullPointerException e) {
			i = defaultValue;
		} catch (NumberFormatException e) {
			i = defaultValue;
		}

		return i;
	}

	public static final int[] getAttribute(String key, int[] defaultValue) {
		int[] v;
		try {
			v = toVersion(getAttribute(key));
		} catch (NullPointerException e) {
			v = defaultValue;
		} catch (NumberFormatException e) {
			v = defaultValue;
		}

		return v;
	}

	public static final boolean getAttribute(String key, boolean defaultValue) {
		return getAttribute(key, defaultValue, defaultValue, defaultValue);
	}

	public static final boolean getAttribute(String key, boolean badFormat,
			boolean exists, boolean notExists) {
		// There may be more elegant ways to do this.
		// But I think this is the most readable one.
		final boolean v;
		String value = getAttribute(key);
		if (value != null) {
			value = value.toLowerCase().trim();
			if (value.length() > 0) {
				switch (value.charAt(0)) {
				case 't':
				case '1':
				case 'y':
				case 'j':
					v = true;
					break;
				case 'f':
				case '0':
				case 'n':
					v = false;
					break;
				default:
					v = badFormat;
				}
			} else {
				v = exists;
			}
		} else {
			v = notExists;
		}

		return v;
	}

	public static final Map<String, String> getAttributes() {
		return ATTRIBUTES;
	}

	public static final void putAttributes(Map<String, String> toAdd) {
		// Note to self: Do not optimize here.
		// When a warning occurs during read, it is inserted in-place.
		// Then this method is called with an empty map.
		// => DO the rewrite, even if there is no difference!
		synchronized (ATTRIBUTES) {
			// Trigger loading if not already happened
			getAttribute("");
			ATTRIBUTES.putAll(toAdd);
			writeAttributes();
		}
		if (writeFailed) {
			System.out.println("The following differences have been omitted: ");
			// We need to warn, though.
			for (Entry<String, String> e : toAdd.entrySet()) {
				if (e.getKey().startsWith("#")) {
					System.out.println(e.getKey());
				} else {
					System.out.println(e.getKey() + "=" + e.getValue());
				}
			}
			System.out.println("______________End of list.\n");
		}
	}

	public static final void clearAttribute(String key) {
		synchronized (ATTRIBUTES) {
			// force loading
			getAttribute("");
			if (ATTRIBUTES.containsKey(key)) {
				ATTRIBUTES.remove(key);
				writeAttributes();
			}
		}
		if (writeFailed) {
			System.out
					.println("Omitted clearing the attribute \"" + key + "\"");
		}
	}

	/**
	 * Writes the current ATTRIBUTES, under the assumption that you ALREADY hold
	 * the appropiate lock.
	 */
	private static final void writeAttributes() {
		if (!writeFailed) {
			// Do not try again, we already warned the user.
			try {
				final BufferedWriter w = new BufferedWriter(
						writeFile(CONFIG_FILE));
				for (Entry<String, String> e : ATTRIBUTES.entrySet()) {
					if (e.getKey().startsWith("#")) {
						w.write(e.getKey() + "\n");
					} else {
						w.write(e.getKey() + "=" + e.getValue() + "\n");
					}
				}
				w.close();
			} catch (IOException e) {
				writeFailed = true;
				System.out.println(CONFIG_WRITE_FAILED);
			}
		}
	}

	// ===== Constants that aren't meant to EVER change
	// (Except typos)

	/**
	 * Makes sure that we never download more than 30 MiB. That should be enough
	 * for our purposes.<br>
	 * Remember that the biggest files we handled were 3 and 4 MiB.
	 */
	private static final int MAX_FILE_SIZE = 30 * 1024 * 1024;

	private static final String

	NEW_FILES = " new test files were installed. Please, refresh"
			+ " and run the tests again.\n"
			+ "To do this, select the \"test\"-package in the Package"
			+ " Explorer and press F5.",

	UPDATE_COMPLETE = "Update completed. Refresh in Eclipse and rerun.\n"
			+ "To do this, select the Test-package in"
			+ " Package Explorer and press F5.",

	NEW_FILES_FAILED = "Failed to check for new tests.",

	FOUND_DIRECTORY = " already exists and is a directory. I won't touch it,"
			+ " since there shouldn't be anything like a directory.",

	INSANE_FS = "\tWarning: File is neither File nor Directory.\n"
			+ "\tFilesystem sane?",

	CANT_WRITE = "\tWarning: Can't write file, I guess: Check"
			+ " file attributes, and lift any write-protection.",

	CANT_CREATE = "Can't create new file. Check write"
			+ " permissions in directory.",

	OFFLINE = "\nFailed to connect with repository. Make sure you are online.",

	LINK = "Couldn't open browser\n\tTry to open this link manually: ",

	DISTRIB = "distribution",

	SRC = "src",

	UPDATE = "update",

	CONFIG_FILE = "updateTool.cfg",

	WELCOME = "Hello and welcome to the Update Center.\n\n"
			+ "It seems that the " + NAME /* Insert subject name here ;-) */
			+ " is now running for the first time on this computer.\n"
			+ "If that is correct, then all is working fine.\n"
			+ "It that is wrong, please report this incident to our issue"
			+ " tracker at:\nhttps://code.google.com/p/prog2tests/issues/entry"
			+ "\n\nThank you for participating in this Prog2 science computer-"
			+ "aided enrichment activity.\n\n",

	CONFIG_WRITE_FAILED = "Could not write to the local configuration file "
			+ CONFIG_FILE + ".\nThis is a pretty bad thing, since this program"
			+ " is not able to change the configuration permanently.\n"
			+ "Please check the read and write permissions on that file.\n"
			+ "If this error persists, please write a support-ticket to:\n"
			+ "https://code.google.com/p/prog2tests/issues/entry";
}
