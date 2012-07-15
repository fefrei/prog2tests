package prog2.project4.tests.prog2tests;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.junit.Test;

import prog2.project4.tests.prog2tests.CompilerTestUpdateTool;

public class StatisticsReporterTool {
	// To prevent any typos:
	private static final String MAY_SEND_REPORTS = "maySendReports";

	// ===== MAGIC
	// If you wish that StaRT asks you again, then simply uncomment the
	// following lines and run any test that can report something.
	// StatisticsReporterTool will ask you *every single startup* with these
	// lines uncommented.
	// Please comment again after use:

	// static {
	// CompilerTestUpdateTool.clearAttribute(MAY_SEND_REPORTS);
	// }

	// ===== INSTANCES need the following stuff

	private final long startedMilli;
	private final String name;
	private final Map<String, String> map = new LinkedHashMap<String, String>();

	public StatisticsReporterTool(String name) {
		startedMilli = System.currentTimeMillis();
		this.name = name;
		put("#report#", UUID.randomUUID().toString());
	}

	public void sendData() {
		long time = System.currentTimeMillis() - startedMilli;
		map.put("totalTime", String.valueOf(time));
		sendReport(name, map);
	}

	public void put(String key, Object value) {
		if (value == null) {
			map.put(key, "null");
		} else {
			map.put(key, String.valueOf(value));
		}
	}

	public void put(String key, int value) {
		map.put(key, String.valueOf(value));
	}

	public void put(String key, double value) {
		map.put(key, String.valueOf(value));
	}

	public void put(String key, long value) {
		map.put(key, String.valueOf(value));
	}

	public void put(String key, boolean value) {
		map.put(key, String.valueOf(value));
	}

	public <T> void put(String key, T[] value) {
		if (value == null) {
			map.put(key, "null");
		} else {
			map.put(key, String.valueOf(value.length));
			for (int i = 0; i < value.length; i++) {
				put(key + "[" + i + "]", value[i]);
			}
		}
	}

	public void putThrowable(Throwable t) {
		putThrowable(t, map);
	}

	public void sendReportAbout(Runnable r) {
		try {
			r.run();
		} catch (RuntimeException e) {
			putThrowable(e);
			throw e;
		} catch (Error e) {
			putThrowable(e);
			throw e;
		} finally {
			sendData();
		}
	}

	// ===== STATIC stuff

	private static String userUUID = "<NULL>";
	private static boolean maySendReports = false, loadedAttributes = false;

	private static final boolean maySendReports() {
		if (!loadedAttributes) {
			loadedAttributes = true;
			userUUID = CompilerTestUpdateTool.getAttribute("UUID");
			if (userUUID == null) {
				userUUID = UUID.randomUUID().toString();
				CompilerTestUpdateTool.putAttributes(Collections.singletonMap(
						"UUID", userUUID));
			}
			if (CompilerTestUpdateTool.getAttribute(MAY_SEND_REPORTS) != null) {
				maySendReports = CompilerTestUpdateTool.getAttribute(
						MAY_SEND_REPORTS, false);
			} else {
				int result = JOptionPane.showConfirmDialog(null,
						ASK_MAY_SEND_REPORTS, ASK_MAY_SEND_REPORTS_TITLE,
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					System.out.println(CHOSE_YES_OPTION);
					maySendReports = true;
					CompilerTestUpdateTool.putAttributes(Collections.singletonMap(
							MAY_SEND_REPORTS, "true"));
				} else if (result == JOptionPane.NO_OPTION) {
					System.out.println(CHOSE_NO_OPTION);
					maySendReports = false;
					CompilerTestUpdateTool.putAttributes(Collections.singletonMap(
							MAY_SEND_REPORTS, "false"));
				} else {
					System.out.println(CHOSE_ABORT_OPTION);
					maySendReports = false;
					// Do not send sny reports, but ask again next time.
				}
			}
		}
		return maySendReports;
	}

	public static final void sendReport(String name, Map<String, String> stats) {
		if (maySendReports()) {
			stats.put("testName", name);
			stats.put("UUID", CompilerTestUpdateTool.getAttribute("UUID"));
			Map<String, String> att = CompilerTestUpdateTool.getAttributes();
			synchronized (att) {
				stats.put("#att#", att.toString());
			}
			stats.put("#system#", System.getProperties().toString());
			feed.add(stats);
		}
	}

