package prog2.project2.tests;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.junit.Test;

import junit.framework.TestCase;

//Prog2 Test Update Tool
//Felix Freiberger, 2012

public class UpdateTool extends TestCase {
	static final String version = "1.0";
	
	@Test
	public void test_Update() {
		UpdateTool.doUpdateTest("UpdateTool", version);
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
					System.out.println("Update completed. Refresh (F5) in Eclipse and rerun.");
					fail("Update completed. Refresh (F5) in Eclipse and rerun.");
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
	
	private static final void downloadTest(String testID, String updatePath) throws Exception {
	    updatePath = updatePath.replace('|', File.separatorChar);

	    URL url = new URL("https://prog2tests.googlecode.com/svn/src/"
				+ testID + ".java");
	    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
	    FileOutputStream fos = new FileOutputStream(updatePath + testID + ".java");
	    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
	    
	    fos.close();
	    rbc.close();
	}
}
