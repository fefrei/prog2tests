package prog2.project3.tests;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Prog2 Test Update Tool
 * 
 * @author Felix Freiberger, 2012
 * @author Ben Wiederhake, 2012
 */

public class SatSolverTestUpdateTool extends TestCase {
	// ===== Constants and parameters =====
	// Feel free to change these

	private static final boolean NEVER_OPEN_BROWSER = false;

	/**
	 * 0 is the standard channel.
	 * 
	 * Higher values are used for beta testing. Use only if you know how to fix
	 * any problems. You don't get any advantages if you set this higher.
	 */
	private static final int OWN_CHANNEL = 5;

	// ===== Internal constants =====
	// You shouldn't need to change anything below this

	private static final String PROJECT_ID = "project3", VERSION = "1.5.3",
			DISTRIB = "distribution", SRC = "src", UPDATE = "update",
			NAME = "SatSolverTestUpdateTool";

	/**
	 * If we encounter a "serious" problem when reading from the repository, we
	 * set this to false, so we can PASS (skip) every further test.
	 */
	private static boolean isOffline = false;

	/**
	 * Copied from BufferedInputStream.defaultBufferSize:
	 */
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	// ===== Tests to be run by this "Test" =====

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest(NAME, VERSION);
	}

	@Test
	public void test_GetNewTests() {
		if (isOffline) {
			return;
		}
		BufferedReader reader = null;
		int updated = 0;
		String current = null;
		boolean delete = false;
		try {
			reader = new BufferedReader(new InputStreamReader(
					createURLInputStream(DISTRIB, PROJECT_ID + ".txt")));

			// Start with root, since there is no way to change it back to root
			// later.
			String releasePath = "";

			while ((current = reader.readLine()) != null) {
				if (current.isEmpty() || isComment(current)) {
					continue;
				}
				if (current.charAt(0) == ':') {
					int channel;
					try {
						channel = Integer.valueOf(current.substring(1,
								current.length()));
					} catch (NumberFormatException e) {
						channel = Integer.MIN_VALUE;
					}
					if (OWN_CHANNEL < channel) {
						current = null;
						break;
					} else {
						continue;
					}
				}
				if (current.contains("..")) {
					reader.close();
					fail("Suspicious line '" + current
							+ "' encountered. You might be under attack.");
				}
				if (current.contains("|")) {
					releasePath = current.replace('|', File.separatorChar);
					continue;
				}
				if (current.charAt(0) == '-') {
					delete = true;
					current = current.substring(1);
				}
				File file = new File(releasePath + current);
				if (delete) {
					delete = false;
					if (file.exists()) {
						updated += 1;
						System.out.println("Deleting file " + file);
						if (!file.delete()) {
							System.out.println("\tCouldn't delete"
									+ " -- please remove manually!");
						}
					}
				} else if (!file.canRead()) {
					// If we can read from it, it probably exists, and someone
					// checks for its version already.
					// If we can't read from it, it probably doesn't exists,
					// and we should fetch that file.

					// We'll test later whether the filesystem is sane.
					// For now, We want to know THAT there needs to be
					// something done: downloadFile() cares about that.
					System.out.println("Downloading " + current);
					file.getParentFile().mkdirs();
					downloadFile(current, releasePath);
					System.out.println("\tCompleted.");
					updated += 1;
				}
			}
			reader.close();
			reader = null;
		} catch (IOException e) {
			// We had a problem reading a file that is DEFINITELY there.
			// => This is serious.
			setOffline(PROJECT_ID + "txt");
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					// Ignore
				}
				reader = null;
			}
			String suggestion;
			if (current == null) {
				suggestion = OFFLINE;
			} else {
				suggestion = "It seems that " + current
						+ " is causing the problem.";
			}
			throw new RuntimeException(NEW_FILES_FAILED + suggestion, e);
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
					createURLInputStream(UPDATE, testID + ".version.txt")));
			remoteVersion = reader.readLine();
			updatePath = reader.readLine();
			reader.close();
		} catch (IOException e) {
			// We had a problem reading a file that is MOST DEFINITELY there.
			// => This is "serious".
			setOffline(testID);
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
		boolean couldOpen = !NEVER_OPEN_BROWSER;
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

	// ===== Internal helper methods =====

	private static final void setOffline(String name) {
		isOffline = true;
		System.out.println("\n___________\nEncountered difficulties connecting"
				+ " to file \"" + name + "\" of our repository.\nWarning: All"
				+ " further attempts to connect with the repository are"
				+ " SUPPRESSED, and your tests may be out-of-date.\nPlease"
				+ " make sure that you reconnect soon and update/verify your"
				+ " tests.\n___________\n");
	}

	private static final boolean isComment(String s) {
		switch (s.charAt(0)) {
		case '#':
		case ';':
		case '/':
		case '\\':
		case ' ':
		case '*':
		case '_':
		case '?':
		case '^':
		case '!':
		case '$':
		case '<':
		case '>':
		case '+':
			return true;
		default:
			return false;
		}
		// System.out.println("This should be dead code");
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

	private static final int[] toVersion(String versionString) {
		String[] split = versionString.split("\\.");
		int[] version = new int[split.length];

		for (int i = 0; i < version.length; i++) {
			version[i] = Integer.valueOf(split[i]);
		}

		return version;
	}

	private static final OutputStream fileOutputStream(String fullPath)
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
		return new BufferedOutputStream(new FileOutputStream(file));
	}

	private static final void downloadFile(String fileName, String updatePath)
			throws IOException {
		updatePath = updatePath.replace('|', File.separatorChar);

		InputStream in = new BufferedInputStream(createURLInputStream(SRC,
				fileName));
		// Sorry, but the princess is in another castle:
		// See fileOutputStream() for handling most the edges.
		OutputStream out = fileOutputStream(updatePath + fileName);

		final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int bytesRead;

		do {
			bytesRead = in.read(buffer);
			if (bytesRead > 0) {
				out.write(buffer, 0, bytesRead);
			}
		} while (bytesRead > 0);

		in.close();
		out.close();
	}

	private static final void downloadTest(String testName, String updatePath)
			throws IOException {
		downloadFile(testName + ".java", updatePath);
	}

	private static final InputStream createURLInputStream(String section,
			String filename) throws IOException {
		return new URL("https://prog2tests.googlecode.com/svn/" + section + "/"
				+ filename).openStream();
	}

	private static final String

	NEW_FILES = " new test files were installed. Please, refresh"
			+ " and run the tests again.\n"
			+ "To do this, select the \"test\"-package in the Package"
			+ " Explorer and press F5.",

	UPDATE_COMPLETE = "Update completed. Refresh in Eclipse and rerun.\n"
			+ "To do this, select the Test-package in"
			+ " Package Explorer and press F5.",

	NEW_FILES_FAILED = "Failed to check for new tests",

	FOUND_DIRECTORY = " already exists and is a directory. I won't touch it,"
			+ " since there shouldn't be anything like a directory.",

	INSANE_FS = "\tWarning: File is neither File nor Directory.\n"
			+ "\tFilesystem sane?",

	CANT_WRITE = "\tWarning: Can't write file, I guess: Check"
			+ " file attributes, and lift any write-protection.",

	CANT_CREATE = "Can't create new file. Check write"
			+ " permissions in directory.",

	OFFLINE = "Failed to connect with repository. Make sure you are online.",

	LINK = "Couldn't open browser\n\tTry to open this link manually: ";
}