	public static void withReport(String name, Runnable r) {
		final StatisticsReporterTool rep = new StatisticsReporterTool(name);
		try {
			r.run();
		} catch (RuntimeException e) {
			rep.putThrowable(e);
			throw e;
		} catch (Error e) {
			rep.putThrowable(e);
			throw e;
		} finally {
			rep.sendData();
		}
	}

	public static void withReport(String name, Reporter r) {
		final StatisticsReporterTool rep = new StatisticsReporterTool(name);
		try {
			r.run(rep);
		} catch (RuntimeException e) {
			rep.putThrowable(e);
			throw e;
		} catch (Error e) {
			rep.putThrowable(e);
			throw e;
		} finally {
			rep.sendData();
		}
	}

	public static final void putThrowable(Throwable t, Map<String, String> rp) {
		String prefix = genField("exception_") + "_";
		rp.put(prefix + "name", t.getClass().toString());
		rp.put(prefix + "message", t.getMessage());
		StackTraceElement[] stack = t.getStackTrace();
		rp.put(prefix + "length", String.valueOf(stack.length));
		prefix += "stack_";
		for (int i = 0; i < stack.length; i++) {
			rp.put(prefix + i + "_native",
					String.valueOf(stack[0].isNativeMethod()));
			rp.put(prefix + i + "_class", stack[0].getClassName());
			rp.put(prefix + i + "_method", stack[0].getMethodName());
			rp.put(prefix + i + "_file", stack[0].getFileName());
			rp.put(prefix + i + "_line",
					String.valueOf(stack[0].getLineNumber()));
		}
	}

	// ===== HELPERS and stuff that helps

