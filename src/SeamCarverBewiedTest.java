package prog2.project2.tests;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Test;

import prog2.project2.SeamCarver;
import prog2.project2.SeamCarverImpl;
import prog2.project2.SeamCarverUtils;

@SuppressWarnings("unused")
public final class SeamCarverBewiedTest extends TestCase {
	static final String version = "1.4";
	
	// ========== IMPORTANT STUFF ==========

	/**
	 * Let this value at "false"
	 */
	private static final boolean OUTPUT_NOT_CHECK = false;

	/**
	 * Be verbose and print out statistics about hashing time, testing time,
	 * etc.
	 */
	private static final boolean VERBOSE = false;

	/**
	 * Guess what it does. Let it at "false" unless you really want to run the
	 * "corner" tests.
	 */
	private static final boolean RUN_CORNER_CASES_ONLY = false;

	// ========== Not so important stuff ==========

	/**
	 * Built from
	 * 
	 * <pre>
	 * http://www.random.org/integers/?num=16&min=1&max=1000000000&col=1&base=16&format=plain&rnd=date.2012-01-02
	 * </pre>
	 * 
	 * which gives us:
	 * 
	 * <pre>
	 * 07fb2791
	 * 1bac3ec0
	 * 129e6e56
	 * 248aa1d7
	 * 0c84396b
	 * 2edf4245
	 * 332d24fe
	 * 0b3a79fd
	 * 05d1e47e
	 * 004fdd12
	 * 2ddec38f
	 * 3138949c
	 * 076e5910
	 * 34230d15
	 * 275921d1
	 * 0a10d6e6
	 * </pre>
	 * 
	 * Taking only the last digit:
	 * 
	 * <pre>
	 * 1 0 6 7 b 5 e d e 2 f c 0 5 1 6
	 * </pre>
	 * 
	 * So yeah: That's why.
	 * <link>http://en.wikipedia.org/wiki/Nothing_up_my_sleeve_number</link> I
	 * always wanted to do that :P
	 */
	private static final long RANDOM_SEED = 0x1067b5ede2fc0516L;

	private static final long RANDOM_END_MAGIC = -3164793632188989568L;

	/**
	 * Do not change unless you know that the hashes will change.
	 */
	private static final int WIDTH = 400, HEIGHT = 300; // , AMOUNT = 150;

	/**
	 * Pre-computed pseudo-random images
	 */
	private BufferedImage[] ret;

	private static BufferedImage[] copy = null;
	private static final Object COPY_LOCK = new Object();

	// ========== Initialization ==========

	/**
	 * Yeah, I *should* use @Before for that.<br>
	 * Do I look like I care?
	 */
	private final SeamCarverImpl prog;

	public SeamCarverBewiedTest() {
		if (OUTPUT_NOT_CHECK || RUN_CORNER_CASES_ONLY) {
			System.out
					.println("WARNING! Some global flags are NOT set to false."
							+ " Make sure that you disable those when trying to test!");
		}
		// Constructor is called about 10 times.
		// ... duh!
		prog = new SeamCarverImpl();
	}

	private final void ensureInited() {
		synchronized (COPY_LOCK) {
			if (!RUN_CORNER_CASES_ONLY && copy == null) {
				ret = copy = computeImages();
			} else {
				ret = copy;
			}
		}
	}

	private static final BufferedImage[] computeImages() {
		System.out.println("Initializing... please wait!");
		int length = 0;
		for (String[] arr : new String[][] { EXPECTED_LOCAL,
				EXPECTED_HORIZONTAL, EXPECTED_VERTICAL,
				EXPECTED_SEAM_HORIZONTAL, EXPECTED_SEAM_VERTICAL }) {
			length = Math.max(arr.length, length);
		}

		BufferedImage[] ret = new BufferedImage[length];

		final Random r = new Random(RANDOM_SEED);

		long start = System.nanoTime();
		for (int i = 0; i < length; i++) {
			ret[i] = makeRandom(r);
		}

		long stop = System.nanoTime();

		double pixels = WIDTH * HEIGHT * length;
		System.out.println("Generated " + length + " images in "
				+ ((stop - start) / 1000000d) + " ms, averaging around "
				+ ((stop - start) / pixels) + " ns/pixel.");

		// Just a short sanity check to test whether something has partially
		// changed:

		if (OUTPUT_NOT_CHECK) {
			System.out.println("RANDOM_END_MAGIC should be: " + r.nextLong());
		} else {
			assertEquals("You modified something without actually knowing what"
					+ " to do.\nYou can redownload this file at: https://forum"
					+ ".st.cs.uni-saarland.de/boards/viewthread?thread=1489",
					RANDOM_END_MAGIC, r.nextLong());
		}

		System.out.println("Initialization complete.");

		return ret;
	}

