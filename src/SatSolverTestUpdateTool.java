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

// Prog2 Test Update Tool
// Felix Freiberger, 2012
// Ben Wiederhake, 2012

public class SatSolverTestUpdateTool extends TestCase {
	// ===== Constants, containing the long, descriptive text =====

	private static final String projectID = "project3", version = "1.2",

	NEW_FILES = " new test files were installed. Please, refresh"
			+ " and run the tests again.\n"
			+ "To do this, select the \"test\"-package in the Package"
			+ " Explorer and press F5.",

	UPDATE_COMPLETE = "Update completed. Refresh in Eclipse and rerun.\n"
			+ "To do this, select the Test-package in"
			+ " Package Explorer and press F5.",

	NEW_FILES_FAILED = "Failed to check for new tests"
			+ " because an error occurred.\n",

	DISTRIB = "distribution", SRC = "src", UPDATE = "update";

	private static final boolean NEVER_OPEN_BROWSER = false;
	
	/**
	 * Copied from BufferedInputStream.defaultBufferSize:
	 */
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	// ===== Tests to be run by this "Test" =====

	@Test
	public void test_Update() {
		SatSolverTestUpdateTool
				.doUpdateTest("SatSolverTestUpdateTool", version);
	}

	@Test
	public void test_GetNewTests() {
		int updated = 0;
		String current = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					createURLInputStream(DISTRIB, projectID + ".txt")));

			// Start with root, since there is no way to change it back to root
			// later.
			String releasePath = "";

			while ((current = reader.readLine()) != null) {
				if (current.isEmpty() || isComment(current)) {
					continue;
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
				File file = new File(releasePath + current);
				if (!file.canRead()) {
					// We'll test later whether the filesystem is sane.
					// For now, We want to report THAT there need to be
					// something done.
					System.out.print("Downloading " + current);
					file.getParentFile().mkdirs();
					downloadFile(current, releasePath);
					System.out.println(" -- Completed.");
					updated += 1;
				}
			}
		} catch (IOException e) {
			String suggestion;
			if (current == null) {
				suggestion = "Maybe you are offline?";
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
		String remoteVersion, updatePath;

		// Poll and verify remote version:

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					createURLInputStream(UPDATE, testID + ".version.txt")));
			remoteVersion = reader.readLine();
			updatePath = reader.readLine();
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to connect with repository. Are you online at all?",
					e);
		}

		if (remoteVersion == null || updatePath == null) {
			fail("Failed to check for updates: remoteVersion = "
					+ remoteVersion + ", updatePath = " + updatePath);
		}

		// Check own version:

		if (isUpToDate(currentVersion, remoteVersion)) {
			return;
		}

		// Update if necesary:

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
			System.out
					.println("We tried to send you to a helpful page, but something went wrong.\n"
							+ "Try going there manually: " + url);
		}
	}


	// ===== Internal helper methods =====

	private static final boolean isComment(String s) {
		switch (s.charAt(0)) {
		case ';':
		case ':':
		case '/':
		case '\\':
		case ' ':
		case '-':
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

	private static final void downloadFile(String fileName, String updatePath)
			throws IOException {
		updatePath = updatePath.replace('|', File.separatorChar);
		
		InputStream in = new BufferedInputStream(createURLInputStream(SRC, fileName));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(updatePath + fileName));
		
		final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int bytesRead;
		
		do {
			bytesRead = in.read(buffer);
			if (bytesRead > 0) {
				out.write(buffer, 0, bytesRead);
			}
		} while(bytesRead > 0);

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
}