	public static void main(String[] args) {
		withReport("StatisticsReporterTool", new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Whatever
				}
				throw new UnsupportedOperationException();
			}
		});
	}

	private static int counter = 0;

	public static String genField() {
		return genField("");
	}

	public static String genField(String prefix) {
		return "_" + prefix + (counter++);
	}

	// @Test
	// public void test_Update() {
	// SatSolverTestUpdateTool.doUpdateTest("StatisticsReporterTool", "1.0");
	// }

	// ===== INTERFACE <-- exactly what it says on the tin

	public static interface Reporter {
		public void run(StatisticsReporterTool report);
	}

	// ===== INTERNAL stuff, like the class of the Thread

	/**
	 * Amount of milliseconds to wait at most for new reports to pour in
	 */
	private static final int MAX_SENDER_WAIT = 100;

	/**
	 * Abstract the producer-consumer-stuff in it's own class. I don't want it
	 * to pollute my wonderfully unreadable code.
	 */
	private static final MapFeed feed = new MapFeed();

	/**
	 * TCP port number where the STAtistics Report Tool server is running.
	 */
	private static final int START_PORT = 19182; // 80;//

	/**
	 * Host of the STAtistics Report Tool
	 */
	private static final String START_HOST = "176.57.129.133";
	private static final boolean VERBOSE_CONNECT = true;
	private static final int CONNECTION_TIMEOUT = 10 * 1000; // 0;//

	private static final class ReportSender implements Runnable {
		private final DataOutputStream out;

		public ReportSender() throws IOException {
			Socket so = new Socket();
			if (VERBOSE_CONNECT) {
				System.out.println("[StatisticsReporterTool.ReportSender." +
						"ReportSender()] Socket created");
			}
			so.bind(null);
			if (VERBOSE_CONNECT) {
				System.out.println("[StatisticsReporterTool.ReportSender." +
						"ReportSender()] Bound");
			}
			so.connect(new InetSocketAddress(START_HOST, START_PORT),
					CONNECTION_TIMEOUT);
			if (VERBOSE_CONNECT) {
				System.out.println("[StatisticsReporterTool.ReportSender." +
						"ReportSender()] Connected");
			}
			out = new DataOutputStream(new BufferedOutputStream(
					so.getOutputStream()));
			out.writeInt(0);
			out.flush();
			if (VERBOSE_CONNECT) {
				System.out.println("[StatisticsReporterTool.ReportSender." +
						"ReportSender()] Stream established");
			}
		}

		@Override
		public void run() {
			if (VERBOSE_CONNECT) {
				System.out.println("[StatisticsReporterTool.ReportSender." +
						"run()] Really running.");
			}
			Map<String, String> report;
			try {
				while ((report = feed.poll()) != null) {
					if (VERBOSE_CONNECT) {
						System.out.println("[StatisticsReporterTool.Report" +
								"Sender.run()] Polled report " + report);
					}
					out.writeInt(report.size());
					String s;
					for (Entry<String, String> e : report.entrySet()) {
						s = e.getKey();
						if (s == null) {
							out.writeUTF("<NULL>");
						} else {
							out.writeUTF(s);
						}
						s = e.getValue();
						if (s == null) {
							out.writeUTF("<NULL>");
						} else {
							out.writeUTF(s);
						}
					}
					out.flush();
					if (VERBOSE_CONNECT) {
						System.out.println("[StatisticsReporterTool.Report" +
								"Sender.run()] Completed writing");
					}

				}
				if (VERBOSE_CONNECT) {
					System.out.println("[StatisticsReporterTool.ReportSender." +
							"run()] No more reports.");
				}
			} catch (IOException e) {
				// Oops.
				if (VERBOSE_CONNECT) {
					System.out.println("[StatisticsReporterTool.ReportSender." +
							"run()] Encountered error during write:");
					e.printStackTrace(System.out);
				}
			}
			try {
				out.close();
			} catch (IOException e) {
				// Whatever.
			}
		}
	}

	private static abstract class Feed<T> implements Runnable {
		private final Object lock = new Object();
		private final LinkedList<T> queue = new LinkedList<T>();
		private boolean started = false;

		public void add(T item) {
			item = clone(item);
			synchronized (lock) {
				queue.add(item);
				if (VERBOSE_CONNECT) {
					System.out.println("[StatisticsReporterTool.Feed.add()]" +
							" Added " + item);
				}
				if (!started) {
					Thread t = new Thread(this);
					t.setDaemon(false);
					// prevent duplicate threads due to slow startup
					started = true;
					if (VERBOSE_CONNECT) {
						System.out.println("[StatisticsReporterTool.Feed." +
								"add()] Starting... ");
					}
					t.start();
				} else {
					lock.notifyAll();
				}
			}
		}

		protected abstract T clone(T item);

		public T poll() {
			if (VERBOSE_CONNECT) {
				System.out.println("[StatisticsReporterTool.Feed.poll()]" +
						" Polling");
			}
			synchronized (lock) {
				if (queue.isEmpty()) {
					try {
						// Note that this releases the lock
						if (VERBOSE_CONNECT) {
							System.out.println("[StatisticsReporterTool.Feed." +
									"poll()] Waiting...");
						}
						lock.wait(MAX_SENDER_WAIT);
					} catch (InterruptedException e) {
						if (VERBOSE_CONNECT) {
							System.out.println("[StatisticsReporterTool.Feed." +
									"poll()] Waiting interrupted!");
						}
						// "Ignore"
						// Doesn't exactly ignore this event, but I think this
						// is rather graceful.
					} catch (Error e) {
						e.printStackTrace();
						throw e;
					} catch (RuntimeException e) {
						e.printStackTrace();
						throw e;
					}
					if (queue.isEmpty()) {
						if (VERBOSE_CONNECT) {
							System.out.println("[StatisticsReporterTool.Feed." +
									"poll()] Queue empty");
						}
						// Nothing came in.
						// Either there are no reports coming in anymore, or
						// we're done.
						// Anyway, stop this thread.
						return null;
					}
				}
				// By now, there is something in the queue.
				T item = queue.poll();
				// If there went something wrong, item is now null.
				if (VERBOSE_CONNECT) {
					System.out.println("[StatisticsReporterTool.Feed.poll()]" +
							" Polled " + item);
				}
				return item;
			}
		}

		@Override
		public void run() {
			if (VERBOSE_CONNECT) {
				System.out.println("[StatisticsReporterTool.Feed.run()]" +
						" Started.");
			}
			boolean keepGoing;
			int fails = 0;
			boolean failedHard = false;
			do {
				ReportSender rs = null;
				try {
					if (VERBOSE_CONNECT) {
						System.out.println("[StatisticsReporterTool.Feed." +
								"run()] Creating...");
					}
					rs = new ReportSender();
				} catch (IOException e) {
					// Oops.
					fails++;
				}
				if (rs != null && fails < 10) {
					if (VERBOSE_CONNECT) {
						System.out.println("[StatisticsReporterTool.Feed." +
								"run()] Running...");
					}
					rs.run();
					if (VERBOSE_CONNECT) {
						System.out.println("[StatisticsReporterTool.Feed." +
								"run()] Exited with fails = " + fails);
					}
					synchronized (lock) {
						keepGoing = !queue.isEmpty();
					}
				} else {
					if (VERBOSE_CONNECT) {
						System.out.println("[StatisticsReporterTool.Feed." +
								"run()] Couldn't create. Abort.");
					}
					failedHard = true;
					keepGoing = false;
				}
			} while (keepGoing);
			if (!failedHard) {
				synchronized (lock) {
					started = false;
				}
			}
			if (VERBOSE_CONNECT) {
				System.out.println("[StatisticsReporterTool.Feed.run()]" +
						" Stopped");
			}
		}
	}

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("StatisticsReporterTool", "1.0");
	}

	private static final class MapFeed extends Feed<Map<String, String>> {
		@Override
		protected Map<String, String> clone(Map<String, String> item) {
			return new LinkedHashMap<String, String>(item);
		}
	}

	private static final String

	ASK_MAY_SEND_REPORTS_TITLE = "Do you agree to sending anonymous"
			+ " statistics?",

	ASK_MAY_SEND_REPORTS = "We would really like to get a rough overview about"
			+ " who is using which part how often, how intesively, in with"
			+ " frequency, which tests fail most often etc.\nI think we could"
			+ " be able to optimize tests, for example, if we notice that"
			+ " everyone is failing testXYZ, then we can split this test up"
			+ " into smaller ones.\n\nThe statistics that are collected are"
			+ " absolutely anonymous. We have no way at all to tell which"
			+ " report came from which user.\n\nIn order to improve the tests,"
			+ " we would really like to see some kind of statistics.\n\nDo you"
			+ " agree to sending, absolutely anonymously, statistics about"
			+ " performance and frequency of the tests to our servers?\n\nIf"
			+ " you choose 'Yes' or 'No', this program won't ask you again."
			+ " You can revert that setting by using the 'Magic' Section in"
			+ " lines 26-29 of the file StatisticsReporterTool.java.\nIf you"
			+ " choose to abort, cancel, or close this window, I won't send"
			+ " any data, but this program will ask you again the next time"
			+ " you start the test-suite.\n\nI'm really looking forward to"
			+ " seeing some rough datapoints," + " thanks you :-)",

	CHOSE_YES_OPTION = "\n\n=================================================="
			+ "===========================\nI, Ben Wiederhake, want to thank"
			+ " you very much for clicking on the YES button\n================"
			+ "============================================================="
			+ "\n\n",

	CHOSE_NO_OPTION = "\nI'm sorry to hear that you don't want to send us any"
			+ " feedback.\nThis program will NEVER ask you again, or get in"
			+ " your way whatsoever.\n\nIf you decide to send us reports"
			+ " again, simply look at the 'MAGIC' Section of the file\n"
			+ "StatisticsReporterTool.java, lines 26-29, and uncomment the"
			+ " four lines there.\nI'd really appreciate if you'd do that,"
			+ " but:\n\n>> Your decision to disallow ANY kind of outward"
			+ " connection\n   for statistical purposes will be honored. <<"
			+ "\n\n",

	CHOSE_ABORT_OPTION = "\nYou chose to abort the dialog.\nThis program will"
			+ " not attempt to ask you again in the current test-invocation,\n"
			+ "and will not attempt to make any kind out outbound network"
			+ " connection\nfor statistical purposes FOR NOW.\nThis is"
			+ " considered a temporary decision, and you will be asked in the"
			+ " next run again.\n\n";
}