	private static final BufferedImage makeRandom(Random r) {
		final BufferedImage ret = TestUtil.createImage(WIDTH, HEIGHT, 0);
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				ret.setRGB(x, y, randomRGB(r));
			}
		}
		return ret;
	}

	private static final int randomRGB(Random r) {
		return SeamCarverUtils.createRGBInt(r.nextInt(256), r.nextInt(256),
				r.nextInt(256));
	}

	// ========== Testing your trust ==========

	@Test
	public void test_Update() {
		UpdateTool.doUpdateTest("SeamCarverFelixTest", version);
	}
	
	@Test
	public void testArraySize() {
		final int W = 5, H = 3;
		BufferedImage bi = TestUtil.createImage(W, H, 0);

		int[][] arr;

		arr = prog.computeImageEnergy(bi, true, true);
		checkArr(arr, W, H);

		arr = prog.computeImageEnergy(bi, true, false);
		checkArr(arr, W, H);

		arr = prog.computeImageEnergy(bi, false, false);
		checkArr(arr, W, H);
	}

	private static final String SERMON = ".\nNote that your computePixelEnergy"
			+ " method MUST NOT use the given array and MUST use the"
			+ " BufferedImage, as specified in https://forum.st.cs.uni-saarlan"
			+ "d.de/boards/viewthread?thread=1494#8320 .\nThere MIGHT be a"
			+ " Nightly that tests exactly for that -- which would be pretty mean.";

	@Test
	public final void testPendantic() {
		final int W = 5, H = 3;
		BufferedImage bi = TestUtil.createImage(W, H, 0);

		int[][] arr;

		arr = new int[W][H]; // Make sure we have a clean copy
		arr[1][1] = 42;
		arr[1][1] = prog.computePixelEnergy(bi, 1, 1, arr, true, false);
		if (arr[1][1] != 0) {
			fail("Expected 0, but returned " + arr[1][1] + SERMON);
		}

		arr = new int[W][H]; // Make sure we have a clean copy
		arr[0][0] = 23;
		arr[0][1] = 24;
		arr[0][2] = 25;
		arr[1][1] = -1;
		arr[1][1] = prog.computePixelEnergy(bi, 1, 1, arr, false, false);
		if (arr[1][1] != 23) {
			fail("Expected 23, but returned " + arr[1][1] + SERMON);
		}
	}

	private static final void checkArr(int[][] arr, int w, int h) {
		if (arr.length == h) {
			fail("Your returned array has swapped dimensions. It's arr[x][y],"
					+ " not the other way.");
		} else if (arr.length == w + 1) {
			fail("Your returned array is one too big. You should NOT create it"
					+ " as int[image.getWidth()+1][image.getHeight()+1]");
		} else if (arr.length == 0) {
			fail("Congratulations. You trapped yourself. The exit is now open.");
		} else if (arr.length != w || arr[0].length != h) {
			fail("The dimensions of your array are wrong, but I don't know"
					+ " why. Your array is int[" + arr.length + "]["
					+ arr[0].length + "], but should have been int[" + w + "]["
					+ h + "];");
		}
	}

	// ========== Corner Cases and utils ==========

	private static final int rgb(int r, int g, int b) {
		return SeamCarverUtils.createRGBInt(r, g, b);
	}

	@Test
	public void testCornerCase2_2_0_1_255_255255255() {
		String name = "CORNERS_2_2_0_1_255_255255255";
		int[] colors = new int[] { rgb(0, 0, 0), rgb(1, 0, 0), rgb(255, 0, 0),
				rgb(255, 255, 255) };
		/**
		 * <pre>
		 * 64	16
		 *  4	 1
		 * </pre>
		 */
		doCornerTest(name, colors, 2, 2);
	}

	@Test
	public void testCornerCase3_3_0_1_255_255255255() {
		String name = "CORNERS_3_3_0_1_255_255255255";
		int[] colors = new int[] { rgb(0, 0, 0), rgb(1, 0, 0), rgb(255, 0, 0),
				rgb(255, 255, 255) };
		/**
		 * <pre>
		 * 65536	16384	4096
		 *  1024	  256	  64
		 *    16	    4	   1
		 * </pre>
		 */
		doCornerTest(name, colors, 3, 3);
	}

	@Test
	public void testCornerCase4_4_0_1() {
		String name = "CORNERS_4_4_0_3";
		int[] colors = new int[] { rgb(0, 0, 0), rgb(3, 0, 0) };
		doCornerTest(name, colors, 4, 4);
	}

	private final void doCornerTest(String name, int[] colors, int w, int h) {
		try {
			LineNumberReader in = OUTPUT_NOT_CHECK ? null : cornerReader(name);
			PrintWriter out = OUTPUT_NOT_CHECK ? cornerWriter(name) : null;
			cornerTest(name, in, out, w, h, colors);
			if (in != null) {
				// Flush stream:
				in.close();
			}
			if (out != null) {
				// Flush stream:
				out.close();
			}
		} catch (HashFailedError e) {
			int ID = e.getID();
			System.out.println("\n\n" + name + "#" + ID
					+ " FAILED!\nTrying to reconstruct image:");
			int[] colorIndices = new int[w * h];
			for (int i = w * h - 1; i >= 0; i--) {
				int newID = ID / colors.length;
				colorIndices[i] = ID - (newID * colors.length);
				ID = newID;
			}
			if (ID != 0) {
				System.out.println("WARNING: The following report might be"
						+ " wrong. Overshoot by " + ID);
			}
			boolean newline = true;
			int i = 0;
			while (i < w * h) {
				if (!newline) {
					System.out.print("\t");
				}
				printRGB(colors[colorIndices[i]]);
				i++;
				if ((i % w) == 0) {
					System.out.println();
				} else {
					System.out.print(", ");
				}
			}
			System.out
					.println("\nThis occured while testing the "
							+ ((ID % 2 == 0) ? "VERTICAL" : "HORIZONTAL")
							+ " seam against your program.\nSeam should have been "
							+ e.getExpected() + " but was " + e.getComputed()
							+ ".\n\n");
			throw e;
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Problem with file " + name
					+ ". Are you sure you copied that file correctly?", e);
		} catch (IOException e) {
			throw new RuntimeException("Can't write to file " + name
					+ ". Are you sure you have write permissions?", e);
		}
	}

	private static final void printRGB(int color) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append("" + (color & 0xFF));
		color >>= 8;
		sb.append(", ");
		sb.append("" + (color & 0xFF));
		color >>= 8;
		sb.append(", ");
		sb.append("" + (color & 0xFF));
		sb.append(')');
		System.out.print(sb.toString());
	}

	private static final LineNumberReader cornerReader(String filename)
			throws FileNotFoundException {
		File f = new File("prog2" + File.separator + "project2"
				+ File.separator + "tests" + File.separator + filename);
		System.out
				.println("Guessed " + filename + " as " + f.getAbsolutePath());
		try {
			return new LineNumberReader(new BufferedReader(new FileReader(f)));
		} catch (FileNotFoundException e) {
			if (OUTPUT_NOT_CHECK) {
				return null;
			}
			throw e;
		}
	}

	private static final PrintWriter cornerWriter(String filename)
			throws IOException {
		File f = new File("prog2" + File.separator + "project2"
				+ File.separator + "tests" + File.separator + filename);
		System.out.println("Associated " + filename + " with "
				+ f.getAbsolutePath());
		try {
			return new PrintWriter(new BufferedWriter(new FileWriter(f)));
		} catch (IOException e) {
			throw e;
		}
	}

	private final void cornerTest(String name, LineNumberReader in,
			PrintWriter out, int w, int h, int[] colors) throws IOException {
		BufferedImage bi = TestUtil.createImage(w, h, 0);

		int counter = 0;
		int[] colorIndices = new int[w * h];
		do {
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					bi.setRGB(x, y, colors[colorIndices[x + w * y]]);
				}
			}
			String computed = Arrays.toString(prog.computeSeam(bi,
					(counter % 2) == 0));
			if (OUTPUT_NOT_CHECK) {
				out.println(computed);
			} else {
				assertHash(name, counter, in.readLine(), computed);
			}
			counter++;
		} while (nextConf(colorIndices, colors.length));
	}

	private static final boolean nextConf(int[] indices, final int colors) {
		for (int i = indices.length - 1; i >= 0; i--) {
			if (indices[i] < colors - 1) {
				indices[i]++;
				return true;
			} else {
				indices[i] = 0;
			}
		}
		// Every single slot is (colors-1).
		return false;
	}

	// ========== Good Ol' Random Testing ==========

	@Test
	public void testEnergyLocal() {
		ensureInited();
		if (RUN_CORNER_CASES_ONLY) {
			System.out.println("WARNING: Running corner tests only!");
			return;
		}
		for (int i = 0; i < EXPECTED_LOCAL.length; i++) {
			String hash = hash(prog.computeImageEnergy(ret[i], (i % 2) == 0,
					true));
			assertHash("EXPECTED_LOCAL", i, EXPECTED_LOCAL[i], hash);
		}
	}

	@Test
	public void testEnergyVertical() {
		ensureInited();
		if (RUN_CORNER_CASES_ONLY) {
			System.out.println("WARNING: Running corner tests only!");
			return;
		}
		for (int i = 0; i < EXPECTED_VERTICAL.length; i++) {
			String hash = hash(prog.computeImageEnergy(ret[i], true, false));
			assertHash("EXPECTED_VERTICAL", i, EXPECTED_VERTICAL[i], hash);
		}
	}

	@Test
	public void testEnergyHorizontal() {
		ensureInited();
		if (RUN_CORNER_CASES_ONLY) {
			System.out.println("WARNING: Running corner tests only!");
			return;
		}
		for (int i = 0; i < EXPECTED_HORIZONTAL.length; i++) {
			String hash = hash(prog.computeImageEnergy(ret[i], false, false));
			assertHash("EXPECTED_HORIZONTAL", i, EXPECTED_HORIZONTAL[i], hash);
		}
	}

	@Test
	public void testSeamVertical() {
		ensureInited();
		if (RUN_CORNER_CASES_ONLY) {
			System.out.println("WARNING: Running corner tests only!");
			return;
		}
		for (int i = 0; i < EXPECTED_SEAM_VERTICAL.length; i++) {
			String hash = hash(prog.computeSeam(ret[i], true));
			assertHash("EXPECTED_SEAM_VERTICAL", i, EXPECTED_SEAM_VERTICAL[i],
					hash);
		}
	}

	@Test
	public void testSeamHorizontal() {
		ensureInited();
		if (RUN_CORNER_CASES_ONLY) {
			System.out.println("WARNING: Running corner tests only!");
			return;
		}
		for (int i = 0; i < EXPECTED_SEAM_HORIZONTAL.length; i++) {
			String hash = hash(prog.computeSeam(ret[i], false));
			assertHash("EXPECTED_SEAM_HORIZONTAL", i,
					EXPECTED_SEAM_HORIZONTAL[i], hash);
		}
	}

	// ========== Classes and functions for Hashing ==========

	private static final class Hasher {
		private final MessageDigest md;
		private byte[] buf = null;

		public Hasher() {
			try {
				md = MessageDigest.getInstance("SHA-512");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(
						"Uh... So, there's no SHA-512 or what?", e);
			}
		}

		private final void fastUpdate(int[] arr) {
			int i4;
			for (int i = 0; i < arr.length; i++) {
				i4 = 4 * i;
				buf[i4++] = (byte) ((arr[i] >> 24) & 0xff);
				buf[i4++] = (byte) ((arr[i] >> 16) & 0xff);
				buf[i4++] = (byte) ((arr[i] >> 8) & 0xff);
				buf[i4] = (byte) (arr[i] & 0xff);
			}
			// CRC32 has two native methods.
			// I'm trying to get a hot path, since the updating seems to be the
			// slowest part of the testing.
			md.update(buf);
		}

		public final void update(final int i) {
			md.update((byte) ((i >> 24) & 0xff));
			md.update((byte) ((i >> 16) & 0xff));
			md.update((byte) ((i >> 8) & 0xff));
			md.update((byte) (i & 0xff));
		}

		public final void update(int[] arr) {
			buf = new byte[arr.length * 4];
			// This actually is a bit slower than the direct approach.
			// But it's the 2D-arrays that give me a headache, so I try to
			// re-use that array if possible.
			fastUpdate(arr);
			update(arr.length);
		}

		public void update(int[][] arr) {
			int assumed = arr[0].length;
			buf = new byte[assumed * 4];
			for (int i = 0; i < arr.length; i++) {
				// t this point, we do have enough time for this stuff:
				assertEquals("Expected a regular 2-dimensional array", assumed,
						arr[i].length);
				fastUpdate(arr[i]);
			}
			buf = null; // Help GC
			update(assumed);
			update(arr.length);
		}

		public String getValue() {
			final byte[] bytes = md.digest();
			final char[] chars = new char[bytes.length * 2];
			for (int i = 0; i < bytes.length; i++) {
				chars[i * 2] = (char) ((bytes[i] & 0xF) + 'a');
				chars[i * 2 + 1] = (char) (((bytes[i] >> 4) & 0xF) + 'a');
			}
			return new String(chars);
		}
	}

	private static final String hash(int[] arr) {
		Hasher h = new Hasher();
		h.update(arr);
		String ret = h.getValue();
		// Being verbose would take more time, and yields often 0, 1 or 2 ms.
		return ret;
	}

	private static final String hash(int[][] arr) {
		long start = System.currentTimeMillis();
		Hasher h = new Hasher();
		h.update(arr);
		String ret = h.getValue();
		long stop = System.currentTimeMillis();
		if (VERBOSE && (stop - start) > 5) {
			System.out.println("Took " + (stop - start) + " ms to hash int["
					+ arr.length + "][" + arr.length + "]");
		}
		return ret;
	}

	private static final void assertHash(String type, int number,
			String expected, String computed) {
		if (OUTPUT_NOT_CHECK) {
			if (number >= 0) {
				if (number == 0) {
					System.out.println("};\n\n\t// ======== " + type
							+ " ========");
					if (!RUN_CORNER_CASES_ONLY) {
						System.out.println("private static final String[] "
								+ type + " = new String[] {");
					}
				}
				if (!RUN_CORNER_CASES_ONLY) {
					System.out.print("\"");
				}
				System.out.print(computed);
				if (!RUN_CORNER_CASES_ONLY) {
					System.out.print("\", // " + number);
				}
				System.out.println();
			}
		} else if (!expected.equals(computed)) {
			throw new HashFailedError(number, type, expected, computed);
		} else if (VERBOSE) {
			System.out.println("PASS " + type + "#" + number);
		}
	}

	@SuppressWarnings("serial")
	private static class HashFailedError extends AssertionFailedError {
		private final int ID;
		private final String type, expected, computed;

		public HashFailedError(int ID, String type, String expected,
				String computed) {
			super(type + "#" + ID + ": Expected hash " + expected
					+ ", but found hash " + computed);
			this.ID = ID;
			this.type = type;
			this.expected = expected;
			this.computed = computed;
		}

		public int getID() {
			return ID;
		}

		public String getType() {
			return type;
		}

		public String getExpected() {
			return expected;
		}

		public String getComputed() {
			return computed;
		}
	}

	// ========== Hashes to check against ==========

	// ======== EXPECTED_LOCAL ========
	private static final String[] EXPECTED_LOCAL = new String[] {
			"jdkenemghjknkmehkafpjohkijfekejdliejhafbfipkfmlmkndbbncmbjgcfnfklmnepobckhkaaoccepahcdlhfpdnopbmjeimjloimcaifklhhbjahaofilkpelje", // 0
			"nacpiaffmhknmlblonphkmmgcjfhiadgpnopbfdfobhpniapdlkjfmbflmdoljbnomndmhfjnngebeakggdkmhlhjihmhmageiedfihbmahhnalfdnnmhodchlcjbnfm", // 1
			"bimkhlcfbgllibmbdfdlfeofbkejolpjfdjiiaafjabookfkpdchmkgkiiohodkcahdejnkkjckldhmdkonhjijkajgngndmlncimaincfgkdoalbbpibmomeemgnkdp", // 2
			"fojooccjlhflbjhlekgnmegejooidibmddhldkdlehdmhekebkagjagamhhhiiikhdfpoohpikcefnjcnmjjnmoagofpinibgjdjffknpphfnioejnjghfopihkdjidi", // 3
			"dfknblfhgmcngngagapngkkbfbglhogejlhnfccgmdlhcegdmdlbofaimiaoiibkoboiphannmdfibjmahacneacclcjhjahbpnjmgpcjcblnoicdkbmbbndecmfdgko", // 4
			"opnejnijedpelbmcdponndndefmjeehginncfakpklelikopcjaalcgmaioiamclndeipnodgklpmbkaaeeccfinbognlbcafehiagbkapbnbnjipnnbjbekkiedencc", // 5
			"mminhfhmmdpfeafmfmiibfeogglgplfinaennmbjfmfjgbcglajipjjbcpplokgfigonkbmfckjehjgnhepnanbbmpljdebimgaeffenakebipmdaenfcmdddbafdbhh", // 6
			"imkeollkiagmfimaeniedfkbbagfkhnfjneplbeffhehdnjjjhlhamknpcbfjakpoejidfokajdjcbfokaoihmoeaihnkmbhkckeflhoakofmljdngckbmkkdogkbomi", // 7
			"ahadfmjmjooklofdhcclihhoinlodapiddbcnajmdmecpadchimjfglinbkdebjfffhcdghbeldaaeoohkocnomklbhnemmfagkohdkhkhiobdmfofacbmhkggeijddh", // 8
			"bkofinikgbjfgmeojihkidjdjgkkjdiimkjmlnjpmbnnbcehboeiknpgipbbegmfblhlmgbjigldgjlaikacafndidepopmnjlaelcdehehkdkoomnhjkbofplafpaim", // 9
			"lciaimidckjngndofemfmlliijbljndeoioleembpgekhgneokoanlaafnkbfmdmijdmihfbnicabhedjikdchdmlkggbhgfkkcglieheheghchfbaofejldbpklenhe", // 10
			"becncglkjmpaoelpmmcajeeibdijolcgkccodgcebklfabjphbigolhjfjjdagddompaobpecfjcldnmpbipeakgbiomlaigmfobhjbaojfkkejcjlbkpdendgjgakng", // 11
			"hbgdmccnjjfpekonoifbkbaekdmnbalkljbggjcjephofjndjkmmkkjogfmfpdbgjdboeopbkbeopldmeeglmaeckjbmdjnbpkfpdpipcbhffijembbjohdimmakdmhh", // 12
			"eocmdbnodiddgoajfofhnhihoffeieliclfpamnbmednkeljnengiajlloncpfamobjpcbbhnagcdahldindlmokcokblicbkkcdghdljmjcibbhnfloponkhmhfmfic", // 13
			"flnljpdjmbbjdjfcngaoajiijhohkbaomhbfmbadnjgkahkbdllfmjlmeaankhcgalgbnpjnbokpnhkbfgendicjcijkncnlgjlgoljjgbbeebnhmbdannepcokgbgdf", // 14
			"aechbkafbamccfcekgagajimengidcdoijfmpjphinjkicngadgmlomfnemmjpchepljoaofmfkdojlgkbmghekenldhpafcfgggckicfkfkhmpmcbkcefofljbdmgho", // 15
			"edcfocbplfknbaamefdommlncgpalncjfjdcjiklmeliikbblhikikneoggbjoaaafmacdejkjdepamajbemilcefemjlipbeioipgfadaodhpnkinllmdkankibnbel", // 16
			"gfhjblgbhhfpgmiealemhiplmhlmanakmhfddipjnickbjimknknidelgeclbaedbmnkpgbilggkeokijmcelhcnhilanghhcoolbpbiooakilnimiabjlbfghcjphle", // 17
			"pglhapengidmkfkknoibaigdbonfgdodglekgddjnamhchopjdpjfpljhfaghocbfjnepddjjgbpjgillepkcmmngfjjgkhliimehihpjeoclcochmceodbgdbjlpbdc", // 18
			"mcacpcnjndfphljepckmpdlpobnifnoeibeljicnobkdipcjhloihhebjfnmalbfghaohnhgoiccdgbcdeijkcncpllakhmdfbfkppdicefndfmiboghegjhjmijdcgl", // 19
			"ffamnnodgnjbajifjegapccdjdkfedplffaocjednajlcgfpelejlaoaeckafdonchfacmhgjffbijeaomelaifcocmamnpfpcadhlnpbpenpfhjojbkejihmcdhmlia", // 20
			"gkapchikhoepcdgboggieofaabhclkbgdebkjkhkggplgicjmfpkigfdinnbgmeoihmccdhokjjjdpgiiaafcpempcnnnbmffnlkijndgmihcodoidomhhboadgniphf", // 21
			"ominmbbnielhjdgnjdioihlhmbjipmcngbjionegabmbimgpceendkoofecjabglfcndfkdkldkpoihgooagknagmhilnlbnfnbkilmikchjcbgmajoodcfppnbiejpg", // 22
			"cknmdelepcalkkicdiloficdaddbmepceifjinidbfgfjlnkpjdgimjiclbncimaalpohhokcpoiklhlflglnpfkmjbkiogjcdennkediheinndipimnaecinemealko", // 23
			"nfccfhfajcldlmfffdocebpjokdpelggjmelkfnmgoanllkalgahkklemlppkbfcpkhgpcgpblaiiaohhiaiedggfcdmmgclgnomlonidichhkpepgjloobfholpelgm", // 24
			"hgefljiofhfggnglalekgklhhjmhncieiknpomfpembdahmiidbdahbjgilhkmhnjgajbgknjaoaffompilmhlplmdmbbkopodpegepneknopfgbcjonpbiblleecljc", // 25
			"loiajfgcghfomkpjmjacpcgfeokgpcebpidpbgmkdjijmpgngicbamckefojiaajkpkchngomlbkbhljaldmgnljkhgddidapofcmlcbknbdfiblcbjfjfncplkbhpli", // 26
			"jahaifgegmknajejbobekfkpbohjmkhcffgadliipbjjabhlcepkndghndnplajbkeebalpblepkijlndhlkmoefdeieabmambfccdponfjaickiiohkfifnhnmeagki", // 27
			"jfjlibblohmbbbkjbmibgcipnhcpbjfdpmbpfdfokgcpkcglncggheggpblhgdaifcpeehojemokgooojnoaplhjfnphaelpkbgcmpikmncekbbmggpdlohnpphhnbfc", // 28
			"jbghfebbdpejeapllohmgcociajnekjhniaghjlobbhilpcaebnhcbjiemphbfjdbhdleckgpmalddclaomfpogblepjifjfiamddahfanejnncgbnfbicpedmdeogdf", // 29
			"pcfkkdegkjajaeoecankmjmpgclbheannjedhifaidmboenpabcbgoedennhipeffjcmhmdmickhheckldhohmdjoipapcnodbbbmhjkghankihiflcpcjpdhkojkpad", // 30
			"obcjnlpgdnphhgeknjmhpcdgoehiecpjbkhcoodilabahjdpjejhoiaeamjcenlheellcgjfahgfhcomadloiilljcfobfflddlcgoadhgebfpegohogjnbkjpdecfnn", // 31
			"jmaandadnnbmadmkfongombcbihpagjdaemjhpdmejnenncgimkjplobbpblbdaekdjkklahkmhgcjefbfjhmiijjpibdjfdpjpbkgdidnfloajiejmnebgfglabgkjc", // 32
			"lnmgcjagbldokcfcdjcncmnbdbdfjaoeilkaaddmepldpaccehhoaecencflogleclkchcpagoflmcalijgacgplkchilopiledpgccjjohlijbcfamkgnbbnhhbmlek", // 33
			"cgdkgohoicdeagpclpgonkmldmajcdaphcobhmeaeoijekjgomcpcgpeembnghoefoiobamihdcepclghehkghejaebkidkfbjjhjdepmkhdbajldfegoadpbendadmg", // 34
			"ginkgblkmpkinojdhpahnidkhjilhmghnpjbpaejdbibldacnnmbpdlbndhanhffinfklfmajejhaonehppfclncjladmnlndbcepmafkjjjmofbbifljiipdogjplfj", // 35
			"cacknhmeomjpniloidgjidgembmajdjgecfmbeplkoochlffoccgcajejiobfjiggjkaadaemkhpbknncakjeilkaaahnidhjoambbefbdnikfedcecmcichcmoiopci", // 36
			"mnefhfboijjeegioiecpjbiildjkkmnjjejpkahnddbjfchhjdkephhkjjnpdndceedodbffedfokglmmljbkjiolkpgpmembeljpilacaeemmmdciphnjjgphhgidij", // 37
			"apkkobbhipcahpnacbghgonejnidkjdglehompjbfbhijdkkohemfoeidmcoiijpegkojekbpkcenlfehhmcgpbjgmmofepggjklffkhnmfbgfdnkbhmjjkhdcdgigih", // 38
			"lapnfndhbjmibkjfjcbndapclohnbigajblclaijhdjnhegdkifjdcpodlabimanhpjpbabimcklpnohagccbfpflilgniidhlaimdjhggghndhcllhbagajhkbhgbfo", // 39
			"ilhggpjepnopkboifbkddajbkdbhmefghcpacmmddfhphelnmenfhekphghoapgodihbfilblaaggjepjghkknoielikhiebjhebdopnnbmphlinmldgnbnahnhenccm", // 40
			"monnckbogcmpngepppkhgnkincnjhdfddgooafnnmedkebeiibgnplfcldfpahbkhbefalegjijjaieaomnjlhhmfhkiedejlmlecilknaclgjejgffgekonnbbpcmha", // 41
			"ofbmabeahnmclicgldghfkhnkfopdeefaaidabamgdebcebmkjijocmalpppkjngceongdohjdpebafhofkjpijamdcpmchnienjcdedbgmcpfnjnngmlceicpbpkpkl", // 42
			"cpjjgnepblnofogdohjfnllenbmgcneaolgnolpkdhonodclhlljfgldijfmbmbmljijblpfdoghnhbdhdmkkmekkkhockjdkdcfmgnmpkjfnaalmobhcicefjpdgdmk", // 43
			"modgojdlgpneelmhahmimhnlcdkemhkonjbdagjkfnhcfeaepokkmkbeinnhaoabgkacheigpglpgpajjeldmafnpooodfeffmopllnllodmgbfgeaombinmammaiicb", // 44
			"bajdnnngimgbpfmiaeibjagfjgomkehnhkdpdmbjkinldmdhcidjjmpijfooopjcbjdppfkfbaigpmkgjpadbhbmbdbgkkhnnehggbilpcegcmiicmkodhekgfedohne", // 45
			"dnhdfccejinajibdgodfolcfiekhlbelajjgcplnnngkppndihomigcifcpmnfngbplancnbhcnliphiknlcdipbfhkibdedkkkakhfmbmlbeinlakbiicdldimfceal", // 46
			"blkaeojlafngicolpilacjembdgkhajnpdepjllllloonjldjhejfblefdlhimecphamdencnpfldaibjcccmgmbpkcedaghchbibifdeaohcjeckehmcgihgdnmpoep", // 47
			"pdgcgdiojoehbgfnigdeopoifgnbjbbmbpenhjkeedpincbbakoibkpfemmppgnpoohaoojpeogpolgofafhjpnbciahamgeecjklhclhhlffkinkalackobadmfggie", // 48
			"kgjmhlngdbhenhifbbhcbdabnnakcmobhonpjgbbdimlhcdchjigmhefbmcfbhcemimkakmaobeieagghkpjaoomomjofkiliegpjjajelieomgomanlopifjndleipd", // 49
			"obpfdhjhagfhmcmdfjcfmnliakgpobckeegdhloinajcnfbblnbdokbdmmlnfjemcfoplbabakblhpdhlegmmdjkfghehkgmoacppfbegpelghjnfcbbpdcmkeojlmkl", // 50
			"anedkgbokpfmaafddblomgkfmokkhiekjohapfhdkckigigajkbcigimeedphbadljagfkkiaaopeipecldheigganjmglnfkkppcmkeeijkdkdngpgdjclnakfnnmog", // 51
			"ggmlfmijblombbgobcehkaiibeaenfbhhjkhgmoejlaaooneklcobdemcnmakkfnnjigbnigciblgiadonaohiefkkaifgjfpalcmpnklplimfcbjjdniebcnhpjljip", // 52
			"aagdnbgahbchnclhbimnbehglkkmkdeaclpafjcnlhpljidpcfjmjdjlmnamjmoefilajjjjmlcdppmekdpfhjpmhdhnalhofgopjdoimhbfohefbofoigohjaeejfle", // 53
			"ofneaahbjmpphenffkfacgajicnichopbkjlehooclgeelcjajfdhhfngbllmlcmndcpeacpoiejhkgkhiojnleankdanlkcfmoohindfnanapjgcoheacdojagobdkm", // 54
			"mejojfekonikeoackogofabikinffnffkcjeknbhoafljliphkdfgaldhmjljlgecekngjkloihhmfhgigjgjlmdhooiondgmpekjpkhpnhlonjfpbblihkfpneblhoo", // 55
			"lgcoklbepmoddnclmafhldfggifchgicgdfmblhlnlcadgpomplddcnaoaijhegpjdgjlenhklfoacpdkdnpeibonifpdgocjmlgbbfcnjnlbagplgalpmlhhdkdlnde", // 56
			"ihjmjomhokhdjmiinfckphckojmcpdkjcoiaklckapbjhajhejjicolahbgapdfegbkcjmbilmdkfdoflnffpfjibdkgmnapkenofamcbicafgabojfbopneciidihko", // 57
			"gkkcdadiijdenhblpdfgjmabnmcdnijpfeicimnfbhcebegmecmgaifbnogomjlpjpckbkdafdkahipdolgjjbmkamjccbdfkbfklffchmpbpleodjmppaelmkgknnpl", // 58
			"domebbpcpbcakcpalefngofjlfpnfjbhpoodphkbilnicncpjlficjeiiokhdkihffchgglnbhpfaiabcfhipggfojenmoaibngdjfcdggmmhmcpcpjloheldmnjapmg", // 59
			"bjeceomakipbnafefjomeommjhkdeoipahkcfeggmneombjnmlicpjnpjnkgmbbfpehbonhgcgaekflplmpcmeikmfnfpjopbbikdcgdlcnkmiimnakkkjodhcfeckbp", // 60
			"clgoiihgojmbjlgldibeodmokcigfnllmmceapiklbakcdnhmppfioiijlahafapnfbbapebckecclacbmfdhalckkmdhfmaiienmdhhkhncfogpidmhgidlppdijdga", // 61
			"gnjcdfmpgfnlgnfdipjifmdelcffmkajhlagdpmcgnbpchppibfcalbnklnfepicjdjjjlfmceglcfbnfelilpmbinnjeiidokhnpgnkjmceaapgmdoaehhfdjfjjlna", // 62
			"opfjffnicfioimiilnipabjaajdekndooklokgooclccnahcmkklhkkiidkfnikpelcafomaomfkimdfgbfkfkfpnlidbokjhmimmifdcdbkjneohpfondbkklcacljh", // 63
			"gcamfhpkjpibhkfehcakefkcfkllcnbhpgafeehdalffbpipafolnanbobhfmgfpejmhogidngclmhmghgjcifoojlcainkgnfjoadmeeoclmihdemmpacmciljpccep", // 64
			"anpjcgoijdfhmpcnoiplinhclcongegfbnoecdecmlelgbkkfcgkidjkimijeplbkjgamdecahhhbnnpcglickpppnfnaompmpmfdbdkngbmlnnkggbonoookpmdmnhp", // 65
			"djknikegngpllipoejgngbcojckhiccabldceklipkopgeponjoknphjalhecgjkigknjfpfeleliplmkddkbmkeibbobojddncpnalbcoominfdlmlglcfiaecfjind", // 66
			"kmedafimdlienhbbpcfhfjbllhhnahopfpnklpoanjkmljabgcckeefkejfibbcgjbafeffkndnkmdnojdipfjpjemhfbhmnhiicnnbbjlenmaoekalbhpncfmkpdppn", // 67
			"bhbilpgpiejjbiijkopkmhnnaglbpomddnngfbngpgfibonkadklnjelcgokmieofnpmhaicpaleejdijnhimkbcgmngkgcoipmeppijcgcjjkjknegdafeieglikdee", // 68
			"eilkpeglhdbkfeiofnoekoopipbcpkhmplpgclgnialnbfnkfmncnppjgapcdgboojkdaifopcindlgcmeipeigobnaijhliolgnmgepdhdifjjhbphocaijnbcfoiie", // 69
			"nhlcjlmljoocjapfelkgepabfddjldhndmhdbjikolmkgiandhobaggjlikdicdlpiceahciadbgkilomcbdpacglljfimndahjgdaebmfilflbaceafinkmnifblgdl", // 70
			"cdljpokjejkbbnciclangioogjamfimokndlbaeeefdnljioooenmkhekgobfbpecoinhdkbpcolfmgkdnfbmnbddicoiffhmapknhpflfpconccakonfakkofpefdpj", // 71
			"bnflildkmcjaojobodbjfemoldbmnilmfocdkjgacbjlldnkgpffgnkjpkiokedikcggdjfdhakcbehpeimpjlbikgblghdhobdmfljapojlcjhpjmppjhbaopdndiea", // 72
			"gicddgfbbijfllhiaoapjieekalmjiedojpebilnminklnikcfanlmobhnohnkgmjjagfdgldomenaihjjdidmhkbhkjbfnlmelhifkoohaiobppedefjglhafpmikfi", // 73
			"ekagnmmcffifhpkgnkjghhcjdfdbhhcflhhglcbdjllbpjnpeffljgcmeaapikdnbghfgamjppmnjaebggfebfnhelfigbomafbanjgmbogaofdpjoiapfhnijedbbja", // 74
			"kiomoddlhldfaoepkhgddjpjbefmmndgdfmmgdcfhejoioeeojmfcflmfacpmhoioagmnlpighdpfkpppnifhbljidcohljdabmpldipmikigdaklcbioeinimahochh", // 75
			"edpkcpondoefdilpccpebemalgkohhgnmdijfpcfchgfnbkmnfepmjhbkbhnjkgkbpejdnlefbeppmnccghikigkgikadnacfchlbpkmjmfgnfanegomjgcagoadbpkm", // 76
			"hhomoncnllopjgkgjjopcmiffcbleecmmbhbobenfbbdolhfodfdgcoakmdoobkfofmolojgdggcimloclkpcahdoedhoipamdnbboheaehgdlbidhnceimmiabapkmk", // 77
			"khcjnnjbahmmbhkcomfekkegglamhphkcnkihkngegpgiikmempjkbgemfckdofefhohmkmhjhahfbfcefdmikcnpbkmenefafdlmldcgnaeehokheokmboolbclfcme", // 78
			"paondlpihcphdhbejfdpdonlmjfapbnkkgnignhgdlpnclflhanfljilbkgbahjeakafgnaeoefmplpdajnhialmgaoenegfbjnbiefmcbaigbdjmgblgcomfnjnodme", // 79
			"cepmnfnifmphknmnifdkphijpmjhefejocmmbhbijhpkpkchdiogbaoimbbbijhiecemegdhfkkajhagmcoanbfjoidldlfdhkpfckdpdacggnhomhfhnoidbamhdkog", // 80
			"pfgoeinnfgpnolccjdhkfjknjjmcmpgbjmdiehejnadiogihmglijambcajbgibnflklopfnakaimcoiankmmlpmhpepinbappnjbcjfolkennndmoakfljjcflifkco", // 81
			"hloflmfhebpkpcajalppneogacddmoomnegjncdpdmiiggjakolicificjgicaajklpipbdmjlnanhakihjagfibcdbhhmijcogflhodaeioigddalckdfhfonkmbpik", // 82
			"ioiihokaikfmmgdekpehbfgchlkcmlkkbnlckgpcdfnnbmjcpaemccjfoefdlakepnineigimjpmgleeahbicogldpmdkbendaneecnmhhkcjmcjojfhgchoecjipndo", // 83
			"ebpdifbfihehmjikbeggdglhefdmemjpfkeppgligdljjeoblkchmgmgpkffnijlmieebhjeookakcboeajbpobmgfkbhfcgfjdoplgbkapmjaekbmocbfddbifacddh", // 84
			"lgjncealiclhcjmlbmeanljjolefkoeifiikcapdicnnabljpjooeekgkhledbdenjebojiineckffnkkcgbamjfogihbhmmmhiolghdiofinmbefpnikbomoopflcni", // 85
			"chkocpgjihiafgehlalpipfiibpphamlbgnlpddlmnjghpfibnmopikpiijjffnbkcnhbpmbbmoelialapljimmlbknbbhnglmljdlhdgbegjghdmddmjbgakkedegpc", // 86
			"cojelnfejnfclkfecbenbmjfploplgdcacbbkkdcpoflccmgockaiolapifhhgkjhpilgkignnoakkdpgndoaelhigdmpnboclbphjebacnalkadnbgljjiicgcfpkdj", // 87
			"fkbbiimhoacdenhlaaeakjiloccckmmcfdjjmadhdjenmdjnjjfbabhicifajhoojdalmkgpeffccdihkmjmgndbklcdkghcemngpdcgknadacajgbcecgpnmmmlbbkf", // 88
			"ailfkhbhbjifpaeddnmmmjbldolckigmeinjlmmmkjjhacnchpcgjfagbncohjoppcaiomekclabnpcfdieklpgpildochoioldffbjhjgappbhlcfibdfgdoaaeioej", // 89
			"iaepmffneeeppaghebegcihhpkmachlfgfljndcjhplmpmnbafbipoohkpdfdibbcdabdgbkpnepabbomfeohljgnkodmokgafhociegkhhcoldhhoagcjjomfgllgpo", // 90
			"bjmaejdbadifbpbajbgecchllbmiclhkpajhlmomhligjdmbmjimjbdmhfgnhmdfolbafcminnekoohfbemibjicngjaoojpdlnkngdealpbjpojmobalbandgppmamp", // 91
			"oibchnecilghnfbbkdmlmclenlcbcphnooilmfgbeaooaoepnmiomfcgdjojfbpahginblpendchleaifnndaokfopdpjcpnihkhjbihjaaiiopobkgoiahgbfbldgel", // 92
			"jdfbicfikgpkhejkphghgkdmblkbjglkpdidkdapkeeeebcbbkjaemffljmgkoecnckkpodgpbmcllmemedgddpfbeadgknflnlghgjmgncaobiabgbfocobjkbeoijd", // 93
			"cpjpnjjnadjcmfemiihhjglioomddapiknghhhapganfkojdjhmaigblbngpkihlapfknphohnbnimlnndikidkfdkdckcpdkpjlboclhmjcmhpbjbpicgdkmeokomlh", // 94
			"gojjmfjnfacfckjljdadbhnbgbokoldlalmadiebahfbhkcaaapbfaflmkdjgoccldjjjpnjlfmfdhaibmffjnopjdmeemakdlnjblohiiipknhanephmclpndkkjafm", // 95
			"jmibbgddjlikcibkpgenchnijebmcagpoaaeppfgnbjjmnmpbcflhgodnfdhdfnkejojfalbdmgffodijlkeljbgceonpnpmckohomhobbcikkddoapofbiebdjkiaia", // 96
			"opllcinmcajcangohocmcbcikablajdbefbjgmpkobgklplaaiojohpefgfpaddolpanignbdjcmebafienblekandjbanapjekejkolngjhhngnmjjniccbiildohlh", // 97
			"nfdnelphlfeimalkejlimfodjnmplbafmjhhlkbdeebdodgalckohidbdopbnejpbkbncekomlaeaplcfndamakjeoeigchdkclgjjiknfmoemakclekebkpdlkiegjp", // 98
			"coakkbllagakmffabecifdndeiloifibbjpbmnlbhbgafijikbnfhgpdnaekmnaajonebbmbacmgmifejlmjlninaodhajjpohhodbpmimdlpibploomloegmmpcfcne", // 99
			"dcfidimcpaapiemdlakolfapeocpkdbbgimgkmoljbddefhhpmaddpelanijkpmldjanfoeagmfhjfddnmbjenbkilollljocngcjebijfkbdbhkgjhdcfahlfeaddnc", // 100
			"nhppoheddbmcldnoakoljdcpbgmabhconjiloldkfbgadmkfikloobhgoeihahamkdpmgnkeopclojkgjngplcfbdaibdkpekgooblmblnbmdmbefohjfipdbebiodhd", // 101
			"lmidokpnigkjgdfooegmglibagjcnedejofnhfnioofligogkabhejamplcdcadlljomgmilfffeocjedcncflkmhhkoheaicmlmlcakiecimhfgoheheleliipjhlli", // 102
			"nfdgdbhemalhbgeffkhnpdlloloajeofgiepeigplflllhdlddmacimekhmmfikdjjmlbgknogndllmdapognndlmbcllkmagbenkpekgkclbeobhgkookipcgofkjaa", // 103
			"mjompgakimdkmcogfmlfhogjmknjfieckeobcgookchpkcoekjcjabdnfloadelcmfkjbdgfanoaofglkgaamemkilhagaabinjfdjnpdiogmkknmdfllphmfkolkjfa", // 104
			"dkkgogjdbopmpopgceoofmiahjaclbdanelmafgfbioaamnnhlbkpeilgdajifppjhgncglmifahinjnhckacadppofncpakojjonhknnhepnjbdafodapnbcfdfhlle", // 105
			"kblobdfiklblopmeajemhpkafjhhpnifaiacpneebfjkbkjgjfbaehpidpfabieodbhokdkfcikaamakfjacmfnljfonnbmmedeadojdbkpillkjblgeoijglpnflcnd", // 106
			"jfollbbphogjjeibhhdbnboobibonaldahpgmhiohnhnjlpbeecmeebgdaaninnblliinagnepeipfhjcjmcobjoieempannkpbhibanfnehoonhkndcklndeieplbbf", // 107
			"ghcampcpflnbdllddpahcghnkimjnbchcejfhdokckmleagjgagacaokkkafaadnmkgmphhakkpfpagggkmkiomffnalbhjdjhijiggjpbhkkgmmaoeffmioniajmjlj", // 108
			"ibellcamajpobjknabbipnglbbcplkpfbdidjbelegbfgoeammcenaepnokgpcpinpokdjnonjdiappcmdbbidbonbagikjcofnikibhaoichdbiafbnfmglkbnpjhob", // 109
			"ibbmioaolpchnnkbjnceihmibdllhocejedkhbkhnhanmjiaiihfiiigojkkfalinhbemibbkdkdnoppejheigjjamkklcodnmjeohkefaaeffndlkdmloffoghjpicd", // 110
			"golmbdaefmkhfknfffjjbfekakldifpckklfjpdjkgfojhnabmeblmnkmdkejblfcepjpcfkbfcnnkhbfcmkgacnpcilbgcebieojclgdojcjnnfcnmodpmgdfnlnmbo", // 111
			"enlbddkoelldfchcnloijkgdjkbccklgpiclgbjejndpichmhhicaeampimkghfdamomcoknfhpglidkfolidefhmofkpiffjfhnapadmjdbflofafghejoaakhilhmm", // 112
			"lmlemgohajpecgjacdgdgfhahgepmpglmnomgfnoholbopekfmhlgdnakgnehgdidfabekmlnobfihplbhbebpmbklniiajhnhoapafcihlpjgfpnaoglgofjpbmnalb", // 113
			"ccelkiebngmgabkdhefahfebfdhigehojkmiebakighkmofgbnagenmijfdcknjmgfaijldmpamblfkippekhkfgoobdnngpbddgibinaofpgfgheidekaofafpjlddb", // 114
			"gaihnjgekgkfagoniplficpfkfkacbhpdkmlckgbcdcjpoaiibojpebajlhpenfcjafbkiplhgnbmjmpfjpafbpoigbjoimcbmaiccjdfckhgoanopapaahonegphbke", // 115
			"hiofclnobclhgoeplhheiomjhiephnjbcjcjpeofcblkceblgcnfafbhhojmkpckachcgodmlgccndnehimdapmimbnegpekpgemhbbgclhibbjdjjniabkefkpklblc", // 116
			"nebfpcgpkocjejkdifaljpnnhholoinimhnkhcmghadebmeodlofmamofjbalihaopmbaogapjngfmelcfdildomfpjaaldincdadhaljblbcgkgbaafkaknkgikcapn", // 117
			"bpfpndcilikahdghboeojblabolegckkfceinifbiadjagefjbkeppbeenffajhhloeihoighmolnoekpalldhabhfbgjmahgjabejlggpaobjfnflbkbhabdijpiljm", // 118
			"mheodjieiiabpimngciklmmiiakooldnhiclinifpnejbekgcbbjlnggmchjcbmeaionanhcnicmkbpojipemnlmgelepnepkndmnkmkedfkapobaaiamhbgmoffcpbf", // 119
			"loecnijbmlemelkpgnigdhjhkacgpfbmnpmflmhjmchmgkchiglaeinlaccfikdllokmcedembkbckihmkcmdgbljbkbflmkjlfebgheojeefbhcpemhacbjmjlamfip", // 120
			"jcinagknfhfeicakjhaahpdbfjipfiifiooihnieeobeccjbaocmagjgmljfgfoeloiackpeapoebfihfobmfgippglpeoklgggffcpaonjcnkkcclkpkpgokhjffldo", // 121
			"dacnieehoafgecjkghkgjfdmbkjjnmghiolipbfoppmbchpdhbihhpapjmcniihgfoelmifnbgnpfmdempffmhoncioopbaganofinpdgphajobebmjnbhjbollebklc", // 122
			"imbgfhehkhmkhnailpodhachehdmomfmjegmdefdojkbgmakklfepkonkodbpkjdcihpahpppboammklmkhjofhmfediekbblgoecpemljbejbaaiiifiennilodjmoe", // 123
			"kgbmfkpdlhigkfdoneodiefliahbpgkjpiaciiajlbbaahkeflkbamcfnkknafeimefaehndjedkobfgilmeiegfjhfhdficgfkfilicjbnambedkdcpccgigpgahdck", // 124
			"angemmicihamgopdcondmgngdeeoihlgfanfjgeblahngmhcffocnifagcchmlhpfkkmfhcjmghmgpgicndlbofoekcgfoepnlkbhkjafjcoijfpjiobcnaghibnlifk", // 125
			"jlknhhbifpadpjppldfgkbepmglapackajiioipipnjblbjimophldfhekhinkehjkofhimiamfdjcnhhbbckopbednagfgeeffbkcakogkdocikgojdmnppapmimokf", // 126
			"cmbhcekbbkhiecjejdkgmnofokpnchmdcnohhgbagfmjaagichklcdebfohhmgkcfhfdoakkehemgpecaniophijfdaillnjdlbghmfcgmmgaifaddjpeahkdpgpfkkg", // 127
			"jnfloipmlkkmjphhkaebidnllglnpdcgdkjbjeaicjigbjbfbmjpmddpbdnjidbjbdcamneobgbfkpldhhokhgijnlplnaccfnifjaefbcohlapmgdolbngcfhjblcng", // 128
			"glpjampffalocddnbdljdblhepkladkalhenceldmegemcfbopeahphkbmbepkcpibjeleklkhjbgidgcfijllpgginljepildajcomdmdljhpcodajgophfdjlhfhch", // 129
			"dmjlckiodimkackladadfhkkildligdddlollbcodibggefkccphicejnhknodjpnefkhhgdfkodmlofoglgmkepjepejdicmneljpnpiflabkfkmamiiooebfbfilld", // 130
			"gicmgdfenfflbddichjcelhbghbjpdmamnjfpekjnpjbgnbahoikimapnopdbekbagnpidpbmfhimfpcijpjdlkapdfpbonekgiooheeonhaenfmgjnbfmcdljghfljf", // 131
			"dcebloncjgpgngbcemmmlkgfjidnlpbiclcgjmamogmniaeakeaklkpbjcnehbpfclojlkldcnenjnmehodbbkikgkiccjejcidolopcfddpilkkjipbnemhihfapgbg", // 132
			"pjbnmbndmacnhglmjnjhaghckpgilmlepjebehgokpcnancjhfffhnhggaapofdhikfjkhbhgpmhpmbhdplodgoplkmjndciadafjnfdhclogjgaoiejhjkljaehhhjf", // 133
			"bokgibpedgnpbacbohmpojajbgmjedlkkfmflapholafhmhbblidcmbeohobebbinjjckekpdedlipjokicbjjcoeonpebiimmfflheonmllaofnemdgflfalnfndlpg", // 134
			"dgkpgeacacolaligneadnbdmooadmdfmhjcobnmnhhkjnnkocfpncamnigelhbpcccepieppgiihflkmoiminbblmgeobgjcebmgkpgfopffekncmdngmognclmjfani", // 135
			"kpiecdiihfkalnehgomgdccbbcafdjjjbcfkonoecbjofapgdihlbidaaffibfncillncjjedmglalbopgabgpoccigfeaacdccedhfdmcpcibcilijmijlldpkbjamg", // 136
			"hfkjbbpdfdhliecokgkcohpapfijffcpdpagmmgeffacmngpgfphkpcplealkciofkodbilbbndccjiooofffilbkpmffgchcgmncnelfnmmaipbdokimnbbdgiglpja", // 137
			"oaippiikiahcmmapcmegmbpkhjeiofpipfellkfadeakkpponpadegnaekgigdggnpjjdeckcafogancionpnfjmkfdoelahkoicioakpkddocbbpddgibcamajekgdp", // 138
			"dbnoddnecbplhkeapedlphpbhbfpehohkgfnchmnnbpfbcpkhagfcmjpdpidpokncmkjoamiaohhganikbhecdenkiadlmmegjplmpcobbdganbigdcilkplndpmbjeg", // 139
			"hlmbbfikedbmkkijlacciaheapcogkpnppehhpmkljondggcnoajfalggnpmancjjialnoojpkojfcpengmmnclcaibgigkmnckcmbegccagjimfihlhdmngbkndcjag", // 140
			"pdeigllinaajbjihiignmgfhbhiaanjhjfhoobeogjohepflbfbjjfnonjhdafgnkdffecmmehnodnlejpchkicgphiombbkaablkadihnoiablkkgnoigopfjefdnfh", // 141
			"gbnagkbfdjofpebkdeljhdkgjifkdlnllppbojlfmkfccodigamokeeaapopnfjffcpfcmpnefbjkhgodnpnpkmonejedjciilmhlagnpchinkaihncgdbgnfdbjehaa", // 142
			"abhimekfjcjgfgglooacpihbmgmnkenmodcchgjknmnfdlgkpekdbacigbdegifikbejecdoefnoicndghcjndekiicnopfninemhjepjgmcokogbmabgjnjfkjggedi", // 143
			"pikdgoeedagclkockbpomfmdpaikfhaooohibiefklbcgaodhhmahophhnfkipffhcpjblelcmepnehejnoikbgmhhadfocjegmelcdhepnfjcmlcjfgnjfaeglpkjeh", // 144
			"ofimeggdljckbknpcmpjjconenfphhcmgeigbdjplmlikgmfkmlhcocgngjlilehllndmmeacopgleicbjmflphmfkaleeckifjiaijbbgdidnlgihopdepchognbjli", // 145
			"efhlphkbgegmfaendfhhhcmdbfhificlpkcjhbhhhlngiiilfckdmpcaljhpdamecbloobadjghbpffndaiijlkimegblmjbopoaabbcodhmaeofamhmijeffhpdgnjd", // 146
			"pionakjflaoaidonklicbheagdkmehgjejdkcopbogpjnkeaaagkijjblamjfgjmhiamjepcdndegjpdhjofephcpbbnbkcchlcahdcnbingmklgimmbolfddnnbnheo", // 147
			"jmodiijjcbbhempkkanknkbpddkjahnlobjfhbhcegdahpbfbcfbljobhbcclhppfgcchdachjedpcjjjffihmghnjkkhlmloiielnmagljkkdhkelfclhmpembhdhge", // 148
			"aoakejndcnnhgjckmnahgbpeedionljjnpjjlggoibhoiiheknfdfhbfojhknghklelofdacocckjeigifomgpeiclgjomnlphdmdoiijeknmamonpciilebdbamjbfe", // 149
	};

	// ======== EXPECTED_VERTICAL ========
	private static final String[] EXPECTED_VERTICAL = new String[] {
			"eadkaomliofealjbmhakcpdjfnelbfopeddniohcioeajgkbooaamkncnpndnopbpohacjloabhkmhcidbhodjanhmbaofbmcebojjckcjaaofdncpmfacgfibajkngd", // 0
			"ncoepcadbnabegbgojbddpmanbeafgjnbeilpmblhbdlabcmfdimekcdfjdbaloeahgladgchhhofngnnjfcoiinbomaplppanpoajkigjobcfpbdhopdemihooigmip", // 1
			"hmnpfhncamkfhbnjpkcaccnmpogaohdhcooflbiimfnneemflgiejoencpdkmjmjhgbjgeahabhgcplmfgijdnmjldpkagmmhlkcjpjleledenjnfbgncmgpehffmlfh", // 2
			"bfjjfeklihokiklaelfpcbmjedjfjmdoekihbijcnoppmjlnehenhfnfijcjghglklcfmmajdfddllmppnldbipmjofdfjoegfilkcelcofalfakimhhndnkjkoacddo", // 3
			"bgmmgppkgaekmdgmnkpfehlefjkkljmaknjfgiggfcdhpiimliihlljlhlpfmnboanelokfbkfhahbpkdbneibhkjcpogikedbolajmlpognhjalnlojoeamkaofapfj", // 4
			"igfhoogdnjdohdgeedfcipomjmahmcfbifeokiaadefpcmnpioknhchnkggcfdpcjioppijlabhjncloaciafenpjiddfmlolbblmgleapikdppjdkpfokgfniklpiic", // 5
			"kipfngbimkplpddgeinoapikioldgkdlbmbgcfpcimnmijbmcaebbbojhkhcddbiplbnlmplplopgggepikabjeomkbigdkjmkafjfiiahomeapeapniekdcibppbfag", // 6
			"hahepadgppmaekpicgkhipfpifdjdbpfeedbmdnpmnlmobhiaicoclidbnekabohiicdhgancgdgjdmkopddieihnhbihneankmmgljgifjhmgipklbedbooljfaoaka", // 7
			"naddcicckackjjcpocghfoedielamkplnmloikdbhaekjpomaimihiidpakkmpamcebjmcmgfojlhdfanbeiakleckodpeplmaonjjkmojcdnedimobopbbcdlmnlcnp", // 8
			"cmokkpljioemifhocajnjlmchfkhadgopaiodphhijgkdeabbjjmmoloboemejphapbkklcbfojeidbphccbmhffjbjckokbgcgcbodlchmpelmaleofempcojojnpnh", // 9
			"gncplmgokaniipfkioianlcmniaclbffacgcnjllgoghghdcckficmjknaicfjckpdjjhjhdepdmofamobheeaohoandhppfciefpmkkcblegoemippjoepamoeccjei", // 10
			"bmacohgihemfeoncmfgdnoohjgehkceiiappmpgejcgpmgdopomjnbkihcpbnceiebobimpilnjamhjehjfkceabmeigkbgimihnkgeemiolecakahijnnclnkihgmpa", // 11
			"fopkgfljjdagbihalaongeapmdkiiojpgkcchfpmehafinmeobhdablppngggjiiemeoegflhmakjmgkoamgoaochajnongfembciibdachnngbhkghbkopdfioihdkl", // 12
			"okmnibjmglpfodgnbipnknbhijemcbejiadgoagejmogoegnadnindkcflahebbfebginclgabcneeclefamjdnnncjfkgimicdlajplncfomhdkcmkgohicnjopmbap", // 13
			"plccmonjfmgibfpkiomklmhigjompkdnndoimdhlcpjopoldolpnakofjkgohanknkpeefaibmdckclieelnmginjkembekjboeojaidheijkeppdjkjmdfniapkcand", // 14
			"iffgmcmjclpecpkalkmgldofjdcbhpelkkdcinfbbjekdgeenlkfdlalkkpcdaijkhfpfoifcmhgpokjghcpgfpdcnjjhfcgfigfopdlnocagnkmjfgnjpcmhphglkck", // 15
			"edclgbbppdpppeoongcdjnkgnaeocfhhklgknfdahfpeajapimhfdjelfjkcpllghjidmphihjieimhnmdoeelngffclglmcodffmfndgajhfliamciodcccolchfeak", // 16
			"okonjiedamgkkbpccoomoolojbkmkmkeekalceegmaijggbkhfibbleaeffmheflchooajlndoefbjeiocmncangkioipbampblfmheldcihdcmmojjgfbfbcllkmcmg", // 17
			"khjnlpelpejpncfenjdcdmgfpakabcpkdapahpagppoljmodnaaneljohmljddjbeoppchoilbelelhjdbalekmdnjndlnablhecmcknjlacohgjcbeeiafimflegjhg", // 18
			"khnffmegigajdjngfoiljeldihlpgcamepaifhoeabihclaoiabocdjfkgdfepcogmafjcijldefgmddcmchcoboeimnbbfjbjgjdddmdccapkblihlenoaakdiaodoi", // 19
			"dppipnhbjhnkcnalkjbblcmndfkbgheggnlgdkbkeohdppjofpioijioacgiojfffjgcfjfomccdignogmelhenjeigbogfepimhkkklgafpinadimnofbdnfjjejeae", // 20
			"admoejmppehcclggjmaapkefkiadbnmilfkdpodnjfjhhciignjkbbpfkjmojfknmonpalcljogmhmonhdcaajhadkkgjpojdeffipalfppaccjfikegkoflfnolibmk", // 21
			"khlhmncpmnkaoogipofdkgkennknbpaljiailgjfdlofcnlhjiggbpgacdiffjgbkakapmalnplnpajahamejjencpjfokbbbeiedaeidfdcjlldlkhohjkajdjblhae", // 22
			"andckicpcpbfhffnlebmciofdhndjjapfnbdoeaidlkpacajgbidbkgdlbgpkopbfbbdndkdchbfcacfmgbpcndcgcpdfbamicdgchokjjbkjkcbbfmcoelbpedipijk", // 23
			"bbedkhaebealnblpikcjkiaakaddckhjnmeiblfkpmlohbmkbdkkmjnmeljmocganplbhaadmegjgafnaekjoomebgchepkfiibagbpnkhjfpjbcbcfaijegcadgbfgp", // 24
			"dkkogiipoijinakmmicmogempdhahcfikdipeobdjpmdccmjpgfokheeecmnnmbjbiigdcmbakjcmieaicoianmaneiijjinhmpafeepfhcphlhbdmbmpdlpcoeajelh", // 25
			"johnndngkhmohlblihbkmoheodpgmkalkiedfcfgmhbemmenjijkfnafbdjjnaaongmmdgemkfdgblgfifcpnkfejecjghkmopiadebgbnmkmhfjcgmnhkjkccammali", // 26
			"cjebhgobegooejbjfkdmlnmgbnlpflmdakgmpehkondamgkagmdjbfeomkdkmgbjbfnecneonnfhfimhpclbfoncfddofkbmifgkmmmkmkfafhghpohhheaiiaefkiop", // 27
			"aaokifbhkbdhdcfmmjffacjdjaglmopgiookbhaebmecedgmpdeodjebllobipejebckhkmmikbpfibkbchejhbfjhapckblfhfjpkikkljfchnajilfjinknebjnfdf", // 28
			"gpljalhhahejmcppgcdaeddjnhcmjplcmaimbfalehicphemebgkpbdhdooljanpafbnfmlmlflhlhkghpaoclnmddejhpbjmiheebjkcfknjdggdgicdnndmokkoiem", // 29
			"aphcnipncgdojidjikhahicondeojnkkhhcapcggpmimeijgcleaelopkdbedhkjnlkiabkfknjacabeioidpahigncodglhboejiflmeophdceobhbplejbnfmbmcle", // 30
			"ooinmlahikjafflemlofgdocgomccgkmpagnapcpngheckopgaglhmhcplmlhafcjomdhjhnfijcahaandecofkmiciojjcchjihcbgpdjmcadkfgofhopafjpanpoll", // 31
			"ccfoohjfkknemokgiflgljgphhiekcmdagljmgnikjpagfllildmifhklpleoifdlddehndonolaepcliiddballkaeipifgbpkbnobdebbohfpccfdkgomkelohjbhh", // 32
			"cipehfnnaefifpjjbicggnedckdmkkbepoognejjmimbghabmiijoiapbnnbckoobpigmjlgdepibepdhoklbfojpcmmcehnehliblnnfpjhdpjcobjegoficcaihpco", // 33
			"ckbaghjccacgoncdlolnpheenfpnhmlknobgdkkjepcokjpjakjimdaoedmbngmooedkmjacopanpelkagahgimepfgknkidjlnaeoghoepbbbmmmfgpdegcgofdkina", // 34
			"lhoifaadpejdmfkghdakfnonjnljednkjjhfblccfckncojbonpeahhdchledahhkkeaceojhdfghiiddolkjmfpbjkahaaobelakgacjlfgcchdhkofhaacnoiicikp", // 35
			"jmhgnekamkagnopkjfboobibnkpglmgbejphkjhfbpendpdapnldjklpdddgjfpmhnhcaeijohabidbnjflphmoipfbnojgfeakhcppfdmdibmbfeneohflbhhidlgok", // 36
			"mdimogipgmlabchpnfpjnbhogcfjdbfaifmfeccfmlcnbpglhgpdncpinkckmeicdembaagmdfjlgelfbcmkhoaeccjmghcnjigiklilaiaocdagnjfilnhhojngebno", // 37
			"hkgggmkdcgjpmjgplpaddhheoldofmemfggahkifijalbdohmhocbcpmlcadlkggihpekeonmgkknkfacigallekeeoenhcglkkanoglonjdplieblgfhkdgepgjoloe", // 38
			"oeefolnahemfoheaamfkapdnbnnkfmhjmlcinkdldmlajphnihccnhfajpiolcfoeadoabdciceelahaeimhbinngilikcakpkdanphdfcpbolheodnopnfleoobnigj", // 39
			"fdefibjjnbkhpjgjbpnnlahfgfpfgpcmgoabigmggecccfcgcfjkaooefafcljemnhfkjlikicoaochdopjkfhpffkiafdmdipdgblmaickfocnikjcfjofflkdhpobc", // 40
			"khopnmegpammbmeedpihlihlbkiibebbbdfdollonnaebeacdldfdknnpbabnelamjcejkcelpndkhkphnjdjmbbdhochafdpoccpbjlhiiiglchmipggbacagijihll", // 41
			"pollplmcjagknnkilkabpecncpekankoapfclgmffmgikconckfkdcnfcfjljagkhahlahgmcnnpanmjnnkgepnnolcddmokkogogbehhacojhfgmoiakkdfgkfbenec", // 42
			"cagjdkedilniikcanncabpoagojcjefejdodfegmpffiaobpjnbpamhccbpegapdfgoeenoobkogfmdknoojfeegaamemgdnlcofpjfgndgnombipiocgaooedmhohlg", // 43
			"pmamfnnhghnhgopecdgpcgambnldfcklladdjlnkpidegemgadmmadbeelpolkegdfdjimfbcehgebjhjklcnafhjfpidjkefncdgdgodfmpienhnehilhbhgjoogicj", // 44
			"dammckeghmnbicplphipennbgfidbmkcjlffdnbndejecjgldcgndjjeagaknfmhlfbkliefkbllocedhndobbffkjokbcajgipdmngnobnibopgbicghhmpdcjekfbm", // 45
			"ifedabpkdoifkpgndgnloobolhkiiaelgejbcbjbdgkeaoomeidbinopjamiaaeekedalkkkbjgnlapbglomnnchejpadgeklgcjpokekncflilejofdhgmddgkbbllb", // 46
			"ejdbcfkmbfonaialbehhhfaahieimbgbiencilappddnkgeknhbbcdaeifjpamkefilghnkbppimlboleakopnomolglbikadlojgoammioedlafgacbfdefajkeibfa", // 47
			"fcghobkidaikhpnnbneknbnbikncaakgflkpjijgmlgnhdfmekcplchpfpchajikbpleaboogcegfhbfkcipllifkdjepaboamonjgfgbklnpkfmdgclhipalainjooe", // 48
			"lmdonghnplphfkndigfkjobeiedpbbpjjpgchlhchkecjldmcjfamljlajmnecdhcaggnnlphngmjlicjacoljhnoahfhgafichankkbmoijfaalmkcfhhfpokcpdfjd", // 49
			"lnhjlocfgidjmmeiojedgopcejmgdomdbnhigedojnpkaedcbigliobbeodkaeobognbanjkkadlajoknnanpmdfcnncgninhnpiiaaplpfbgobmhnkdkjngjbnkbdaa", // 50
			"kploilgkcegpeeabopbhcebhghbbdfmgooeikpoleinfdinjjdoocokapepoggcmemlnkcocifbamncecomgbnmbklbmmkngjchbblncapjjfijocbnppangipidfmon", // 51
			"gfpmlnoeoappgiifbfildglegifopjmcgmancijbbjphbapcjbkobfllijclogepcmgkgfmihnjoebbibnfdcbpcfbfgcjgomfbbchhphbnboaehfglfdbbdcenoeelg", // 52
			"epjnikfknpfalemkfmomdflfpdhhfapjlejjmlafifgnhmjafgejkkblangjllkahdfbbgkjdjmbdlhfbdabdmffmaldmilmoefnohdlimhoeaghafngocnpljnhcidh", // 53
			"ofkppdjbnpiahiecgonmjbifjofjflfbecgidohlphaaddoheohngdjmnamffolfnibgjlefbjpgghlbpabhchodojkjbjcfkkgiejkjknnefofcjkjggbpncbkjljcp", // 54
			"jgjjhadmkmcmfdgebofgieldfipkafimobbemhkdbkghfeobnnjaegmlifmgcdpfhhilkikenbfgociidjndegagagmdhhbpcgolioilbclgoabeonalkhadabndebhh", // 55
			"pfpjbbekhfekghnnalfdkdclehiokmfkicoomcgkjibbnaljodgdohokcdgccfbmnimapdlcblpmhgnkdjjflkkecnbonlonegmgnejfeckebecoadccaojkdpikmncc", // 56
			"mdlgeekkkdbebjnknjddmjjjeokjoinakehjjpgjnchaihganhlmkhbocjjenjcbhiigflkmncfiggcenffhnknpdfbocekhcejaelhpjminaginiihjabibmmkdchhi", // 57
			"befgcamibpbiafaghdedodidfbaliabbbjmbbggndnamkcjhpcojmaaonamddjebidhgoffdkebmnbpgjodhhpcpeagmnaeiiiabolodkkjpblfnkhfmcfpjbnfdedpl", // 58
			"fdanabhadnapckiolhghpmepbkgkoccpgaeeejgkdiifcambdacdgljcnelaipglddkhihcmjhehiipfbjniaflbfggbfjdgegiijgafphnabldjhocbiljjhaamckdk", // 59
			"jebckiahhgglkpokdpgebajkklelakhmigdmbjadpblkoaglddebmcbfgddpkahgjgmaehifdegmcgfolmefbapdangjafkinghfnlgbdcajpmkefbaaoeppfcpfdpcc", // 60
			"ledfadkfbocaogbohhahmdibnioanponbibhalmdimclfficndimfpooilbecemfcommgkmcfdbbcfgjmkjjjlekgedcbfpdkcpbeeknfladmgnobgijjmclcapjiham", // 61
			"efmededpfaehkpkgoaiebncjnklakkanpdmjaofcdfhmpoibjdmalgdedfnoikgacapciflkbggiioonbcimaneaafomgbjmipgabgaojbpmeihafeljmedaaakbjnih", // 62
			"nnkmhoecgdlmeihbhhdkjfmmjnanbidohepnhfmbblbjmaapjhbdofmjeaphhilelhlapcbnbldjhnmelaflgelmjmkfdhmpilcbfjckpcjmieinnphnbiohalmambch", // 63
			"lfolpkgikbinadbmhceibclifmmnhfookpcgbilbjclgmnkmjaaeelibpoaffehmdmnbphjolohdibcfleijeohbbebpdjidcmnkdfaolpcobjiliddopmheomffmija", // 64
			"achgojeedmdjhkpfkkifaipgeoffndpjjjkpmoeghmdlhiclbmhgmdnchkcmhfgeldknannlbonfngjmhjeohdangagbjkelmlfibaepompfbibefomokcdlnpjaaoda", // 65
			"dpmlappjdonomhplpfbkogmcllbaiemdgnpnooeijoofomoahgjdabkcnakabafbiaihpodflbibnfjgcgempobpanakddlclfofhjadieikjjjgidfbkdefhipejlin", // 66
			"aepgbnceeegogacahifdlnmngfckabgcmedmnbpkaclhfedecchcnkcinibbnbipdmlfgahoemoebjgfefccofbgofmolnhpjnnbcffepbpjjjhhidhpoigjbhghkmhn", // 67
			"kjikanpmpodiplmhigdhmafmihpjoefcogfccfoonmcpehlmdambeijmocfblceekfofkeihjfnjfhgnmkadielphmghnpdfbcngkhcodfabipfbnihbnopechnjooia", // 68
			"kmmfdelppogalbkgjgpfpedlmpdhmhinibkgcniecafkggoinjcomefefjjlbhpojdkeoefncamcplnpbfmkoibbamiadlbedjkaoafdedbapjjhkfhopfcccgmfaoji", // 69
			"lpghlipfkffchhjikkdfamehnjpkdeapdmfnfkmeokbkibccpbmccpfolhnjimmpdpiklogemkapiigfkopkkhmkgccepmmgfpladdojlemabaiopacgldbhdkakalhk", // 70
			"cdmgabcncpfcgebflikinhjkojfeaicablfkflhchiobclkahgeoebjbbefgjebkoncekcammlboihiphhbgilpkdolagelhaajmgopbaoeiljkipipnpjjpppkfacpf", // 71
			"efebjghcpmdnepepllngeccfipjhfhgpcfpcdnbiimifljhkepjganlljfgkebnlnlcbkcogknbgjomdigfnmbnbnhkejhpnhadeimeppnjjgekfadakialpledecijp", // 72
			"gblaehnmeppagfkfliocebpggdeejmmdcbjmholcjcgppdibfkjnaachbjbhmkdlakebjeggklpjgeamcokcfcmnhmffcjjidndlpfbnmfcfbaabaiaacgemenlgpffb", // 73
			"pdipicagckajidngmmpbkknbbdbeookapddibifdliahbfjcpanfgjjnedbjebmbpeaoaidipbkbfgdabppfcmbaopecpodgjljphphfmmgafkcimnkimchhplmbppnh", // 74
			"clepapjdhglphnonipkcmphejjimolaccjmlhonnbbmlelnnlefnehefgcncakhkomophbgpndaphgbpniecbeflgdomdibbofbjgpghlkglkaeagheliobeafknfdol", // 75
			"phmijomjjacgipbigigjhlknpdifaicffnhmlmlaalmcpklakjkiaipcahglpefgecejbjnoejjmcfndlekmdhjekdfmhonhgoohllmbeckdgfjdmnbflmfknnlbhccl", // 76
			"fajmfkfkonlcigbkkjefpomclfneacjkdblaeoopmijcnmaleefjmmnpkfoplokdlpkldmahhicmopbfpdknfgoglhkmlekphdnciiliolpddeclnaplifbammpicgbn", // 77
			"aapnplooijjhjdmoocdobdobimnlbdpccddjimkicgmfofgfjlgileejllfjbckceagchcmmapehcmjpfppnbdlglejmedcdhpgiimjjcphcdglhkjcfogoohneoddnj", // 78
			"ddjejndkihmgelmdkochpjhalgncjdlmefaolifjmmlhjoiclmphjhpfdimclccfhplibiafchaiechikilamhhobafmedimijjicobmmjcfpenimadjkhoknhamkcbl", // 79
			"ehmfaklgfejfbmpeplfkkenamneejmflfhjhmianagmfooabamakopmboekenenlpcjkjickfbocgkelpakfbieibgploedcglehngonpdghegmlhplgfogofbpldloj", // 80
			"mkbcnpmlebgjmlhmonncnabaopgnppmpfkjolembgbemhebhpjfcmdiggmhidfgabogblamiijjpmhjjbmgannkbfnheclaoiffelgpgenlbkbgemfonnbohmjpbchoo", // 81
			"kelahegfojlkckhgakeeebmmphidhngemffnkikmajoliccndknaidipgdnngjfkhlmjnnoigcbdfgeffadlpgjcooijbphnmcihbgonbkcicfihlkpjlnbchphlpmaf", // 82
			"imllkjlcjemckdmcehfiacmoagcomgejjngmbjfgibdmophpflgidlljhlkbmpjepfhdbpkemhllnmlpkjbjakfmjnimfjpklpdibjdbmpghpenjoblbmhibakfphhoa", // 83
			"lmfaedegfmolbgdefklhgefmjhkpfajenckpahajlhjmefckbjlcjbpincejidfojjhokafolpfooccbjpgpbjhoehlhkhgbmllcbdnhijkngfmapeakmbneacdbhmid", // 84
			"liemlnkgojknkefbohcgajooiahjaekhahibaenkdnodbkghbfgheeodmaeioemgchhmllpoehfabekhccodiifkdjoagbloaopamnoeemlhhhahdmplbljccneahacf", // 85
			"ohckbkmnmakhddfflffgdaamadnhnagecjmjolnblfmfdbickaidggchjfmimoiehmhpiehkbhgjkjkjibhgbpnhmfnamanfbbgedfcnggkgehjdojfocjmgihoiihoj", // 86
			"ifflfpiiegffphkgpmjmjenoelmmijodpdbifohjgkhajipkbgplmddeiclmpdmnpnnfcenolmdccdijjdkikaechfkimnlgkfifnichlecmldlmclbjmimcbindaccd", // 87
			"ippfgnigeibodkefndhophbhclmlgejlejagnopcdnkgndmngniiigddalbaikpdbfjehlkofpheoaeeafafkcfgnjknmkcbljehmdcamieicfahlpadnjccmocaofga", // 88
			"hnjemejnglapjgbcamalcocgmgdmcahnckcobjpooilhfiddokpceljhicgohmgohnlmgoohebdalajmecjcjlphldghokkmhiglmjigpcfmcfgpmhcfobepjbdfcofn", // 89
			"cdepmajcmiofeaifeheicdabamajdkgfpcgdkfomlcaicglofmnmlanhabbieooibhgomgoecekfgogkoidjnnhaoikecfjkiajagcdhbbgckcbgpiclhjgiblpmhcmc", // 90
			"eocfoieigibodlmhbbbjabgodffgimcpcajmmhojgipghlmocjbokfgkahpojiohllkhdplbckbbbacghcihmlcphddbhjolphojkgcgmnabkoppkgpjgkjgoadbekah", // 91
			"nikaiikiooigknekjlfkkjonakncocakakpmnjnecbamlildljlcdobfhcgfdagoilnpjnklojjggfppmackkfanddaeknbihmbaapdmohebjflcohieijmjgjobndid", // 92
			"lbjnkgofdgkceokohblgmnlmelpneaikhhdkcokbomammeafhahlomappclkbljcjfmenbkpfbcgnkjoakgbcohgebnbdoagflchphebbodghhbooojpigcikhjjhbpb", // 93
			"edmecfndliebigefalbomggibnhdpbedehmdkooealgoiflnbaadcegnabohngkdielahehanfgngajicakfmfooicnacdjiacljecnnjpngcmjlnkaciahfgeihlajl", // 94
			"hbbdjeainedokniflakiohpldnnajlhncffphlffkccjagbfanopfdakgjicfinckdjikeieoeaanjofgbbddhfaalihgnpjmocebmekdolhbjmlendpihbaopekldjm", // 95
			"iammdnnflhioddbbikolbpdpcdgkinlobhbhiipmgbhefkdaiopmaggnggbicbofmnieeegnkcdcihbonidcenilmnjnlfakodhdhghhdcpmdceefhdccknkechlfndj", // 96
			"ldlkkbeafggpjnfefmmodpoopfbkojklgpmchjopnpmgplmplgjdmclcalbhidhmkfbjdpdehmhcnahngibaeneigkehdgfhnfoglbocbhhglolpjeaclniobjjcafhj", // 97
			"dgpkdpaapnclnmjiaphbkconmlhdamkomgnmlajmcammllpopginkenmhbbkjjbilgcalleakdjnlflbcdinefjfdpeldmgpdkaikkbmnjlobinllfbfinhhnagcdbao", // 98
			"ageedhblbcnejgohnmkfogikjndjocmncagecknhhbakoglffpncmbpnkjpigcjkigjgjhbnoggmdehknidbmdkfbpkpojdilgijfaaaaiolhoaifafheedbkdbgpdnl", // 99
			"ncifaljbfaadlglljkjaacphoalhilblgblinlnbgkjlfaohllonbnockodnokolkijpcagbgmpllbbocnkldhfellkfeibbfendpjjmgengimobogbhgeaamadamgcm", // 100
			"aifhblojlpehkekgopjheojajgjlkobghiagkgdcihcekafpbdleihdibbjlmhefkligbcfkjcpdnhfbobcinmacjmcgjifadldnfkfkhmcfgkdokdnhbiflgmjkgmol", // 101
			"blofaoomejdjoggbloekkfgckngcmppifmgpdmkomkkleaompnngfmmahepapakgijilabgdkkjemphgebjhgbddbbhoikdhgllmbcelcobffoabehglnjikkpgngige", // 102
			"bgkipjihckahccnjleklganeiiipaeaehmmemfipbehpfikbgificijlimaoaikbadpaeoapamlcdcnmfnjcpajjhmbcdfcdigfjkbggbjdmnfijoohfiifmehebbpce", // 103
			"cdloakkdammcecblkeejjkfcjhdmbbdmfafdckbomdmaifdkcfkobmckaajlghpoikelaffngmffolhmlhlnbjoaoamnifijfcdoebhdiacehhdfkjhpebekdmejplbn", // 104
			"pcefbcpmlddjkajohaignlghaehfhfmfjclimbbiceklknklhpdpiaaojmmkoipcgghmhljipkenadndbkamjnflccnkdkmcgicmndjchnlkalinofbkfbonboimdcie", // 105
			"hlilmfhhhpckehbppccleplphdkhmgfnbpncgahncppdpiflfjpdanligfidnmddapgeahmmcjfmijnkojkjdalkpiebmhdpcnejjgpfilclkjjmcdnejfomihnjbdbp", // 106
			"ggeenokhghldbmjbnbnhghbgfdibmohobaoibogdaknodlfhfomnpdpgiajgedkecpdgolpnhkjpiakjeodhkmkjocipcocgibcgfodbjejlmidiilgldpmmoaggdnip", // 107
			"ojkeenolpildmibmbnbljbbehllmjcgffldfgfacbcpnfcpncdkcafadigpokblinioaembfaihoeobhllhbpioaoigpomjpabbbdmmmbfbihemlnfenldbmcneegkgd", // 108
			"fmfiajpphkiblbnifjfjkjkcpidmojnnnnljnbcdmdknlpmolalcphmbohacmcfopakdampoilmpjlcejgeldieahallepchcgkelkejnlihincpapidanjdnnhamboh", // 109
			"dopabdjhmagpbkaobemceaalplnilmclfnbpgkheffgpfckkgfpacakaomjbjhlgkempbpnpnejpfaaepmgmhebfhblledekhnfjjmffiangmkoeoibajnfkfbbiaabn", // 110
			"ejalccihpdbljgmcflkbmdihohikcdmjecenjdeckinibmollelmpkggmlpbiabapniehkladgobcinafeiibdplniiakcndjnalbakljifcolggffjbpopplfpfcgnn", // 111
			"neldjdlhgomjphkiolaahgngoljeidnfobfnceifiglaokibcmicejdlfnoaikonpkaedemkldamolbejomnppbiapinigjllhicnpogecmdgmebkkmmapoacacffike", // 112
			"ofincogmdggdfmnkomclhlplhddolpcckagimbjphokphelaaggnlaaiendcmiagkafifhimhdfkbpjmbnndpilmecmgeddhkbkioifgodjkoffhljnibpdfnoikoagc", // 113
			"fapghikfaahflaenhnokhighndmhnjojleaapdpehlpcnkffcgbkblibjckekanacdfnkjhdamejjgicdjnondappempiflfhikhmiahhoklbichogfpelkmhllkcoih", // 114
			"cdlelcpigkkmnbenffgbdjnecogdnnoomagkkpephnobjjhhfnbpdilihofllnbjndooapbfppidbbgcgoliildniocgloookoaejkmgpanpkoalkecnilbohhjmcefc", // 115
			"hjjhgoelkjogmmjjhopmlfcfhilfeanjfmfjngogiddalfbkjigfnabacdmhfpheofdfgcpadplclhaoahamifbahidkfaadbibdpaigcjcjjdbnhgdfajkdemgjpgee", // 116
			"pmcmdaclonkeiapkbkeckocghoibakbdphkclmmnennjpghdmiafmkenmncigocpdaagonhdjdncckfmekdfmjljcfilgogebejaedibaeinmackjaffkeipahgmijfc", // 117
			"cpieobhknjipphnocbjmipkgencacbbkfmjidcbbcmnoikjabkeffmnbaiicmmbpjjomapameopijnlejhfillnjecgcogindlpmkfnflejgloopnjapcddoinonmpgi", // 118
			"nbimdfglfnhilapbcnlflggljgfbmjkckdimgjeicjgdfpeoeffjnlbanapgplmkcopcfnjegbkfellekeodpjiemimhndbkkddffeiniepiiecjofcphnkpnfamnfef", // 119
			"mcfjedmcmghdpedkkemccihidiidfbhpdjdijpemkpgojdpehhjmiljfemihapgobgdaeocmdmogjndpgdfmoleapiamjbbbcbbpohbomjdlagjoljdbhikffjhmiajn", // 120
			"fdlnogchhiibjbpoggnohdipgbldeedgieolnpfhaaenloifbdjdnnmbaobbemkjhgmecijnhkijoehjhnpafhbhbcjkijdmangchllkhcflagnincifjpaicgdilmdl", // 121
			"jaehndhndbgfdlepfpoomkagnokngfagpmfdjgjegojjoadjnjmamhgklbihmdfphopedoegkkkdkdnefidajfilpeijbpgachehpnkgdjgmgimjdldgabinelioeiak", // 122
			"ennijiknffmojfekjpbnjljegjpdnlkmpkoffndinmbcchenejfoofcpchdolignolcdbhfinpcedhmandgbameandchbmidibhnkobpnbfegicapdodmankkoklllpf", // 123
			"fplonmlbkjhgddkkmjidjlgogcobmiccphhinnlobeopakpcapikaheemlchimmhajjdahmghgipgglebdamhakjfncahodobgpcpjpngealclinhaomajhhflgppmnf", // 124
			"kfjjfdpacbhmlehdmampocejlmdddiandldoihhlfnimfnckimehecocaiaophkgfjclichlmognbglofdncgkmklcgiboohggjhafkhfbbjpbldociiikmmenipakak", // 125
			"hanfnobilmkmlldaimfoklicjhocnghhmnodehpeefapjjgfipnbppjffejmdpoombphggbdedlohcgphhmdhnablggpckhjekhldnmehoplpgeiemllimnlgcimnoln", // 126
			"peedhdogplbamheilnmdglneongpciockbeblbaddkciddapnojdeleghlnbcmmcgmdmlahikaigiejpdmmjligaedepbobioegfijdmodeekejfpoccnhgnphdkbeoj", // 127
			"odocadmpaofmpmdomlddgkcocodiicddcimefoppfmkeloipgebabgdikmhacbhilbhibblpmngipmneanhlkgidbmkdnebjkojjmahkabcfbboaefhgdfkpmjbhfagp", // 128
			"fpklnlnamiikihhfndgpebkkfijglaphpgjpcjkagphmffnnhidmfjhcegfokflpbmplnenfhnkmdlkocnchbapfjgplldidhnegcooghbfaghbegkadkdpclfladcgi", // 129
			"mcolefgplbdonpcdbdlndakkiagjeegnhafkkeahhcoihhdecnaapdofemgmcpdbgidloncecjlffkcmdkchdaoonecpdhdkfainhofkncfdchpkmgjcdhdkohkdjmll", // 130
			"bkkfdagejaklpedpjfpihhagbhakoofmklbagdmhcghldphdekfaelmlacegdkmfgflcephmebnkddngifkafakkbochgjopnnfgkanaijoccloafhacoinckbjhkofb", // 131
			"jekdlmapcjkgleepldicbjmgfmongkalpeeidmcehfefoanjbooklkkehlpjjcgnlbmnjpcjlmgkalcelcajcnhpmndbellifiamaibchghhapfoklifmjaakinogglo", // 132
			"ccaflignmimhblknkjoddjphdkbncojgagjendicibifgdghlbhfmjclpddgipbppadcfiainkndefdjdcemfecndndbajglhagmehanandgifogikhhncpdnkplfomn", // 133
			"bdjnhgmfmfnhocnhnppbdfogebmkedcimkabclkmpndkjblhbgcjmfghfjlhihdccfogdooadelimfhldedhcjdmfmgonalifacdcbinoipndokhfkomliahljmahdho", // 134
			"abjmgodjlbbddocmldmmepkfjdghmhfcbhjlhoggolpkjbdjpakdfafnljkpkmniihpipdillemikkmbehgiemkbhmkjenpjnoephcanmpmaehgnopjlbcbmihdgofoi", // 135
			"jabpmbfjdanfkegdohjbiggfkcneofedndaamgmkgnfommagkcajljgcpjfnlclclcclkklfdopgbkeleplohbkjkakpihodmdnhfobcfpnpkcjkognehflkmkdhkcdf", // 136
			"eijdbnbfkcgahmnkmgeffgfcehegmhcchlklhhgomgjpfbdkalkbpmmdiopibhcjjbbccagbhlccdhbokjafcdbafojooelnelfnbidjekkokpmknjiladhfmlmhpjfn", // 137
			"nohkbnjhmihfjhcplejdbaobinfdbefklpoabfkkmffdhholmohblffcjejelgkbgdklkaonodgkhlnnpmcjhbpgloobhagggjfdcbllnbicadkdkcomeenkdgnhjmob", // 138
			"dkakfldjplomeepfmjaanmmpajajkjlpnagaephgmgaggccbgkddhgipbiaokhaoioengambnblcfjjolcggaachgfhkejgakbnmopongmggcdchecidpkpkpodjcgik", // 139
			"apgbimidbmfhbdgohgbhggpdcmaggeoangiffaggchafdnidgpobepoooklpmnefnfnidbhkhdigkfngnadkkjdgddgllgfjdknllmeeohoanlkeapjmnfokeimdbaln", // 140
			"mcdchfpnfobjimaijmonndjkfcbcbhfpkoiacbbelmilhcnfjjjlinchkfichgemcalmjjblibkgldmblipjjldcpeegphhofppalpdmelmhcmgifajigiinbaifoklp", // 141
			"dggccnpdkoopojlfdppknmlplidcogipgkfpgdiofbdoflikfgghbkjngoecpmahkdjibichfhjeopaknabladihcmeaffineppcnbbbldkmmajkdlmcblkmobchknme", // 142
			"hgfjidfeblochgeboaolodjhkjhaankjnbjindcalnnlfdnnnfhcnanogfcmcmehchdhkhoogkkpfmjffnbhkdilkapicbjabhjakpjngoappkjkfcbmehomnlihaopl", // 143
			"aoekhgofimmlnaikohpebkpaneomaoadnpdjakmaigledeipfoiflcacjkppgdemfipoinchcmgiddajmcpjbbbghfgljpfgcgjonopooehcdanoncdopfaeceeocmjk", // 144
			"ncdlompphajanbnomanlfigbjgddbgpgfgnjpdhbfljdnnhkinbodbdgjecdfffkkoiakokjmgpmangbfflgjgacmefokilhghpfgkfmmfbnckigbmjoggdppaaamolg", // 145
			"pioboeoajmlbkdahhknnnpefokbhhjpnppehdnbnpglbpkhfldddkgpbfemcdphhnnhlckpnfeadofbpmcdldacdbohiknfkgbeolnklfdlncmjcphdpgfppdgfkpnph", // 146
			"balbjcjeknokgmkidifkehjddfeeekkphkhjfihbdombedgifpjhlnghepnojpojaljoicddmfafkgloolaoigldjjbnccdcjfdjkgolekpgphilcbbdcpbpacakinih", // 147
			"ighencmahfepdjjbipgpibmoejpafiineegiffjnmigajoglackagfknockfllanpineinocmpelmkhjkncnaigkabapdoaejllohlfpoboigmnhhdglnfaegpbamjjp", // 148
			"ohdkadojfapcpcfndolpojpeepgogahhpenhenlbndjdgbohnjjooldabndeioedgeibadchmhgcnhijaofjgdkljhbhpkbnmhnbjmpbamabkpgjkhcdehmkcikaodhj", // 149
	};

	// ======== EXPECTED_HORIZONTAL ========
	private static final String[] EXPECTED_HORIZONTAL = new String[] {
			"mhbkklknldhnapcpcnlfgldfomclbmioaljohemfmanpknjfmjjdiibdcmfcfmjifhdhhlnfdijiafllcooonmagimcmoenpbkpofjjjfhfjceeojgjoglbfmhipecfg", // 0
			"ccbhagpppjegjgmafpdafcaagfhabnhldiienonnlgaielbpnbloppdbplebceacfkkgcafkkmmfibedfkhagmpmippjljglpcdppgbccibiiipihagkikpeekpbnlef", // 1
			"okefnmgdkfkecnbjeejcngihinihcohabhcfooeoemjkgjbcokbobonclgcdhlplbbfcadcmgekkaediofjamoppcmimeiiekagkeeolplncakmiobbjhoiggabjhloh", // 2
			"kdjfeidhoplhhhfmioodiaoeabljielhkjmignoelglocghmmemgdffmcgneciapofkkmcdkldcnkijdgifkmjfclhakhmhejiohoeaooddbnblljocbglhiglbambpa", // 3
			"kdclbjlipipfncnmnoolahkpbijehdomibcmlhkgopdbljfjhdjpaeplhnfjbmdjfbkkomcooiglgifpncaaclkbheppnnlghcahkhlejklffpoooapdeannfhdfkjgp", // 4
			"flkbhcdbhcmngihmhdjodlobfljioeddkhkoaaofnideekfcpddllfbabmkmhamomcldnbdlghmnhedhepkiipappckhakjdfdamaeeigjhgbaogmknifnbkflegnckk", // 5
			"mahmkjkgoaipffiipkpmcicpfkncpkipliddjjijninnifjempbefhgglldppgbcjkgieiljkomackohkjmdhcnhmbffdcdmohakijbccllnjpgcfjaihggcklienjpa", // 6
			"hlohogemlppahoeijbcdhoajjnoggamhggkjakfhjcbkdmnipnpjdlfikfnihbnfnobjdhjgengnocnflgfdjfkljmglcdhamdagifekamhfeckjccloibnkldfkebnf", // 7
			"pcbfhklinanilbffkhejobjfhgaifkpplmoiioaodeddiakadmgliaopfgclmcdcolgadiapjdhefiomemdbcjakbkenoleolbhemcolajhlggejaklbnnhncladlnan", // 8
			"eadpfeleonicjnkmhfmmolgmocgmfpeegmapcdfligmofkkplohgjhiningdpomfcmcimohagedpjhdkliaighblaikibhpddmabnofbbfmonpcbjhaoknhficbfobbm", // 9
			"gckicmhakhjplkmmjjaacfeibmmhomjkdhelfodbacjjpjahkielijfijceinbalhlfhcljchhefgldpjhnhdjoebjhealfcnpelfhccadjiglcfnakeaaphahmegplg", // 10
			"ddmahndckkgldkoanghckhcapmdplhppalkbkpblblbiipbcooecedgjjemomnfjhndmcppfjkcfoliijdfjdleefninnaidlmpjpgoajnlebhabhgiojalpndjcepdf", // 11
			"lkedjpfanclbnoilkalbnlgjkhhippgaopgjbaobonajfnbekddlofhnecppahmaaknnhnjbginbdpanjbmdmjhckialhgjlgldkjokgghhlinngchacbgdmgljlejac", // 12
			"dbggnoohepepjgfhflomilbblnfjigmemlkkaphcbmpmgifkkokahekjfmfaelneocdjfdmchmalognnolajfflhggpmlinbkdhlklfimpceiojaldmdkhpgjhfklhah", // 13
			"fjolginejmjmimpfipbcdoifadpmnippcddiammjindljidnfhendkbepjffjkojgaciiimdbhjglammmlofgbbiammdgjcbkkkfdbomnhabolglmkhdabfmjbmdfegb", // 14
			"ibddcoddbjkihkhfgokiiimnbcomacfpbgapgbjhgmjbpebbhmcmbmgfbdliohfoefmbjnbonclfpjeegdpaejkbokalefaahoioadjdppakkgggplpmobgpflehcfme", // 15
			"ahndgdofeffodhdebpmonjjgpjknpajcnldlfhmkpdimejgbdkpcegihomiaplaponldcmlldkmmnkekelnckhpkpiljednkbeankihiembcdbihbhpilfpdfiamdakh", // 16
			"jhmphjiodmppiggbjjnfbddafiichppgldmadnhcjeenghggfffpicbfieldhcdkcbianpomglngbhedcfggfeodjclhkmnjdedgdodgoghogpbpnnbocmakfmncolci", // 17
			"jedcjpiegoiihneecandpicniccnlgneolaeookbnoonfjikhhlofilepgflnfcfipanfdnmlackfhdiaonidfpmmhmlcidaelfeolpkgcbnjadmplccgckmiifajlpe", // 18
			"goajakkafcmlgkipflocpaabekhliljdkcdgjidlbcmdbifaojjhdhhdemadkjgmcmbjafaobkepnaidhbillghmjpcfejdfadphcddhondnfcnbejdmemgpkmooinef", // 19
			"fednlcblhkmlmcokbfbahcjkdeoljpgndgcnfnjaikalmmlkglokikccfcpdondnlfbbgcekbiheijdbmpmekclmlpmgmdnbdaophbbajjamfdmehkboichfonmjnlnk", // 20
			"jhacppoholehllicjokomjnhihapiekdlmdpikllklfpdjmkpplkojknflihlgdpnbmconlefjnfoifkeijajnjipcjpfijdlkgdmddmkimnoknldkpefmiiocmpgpfa", // 21
			"hcoejbfeecmjphmplfklgbkomdpngjgjcmigmfabigflioefnjbajjcpaefjphaenmhhbnnblolegjajjabigoicaaolfmibmcaobabhllajninedkmeabepobakenpl", // 22
			"dflaalmgcgombcncggjndigohljbkgmknnhelajclaiiakhikencimpcnhmlgnennklafabmflnaahkfacklgphaimejelemdjlclecpenbolgpkjhojidglhiacealn", // 23
			"odnmoggicpbhphddgfpfebkolmdhnoeojgpbnnkpaonebemeofjnfonaomoefpanglbogdhmicbhmoadcndmjmaphjbjmgpnpmadckdihcfdiifaldodhehfipgkbdgk", // 24
			"ghiehajfjhmbdnnokmpnamcnlcbgapnpomckngcpjckcgljknjijccphdigenekmboomoioeknolhamgafekgooaolfgdhlaidpolkgmahkijnimgnfcbchmmagdkpim", // 25
			"ehnodegjajipgfcbpbdfnfenhmfmljacfpkkecfddbhjiefpfagfoglebaillpojlglepiijbeacobigllcpbmbkopcmeebdfaolpakhnnjanpibpgkklgfklmiglcjm", // 26
			"ibnhobdnddfokmjmdcgaabnmnohapjnnkidibnpggabbdhdfkfkpljffanhdkmgmacihehmibabepfoollpeplakklgclahaeaalmpkolfkjjecjafnbdheplegphnni", // 27
			"ofppimkejlamlmokabbopohbfjbplekkadgbggkicnfhdidjcejlhoghnfipijigmdhnpmpoedpaagaakeofncchagolncfgdlgmdaekgdhnlhaobiigpoeidlnijige", // 28
			"npabbkognlohkegbconbpimhkddiopocaclbghhongmllldahnlpaebhdcbhhadhdlejnlclkeibjdbpohkamemafglfkbhifboedcpngnppkhaooljflhckicjhfgam", // 29
			"eijgniiogcklpjnjmkagbebpphffkolbcggofnnkjcgpkmlhhjiopgkbhkffmabpjofefpkpifpmlfcdjeedoenaghmlembnlimjoggjmlhjjilpbhifofhbifknfilm", // 30
			"hdpbbneagangaoaeipodimmodcbcfniioiobldhpcpledneidiemlboeaohmpacfolpdfobnpoiaiifdkkhjkjaocjojgaggdphfibgkimnhllphaomalgdilpnikabi", // 31
			"ikcfkkhlcicgocpbddjgkkhgfomhdpckjahlcagbicegcakoblljldehmamckbkigllpmljppimgoepohljnajcompgkamjhcdkeijcocoodonacgjamofnmjooeppoa", // 32
			"icaokldpieimaemiplphfhiedbiglpcaagkngkihjkbgldaclcfoighmdpnndifcbpmbglcalonoohfaeelnnmggmcmecidbdddajffjmgmdicdohncmljkiekallmeg", // 33
			"kafjondbnhfablfphdgnckefbbihcladlnhmpoopenaoepjfpkfhcnfgoigildaijdfnhgflljldocdfngmmdmjghpbelaipgmlminjfcflkclmcenhajfkhbkbkbkik", // 34
			"hdhihilebmiejcpphklgdpmjlogepmbfaoeliagkpndiebkopnkeekfkiifanmfbnaelkpiajjkpfnklalbadimcfipilpeeenbidlmoedeeiphknkheecjpiangighc", // 35
			"ecllnananbjmaalamncbpikeebaodbfgpngfobelcidikdbancihbhleniopihilgeejlofiehhjnohiajcdkcndmbchiockfaminibaenngjblcbadhdddelcipfeof", // 36
			"nkgjckpeolnaplmfkgilcdnpgdkaibomoodehfcadhdojfkmmdemcjknpbopinndnpfkmbljecdlidcmgndjonncpgmkbcipmdklojlaedckifceokecadmjbakomnnj", // 37
			"jcifmfefipalmbjmfmjhlekciecdolabifnmjogdebjoajhhfeppjbanhghbmhpdjdngpdgdgeffmfnbmpgfbplhahkbhehhpmhcgalimhjjlilojkblfgdpdgkcpibp", // 38
			"omikgfhdfilmaiefhelhkdkodjageheddkbidoagfjgkjbgbcokalengoocnocemflfhmbdedhfileamdhnmfmmoandoohbjcojjdplakehblmeokpcboihailgkkkkg", // 39
			"bkldpijcffkghilefoickpjfagmkpjmjcbajaekfoffeajfclefbamfnbhjnahemgmcfhcecneepahpchdaiponpaghmaeimjlckednmnojaigbapapgdepncoodoeln", // 40
			"cmkcbjabafjcihgjpcnhphnlkholejipflpljbdfcalppkhcncjdidolnoghkkbpnmmcilokjgoneedmoghaaddpkmpbfljgnhohbibpmnhmjpfgoklkodbidoomkjem", // 41
			"dcccijdchppmdgfhelnajjcgdbghdlfkboedeoccbgbpapolopapfmejmlemdnmilegmpmhgfofnoepigjjppbaeckbmjnmohionlijcoikihjilcajndkhlogdiabci", // 42
			"ccbjkoncagmknhplbgcdfgmneoeiekkojphcildmbhdckklfmecnpchebijjognlimolddefeoendbhokolojfckjgebjbjjajoaoigjkhajhkmdadmnhbnngpnkengd", // 43
			"cglfmckahikbjolkbggacmanhpbaeohdllkbnbinmkoodbioalfaedbjpbjnnnpfkgcjhlaaomnpedolahlhdmgnabdnlbilbelkebdgcnoencjlckamfabodbnnegkm", // 44
			"ejjdmjiebjondoohbdicmdhnkgendefghbaopbcnnojhcoeifdigfoncnfoifcpljohcpdmglpjpoclnngogbhldjpjaohgjkfgbkgoijfkhpmollogcggcgbnbddcce", // 45
			"collabmbgdfefiambkfpegamnpheecmnliakfjdiihdjngodlbaejkjapenkhjiaefhpdnkbmnnaojccchgafbmhblfmgihekafpbgkcbcecojplmeogfpohjjbahnfn", // 46
			"fcklgepeoomkmknclejmlgfeamejknbaeebheeemlembejmokajnodjhjlmgffijklabhoggcfdafaehfhecjoaghbdnbdobgkdgmapakpdffihoiiomjchncppgdhoj", // 47
			"fdgandfablmmemhobpcbkdlkcmaioeecpmogbgfhjjgbnahlkkblolfnipcakfdenbdnhfdgpldelmfohogfjocbcngllpdmcihcfeommmcbpoldkfgndhphhjchjbic", // 48
			"gcefclffedgjcppdjepamoppjakhicpmaehfgllmakldclfdflbnnihpefbimackkmmmkjkiolclbaieikjmpogdmpmbpdoealkhdmjgndldlljengijbcjldmabgiil", // 49
			"dcdchpopgcpoedagbihbjgbbonjampjmfpnoofeiogoelkhjjnpmjcnihafbfenicjmkdaknfhahbmaofohikmjfijfeblikebfahjnbogpnammnifnagchhmlnjipki", // 50
			"ngkcpjlomkcnbabhjhlkgjdcoleemmpjicefgapaneefmjiglgkghchognmpmfngmgklpemmgnlcogpokcnkgnodoahpgaahephncojnakempocnmalknbeffmdmiihn", // 51
			"ojpbidfbnegbkgmpgcmcfakhfpnbibeaikkmnlgpdnppjgmhnpadfpejbojcdlghiglpolpkfikflbgpaioejdcghpjgmpnkoaggpnemdlbmnhidkfhkejpkpabmnnhc", // 52
			"foaigbddeappnimocidefldooafkdmenpdhodmbedofabimooggbdlminbieiplmijlehinbdonemolledifghjcfbmpbloeghflnjfhpjbgoaapombiodgkckkmclmj", // 53
			"eibocdkejnlelfimaigckhejoaocagebfajbbkpiphejfkajfbnnieagfjmpblffkcaejpnhjiibdihpfcbacfnbicopeiohcpieendkbdakahgmgibpapejckbnfakj", // 54
			"bmhkkbafaemlcfejhjnihoaopljnopdehjjamakjhdclflpanlhlafoffhedhbigboigmcbgmnmfdgogdblklnmocmeggncindmofifmkmdgccpagdgdonjmhippebem", // 55
			"mdobjfnolpggjgahgdokoddgfkhpegmgaeikbihcgcefpoalpoaihajgolbpcknnnlodfhdgoenbgkdclnblapfenmalajeeehakppebclkmdldmnmfomdegkmbicnlb", // 56
			"eefibpomdfkfemfjffkgcjpnenkbfhmboealplfjngfcdngpfkhjipjhnhmfohbcbbnogokmppcnfnhmealgffaiggfbanlnaeigiefgfkdbhencjhkngfoamedfchpn", // 57
			"aioomimpfcemjjnaejjbbgonpnaeifihdnnghcghkjldpknmkmleoajamhlocpdejhamgliaimpjegkjjkglecgjdgjpdncmmkmheejlmihkeobaifemnfmhmhbkjgcj", // 58
			"ohpidphcphaoccfjekolienbpcofjegmpfdlfdpangemjamamjhokokclfpoeednjncfmaolpjkkgfagmaojjhkeedffgneincgknodmblhdjllljjmgagmlmaalaldg", // 59
			"cglcfonhjiminpognjppajgbhibgfdlledglgfhokhgilpgchbojnbmklkfgoojhambecfnogjngaeemdfkiepijhefibldoeglngngjjcacpammbkmfplhliinfbojp", // 60
			"mbjdinflanachcbbimaeokpeabjknlhinhboaijfionjdiplpibpofpmeclnafjamgipffdagolhoedljndgamjnljogfjdbgkjemlhkghfignkmfpekmnpmafeakdkb", // 61
			"jbgmdldfbfhjjlipammebljcpdlfodgeddmlikhigbcpeacbddfpedignlacemeokkfpcebnohelhbohjnfdnjhpeefpnpngeemfffeoeghhdpdecpoljhmfpdgeogpn", // 62
			"oogedbegdjjglhgjadnimocdpnnnmhlaanmfjioelcdhfiomamgjlfefmelofbleoplnlapiijapkjkgkgflpbekgdhcfilobgjcfgkdhfbflmfgepjcbkcljmcllecg", // 63
			"dbdchiebecgeejlioodlhddnbaheikdplfbinkdjfkahkidcbnnkcoedicpifcleomhlmikfjhljmkhcglaiehjhameclnofpocgkmeogdbbdhelkljnngnajeigpjaf", // 64
			"bgfkahgipjkghljodfahgjegkjgnidjbihjaobfoiajmmkkiepohemlklecocbokjhfpkmeckmddniajijhgkfabkhhemjdcpldlnhkaphhphdnmdgbppkhnkmeplchp", // 65
			"mjofenllmmhnmbagfcecpdmffoackcopaokehmciipdbfgebmdiddfmblkgnknaiobnackjnkejgnelkcabejdllpnaodidcbpeccpbckpdeheojlkfiikecbadfjbed", // 66
			"gmlkdclbkmpoegdnfbmdlhgpneiaoihfdagdedjieljeigpnnmemffaljkgghfadefnnpkaeichejpackmgoaigipcfffokmjbmkpnfgajddpabmhffbfogknbocjnji", // 67
			"pjhmnapipcikejcjhalbkibhkelnagkennohpkfdcgllbnkleapgpapllifppekmpheacamkokemhnhehmdbgabfeopeileckocpnmjlmgpfdigelmcndagcppaanlmj", // 68
			"glhgnabeckbjnffjmdlodfcceichkgdelhkeliihobeelibpgbodnleeijednbkdhmjpdodjmkleblmhjnmhpipfhkbpfhcgbfkcbollncnkmfofmojlcmofodpfgpmd", // 69
			"iglehdbeanddhcgalbdofakeljhmjlcgoloocalbjghajihhkdaogplejdicbjfegoihmliblipegmgfldmpoajglelfeaiflbnecelebjpdifecngdkbmamkjkhklff", // 70
			"jkmhfkomlajfblifglkbojmedijnbnoihbjkaicbbijmhnkhofijikbmapnpogdgnanoeemjlbfgcefcgnfmolmjjgjhbgidddpadeidbbimbdmddhenmlefgldnkdcf", // 71
			"nobmdnledohlcibgjhllohnkmffdclnfdlielgklaghnbknoocijnakaoknapjdkaocgkdpnppffabahkjdlldcebdomdogdfpmkfambioajefeogfklgneoeamkkomf", // 72
			"dedepmmlpogpmbcfimcddkjodgdidgoppapbflmnfpfppomcpffijonfnolcnfhehdmigiamcammgfgemdnpcfhgmbclioehfmmajfplooeidonbfebdhagbcjibigoa", // 73
			"bnhjhdpiolcdjbebdjllindbhjgbcagdapbdpldbabdhjncgcficookgmgmcnbhcdjlpjhmecplnpdpdaaiiagdmimejhghceammphljifofdgapdopeahgaejjeaclb", // 74
			"blilhjjjibcdplnomhlnlcfpchoohoagflhfeecdifkbodanjoepgmhebpcnldohicigjlfjpndcmlplliifmhegfgahgpiaabchjddiimjgcojpdadjjfjmnalnilmg", // 75
			"fjognklekckjcdliofkbebkdfefhilmhljlbcacjlfdbkkckjganagfidabkokmpjpkkmaoahpdgdlnomccbnedjhmnkdckgbdlfjdngljiadmpokicemifnlglodnmn", // 76
			"afbokkgbldckneembngonnlccfbohgeckbpegighfjelhdnokbfalkfjcjedpkbnfdmpaiiemddemnkehghgommplggllijkpkaeokjccceclpfjdgpbdfoojlpmogpf", // 77
			"momfhodkdcdnmddikbgpfjeajceihbhkgomdoijeiaemnheoejoafohdabcancomcnplbjefeaigacgancpeobabancjjaimbdgfikhfnkhpgeoklcokghaenmeiigme", // 78
			"jgdbmmficfjeiiplnaamoahefdaclpggjoibhegjeaoamgmdlnbijfkgbanmklcagfaihbjncamddobljnlmdcflnmjkglajopdlgikfihcacikbljiaclfgbbgmfnic", // 79
			"jiklinlpflnnokpblgaohnmaefdeodigcophhminmaebbpebgieghbeongaaciddoionmemfehmdgnpdddpcnmocokfkidodoafmdbiigncpmgmnmdpbejgclbbapkhp", // 80
			"klgdphbhjdddcdgflijmangiohdpeeohhiochghafnejfkigmpfbaojjogfccblbficofadacnijdmmiokfdapnadkheiompgafojdjabiiillfibbdflcnlkbjkphfp", // 81
			"bfeoedifjcfoifdcifjbpcifdnlpaekneaohgbbegacgginladiegmppknecbmbhbbckobdlpppfgchihekndnmgannokbdmgkdofbbpfkgkoebacbleahobjddoible", // 82
			"fapjlpnphnhphgbndlloolhfmaefgkpkbjbjgebnkfikpffpljodpipkdhpoekjgfflcibjnkefhlnobmeajjbknplnjkbbjbckenbondclbijpngibpiemjflgibaec", // 83
			"fdpjemppfbmjgalldlggajeibinjncecjddhegefkalikaldhkkiciipfblocnhehhminandpmldfnfklnbmjhmkbblinhnohhepkjbcpcccfbokolpmmnfdnkpgolgo", // 84
			"afkoabpeoiodenkailklogjfepbepmhglfnbddgjoeeifcaighmjdmeafhaghiaikomaehahodimghimioipmabfgfpapglmdedaecmbniacgoppdeaheckhlojjidnd", // 85
			"cpjbcfedffbekbdmgmianefedcahmdakedeelfdlnmooheofbmlgjdnnnnioaaopememhhhliljfgemflhabpmcbmkfekbphfccchcklcmibnemnpoejmjdhoekcajlj", // 86
			"dbbeidekakechmlaeajeeooagfcpdknlicodpobaklnponemjamjhdfomedcnackndahdbidejpjfjmojibcbdcmalccijpgacfkkpkhnagkbkcegkkpkomblkjgidko", // 87
			"pijhncmgcpenbhcoaejjgiganmcnchmjibcaoloiicoegfccpclljendndihmkcafiggnffflnfahfbacnahemdcolokiaigidmkefomabgloekdkahmgpcojhngjpci", // 88
			"oinnjbkhkcpgcccbfdkddojjhnndjkjkfpakimdojejmagogipceknieldjiaglifhnckfpkfbhkpdibajmffgafefcpdfgdbmplgdkciigncneljbdbjefjcpplpbpf", // 89
			"giifbbpbmggkdhcbjllmfdmefkofdjmbpbdpjfhhphmcgkdadjokgeeonacbnljbdblafdbokobbchflbodggekfbjccbefiengalhbjolbikfdiadpgifgnhiilechp", // 90
			"foddlhjealfoanhdafepfoapngmgklamppcghpdnjbkmcdheokfhbddfmmfeonflpjpjjbfhcfondnodieolbojeiakkdjkhhokoehdfenlboimmogdcimcigfllmmmb", // 91
			"kpdgocmdkpekdknepkjdapahokpnhjojihiallbljihognippcbeckndjjegbgijnekpdnppgdemaolhednlodjekkofkogjdcnidflgmhflolfompagedebpeekklbm", // 92
			"fifonijoiphkkdoaejdbfffpjccbfkmfgakamconaoaidgdiadgnaigpdbknddgncdndaafmfkghfgidmmmccodkmianlhglbjagodjhmfchmmejjeiecdlphmlblibn", // 93
			"bamedkcanfeaegnlchiadnnhnmaibhgbkjlgpdlikkeilhlmdakeiomcjeomelgjknnpidolgdglimkhlbkhifimgjcbbfidhplgccdbdjlaafgnnolefggbompjapdb", // 94
			"dfniggieggpiigcoibdlmgdhjobhfmbndllepnkccfadkamplmikkjedbgmjhjapandfdfocbagbjgkmcakhmaeddmjjpnbdcfnbikgjipdibnmbjbeblgcbngchfgei", // 95
			"cjfmihgjfjhfhombeihdchlmgbicchgopmhhelmibcpolbbhdenbjlebhlepdobipgnfnmhgchbplkbcmfdknfgbbdkeeodddhfefdfmbekabdiohmlocdfibmljpnjo", // 96
			"eikeppjeggnflandoabncmalfhlobllkjkfhakomgnnhnkdohcaeoledpgnabeidmjlhmjeiknffcfmfcenikhcnikjjgpadbnmkcdoblonpjkoappifkiodhcengnmf", // 97
			"gmhanhfcclbelekkbhimefomciafafpfienkfondmhmdbbedfdklcoflippenfakleibcjogmlmcgbcboldjpkmhckocoaeedjojodioobpekjfphdggahhjnmjegebj", // 98
			"ognakncodhiecldikhedjbgghagbjjfjcmicemaoghgfdjjkdfclokbpdailfgmekedbfakodppccglaiioghmnbbnoaneihnhijoehpbgmohfhpmlhfecamjiblpack", // 99
			"bgfaebgpbekjfikbhepapfgcdeobgbiiicckfmbcgcpbgbipekmdoelajpdebmpleihogogoconiobfdppfkkloflebjicnilhkoclalnjbnplolgbggodpigipmfdml", // 100
			"jdppnhcnapegojcfopbedmjdljphplpigcacoimjknnbalegbiakbmihgjeimbocophpebgoahogfedgjniknkeojojbmgpjaibbbhkpeofpaopahoeddihcgobamoic", // 101
			"jggcloahbfhmilfapgnelaokgmhmkfdnbdieohcjimpkjmmkadghenmlgpekodhceogncjpcoojhnnpplaohcdajeaphiaobdjajeacdbdehelpkmhjnflglfiibeihi", // 102
			"dlkkdjinbggpdnhpbgbndifdjoamcnkpebhgiffjpmpajkkcejcfpemgflkglbagjadlgaannbigbehbmjggmoapiliepodkdgfnfdchmcalaidbdkkldeembfbelljn", // 103
			"dipgggmpkamjnlobekkefniiljkkldfbgdebgpopagaadggefhchbpdmbidhhpldanbjcjhjopbdlapojooknmoghepecophdmbplfapdandcanfchkkbdekpmmdiefd", // 104
			"iffhmgncjhcmnphocojnjiljokbobajajhgboggifgjafmjcjgepmlllkobpnffapaacickkkehlpcdoahibdgeiadajjllnpdajbfopgnfehogcndnchjbhfoepodel", // 105
			"hcjjbgcoghfebfefenkfhilpkplfchppkdgpldpianbhlpoimfogjekakoamclbcmojbebfklhinmfodkmhkfdbppnbipnfcanfmblfiipeaopingnlngegeoekhegii", // 106
			"lkbnlockihnkhboidjcaiffamkbkhmoooppegjfbbpebgbcajjhgnbekmoloobjpgomcpicanmbeodkepgflehooejajocbmgggloeeclddgkfkphialehhgdnpmaccb", // 107
			"geepajjebklbebblneiiejjkpjnpadklphpehdbghacgonjbgeppmjaggpdhakcgmpdafhmolokpoagbmahlohnchdaopojpgogginiejjfoghpjlllnjbnmpbnocnpa", // 108
			"cblddiohfmdfpmfacmdagbkilblkjidlgmpmeemeiobpgknlhhiddncfkedniibkamhdcnhnenhhfanokfncannffnecdabgnjoojklangdaelkahdacgjnmlanihahh", // 109
			"pjkofhajkkmigfiaomeggbjhnnopljaaejnppdmaninmgfkgaijacjlnogeefnjjnliifjjelioadcoooejmmcjakbcfbcjmnnponmichmkegefjkcmankpmdpnnbllj", // 110
			"felalnmcebkohpekojjaehjaeldnmlmmiogmcopjmcoggogmmjdhkjkokacbgjabeeahghfggnjdcicelealgnbmappopcldpfnjjhjpikmignlfkgccaecggojpiddi", // 111
			"oafgooelnnpningoopaaedealhhckbaohgalnefnnjlmdlcgmccbhkalplijcfgkbbejdmjknikmflmdhofloicplflbnpacdoebcjnfmdmkgkkciimiebioeheelmpn", // 112
			"lgoidbnlfpgadckeabemkjjjhdeeadhmhcgmpnealpkdneakadfanppoiafhlmgcimadknebpicjipnhpeopnmoebdlibgkjcacipbfmefnibpjnadnlodldeicclggc", // 113
			"efccbjjbmlgknpkjcfemahhjilaknjpkdmddgenebeoemkkmhegnnbadpmfaenidjnmajhbmdhgjidojnknlnhfmnopbbahfgagllmcgnnkmjhbbclkmkecofehdgoba", // 114
			"goeddgjlejfddlaampcefbegpocgcpafodngamoaolhiljkiblhmebiilfcoibfbmecahlblnbnkokimmlkbcfcnocfjhnklikieenoamlmdmfmioojecidkmejglfon", // 115
			"apcihbphfjnhbopkfmebhoioofojjaeekijohchaikpipekdpnlgpbeajojdmafpamicojaefgnfkoeddgihbihkdjjiecenaobafikjkkllkconnmmnaainlgkmnijh", // 116
			"mgdfkfbaempgflpahhffgkpcknolgbnjndmelgpemlajoacmoagndciobhnemajfdfijdfchbbllgiianenhgnacaflddlclbhnaplledcejljdkdldoegbkomogpfod", // 117
			"kfgjhcndefgbflchpcohdmbnajphklhkmbgpbnpkcojemcopfimlimnemggoopmbnbdppifjickmkaehloiaiabmlbnfdmfodenjjmgpobicbdjhgccbcmgggmippmbl", // 118
			"ccimelinkjbmgimnkebcimpgghgdaonkncpnohidnlickalaojjfmglkdlpgndjckpafjhjcmdnldddiohchagbpfjehekdnpanlmnejbhfjmnngckjeaikjeipppicm", // 119
			"oblljklgbbcabfpimbogdbdkdhifgdjfdbgihecoljpeholgdldbcnbbceboihmehkhliggpneabpigcafacigkldhlhkgnoddcmkodpombdiicgehbdkficedhokkok", // 120
			"nkijphomccfmndlmkkkhdjijkmcboigolckphnheiafemfmgjmdeohobcppocbmefjckekcbkbimkamoknkjibiilpijcamfnmcadjodaipnpfecgfkncjpcpbemncgc", // 121
			"jmlmmembjoebhbpmmjgbkbnbgccnbkabfepcdjdjdbdjnbbafgnbfnmpgnnpnbagkhdlhlihclgkefepfgloinadhejcgapenlkkgaaojnnphmbanmohomajmbnjebbd", // 122
			"kgdnipenicljomodomolmgpdjmidmelcaailplkhhmfmdpfadpblamkhiheldgpmkphkgihpcplmelnjnmkhbepekkpfencimplhlfdlfenffeiodkjpcedibgahankf", // 123
			"nldpmombfjamjlkkbokmlbohhijfjplkfknkikjcgccahoobafidadgocnkhabaajaacmapdpfcjdooaohkjapdehedmolhhfcalkmmhomdoabadihcmdgmppdlbpnef", // 124
			"fmdkdliogjpjgbloiglfhkjniehkoioollfppnalfmjgdjophifhabgghhfnnajpkaakbillgombkkiadkmnbfeepmkicfklpmiooflfmkainkddhfkoigglebdgkedb", // 125
			"gmfkdppephlijabaeajjleencjjcbeljciphppbpknmmflmmmfjcbjhimdklokfeakoecdbllblncilhcgkcamcebnhglgndkjpefmfibiifnkkoplglcblkmkogbgjd", // 126
			"hcfajmeedfbpojfedfgaoelidhbhioaoedhinbbpdefdlpejfogakmldahmojihkiaoohgepanbjdlcclakfmgheigeaacbkdhaedjdkdeackoddemboofjejglglchp", // 127
			"jbgcpmdjpmijjknokonlemllkpfedoheakkkhaopppgpmddpgiimbkaoicbeggonbjfnbgkjlckdmmkiicagpbhggckapllljeckinodamdjllepdhnbikkhacldpech", // 128
			"njceehplbdohnmoocajgehipgjhigamfmljnljiijdcjdckdcfjngohnnfodbnkgoghcppfklljifdjdlnpjiaipcbbofoagckmpijfhgkcapheikjljgmkmephiplhk", // 129
			"hlhmbdinjijpbmhadfipiojfmaaleghmpbfjnplobjhkjllldhkbmolknooaiafhaeomlnhmjephgohkdjgbolebbhdlbdnnecfohopmeckckldjangmhonmeofoppcf", // 130
			"dagfkcalnlmlddimfpacgcblnpoomjjhbmiepolchpeilcocjfmgklgnocnlaaafpklhalpdlaipcekbaokediaikeijajonpollcjbfmldgjgcknefmfgccbcbcmbhd", // 131
			"gaboopkcflnmhagimejfopnpdepfegaiplhbchinjgonjgcongjibhaimifihecknckghpiffadaicmpfdjidpffjniiekglninfjfmahcldohpdlackkadlmcagmdcp", // 132
			"eggclnpgeiknmhcagnelbieppgmidagocnopmpeckfcgfgjfdcopfilnmimjlgehehoihldlngppgkkoafjocbhiomafbapdcoffplmnbloomgmonkjajennknmnackd", // 133
			"gjlpllnclclahjkakeajnihahpbmahmpimalfcjiihdbnimbconppjijlmkkijmbcbeihdcpeanihjmeiccjiealpkkaijogbgonhholjjcoinflmffoeicfpbkbhgee", // 134
			"feekhlkeiapidhapoeohekgjdegenjikchekmnbjnfahlppcdoahljbgfepgljfnnkdnjbmkemojielindlccfhcolpnbgmncbojgmdgnpjgdpfpjehjpcmddjdddold", // 135
			"naibclbckmffjajnjgepebhoioiakbhnkmmiiehjhojkkpckkgojbkhicclpldhegeidjnokofciabaenndochpaoannnpgoaonbaifbomfdmfenenkogjdfnejkdmcn", // 136
			"bcnanpnbknfpgichnihkaifojifahhfinejnfnajmmfjpdejikmldkdadopnjclbcdplecipecliplpjpipnhbjkmaldfagokmllncdjlocbeolnfemhoiplpmgmbnld", // 137
			"hooidcnpbgpiipekkdenfcmpfpckkdpdbafkemgmbbhgekcaemkjncmbeohfdmcboolgfkfbpobiikbhofkpmjbabebbkphhnliagpkjpndebbdfbkkepmkpbicajebe", // 138
			"iekjkmaemckjlagipgpglcbmgbhliafnnbffhobkopekfokjbdeonkaihgbmcelhalbdifmdflbcielfhkbmdlenhhplgjmmdmhbbeenlmmpeaeobickhannhbmiopkh", // 139
			"njkipfahbolinfgoicenhhcdiklahlomhoijmahahjidnjgmjbabfobjblckjpiafagieoijlobnmhojbbcfdegfomijdagdjhedgeeekiidjiobphcjcpalfephkgeb", // 140
			"akjkfpfoknfgjeojjkecloaokdobdfbokihpmifjhbecomngbhobiolmapmlaidgbhaemlbbcjkccejjhkjbbklgepgdapafpmpkodlgfgbnambhjneecnbbehjgiklh", // 141
			"ifdlpkbhlaglcbpdapnfpbhdmcccjlnnelepegfphekgncibdjcopeemdnpmjajmciiblmopddgefgpafdfmimpjfpabgaadejhngcjbmhljejhaohplhoilpfbfklep", // 142
			"jlnbicannidibcgdncbjbbkdgbhhbdinkifoljhoehbaoaeelgaaedmjkhocmcadnihkiefinjmbehcjmpnomclbnefcljcbafjadiaoalaedfgfdhbccfocccohllkl", // 143
			"pephggdgjnaddhpgokoidfpehpkfgnkfoaddpddjlcmiehdpbhadlobmailbhgnodocmmhgdlgjlilamoimpfcihabchaficpmloepjhabopkjoffpijbiifflpnbdbd", // 144
			"dbfggkleiiobblkaapeplbiakpnabcmdbkadlenndjgadhadnbkeihpefkpcbdjmcnjgdggkpcldgeidihhpbpcgakekfklhfnjongbkaokppolpkcddkbdpnmkjhgin", // 145
			"lemihdaplbfbmcimbdaiimipcoolalcamcaojfpcfpiipomflhaghnakkbinlfcfkkoohfgkmmcgalafmiooilbkbnekjilabhmacgcmldnmohpoidekgljaoidhbjif", // 146
			"ejnfhieogjkpnniflljdmdihblhmogidnmaonbhbimklhlmdgbheibfedimjfiepmfdflclcaingpldemfablcnamiefknbechmndlblnkchgabfigmmolaficggjjmc", // 147
			"mngiiafglmbhfcfmcaagfpfiijgnlpnmfepjfgbmkbjhbkdgdpeahhjkkhaabkafghfbfocjbfnebfdcmjlnbgobhckcgohnifdabbligeobkibkpmppmbjeclcmjiin", // 148
			"peldcifiijjcoihpbeidcnlbfjjhlhkfpijppfphdncgdlphcpgehcccfmgpoelpldcjkdcfpbeaaplimhahflopmkjnomomppilfnhcpkmoogdinlikjinkabjnglan", // 149
	};

	// ======== EXPECTED_SEAM_VERTICAL ========
	private static final String[] EXPECTED_SEAM_VERTICAL = new String[] {
			"pliinoknmepjnahchlpnpgjbbpdgfaipjhidgnenihcnidnfkfpilabmfmomnlaajomhokegkamfcnbkbbiglkhjhgdechapifbedhckbgfkcjjjhkepfjagngjdbibe", // 0
			"dhajfpefdkfghnogegcmmfapnjpedjjmiaobggefidgibadebpikdghppoaaloodhoggnbhklhhifhbmdghkobldjgndcogamcbkcidgfhigphkdlmlanllhkgpgbdfi", // 1
			"dakahfkbjaodeilhgccaegljdmjpkkhbhdkgfkanhmfpppmijhcdalhnbjfkamkbmdhbfaldgebegnflhojboplnocfdacekgphplgdjafiglihbhmfbaeclboefglca", // 2
			"fjklbeminlpkmigoclkmgmlhglfpeeplgblalfdgmacbpmmcopjhglmmgablaiefopphfhlkpmeplidkcfppnlcfdlobniabocfcofbhckbigjbmdgmjbaldnmcbbjke", // 3
			"pahlllipjogckfmihabpolhfdobhbnogefiebpihonabdcekelobkdaibcbgeaklpidpflanfdnkfcmecfgoblpflghjfblaoodfiimpbdpjobfmjnobhklhneaomkfn", // 4
			"nimndhfkcmlnfdlmjbcjcfalgjcpgdnojhbfhmfahbkbcjcdnnbpdnejfccaamkpjpgjjjdibddgjlgfanlphiahhmenoddciopgegcbhlldckelcmalaealmknndgod", // 5
			"pcmdgafpmgpmfgibgjnebggkiliiccjnlmngpklkggkdjhipnefjmnjadaihaokbidnhjogoljngppoemhniljempacdaleggljjmncpcolankkmlhfhhgodmhbfbnal", // 6
			"okgeiohampaeiebecebopolhegoaejijdmfaikihpalnjabcbjaijmdclmcpccdkaafhepmjbmlgajkhmiidclfmapemdobhgefphojdljggjppcfcpcllihhabgjakb", // 7
			"hnpjbafanhjljhjlcclhilcbppdgajiajhdhjcbnbidegajfmhepdkpokfkhpledcbdfjgbgnjhgmgjcihceicamcfnindelnahpmjokfgmefcbkillcaepmkidihcmd", // 8
			"khnbghhpginlcfdbbnhpoodebhnpgpjdbmdigdjikmlameclkfdgbnbemmjcimhobhabhkommcipdmoobbncpnkccomanjbimllkbjobpehmhiclbecigpmegdeecejh", // 9
			"cgingmdcgihlbbpjjadmkejcpnefbfpglnjncpdjmiljkgahlpfemjndblpjinnlpaomfmchiggpbncpogcjepepmokkhdiagmpbkkecfkgmkjcbkcnhloaconkpkgmh", // 10
			"mpdjnhghhchkiplokejlmmfljiiicpdjpdofpcmaakigimpbbicfbgobiomneicipllhdlfaolmfdgonnkcmelkpmcdocplkilgemgnoeicokdelgfibllfohehanakh", // 11
			"kghhahmlcicnpofengmepeaikanampobaocjeicplkhkegmnpbejbfojkcgacehhklahhckcbplcbmdpcfnfldidjamoihiaacooknoinlkamcdffdljocakpdfepjlo", // 12
			"gokpnnckommfphkmpchfnhnlehfljokeeiniinjpchidemhfhcdmoaeedikmcgeogaaghmioeipfffmmnodahmjjcklbdgbjlnhldajkpjfffnpglmplljkjcambieok", // 13
			"fheepanfmfcecnggfkpkjhhcniphcdjfgmbfejpdembdomjggiofjllodmhpfcaihmcljmpbghgdccgmjbinepekjfkhnokkbaninphjnndjdefcjkeligdamnkhigal", // 14
			"gfnkaoobnnoghcggehccfdffjfifgodibdfcjkmdoaepohannfjgikakjggjnpeldjihcjaifejeffjjjgipbnamogeklmcaohgicikgnhfbgfdnppilaniefbajchgo", // 15
			"igpmhcjfjmphaipfiijcghfeeoogjckofniipinjicfkgdafdmgpmpcdikijjdenfbkfidniigkgjfmjdaimolmoiglpmfolkfffjohnmohdbodobhcaolijacomjfio", // 16
			"deponfdfdociapaplbdbkdjhdcflfiemdedfbdpocbcehcocpeclijnkodlmhdjhcgoipjkjbmkjpkhkjglikoadhceidfmnkgapmkgplilnabijplnpeflbgaoklndp", // 17
			"jhhkikbkjbnfmedognknepjcnkjmhjdhljpnlmloddbenjbhhmnfonmofagbplhdlpmaeaahbfokkcpbpmlilgdnegbnpabodpegnggeimelbdololmdjjonnnaeekfb", // 18
			"jcmmkcefmkhinmhlljcmkajlhfchegagbnjimppnmgfafidckiandbgbajbnbgenecbjaibjlpmlogncoaamafebbgippiendehgokfbmbnmjmhkhhfncfpjakglcpbo", // 19
			"amapoaekjjbiinfpjbbkdejilalppjeoljekfcjenanoibnkkjfmeidfjpkiicbnghndkcafajgfddpobklncegglgoajnbjonfinmfecaolkcjplljhaonnjedefabg", // 20
			"dickagmjicbcblelddejbmpkfegkhinebidhndocbmadhdpdnhfnfdbmofhocapkgpkpbbmhfkpdeknbjohbdddofehgokfamdpphbpiobomaeocejomkmdiihompmee", // 21
			"ahkaknjoolbpdlefamkmdgmeoibeoiafnoailjcpahdimdemmkffecbnkkojmonkcfoibclgejgnnldjfklmkbajanpffhgbdjfaagcmlbfmapaldkkekjhieacoakkl", // 22
			"ofkhepanpcneaenihcdmnjfomiajoacejflbnjiihkglohekdbbkjddoknikmbphgfbigingfjinaipbhccnghmpdlgfjbabnbiamajcalmdfeanfknogdedhjnedppm", // 23
			"hanpaeblfineadmfbbiflfgfeghccjdangkedoelddjklkdfhehbefinjdkhjbgmpcmpcnjjjjpiahihalnpmbmdcabekhflllmdakobjhjmngopakomlcmaemehijbc", // 24
			"gdjahfcolndlphomfgmpncpipbbcgefialhgmhammlbfjbjljjegjmgadigcgckbiglnimkdkclmeagndiddpfihkjcmjpebdmnmfkgiikkbnpfolbddlennofcfnobo", // 25
			"mmchddknocokhjmcjhoieebboaagoapnmhifhfjbnaeblagggliknfcbdkpjpiifbcpadikoonlcfomailnpoflnompmaohobkjldbbofgamoppbnnjfflmaiealjjlm", // 26
			"jfbocalokgeaaofkoaikjmioiileeleemekmibfgafppjdbpddodnkkgpgmibilbienmcdjmkbcahhofgfgnoplghjbfodpdopgigjephhijabgbmbacchojmbbnenhi", // 27
			"jjlahhjeikjgiebllehjpjbdpdbemopmeenofllgihbkganfmnkkjkpgmfjjhickdgolipopcabndmhbaomejebmghfadpocebgjjkljmomgfjpllcmbhiocolgmgefk", // 28
			"fkahdehfjfeghjncnllhflafnjmdfidblkljflojoeiidfmcpnaekdgamiaebjcbeilafenopcbblffjcnblclfkkafpinhlhipekinmcogbdkjackpiljmmnbclfpej", // 29
			"amoheobogcjmehceddalenkmifmjodnjbkkmkjjngocnogcpcppgakoedceldmcakcnbfgfohmlepfjjonhaahbldfooinepjcnhdgobgeoobgphkpmdaceeeloigobn", // 30
			"fepokcmpmcjldjphkjpmmphhnhjjagkohempfeomhdlooidhhmpalhjhlojmpmkhiakleoopelchokoahmcdbddcgmfnjefogipignhklciaeijmiohhacjicmllpncc", // 31
			"chfofcepfinpmnmbpijhakmgjknjhnogdpmnkjmppddinmofogeonkikahpfmcndolapfmlofakmipbfbpkghojddikiagaankkphjcpnhbcpjhfhoaabadomnifalhd", // 32
			"lmeegabmjkcheaciklmcklnplmihcaigpapophedikgainfbhfhakpddhccclkkoaphbaaphpeaeiomhdmhkmcnmkdnbcfelnbedkjoicappbihnjcidicelfjbngemb", // 33
			"fnfdiijnadckbphkbpdnopdemnnobeiblnnnfjbndmgfdkeipjikklkaogidcmllombnfiimgicheplcfmiafebbfnhklbdcdmeaalapneigmfblblcljgpignoageic", // 34
			"abninbncfhfmjeddlbakhonhjcndcmkbfcnghhnconhonfohnohmcdeobgomdndbfhaegijkdokhmldjaibkeoccngfiebmjabppeeagfghdndfppdbhbokaobcnaink", // 35
			"ikmkibmokldelfpacigmdapkopofppdhalophmjepgkhhonjeakbcoomleolhegmklicdnhciefgjiacejcenoilkmibbknhjjdbieojmekkgcehgmnbflbloaieijjb", // 36
			"jbnbnkflpjpacijlojnbbajhfoecimpbhecjdekbcjiaemfnlpmhpoejlcimdphnccpiodgggcpjpefkacppcflhkjpdhfbejekhjijmklcnmjkcbohenhhcpamhbamn", // 37
			"dgppbnamjjbchalpiklmcjgnbmppfhhofkijlkkiipkolgklfkbeghjpmmocdocndeplgbpjjpbekinkalmcaonobfdkmfjaioldodlehmdkggahipgedahjibdplakd", // 38
			"eflgmnbhmnjckdaobmhndmfbgfpcgmpojkadgcoagdddmnccklkomjieijamcfecpipkngjjlogkbfcellfinidkjlclkbmnhbielpgeflhmmafdhofacjeinanggami", // 39
			"nnheiebnlemhhifccfpcjkbpnkcflehfppfncpfnjgfcmmkgmeimimojkfflmmhlgmhbodeelhlllihaiohldldiananpgodlbooilfegllihenffgjleohoifcaldka", // 40
			"gfjhloidghhakpkebecmlhikkhpggjcgblkeebaanheanpagpejpffiafapaldapfohcdglcnjjokofkcfgplmpmdnipndgkaadpegcojbchllommbbknnacfdjmkjoe", // 41
			"cbnkgjalbplpmhbmpphjfnclikfflmicffdenkmffkkaabokojllnpnlbbfidfoofaekfjhabdgnnppnkolhgegnfngkpbonlhpdipgbanokebibfcphnobiefgfkkkk", // 42
			"ppkabobhhhahnofanfbnlppaiiggcmpmdbpdeoldjlhbpjofpbhhfefcfdnnbjeboglnfcbeelpnejapnfbindnmfpndhbinnmbkjeigklddhkmliifanlmojjpmidem", // 43
			"fpphjbnfnofmmmjokkgehidmcedklcaghmicihmmaaeilhlkehefoeipnnfdjldjffhbhainibmnaclnmginedacikobjgglcdhkakgdllcemaiemidjndflbclnjfig", // 44
			"iacgeofipcpmbgepchpbkceeldifhlmccmicebadbnafkheebpgabggoijhefmfednilhhopjmhcnilnljodkjagdggjipbjkohbiimdmapgpohhoidjinnahfcnlkim", // 45
			"gpgfkcfdgmnbnledmemiimghjjehfgmmeaoaemihkgmjjbmnlnpigdfapeopljmahmblpeelhlboihamajpcipfkkbjadmfackdbghkmhceehikbcpffpnbannkdhoik", // 46
			"pijihoojhgdlfaoidpddacfdlbnkalopjnegmpnhdggppfhgmmpibdmifajmpgnlopmaepefnelaelfcaojlnpjaeiclbknbcbedfoimfmkckhpaadeakmehbcicdham", // 47
			"goandbdileicicibhmmhpcmcmgncamfjogpgioodnieadbnmkihapaalajhphaedkmbofnjpoogojjfaliaimiiaomegnmpemnbepmocjpdeccbgoiipfffmomlnecpa", // 48
			"mpgohkkpnjebpilfcmeljicnhcokpandlghkekofempijbbhkdaddngdcafddjdkdgdlckaffjplikgbkjjlcnjpfefmpbjfpnnhdabakpgejpgfofljfamikfinggnc", // 49
			"mlohbapejgnehkfhgbjiajfjlgcjhlhgfihmkaddkoapolobiingjenknmknnghmhaciififgkbmgjcchoaabjnjefmclphcdclfbkeghjmogmfcmiikcknghckmpnhp", // 50
			"jhjaapinlfbpklfiebplenpfhcbfdlnogdnpoikepkehngafccdadghibmiennlaadljbnhcpcpggddiecakhdbcadhncoknlodlmcghfdeaaogkkalbalegachddajl", // 51
			"gppapimmbdbhcfcjcokkgiihgionfobnkpefgalaapnckgcnlaefdnejjobjnpceaaknlnlgfooiffpghgglppckgjfhodcndmgjfickdimlgimblagcigiamenooaeh", // 52
			"bhifejojppcebigabgcekabcdcnooekkacpabhlbffpfnmaekfnfpfeanomapnlmgjcabbcnggiidpajkaedjdobfflebmnmebpjbfbbcekkpecigmookobpidjkdeho", // 53
			"mplfbjaljkhlcphcmemoljcildnohbpjjkdjeolkebdalndgdkaehklclgbnflkkoebnmgenmpmdikhkalipnjolbdpfdkgimkjkiohbconedabkogfbmeggglgfjpfk", // 54
			"bnidlibgmhjofnffdpjkipmooaggkfdbjojgdefplimhmpcchkkfhlloiocealaiejmmolfepabepheimlcmniehbhfmflojnphdnpipkkemgcbkmnipanbkgblielig", // 55
			"nfpinbmnajifcpmaacadlbkfidmlkkohcnndpkfnbnlccjfaeapbaeihndpagfimeclebaiedlmbpjmfddkcoeibmoidjbfmaljnpobhbgniedjhhfolcfnigjlbnaii", // 56
			"bjmeejlockbdlmlaogekaopliklllajhpikdaibhbbaajafhdoimbdbfbjbkgjgkbegkcknddokmchneempemninkhnlckfaaepgllhpcbkjhoopcekakfdblhgeddnn", // 57
			"ibbccijgnihjokeonfhldkodddcfkhngpnkapganmeimjfbfmfhpmlclgegleofegdigoopgnhoehonbilokbkggdlahpmghhnhmojdfmglgobifgojfgekcbamenihg", // 58
			"ppikaglljdpjakgmnbhlimaebcdhoakebbaadbhdjcelkkekheacgppllocncjnacghgamkhiiglkjhckanjlflpfpfbloedakklbhhcloedacpbhodhlilbllclfmlp", // 59
			"kmeiejdfahkmphaecaobmibbokihndlcdpgmdbkaonkfnlohpeliadmamkahfheiaiddbbdgokahdlmfoplmnbfhmiaficiaecnpejkepigoefddpdkeafcfhhadkika", // 60
			"joehacfnoinkenjdmodmlkkmklocgamgfkelnnbnmpbcpifblgeckinhkkmcpeaendcpobfpjnphcadkpiffgjjkicdnacmdddeadnlbadfhfaicmobiigdlddolhelp", // 61
			"hfgebliaafeehapbfalgbmndhhlhhihajdjmoknplpaccpaoolffbogbpmllolfneadkmpihajlkpmjignnmcdabgjefagcbghmibclebgogifhgmkjheephfjfgmjka", // 62
			"nmnmkjphpdjjnijhamojnmjiaibnoncjeplbmjkjmfgbhiglehelblghfkgplkgiopanjdhjodonmocacjgbeaeijbijdggbfnfcoiiaapbddiddhghpeejlflmhmmob", // 63
			"pighccplojcklocdplmdaaegkmaahkfcjdnigbhoobdjcdlnkehhophfboddncjjcnmjhdbnpgobfobkkooanmekdbfegdfhfahabppipdfabamainbgnmapfmciiigf", // 64
			"iolcljdpgkcghjolipebjhnpcfcjahblmlehgjchlnpdnajapbgldlbgdhnjaiebfajoobmehbdhomldjoleejhmkljoaeckbfahfgcbjnelhhbgbfdlbgpldpjkdoag", // 65
			"gkfmdpohbmcdbjcnlimpmjlelhioichondlbdblogikdpjbabhjkpoojjjgopnlpdkammahidhkcnhkaojgopbhkoblonghmcalfkimkdfpfjlnbbhkahglpnkijhidn", // 66
			"eacodmadkgocangfbcjanajppfnmkkacmhekofooglogjoiidbkmpjegfbepdcibdmlbnciimmfhaaikbengfeflkjebefcomfflmlappbjdcggpieofgalcipcllnch", // 67
			"pdnoklihapigldmnajbfencafebnngdmfcbhjeioeneemankpnehpoghnaolifbnnjdnhbmhgjnkfggabnfcakofkpnpamiekfcmjbkdejpfagcpollbohbgfapclgbg", // 68
			"jidlladphihpeclkiogkonlojmgooiidgpmjgkihocalimjdkkambpdfcbigchfglaedbgncohfcnbjfjnmcdgbgcnpdmekojhdooejmeopfnmjkaogdafpelkiidjmp", // 69
			"bpdjnipddoechfejloblijfkafcbegicenmeghmcnhjbdhmbgbmhhpglpgebbkokflpfcahiphegmeaddgjhlncbgmbcbemmomjffbomafaihhhcobnanlflaladpbel", // 70
			"gbnkldbllofehpnamlcfbjfjajlnjedolaoaeeleaakpcgagjlbhllgjeogomdjfebinghipidpakkjhenegbcbgmlcijanlameeopbcagejlkijafcjahhdddbkdghp", // 71
			"fnabapdfiabfddmibahalifcjmbajaikidkffeomecgjhfpnnpfoflgmjaofhhnblbgneblkolochjffafcpaeopakacffnlkmgdginipmnihommhabheoedamlnjonl", // 72
			"nejhnfngakfldjnbjklgbiikfjicmhldfcpkaajkgfghegejjhehgagclholndjdekicghdgbeoiooilmbjlfnfdbhnelmcbfiimjibbkahkdajcmmjoehhmcdlokgpe", // 73
			"bbmokknghnelokpimdcimdllfgpjomacbhljgpgnmgngoboockbplmngjlmiljdcppbldmmbcceinmgcfgimflkmlnoamlgpcdnhphhlpbpnhnlohibkdjkeklimlabk", // 74
			"kkdnkdlcajehajggnpbnkccpgakjkjhdfmndapbdckmnephfldncklclanmpmfcpppajempochmnneodgajnjfoejhfbkmgpogngednnddcdkningcjgepbihdcgfbjp", // 75
			"cbnkpfophkchpbgkbfkjckichpdeaaoheoiaffpfdeanimikjeoihoioecipdemhoiigkeaghjelmmoihemoalbpojaoppafpfkmhjnbjbofjdfnahgbicagmbngonnl", // 76
			"aaefnkngobnboniofgibfmehddneankcnjpbpbjggpppiopjhjnafkoehglifodeolbfonpblhpkefbfncjldchcikfloknnoelhciihooippmjgamjfadoaggakhkfl", // 77
			"gkjhnommaoiikafdiofnmhipnebifibiihfjefdjcakhfakpkliiddccffkldlgdcmopaljmhcicgnhbefngamoehbhohaoadclcgomcemehafnbmniolfndflddeikp", // 78
			"dbpijlpkmncgjijkalcajfjaciojippapdgkgcbenbanddbeemlhkoalmkboancajgpfkenkhganofdnngbebnldhiahllbpblhglccaicmomoaaebjanempimflacod", // 79
			"aklgboifnemdpfljigbojmhfanlfaemindjjdgoiiioibmkcbnajajedlibpafopdlecmfcbcbiakbmdlkkbkanioeppbpppokdcpankhnbefoajbacdahliedhlmabp", // 80
			"mgblachaehagbpaiechjibabchfpjnlcfnfhldcideheibfbinednagpagaadfkbiidflbihigojlcbbdkgokcbedcmdagncagojkppalmpkdfkiahjeppcoedgfggci", // 81
			"gllmacfjoebndhiihbafakgeanapngoppmflnmhkedgibbinejmkhnmlhdhchakalhmlfdgmpcnlekolibfnlhjhpbfbbggoijdjpfhkfgoljcmakjdmaennckmklcbc", // 82
			"oehmliocnkilhfeimnabjilcfjeblkdgnjlboppbldbmdhodlconcifljpfbnjahkeoclhlcgganbkhfinbkmkeoklknpgohmdojjfccmbfglnimbabacmnfknenedbi", // 83
			"kjfdkngokaeebknfcaciidojlcjfampdhnoijpobbgghckndpkgnpfahcfdgecnaalllfglheooblaflaolmmhjlpijjpdfipbpikoamodokolmbplmjhbffmddkmdcj", // 84
			"lpmndalmbhmabdnlbhlffmlmioagkihakdaipinmeanfeahbmpmhgomflhnhlefdjnbenonbaldmhokmaklgglmcicmejalmndijombngpbbhhfcobljlfpldmcjjfmm", // 85
			"kijkjhiepnmnnoekmofjcdanfkmiohkeeafgknkgdgmjkkbkibehfkcpcdhbbhmdoinmhnkfhhgcacomjaknlfgjflijnangflihpchldfkfmcknhmakffeabhieioid", // 86
			"knechnbhgppnfnblocmdpkicpahgaefliiagiojdnjlefaagimikgncehkcbfjihkeiicfgnnbhafheakookdnchnocdfgdpboalkicnjkpiolnklcdhehejehkclpko", // 87
			"mphfelgkhgmikphdcienfoejeamjnbgakoacopeakefbcecalnkampejggaeabffncfgocljcphjjphidabacbnoegplclkegoioefgddplpfgigeicggjabnpbjhjdn", // 88
			"maodohikclecdlgejbfihjdkjibbpmefflhpabbfdalphnkjebcbebdffmjofoemgnncjmahgglejmaigjgekjbgmmipahmppkmeamfpmpekhccadndpdmiaknofekkn", // 89
			"pgclfplpjfihbgbfmnbfiiifpgapfilikngjeeoahalbghamcbacjdlpkmiionjjgbglojcipjjobahlkgbihocgipchleabhkgnmocnakhegdfhjpjahblheliofpgd", // 90
			"ljfichdnmkjibhmjiphpolchmdjkdecgodclckhmlefddighadgchnhdibpdnlegabcpfejndjgnaehkedclcfbcbbeibfmbopomfgkljfkflmnhncjoahfhanggbkjp", // 91
			"oeidfhpficdfbpbnpjkadhmmmnnmiedfihdaklmmfncdfegbbmgbgepalihiplilkjedhiahdpbbfahefejhlacfimelnfnceanjobimkfbacccaadclbelmalhfkeij", // 92
			"gkjkpkfcmkkplbkhdlificjlcmimagonokdbhhnmjdoonkkmcmdhedlbjabhhehmadhhopobdhgckmalnjobopehhbgjkkhgbbdefafofelalfoemhoiaocgnjlpohco", // 93
			"odamfieicenjnangnjedmldhfalkkgnigfihdeojnlhalkkggbiiikkalcnlpldeegedalcefepbcpldhpmkfnbleefaffnpodlommfhhlgopgjcfjeocgcgfmffmdnl", // 94
			"kafjhioecelodalkghccgmihmnnmjjjjkhfmhcjomjnkfgkjcnmnlfjjcgomfdcpckejcemhfjgcondldmmohmfnlbpoldmecofnpijcnnannbpeecibdgajehimmpgg", // 95
			"jlillkcbfofoeaokfecalmhnoglhocpfbjcnkldkddhfaegpifmmbkofkplnocjojdoepogmnkejdniinejcjnmhgcoafnmafdcligocmjbbiomnpcifnoohbiknehkp", // 96
			"ifcppddjgpealhcledpdmcobieedopphenkfnkbfgffoiklfgfjjjblbhbeebnheogjdampgnbmeplncpkokggehgaafbbkdienpolemnhcemikgkfekignfociliief", // 97
			"coandblmnlnlnknddmmmhedhlhfiadmggholmglokgibgkabphflmflkmhbdmabhlenfnmomkfbbfcfeheojpdbcmbnobkmjkecjndbebcpagabgchoghekeiapcnccl", // 98
			"edekjmhidhdmbiillhcfhdljognnipgnfaoanfloepelcihmfdcinlmgflnkieiokpifcekeemlidnmpmilbklamgncabbgkicfdjbkfmbijiniedgfmemhojanpmpjo", // 99
			"degebmjbfobbfdhbebhblgfipjomplcfgehmpkibddafkgmajbhkojngmpblneccmkjfokjfcjbbpkfoicpcmcndjpgdjgaichidjkddiiooibfnaacidpkebfnoddah", // 100
			"pehmiomeokjafgiaichengkilfgpmafhpckdnpaeibjjpphmiiolkpjolojbdgohkcdlgidglkmcgidcilnbahmnedagaffcpnkfkngnfdgikkfinppccejckgkeikaj", // 101
			"bnaklefaomhkacimgnbhbjnmnclcjfbkbgnclnindjajjhfolilfpgndahpmmjdeepfmiegpepfipinmkbamodeagnchadciebncjhkcipoohlcjkjeledhigfejpapn", // 102
			"dpcidheakbflilaejabmihlphlnbckpmpmodpdeoblmjgidopafnnbmpinhfecnpjjfhapnkhhahaelgjcholmiiafpajcoibkiimmfehkcbiapfcafohfkejblpmeoe", // 103
			"nibiokljegpcnhacnlddhhpgkdjppffmahikbnhogaenbmicajfabpdjadmfhlofcknlmpolbngmdonjefadonijnpfpdbfapmnpkihbjmdogekkieebcbkhocpdpbdc", // 104
			"fdpmgkeddfigjoecbnoeeihffbaikpadmneelmjmllmoldhpglgdkomocpahnehlnokeegkkdheafflbfcppobminiillchheaaenpmbcopjfchmmkgccmendbkcljhk", // 105
			"ppblfelbfembkjmdjonjkokedcoiibmpnkpcpolebmamejgomkopnnkooabjgolfnhbacbfnkcjhjcifmlogpljpmblncbmpoepbklkmlgdkjallmdmlhhaecdjnmkde", // 106
			"ikgknjgncghknomlcedamghmofbgjmiinjckcmojjalndbnfedfngmmogejaidajdidjbgahebllckmikaikcaaheaidibmckekgfcjmmkjdihepkjoaejkjpfipglkh", // 107
			"kebdcjaldpdgdpckgdgkedkjdabdljnmnmogaoejgnjfkippefgpflppcfipbiheodfeacgdidnimppgdkapimgbcbjioccbnnbmhijnokojecnmmbackdjakpffeono", // 108
			"kledieengmnofonccngjlgbmcmmdcjmiacdbkmkkpanjnmobgacgfbgifkfifjkjdnlocnbaidjpnkkmfgbaofinbfoodlkjanpnhgjcbgckdmilngncknpfejnfilpg", // 109
			"aipogdcgjbhpgbcmlnhacbieaniiimffahopnfcindnhfjcpfogfodjcbpfdnmeiihgdojihokkepadkmkkffgnljoedbolclnpldkjhiieihibfankcpgcnebbheipk", // 110
			"jjpdidjnmmhmebpbfffkngnhjakplecnfibmldghdijhejpifefneaihinifigneochnkndaempnhlljihnolhlokiiaiijlofmdeniedhfcbklikaalmgmkmccgdknk", // 111
			"lhofhkfglomofpmcbjbkdjeojcfimgnegpcjecbkjmnmccijnpekdmpgkbfjijecjgbmjhdimcnehfiihgmkhabgobpbecdjgdelbcjnpkmbjkbpbcpokkldepcdodgc", // 112
			"nlnehmfhfbeokbppjbppbaghooblpfakcnohlnmffdijkdfabmnbflmgdehmmpemflnndmhkigilbakambjlifflmgoiglgpajhicpnhedafhennecdokalopalgefod", // 113
			"hkblfphbimeonfkcbclpbfhmhppikhibonkbnmlkalpdhpcgahpbpaphefbolbledmhmecgknafadkkhjafahmobgmjdboinafffonpiclelgllfcaghfmjkeobkpobm", // 114
			"okipanmlfobliefkmdcfimpmkinaeaocblhojknmbofmckhpblmogapdolhgippniaklofmphegobolikkdafokcnalgmedmjgighbdpjkjnjepjenalefgleiljbckh", // 115
			"mmkeehclancnfdembhhjnkkijlapddhbanhielkgkkllnnfkhghlppkimjganndmcojpjlfeegmapkppnichdfmjidmmfgakifhihoecijphefbhmdncfjkhnjdinhij", // 116
			"pmcljjjimmehpfddomglphppjmdfdeilamcoceapenandogkibemjpnbjjbeligckfnfomekfinjaonbcghgomkniambmebclnchfidodmjdkdiidpjehamplchgolbo", // 117
			"kdgadnmionfchcdiefcklmgfcccjdcmepjankjhenooeloioehjadikhdkihejalgjaclohcokkmfmikgdgdehokbefibbheabpafdpomdgdmgigjaikbbblcgbaoecn", // 118
			"ieecelkigkdaccbchjfoanilkcfkfemknknalfooihnmikbkifcphlehpdedgdpklibkmgpommcnpbncmpamibelelfjcbombiglpkjbgbfnmhfencjnflkhdengogab", // 119
			"idgbeaikeakcnlnkkhfnhbnfbbpfciofmignkphbdfnjbcijadmmeigjanebgagdkelofdfbkkaidadcbbbfcbegcaaoekdiapebjcdcphkbfcggkaddmideainbcfdf", // 120
			"mdgodmjnfkiemhkgmmecmadddhhoicpjbfbdcbpifljkiodelejmcgofaejgcfoojnfgcocialhnpaommeoelkljhijmeoedbdfcimjbmjjienjpihpbbphaiedalpfl", // 121
			"okjkljfkphncnhnndjcpkbpdejnknhgmcllnidmbkecjcjiipkckmhokbklkhiigdeehldaannonmejlkodnjcchplbdagemaljnjggadiocdefmhfgmoegaebochind", // 122
			"dpchghaajjgmpjednhngokbklhelbgklbbeghgljbajgohjfngdihehbihodehkbbcebdioaoneommafjcobehnllgkfhdgiipifhgfjlhgjehkkdmmkpibkgamoijhg", // 123
			"nknmpkijmeefhdjekhfpbihonhciddkjkliphjpekpfhckdpejpnnhpabfadcogahibindamejnemodhedahpkjfakgolopopghinlddpkheackfffjodmpmdidmbodi", // 124
			"dcjpobffgfopdchkgginedonbdbeddamjgoggbhllaamdbpklkmjcbfbmfkabonjogmjijenddhaigeijpniinbbjihmlipghhfebnfebgilollcgpljcaamcjbhodmd", // 125
			"aobiejkjndplieaimhlfglkecnakkmhogadjglinpillmaggpdhhjncheooeaagnhhdjjchloofmfkghdhdlghebcgohnnheoaogdlhikjnglglfpkempcpeflnmkgcn", // 126
			"pbkhjhgkfmmkncpihblhidehiejicabfhbhiefpabaddggohcjbobnhhojkplijiipgfmleekemponlkeomofpefbmcaopihljddnancdkeadcmjenamgjbedcajoglj", // 127
			"cdokdhkflhbdjjmginagagopemhlhmcomnbppfhehbndcbllemagjhnmpagfodlponlophdpckmhbpjcokpdibidgbejpmgalbgimbijoldacpflnbaopcbkbmfbnckj", // 128
			"bfbglknbidggdbheeppiigbpfcgmjjiodkiioknedkmfhlpiicgbldeigoaphbgammejkidfiminiacaalobbdephplilmknhglliinbclheoicbgincdldobbehfjap", // 129
			"geiopobjlfbegdolekcfnjdnoafbdjjfldadbcbmnecongmihagmgdfbcaopbchkflaojobhembcjemmcedikomhimlnkgpdafkbjjdooohkoglkdeicfmjcdocmkhfj", // 130
			"pceclfdflkcknpgpkpadljpanlokdfiogbagmllicpokcagbmpccofbabocakdbbkpepbfckgajghnpbfhjdkbiomjfbacmfgnkmmigbgabgegooeboenpeidjcppclh", // 131
			"poojfohoncjangfongcnjbmfedhncckidafolebdmkbdlffalonhagjodlhhakokcmjabnccacoalccippjdhchhlnbidpjnddbopelhapfifncjdkapmofdamfcdidf", // 132
			"ppohmapogcohdcgjfapaeflalafeedgebkldonommbmooddiepnljjipgbfgdalgnkoibpodgbkaepjoghikepiikhchipbjgkpcloalafpiajjgmaaokjckpehlicdo", // 133
			"capjnhojaeobedkjofmniohifmbmbhjpldbdbhpakjdldaclcilikbaednealedafjpanppminccmgllmpgkcchkmmaiciilmfeleblebgigcglidlcglihaonjidech", // 134
			"cjfkknfpkpfmjpfkajpmbpimmnclldekkbhcffmghfcmfcaonbkgolloglogppjodafmefgbdabcpklpljacekiceehbhcfdojjcmnnnjbmmppjfnlabolfklbgeggof", // 135
			"bmhemimcmcnkodcpepcaljlpajpimgkjbijfkamngdappabmbikbmcikjooicfgmiclbgmjhkgjecnedofmnoojndnacioceiemgcabiiipdndfmojdopcgmlolflfcb", // 136
			"amopgdahdidkehhnemmbjnmgainpmjiihgfelepiedoodeijhcpficfkkmihlgdgomdhhhannkalpkkicidfggeaoilfpdeoenhlgiehciicpkhcinnoiogajdefjjle", // 137
			"diehcbckonfdfnlpmgibnknpjjkbmhcflponamoelggdmbofddcfmgbehlmfchhngoaljhnklbgmbpdbggadoeeclcmmagojlheedbgaejgnnpileihnefcbjhibcpck", // 138
			"gjbbfkojnnfeaefjijklpkipgelpddaogbifelglpoemijapidbjahomapjmpnmkijjhbldmejabnnobmoihohplenofflmjdopgddcompffofopginoloplkmbidebk", // 139
			"ohhkehndioichefkiinbknoacoemelnocnlbmeabmbdbghbigoalkihhnmhnmfdoomphgnpeoldlgkechdoekjdjckbejjkbpkbjbnnnnippkohhnpomfcllchpibbkb", // 140
			"bpjednjoncjbipdkcblpgbmbkcoaaklggefidbclbbbighlikdohabjjmgomoocmgnfhiihiinbondakkieeldbbacbabapkimbhfglmlmgdecfgdipijichdpdajpfm", // 141
			"keejohpapnpnpeaaijekbadadcocenaghfpfeidjlnjgihhccgmgdmjobafffanfninnaclbhmmcjnfndhellldpbpmdgifmkldcmjldmdplempmfmjmgaiibbcedinn", // 142
			"jmmimclppkkpdpmbgocemhifbembnfpkidafcfbdkgnjppjppnlllmbfkjadnhpfldpighpblnahldneadcjibkngoinnodbdkfgdnddedfpangkholdcbpnnencnepm", // 143
			"lcehdmonbieeccofmcnhmgnkencnpcohdpammppijeddggbbdpfacmmdpnjbcmcaopdjokcgmhbchbgbljnilafhgnjohdpjiefmalpibiakckdpmoclefijchkbhofd", // 144
			"bielahmiiogdfmnomfnifabjpgcobnfocaebpnngplofdianloeiihijhafkkehpohafboiebbpknelholempkeadfjflpfehngmekonejlfakmholglmljohpgpjnhe", // 145
			"jfmndcoealbbmlagpkgljccgldkooimdpefdalpfjpkmmebpmjcghchdebiojnjbbnfggemfnallpgbngaglekpkgagbgegmbicibhlkcpaoondhgnckgblhijlfdice", // 146
			"bnahnedbnljdjmjhodmfhigecmcmogdgcbcalcnakfpfdigebfmnkpjjpkhihjhpegianibmgifegnhidcinncgglfemnekeaccdebcnahdkdpbapkocgaimklbimjmm", // 147
			"amkcmpopnafjcbjjeiijmhaokmlkoiojelannbegpcockkknjbhifolonfcpfhdnmplmbjmjbdeldiofoaifpodldnegekanmnbejhapapmgdjfjmaijpkfaooemllje", // 148
			"haeblfekabldohiejpnmbikhkafedggfddpcacndlmjbeldkdinaoolllondjmdbpcpimmobkeacadgpkhabcjgkdefiehkjfeikgeghlgiedckppfmkgkdgpjlcllhe", // 149
	};

	// ======== EXPECTED_SEAM_HORIZONTAL ========
	private static final String[] EXPECTED_SEAM_HORIZONTAL = new String[] {
			"pcjkfejpganpfljhjdlnplhfgddkecbefbpfbgahciobocijbpmpogjhemekaeebmgaonahhijjldjhhlhgfegigcelclojllgebcmdikoancognjdphbfjknmmhdjpe", // 0
			"fcgoolpefdpfgamhdilpgpoodkhknlnicookdhofjkoogjpafeecgnknekklhlbhhagiihmlffkjoihipgccfdfbadacpmcbmedealpjmgoeocmodbdbicefonajepmj", // 1
			"eikocjjcaoedlhjebdmhhgbogkipgghmpceopekhmmafhbkbjjlomkkgfigljbhokgjocjjahgeldkchhljkgkdegenfhlnjclddpmfbjedoeknjdakhahffalfppkmc", // 2
			"gmcabeoojoakiochhelkgolejmedplomhofijhkbpjbciiipaboomaimdjiobnckkegmngopjiidebjienfhkkhelpdalmcmmfmjhkohjlhdalchpdamhckcecjcocii", // 3
			"fpmenoioneimmgplekdcmfnafcfpnaonmfpjdjaflppcodmpfofgepinoahkdmhhppboibafbaofhdfdnmlalinlammimpdofpjkeafjnbeiaadcffoaaooklibeiofa", // 4
			"mbiakebhhbbnpfoijnjaacpicekcofjcdiagemfelcmomhfphmceafcpbhoehbdfefmmfbfkbdajeojollohgianddamkbbhnbiaiamabdadbcaiokgekppmloibkema", // 5
			"njdbmjmfclkadnpkfcfifihmanhohlnojaeppdbdkdlajdhpidfphoefjkbbodjlmifeidhflbmnlalnbjohihkpbmkpnogoalcoiccidgbickceecmgfkpplbobcgfe", // 6
			"ckmgoclnamneponpklioiplejpdfeimkpinfibpjhpmkfmgfmojfakmlpbhblnepbbakcbeicdedcgjkjkgipncopnnpbnoomlapdaopopnlehnlpocbjmplciocdkcm", // 7
			"iceelmpmjalkjkkkbbgdgohlcmabcpedbiaajnmamjfnjkginneefopdohokcpphejdiiegdmdbfclghcplikakflkfbiehkcabodakiocinpagnmhkpgfegipgigpnf", // 8
			"jilghfenebmlgkodlijcoekfenbdblhhiapjcpcngdagpnapedbcaicnjenglhcppmohdlfjfbblpbajkiogfnjjppapmkmkolcgdhdllljbgonnnpehcjjjkfocpdbl", // 9
			"gnhidibfnfocjmfbmoeagfjccjacjcnphphbckeifdliibpnpgbncffiknafblkadmfelobpgkfbnfcipcjlpcfognafdjaljliphlbnnhdchlihnbcabfahpdkfmpng", // 10
			"hkpiogpidhijakcoogipgeighkchheipofbnieibnnnoecdmiikfjgnpdkpmmlbdpjklncilogfcedejejodafedkdjppjedamdgkcmelldbkgjmjagggefagplejoen", // 11
			"mobgmgcblmhdpbcfinkpebgkldiflbpomjofhmefohppnllodkndaaekjlicgiddgflgbgkdnelbpllahadilnglolcknllncmpipbacionagoeolkhbekkogkfbamkc", // 12
			"hafmcnpjhlokngiifggodalhpjpignppgmbianinphkoimnjoflmkhnphhnnlpfpdfdmfbpidoacnobbpmnekajmgneapenhkdbgldffkeaddgbpemnjkipcngoccflc", // 13
			"fnehplehmjgpnfdlicdhkegnhcfdnadookalcfeelpkjbfbhpoplankanhdjjdhogllpmaegfedgmipolhagaoopcdbcicpailblkigdeamjfpcpaebibpoiebafpief", // 14
			"jjckigmcjdijalkcnkfiokcilichdlhgamdnmnfklnbehipeidkchaccfabnogjfnkpdgneobkhngneedmhehjmepcdaemcnikjplgkclnlneoafpifhbonhboknmjhl", // 15
			"hoejbmkkoihnkckdikhldbpaacecpajnhkadaeogpiejbnjcopiegmokmldokhfgimjilmkgkpnhbbpahbnlglhlbpnoamghcojmcllojkmnpakfdhpbipkkpdnelnpd", // 16
			"cnkfaleldhjiiffhomgnadagnllhmebginoidjihoomlfmanmnpnkojkgeabchmmnoakffgoogdobiapdhfmfhnkofkdicamcpbeflmhpkhpfajgjdmakhembcljagpa", // 17
			"mmnafjalfkmgdbpmdeeafnngmpolpmgkkgnfbgnagagmdkppgldojidhpfkagkneaglcdaafmlpkhigiacmndgomddaeanmcildkhagpklpkppgljhlojfglpmnkfiok", // 18
			"nphdplpldmnhpmghfipaikncfhjiaklddoepkomenfhpimgenofepapaebpfbonpoighakmaopngkoicelllfnlmeadnagcgaofefjfbngflfcadkemlakhijcddfhoi", // 19
			"mijaaeahmcdajamipihehpaocaglemhhalegdpjaemehepnnecoelaajjafpclliffmohjpelbjemldknjgcondnlofgecpbckchnijmgdmgkbfinkdiejomcpebgick", // 20
			"ibedkdniolbnhenggemjbhjcmfbfnglgknphomnoppflabjpdebaoiklobgdjgdobfipgfcbebnjedemefhbfckmpacmpojihfhfhkpgeenembmfnnanjfhklfohbiip", // 21
			"knfimbepchpbojndnaeedblbebhggnalaacpkmehppeiceiaipaiepjflgpogkihkpkgnnmejbfnhgddoiejkphbeeeiadkdjbdifnlpdpllngkaamlgfljmkaahgmhl", // 22
			"efnfpajggmcfjifcoiobpidbdlcebljpkbnjnjcfmdocagbhgkcikmcdpiclkpneckcoiehjkpggajdnboilibnihdkckdegmachcmognnehndfagafboelcfokpmgjb", // 23
			"nebbnonjcpgicddfnijheknpgjabnlkmopcophpcgaiikpbjjgcfcbadhgpjpiahcnjgemjgglgkhggmfgfbklemmgcjcheafbkcbmcojffnnbdjpkfbkbnfpjnfmbfg", // 24
			"gonblbolpjggldadmhpclbpbfkbngcmkechacbkopjcfjjklndlfbfjjejnpocknldgammiafophajlkhmjikdlaomdgidkhepjhhjjhpamogcaogojbmbihhaipdjne", // 25
			"gonpbcednakdhhojhfnjpiijgoblgkiimfjeelbdgoacmagcgaenofadlpocdbdcmamacfekmjkgddmceicdhdklifpmdkcicapildaleceahobhgfnjgjgbooncefpm", // 26
			"kcilefofoigfjmmfneoeanahbnigglcdpddmdlknfdfihlkbphmfkbkihicjkcmkkaodcmfckchogohaccjljkimdgbmgjnacphhgilockbmolefmolocgfffbgamhme", // 27
			"fonngjjbmiijaamaekepbfadoipepjcnakdecnplomgmpiajldkodnopcdkincgdopgjkjfjcpemoefhnlcmpmihceeinacfnppmpkelhkedicoefffhgglanafcjiok", // 28
			"nbknmjbpcedmgjdiimnhjkiohndlkddaogebmhfbfiflpeclbeglnfncgnaapemiolgfgbdlokjnnipmphjffkjelfaibeabalmjocjigenmiabidfcdcbnilhnaicbn", // 29
			"ifdhcggplnljgngfdjijlbljijpodofhagicclnjiibihaegejnpkbmieknnhglegkjckpnabigehijibncdnebnbookmiaeooplacbgclnlcmghabmfklggfnfnbmba", // 30
			"aaajpmfpphpfkdggnfaehblkamdmlmbenpclafeofkdbaehbaclgliilpojcampgigbpbnkogcohhpkemmnjjlmjacppfkneibmdjajhcdomkkjjcgipgpkegljbpjhf", // 31
			"mmkdcckmegfnjmhfcbmgljehoenmgkfknbiibfhnjlnanhidhgnoiiclajacmbfehkfbpblnhfkadgbemldcckloijlinelhfillkpkhehkcnpgnincoicikajbipoio", // 32
			"kemoigffajhnmnbpgkbfajkkigpagkhopbaabepippdhdlgckhikjclpmljgphpbfnnobchdajafmmpgddnjechpjjpgcpmcnmcncefcnlllmeloocdfcklbocmphchm", // 33
			"bjimpmfbpaenpcoglidcpmmloccmlcjldhojonbdmilnbgkpkbaokpcbdpjgolcemchcoepcmolbkpgfnlkapgiddcdpbpmnplfboapkkahlbecpladcbinfoidfmgen", // 34
			"bincnglgpjlaibelolkehngkmlcdpipejkekmfeppakjpojcbadpfoapojpnddnkmplgpbnonblghggpilbfajeiompponkdfdlbhkhedhlldiidpbkbjfojgjpohkdh", // 35
			"cobdeikhpcfibmekciohhpompjhhiglflmpbbhipmonbdflknobioihmmdbebaggjigcldpikbihlcnkkhhekgkjlijelkhlimlkgkfehbffckonkhkidppfeiadnecl", // 36
			"hdeagakcamkifebfojdifliaoloafnmkollkelekgojkaplplmbekmlmgmahbpnjbemhmlhkedahkbgcnonlabnblbcabbjihhlmgjdgeflomefdmflfaelagfalllge", // 37
			"aoplkibeedflgbgfaecmmcndaibaepbdeljbdnmfkcbajaliclbilamnhkkhgjolhmahjhimaccjnjehodlhihcjmcjbmfahbpkkhihndajpofjnkbmfegcedadgfhpg", // 38
			"ngedhjnhalnefgkjdgflopkmecffknonfebpaneonjlekgibdabicbfabbfgpmeiamedbeifmebmjcdjlblgmcmndlfhcdpljaaghmmnnjpjmbggfkaidcdedclapckc", // 39
			"hfekbphfagahplijfmicnlknheohholojkjjbddedklfiglmfdpdpgkkofaicoehbpakfiflhemmkinajmecbfjbfmnlgaibiadbjbgdimbianeieggikeddiimbndne", // 40
			"cmnalegefigabaahoeflkjlaagfnmklkcdkegcibmlceagfnakjcmgkioldboblfkipkjghphekeglaldoilnjlojekakllkpgegljaceokoajdbkdhlacebdclhloae", // 41
			"bppnoiefeohelbcbajkmhgomngdkljbeajladjjinhfakhgcnfpbhaifdlmndkoceffdnlmcaahjnmflkdcicjaoloeelndhdgdcedepgonhegmlfkpeifajmecbheoc", // 42
			"dbbfckclenjmidnhmdmgpombhhhkineecighkhllilbblnjlljljcmkdnepkgfolicnjdlmcgccaoapglfnildgicakapmemekmbjdickenejgfmjpckpklgchnmlfeo", // 43
			"gepmdjhcnjiffcachdhagfignglhdaljgedfoalhmelcnmjfhdoeajhadngjdnlmcipbfmndfgkhabbgilaggpnkecjfagjfpnmnnhfokfjngnhlfdeiifcifoofimgb", // 44
			"omiganlcgpdlmadbbjioajndhledofmkffjangfejiblchohmaiopnigghnikdnnndalfchpdjkhcpakcagcikdnckohgnmlnakfkjehjhkbcokahbbhfnnopiaanmli", // 45
			"ebkhakmaelocjegieckcclbakkhjapbbpbkclnhkipmahpffokgijangnkdhpdacodcbipdaohpiodfnglhcopbllheclefncaecemcmjbgobhmpgedbjfhkahglknlj", // 46
			"jhjmghbbfboahinlncomnfkdjdnpkniaogllpebifdkkblljgafpglhaabiddohmbgccihilkopggfmemimoemcpbbieglmodcaefofjcfmogoljoecjilpmhaeelekc", // 47
			"maejngjfgflbfafhkmleplhpihpkihkhbmcfohgpchkpocahojelndfgdeelpplofmoagjdojgmeofdocebdeiimclngifmcobkflfafofdhcdhjjhiihhhainifdgop", // 48
			"ecjifjfhnkjpnlpgnffdejhgcbicofeahmcdnfnkmdkkfmhlecdclahpaojnplmicoeipmfefnkmbieedcpjagpnjckicfnhajlbbopppnfgeielfkcbkmefelkhndle", // 49
			"fpnckcnjlaombjmiagpkmmfkpjkpicgmigcbhjffeldnmmaiijchokdieihfjmemakcmfleagkakdhglhcpbnpibepdnigoldehkepkjhohejhhnapkcniiakhcidakj", // 50
			"eikahogafmdleofoepafopbkljgifnoljmapknjhkofnkakoobfdgelhkebdloljeodcdcbbkkfnllfodljmhapeikopolfpdhfmgooedifmnldjlpcjbmhnedjnadce", // 51
			"cnenglcajfijdidcmmjmgmldbejklnhdkilekmpmhfgnenjckdoafgeldioplenciddkhbkjonbbihmiobgbgmnojmjiieancfhkolbkepeejlnadhkniemmidancfof", // 52
			"milobggphmfmlomffodmgdimhpimjpbkhnmnmkpklhjmjljkhdaledgelgbkgjbjdjndfdpbhinnnlankladphdgalceflnikppijhfnaegjjodpppkjpjkjhdliaekm", // 53
			"agdfkokoiagmpfnhfbjeocibldpeelalnhggegafkdjecldagoicogllmemcijabcciaeflkcmkcjafojjfbalhfeidhgljjgamgpkalhajojhnigfohfolkhjijgofe", // 54
			"kbpdmakiadjlnkckifbgbcnlndgaeblelnjgeefedicggkfhbhajghhkgebfalimcaahfbkidfpjneiccenjfjmmgikjhhgljiokceloomaheaccgmmfefamhdmooecn", // 55
			"ndbdcekahebiomdlklaajcenlhdpdbjfpokileigkpknjfomkhibedfkealgdcihlcjbgplijpcioifkabgbohkaamdikbmphdijpjedpgmkjmchplkidlcbkneaalin", // 56
			"lpjkcgccmfkfeabepkopigghomolalpkboojbmipnobfinadcaocppgdplhbacekmpclfeonklepcpefhifljalfngaelcngkpijonjojmkkabehbjoiacbdfiajbkgl", // 57
			"gocghpiifanigonkopopgioadlfimnfdlkomilnndpamdhlpnjapoklifiipjepdbgeepgfmpjnikejilclpigjjnegfgnailbmhpjkchfflklfafjpahefmngkenkan", // 58
			"jmfnajccckonojnfjahgoeakncglpphbdoggdagbngbkfgmnomampbkpekhlgplfkhogmdnlgefpfgonjdahimbjclcaoloecknlfjmliihbaafloghfmelhmfpoobcg", // 59
			"gomkmnlgmefpfngpnokljckmoomjhgmhpbngekgimpnbnghpodmjbgphnbjilmgcoddlcejmhfngjibjimedgmbbbmpenhfbgpjlkppcnbgjhhpinlhgadjjenilnjcd", // 60
			"idfabkjonfhnnjdndnmglnanceocnifghblnfclfccajphhmbdjndegbipmbmlbpkomllnhblpjlhfajmfojfhnffkddeeabohccoohlpkiomehkfkdbhoaekaoinejm", // 61
			"bkcedpgfiiofnmohfdpgmcbopbioecodkfgcekgdlpdaeaajpofhliiaacdbbidanhcpmfmijghagdgbiadfhpapfiodmfjbiadpbgijicdgimfeaofpakkglhbfgbke", // 62
			"hbfghoagechpilbkiomlblpeemdogpppoahlnikfeaoebbliiecgcfjpciaolbhpajammpgipamleklcffegpkbkhamcmabclacjjjfdaaiaipmfomgnpdfknbompejk", // 63
			"jboofmgijpoffmpophpkaoifniklokjkghogmbdkjbopikodlbjdbmjidkhccbjnechbbfbghppjncfpooemipajdjamkmafdmnjefkmfjfggchomggkphhcbakifjgn", // 64
			"iefjbneellbhbalfahcjblndbpgkckieodhhhhifcmkigpfjpiblmnknkgjlicmchhogbnebbfmbkobkkbajjgiiicbefehefdjdmdpngmmpinpmpommgdihadofhnpe", // 65
			"mhacockpcgpnloldhmdakpbmiikfbnoplligakhlejdddhakpjkodklajbnpokacjjkpmmbjdaebhibfijedmpgdcljleackkkobgkgnfpfjlkkbdjgpeiiikenejngl", // 66
			"nmfkjglalhjcpfjemkajhfoejnfddohfalkakhdpjmlndfjkhapfibkigbgfkehldehacblhjcdldaaeinjgbjhfipkcojinhnjieepmeagdclpcjpfjbmkaljfhenld", // 67
			"bhnpihlcfmabkimglioaknofakdcnnioocibcpdjddedhbokiehkofkbonecoibfhjijbmmnikpndjbbpooeoppedmnennodceibgfkeegdnddfkeniphnhbcmcamphn", // 68
			"higbhenfieehljphammlioalchnafdcpkpdhidpiaoclckjbiopjnddlfiecmijkbkoadchpfgehkohfakddbkpmpokmfhambjkefmajachkajnphhmllghjcfnlnhha", // 69
			"llkmmpbdmcdhjahmfgjblmagmbabeliihocoolnodocaodlenginfhakeelegodkndkbiankonbffhidikkaoeflfnembjmfmehojgabhcbjlfcdnbgahenjcgpmcndh", // 70
			"glpjdneknkefhhiabbamiddleelmnlpkjakjicmggidjmnkpkdnfdegkbkbkbebkhngkfnpjaddjdjmmijcnhhjkinmpnchplgohembmcpndcgijggmdcgbenldidpdp", // 71
			"ghiiflbbjflcjmnefnaabkjenpbpdgifcflpmjhllopikcgdajakdkgmlkjbacakpobdneicignjnfpphhkkoifgloijhcdefjbcehicopjmbjemgjapjoeoffiohdoj", // 72
			"ahpnlnbepdagagdfngdgbogpdmbalhaoiifbhlnmajindbcbmkkegjjmdjaaclnbaapoepdelpkknlmpggpgidcddamhmmhdckjialiagpmgebpgkdljcckhambpdobf", // 73
			"mjgdgkkpikaifnigpcmedkfjikcjdnchpogoajoglbnapkpfckkfpjafkminedjlgikbgodbhjhdgjbiehmbingfmhnmdhahfidnaklfgenniealgjpdflepoajniplb", // 74
			"lljcodcgmjnbaoigjalaleghoeneejcobopbkfbdblajaopefgionhnceelkacdgpcmadlnjepijafndkokcccdbpbmhhjdnnmjmckjdeoaejlhdncgopckgflhjadid", // 75
			"ajejoeohlkjlcapehjfhihnlkombkoamaoibolhhgkmfpkgfamfoelpfojoidhbpdiokghldapnmnkjloipajdpmenihfnfpjfhbmfbonfgooikhnmnfeloeiipmeikp", // 76
			"baeajgnpjmgkogjclmlhnhhaiknfkckdilecmcpipbpofdhfngmeopoajcofjapjgkbdddmpnfhkkepeloabbcbffneplhjhhgpigndhdpbjalkonpejpahglmieokjh", // 77
			"okcdebeaekpemlnimgifamffegfpejmikndfdibdjgggldpfcbkepgbobamfcnamldgndepkjnphdfdclepehjfjkmglmofglmhglkhnbikjgnmlmeggjcbmdjncjocm", // 78
			"felcebhnooedcehlkebmooolmcphnidenhlbedddicmdmedphkdbcicdmflkijilnkbkmflifdgmhhdpkobbkegpnbelbndbkkbfddoggiijkeapdlcgblmacmailkgd", // 79
			"ongkckfdiifjnpmahiakpkffaahmgeifgmlokgajeoomaodbedfioocajkofgpckelomcjgbhcngajlmghchmlfghlbdmjdlocflndddfgpgfacgpokjijfkkpnglbgf", // 80
			"hloahnejlhpdciaejacmllbkcnecfmjglphlnfnjgmjikgmkilhlogdahejagcbgkkgajbcliomdpaphplonncncmpghpkidplpdbnedecbiglmlnmfhheamlobpdgin", // 81
			"efihjghjdcmmgnbkbbcdippheipaeligbplmgllcojnanhjogjkdkehbhlaplakcgabbhhhmnijbognmehjbhejnmpnboaeejhjomcfbajalnbialmlacmpbjegnhhgk", // 82
			"ckkjhhoimfkcbfdmliapkphdffgpimgndffaamdofhbdgepeiiihjeafabnfpppibdgjeccnmmnlgonlkgkmieganpkbifkbnkigedhlhhlmkldjhfiphjnphakhildg", // 83
			"bmakblcdhkajkejgglncceecoefjcmailoichpadgbgpahdcoahafhcfjbclapcjjhdkbpkljmegeaamejmlejlofjghnlmebenjfklmmjoanfcndigihgcalaggcjgm", // 84
			"klmhonpcbikjlfdagdajpfjnfloabkcilgngieilgojeaijcbhfnmoidbdnkgffkbcnegdkbdngapcmgnlpddheneladiiacpegmlembcajoiillfogindnojadojann", // 85
			"nnmnkejjgmjeohiaiaefanjgjibibmfblgicknmlglbcfppanjjkpjikepgkeffmbhgmifgebohpjpcggkamljmlpkilefkhnofmabmdjlljckjicjadgmeedibhckee", // 86
			"adndfgdillbjdmcfjpajlakahiaglfiocfbknncikcabdigheklmnadhmmpgpnieilmdhbnpfnkinakoklnkdnfdedaloaddjjdfpodfpelidgmbmcdgodegfledlcha", // 87
			"kgacfdkdoppbaaogabenlaagibggmbpoikbgchllodgbcocplfadglnapilbffgghppjopepbiicpiofeclamdoihhdpdmlajdnaldcbmjkpbdcapknpfdlejofdbejk", // 88
			"fmhljgjnhpiagcehhcjeophnoopocfmklnpcpplfmjkopmpmmoibahdcdaoekbodbklihdlcneagbijpppojjphlnmbkfalnonlpbloemdncmelhdjejeopmkfbhhako", // 89
			"bildbmlpihmeagamgnbeldkenefcifonchebgmcbolcpalcnhaaldmjbcpdhdnnmanbdmimjjejbgbonbpjbomjmifbnnckbelcbfjmciamagdnebiglikbfcgemadfm", // 90
			"dndnijkngpgpgeecodhojgoiacabfgancidojmjfjpbjmajeoniflibpepefkpcebghhpjdocobccaplgnccjligjfefkpgmfkgofajhpfhlhlnpbdfolgknmnjbinoa", // 91
			"hmfhepnohddaackhebdggfamlmdphhdajhgkjgigaopdkdkjebiebbmjmcebldphdnbfkgfkannoccjglnaeacldlnmoaijiijbnfkncekbecolfnkoledmhpcogcafc", // 92
			"aihchlgfdfkokhfiglmddlignhbnfcepbedaainajombfmaongnamhdhnkoolhongdajjpncfpbemjjhpaofppcbhflpookjcabinogbjocjiidmlgghialnklaofiop", // 93
			"gnejddfklhkinholhpfbigpnlkphjkjkedjncfhjccgjodcbhmoghomgifmhnemikjehgipolllhocanedcmolgfeechmjlbmpagkccamcfaiciboljelooahggkeock", // 94
			"gooibnfdmcklfmjhkpdhohlfocgbbkancpglapbnlbcclmkdachnjjmncaaljknclljdiocldedeklmgdgnglbjfpjgkoicpodkgnepebepoffjlahmghiijeajjihoo", // 95
			"acmlnomifkhamibhpaiakciphpcjaonipngghmeelgbcdihkcnioliiljgpfclplpkideiblgecbnanpobadajebmnpcjlklmeklbafblicjhfdeepcpbdjbocfacapa", // 96
			"lghklamjpnpndfommngdlgiigmbpgohhhmkcdoffgkbofplkbbakemhbjlphapikgppdpakadidenddhfbmolfhmllfnfnmpjfnbbfhllofhlkokmlhmmekpakgllehk", // 97
			"dcfnblpbmgkelfbdihicloggcfbgkgnkfilhckgepogmdkgmjhfmeclgcfgdlipliaafoaapafhdcaplokpjgpdbjchehelinehkokbcnkpeabodhnpgidfgmmhbfakl", // 98
			"mfcpmaahcagefngffiffpokcfagggmdloefnppefdmifejbmmadbhlkbhdpfaeeingcahccomhnddfmoodimmebegiipbkaacimlhaokcphoiaflnhbgoholplcbiaib", // 99
			"oocmfodgcbfcfgdjngecbmnmhecjbhlhbliaglfhpkcfmaenjaibahffogpcielaknpnkpajlcimnanmefmifhgbhipeaiilbgfphjmnmkjpbchdkngmhcfdgkddkefh", // 100
			"jlodhkaohmgedpejbkiabnfjgomflmocgkgdahepliihmomonaohaihjfkpcpnlclmdddfdakmhoekoiinaieajbkjdkhdglmhgbjleklldllefjndgncaabbocbfbdh", // 101
			"jbbehofbkpjaohiokbhooadfgmeiaffdlhgidkcmcomdgfhpoaicjbfijofmddkklklkekmkfddodcpfkklpmeilffcplkhlhjhbfmlfobahcmcfepejakiffgffmlji", // 102
			"epnpplmoojoamakfefgmigobdkfjomfppecaijhhomfbledlpfpjbnmfefpjcigkegfaiifolhkoinhgpgilccjagnojajpbkdjkkdnecochkmpadckdoifpfhpedgmg", // 103
			"hgpacdcgpbfohkeaoneekdnnoeccflkbihifeoblgkjbdamfheiomielkiniajdelcpblijfiifijdnplpjkaniobiajaljghileibifgkekpopnmjphbhlhiahnafgd", // 104
			"kajaoihccmiafnpbnhmfgainngnficahpldcibimgokfnpoipnokcndbifokikfmioilaaeagbaphdjnobjlpabdhfpjhkfpjijojkchpabieapobdciefkcohcfeble", // 105
			"nhgcfpcedokeghleakkdngipdgdfjphijgfmdcabcmcgfjlbcokhmfijlgnkmhpmbogchfjppjkibibapndfddoedcplnnkfohmlbhlaondhpfmdckennmaehhclnihd", // 106
			"lbclcfkcckodbeghclnilgimjaddcljbdkbphlcifodlblacmkmkbhfpabbkcbojhndkgkhikiiadjhjfbfdfkmdkgegjecokcioafcdbapigppgknnkimkfohejbmlh", // 107
			"lfipoecaanhfdbnhgaafkogklcloikbbcfcndpabdacnfmdlnobcciknmabfncnkjecmjndmacmakjdfcckpmicielabhjhbmnidalkekhgkbhkffppdohbofajdgdck", // 108
			"amckebdenaeppcjhiiaopiffbfndjgfjkpokgmaklcldeicginlffoepipijeapndhmpgafennceciofofnbbfpnlkdlfggffcoibncfflgpilfhmaadckfnblkljjcn", // 109
			"hjpdpjdljccanlagjjjedofkibokhhdhfkjikoakebedlnolhpdnefjcddalbfkpeocmaaefohhlhhjikccagdomipocbkplpbjndeffogdjkjpabmgeojekjellpgdc", // 110
			"cbbiaijhikmpgejckcoklnolbhkjnjombcoaajpepgpnfedcmfcflmffdgnfkhnplanhpchiedlhhpnpmdeohiibkdpjelbjpkejooendjcnjmmdmdaphajmncadoipj", // 111
			"odipenimakicoaknndkgggihehlmhhapmcmhbdkloodjkahmcihmffngjjngcofhecoanajepeajijbokddfdmlaammhmofcmdehfdmpikjamfbemebehibfdlallpbd", // 112
			"mgdhphdbdknmpnfhkbgogjikjpehlgjfieeanokheklgfejiphpcppjionalbaglhipcnbjgbeondahaiimikdjnnidbbpljjelhieaahippbkpmkobaemekiabhnoak", // 113
			"moielnpcokijhfcfeddencghhfenicegabdligkidaijajmecebgigdincdcpmnkcpoceblagdjpaicadmmpepaiodfacgnjdilnfdkjihgebnhbinkjhnnamlammppj", // 114
			"gkaehphchpgmhiibamkbboeeecmimdmiocmhggnbkeoocophdfeaioemfodpinplcefmpldacbddibmgcengkkecclkmdckdmpciocanmkgfifjblcikocohpbfjcmcg", // 115
			"hcmjjcajapbkjdamijnemnaclcoalkonhfnmhfeoellpaopjjmkbcjhjihbdebfhilbcjbadcejpkbjoikdbnndmpkpepgdljcbmhhihpeigdhefpolokdfdjifdpjbf", // 116
			"gjnamnaaoihnbceeokjcjedpabjkmoagnegcgfkolfgcclajpnamgpfgaabnfpfpflbpdpmaplpifffbidhhinaijmackfgccadhmcdiomlcdkhdjiaobkpmflbpdmno", // 117
			"aconfdblbopjpibelaeigjakeaolbmoemjemleecbknccpbokjcgkonlfecmjmgikmpjjffdpbjkcmfgmkbmjcoeoiipiiljpcapknmjhbhepaboomgihlhmgcnhampm", // 118
			"keaocmepfkcbpeoolcompejfgoipjehakmemadcaommfgbfijanobbneahnmojpiiblflanbhhfmjjoggepoccjopopbnbikcdbcbnemolobpfommhlfdnhkleedakbb", // 119
			"aknbooopekknajflafekpghpgbchgmompkganppgdindpiapigcoplemhknbikjafjkljmhmaanncjdpjjgafkgcgedohmefkkhbeaonjlcdlkjkkmcehfojmcnifhcc", // 120
			"fimafcjennejlkgbiocihpokaieafhalndgkmcdecekjpdddckpmkekbggeadnkafejmjepdfccaedaopchealhgfahjciedbnkmmcpbeifemplmjligglcfocombbaf", // 121
			"ieabiofbibocldncfdnfofmmfnkncmhabhphofcmkbcajibdpbakkcgfhkcaaaigpkgajkfkgijjpchcmjmlolmnklnbcjednadlnbjckajoknplinejhminngggmmdj", // 122
			"amdaoejffgdnahoaimjdkfhphdpoccgkpbpkclhfbphkmghfchedjaceehcldiednlihnfnaknhjjfdogjpfacedjmkejhjibdilnmiilckhomclcgcljlopdoieembb", // 123
			"nbpdmlbfhbkmiogfmdahibkmkmjdipgggkaoehjmhkllnpbpdoolejlkgpbedpldnecphpggnlhkkdmfamlpgocfkihpgeomibgobccdpjipldbibnjlgdihomgafjna", // 124
			"phhgpcgbmgplicgpngfklpfkjomedpeihmfpbafokbafiffjdainfkgglibemljbbpcpglggnepdiaimobdlnbjbdlgabobpkdebpmobblkkoakmgbeakmpmbnkaooal", // 125
			"glopknkejiebojhojlpdaeelldaiohpjgpldaabfdjojfmmleiiabhpfcilbaihjmkgpjegbjjlkmjoapmlkohkmdhbelodbnjlobpcaabjodhmmioagahdhbidkcioa", // 126
			"idjjmpngngdpjlbnnbdajipdjlfmcfclhipgoelndnofcieimnopbmpgjoijeeckpdleabfndbilbnpmlgklpoogajoobjikifpmainpmpfjeeofjdnadcjhppcdncel", // 127
			"ggpieajpkfbdgfgjhpnibhghbahnapjoohppjefeiholdjmaconffdplnachionkimamoogchkegameklcnifnehiogopkjbbphgjlpknmmpelkfnmlchjpeipchoeia", // 128
			"cpnbaemcoohmaciocefghjecbinoimlihmcjfneehjpbohkgmogkeokdgfeljdekflknglijaaolefekgiaphhfmcncpocijjkoehhmdmjlnicbgaalhjhdgnnladgem", // 129
			"oeodhngdpfolfndlifhpimhkbkcambcgapcbgmmckihkejkfpkckojahgcjehjabeolglnbpmcdjblmbnjiacejcoedoclelakdgfilajfibapeeodcamnfemfgfihfg", // 130
			"fdolhjiieilimokebgihjdondemlecbkfdgejgaefjebnghkgllobemdjlaiafedjejhgoemeiojllldeojjaaojeodkjnoohadblhlcbfmdbkghhkdkojihndcndbjp", // 131
			"lkfnfbehbkklmcaaaadfogeohhifflidhokgjnbcmfjoenojbnidimbpnabdlnkgpbdiknjmpbmhohbdlnbbepbjomkoldhbemjbgllakmhnlgkmlkboealoclkcmdmm", // 132
			"dndfcggagljadeijpnbkcpalmcpieppidhijdlglaomeikjdcmoklaeicpbajmgkdiclalapgcicbfkmnkklbglfcbempddpljfkebllnggglecalamdnedkonaondjm", // 133
			"apmnpdgneckjfncjignfpdlgkofpnmjocdeomkenkljgbdfmlfjongkedlgobhpcojpkljlidjjefcleapmnefoinifacagbhabknjdbiobbkblaaopajokjiloplfge", // 134
			"cojmahnakphelnlcpggofmelnnoakacminhkabnekmlfcahcgmmhmgoaipnmooepepjbpebdlnabhkpgihghnagnmbjchhkdecldhchdnhppcdnemnjlfnkakdfekbnb", // 135
			"anhchfmfamkflnnlojijmffmodfbgpdkomanafjjpgohfffndljalbmdgchaimlfmcmflmphhpinbolciopidaechdefccmphkmdmpjembkpbfmafomgohagfkdghmll", // 136
			"aninnjgobdppfcelaopchhkckillmdoeafmiecinghfkmcjpjbmabecggcjkgahopofcefifhddlmcmajkhbdagldmjbfbjelbmplbdlikaefmpongegclfolfcndean", // 137
			"pjiiaoefjbjhhalkeendfgadbkfcjfghnmmpdninncfiekpialhhcpiophbdninndngllkmfinbbgodgmblgbgcolcbfkjgakohgpjhjlogijlkdljfhmhljgolmelhj", // 138
			"oiboabdmahkpdjfebeccfbpgdfidhjneakppiobepehceccldnninpjacepimdnkbmaikmakhkihpobdlpjjklpeohcamaldnfehpaabknecoceanoedckpdghflejnl", // 139
			"egldmmhhflniddlmjhciohclnknliamlmdgmnopfcgkfbaajapenenhmjceiknbcaeccknmgbibfeoejamckhgpndfplhhnnijnjehibcolhdhhddilgmplpalifceca", // 140
			"jlfnhjlijklnmjmjpcjhcmcbndnkajdoggdnebaehoknbibjkcmmjgcfkoidgmnbnpfbnihlhpgcbanheccgmdgcjhbdgdleidhnmbkockcehcjoonoefjhphodjcodb", // 141
			"lbnphhielgpjchaopcaihidbjeokjdhkgpahphjkffjlcdihicppmeimjobiifehcepahcochnnlkikbdbbcdgggfdaigdnfnnhpdfmfjnmkdgaoccnoahhfhajigcba", // 142
			"hghemnnagcjghkephallldgjdolllnhdlehceigbblnaedcmibladkjocjmociaijkanjamjopigjjgkccdojdmldookaolmamepgdhmpapgadbmccjobcjooclffidn", // 143
			"ifcihfkjioacaihlclgboijbjiikcfjjecjdljgbahpoikdlflocohjiempcdhldbilgojgmaejnegaofljckpeeijlocjnnfleikkmbeijllininfifdcemjoifllni", // 144
			"pfnnjjkliiimjedeidlcdlokhhmoacjoddkmccmeipfdipdlgfcakondcalfmjjfaojblkgeligjjpaoacdganfmlnapjflmifjllkffdfbdlhmcjhmijbpjiehclioe", // 145
			"eaeignmicoljlcfmkapgiiljkodinaocpdnfbmahdbkjbdfjoihefclfnacimdbbjijpfldflgbmhigmjhelkiglfbclomjfeclecgmoihdlgfmmbkknkmampdfcknlo", // 146
			"kdpfhjjlpaagakpoodggfijaagbbjhlmjjnnmkeboooogidmcglamclbdkebmmmpcoedcbknfphbdmdfjehlpkmefdgllpnkgpocljgenalbcngibdcpbaickkliomdf", // 147
			"lcmjcaokalmbdjmmibaphapgbdppcldepklmhnajhmlpcjpdkmnimgppbjgjnhojclcddnoclnbkehoomcnobmagoocpbdcanigcaknaepnjhmmpadglohjhicohjigg", // 148
			"oipahpgnjnoagpakbilgmoeaahpmbjaochalmflimodgbeodhimiebhdbgahfjceilhohgafladllmoflfgfefpkcmijffgjhgpnopjjjomdghpkkdhljfcomjhhkhap", // 149
	};
}
