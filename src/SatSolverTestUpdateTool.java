package prog2.project3.tests;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;

// Prog2 Test Update Tool
// Felix Freiberger, 2012
// ONLY FOR USE IN PROJECT 2

public class SatSolverTestUpdateTool extends TestCase {
	static final String projectID = "project3";
	static final String version = "1.0";
	
	@Test
	public void test_Update() {
		SatSolverTestUpdateTool.doUpdateTest("SatSolverTestUpdateTool", version);
	}
	
	@Test
	public static final void test_GetNewTests() {
		try {
			URL url = new URL("https://prog2tests.googlecode.com/svn/distribution/" + projectID + ".txt");
			InputStreamReader streamReader = new InputStreamReader(
					url.openStream());
			BufferedReader reader = new BufferedReader(streamReader);
			String releasePath = "";
			List<String> releaseInfo = new ArrayList<String>();
			while(true) {
				String in = reader.readLine();
				if(in == null) {
					break;
				} else {
					if(in.length() > 0) {
						releaseInfo.add(in);
					}
				}
			}
			
			reader.close();
			streamReader.close();
			
			
			boolean newTestsInstalled = false;
			
			for(String item : releaseInfo) {
				if (item.contains("|")) {
					releasePath = item.replace('|', File.separatorChar);
				} else {
					File file = new File(releasePath + item);
					if(!file.exists()) {
						System.out.println("A new test file is available and will be downloaded: " + item);
						file.mkdirs();
						downloadFile(item, releasePath);
						newTestsInstalled = true;
					}
				}
			}
			
			if(newTestsInstalled) {
				System.out.println("New test files were installed. Please, refresh and run the tests again.\n" +
						"To do this, select the Test-package in Package Explorer and press F5.");
				fail("New test files were installed. Please, refresh and run the tests again.");
			}
		} catch (Exception e) {
			fail("Failed to check for new tests because an error occurred. Maybe you are offline?\n"
					+ "That happened: " + e.getMessage());
		}
	}
	
	public static final void doUpdateTest(final String testID,
			final String currentVersion) {
		try {
			URL url = new URL("https://prog2tests.googlecode.com/svn/update/"
					+ testID + ".version.txt");
			InputStreamReader streamReader = new InputStreamReader(
					url.openStream());
			BufferedReader reader = new BufferedReader(streamReader);
			String remoteVersion = reader.readLine();
			String updatePath = reader.readLine();
			reader.close();
			streamReader.close();

			if (remoteVersion == null) {
				fail("Failed to check for updates, the remote version string is null");
			}

			if (!isUpToDate(currentVersion, remoteVersion)) {
				System.out.println("You test is out of date.\n" + "You have version "
						+ currentVersion + ", but the current version is " + remoteVersion + "\n" +
								"You will be auto-updated to that version...");
				
				try {
					downloadTest(testID, updatePath);
					System.out.println("Update completed. Refresh in Eclipse and rerun.\n" +
							"To do this, select the Test-package in Package Explorer and press F5.");
					fail("Update completed. Refresh in Eclipse and rerun.");
				} catch (Exception e) {
					System.out.println("ERROR: " + e.getMessage());
					fail("Your version is out-of-date, but the update failed.");
				}
			}

			return; // pass
		} catch (Exception e) {
			fail("Failed to check for updates because an error occurred. Maybe you are offline?\n"
					+ "That happened: " + e.getMessage());
		}
	}

	public static final void openUrl(String url) {
    	try {
    	      if(java.awt.Desktop.isDesktopSupported() ) {
    	            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
    	     
    	            if(desktop.isSupported(java.awt.Desktop.Action.BROWSE) ) {
    	              java.net.URI uri = new java.net.URI(url);
    	                  desktop.browse(uri);
    	            }
    	          }
    	} catch (Exception e) {
    		System.out.println("We tried to send you to a helpful page, but something went wrong.\n" +
    				"Try going there manually: " + url);
    	}
    }
	
	private static final boolean isUpToDate(String currentS, String remoteS) {
		int[] current = toVersion(currentS);
		int[] remote = toVersion(remoteS);
		
		for(int i = 0; i < Math.min(current.length, remote.length); i++) {
			if (current[i] < remote[i])
				return false;
			else if (current[i] > remote[i])
				return true;
		}
		
		if(current.length < remote.length)
			return false; //1.0.0 should be more up-to-date than 1.0
		else
			return true;
	}
	
	private static final int[] toVersion(String versionString) {
		String[] split = versionString.split("\\.");
		int[] version = new int[split.length];
		
		for(int i = 0; i < version.length; i++) {
			version[i] = Integer.valueOf(split[i]);
		}
		
		return version;
	}
	
	private static final void downloadFile(String fileName, String updatePath) throws Exception {
	    updatePath = updatePath.replace('|', File.separatorChar);

	    URL url = new URL("https://prog2tests.googlecode.com/svn/src/"
				+ fileName);
	    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
	    FileOutputStream fos = new FileOutputStream(updatePath + fileName);
	    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
	    
	    fos.close();
	    rbc.close();
	}
	
	private static final void downloadTest(String testName, String updatePath) throws Exception {
		downloadFile(testName + ".java", updatePath);
	}
}
