package prog2.project2.tests;


import java.awt.image.BufferedImage;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import prog2.project2.SeamCarver;
//import prog2.project2.SeamCarverImage;
import prog2.project2.SeamCarverImpl;
import prog2.project2.SeamCarverUtils;

// hash
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SeamCarverFelixTest extends TestCase {
	static final String version = "1.3";
	
	private SeamCarver prog;

	// hash
	private static String convertToHex(byte[] data)
    {
        StringBuffer buf = new StringBuffer();
 
        for (int i = 0; i < data.length; i++)
        {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do
            {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            }
            while(two_halfs++ < 1);
        }
        return buf.toString();
    }
    private static String SHA512(String text)
    throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-512");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("UTF-8"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
	// end hash
	
	private BufferedImage generateTestImage(int w, int h, int seed) {
		BufferedImage image = TestUtil.createImage(w, h, 0);
		
		int r = seed;
		int g = seed + 1;
		int b = seed + 2;
		
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				image.setRGB(x, y, SeamCarverUtils.createRGBInt(r, g, b)); // middle-left
				
				r = ((r * 2) + 5) % 255;
				g = ((g * 3) + 6) % 255;
				b = ((b * 4) + 7) % 255;
			}
		}
		
		return image;
	}

	private String hashSeam(int[] seam) {
		try {
			return SHA512(SeamCarverUtils.printSeamArray(seam));
		} catch (Exception e) {
			return "FAILED";
		}
	}
	
	private String hashEnergy(int[][] energy) {
		try {
			return SHA512(SeamCarverUtils.printEnergy(energy));
		} catch (Exception e) {
			return "FAILED";
		}
	}
	
	@Override
	@Before
	public void setUp() {
		prog = new SeamCarverImpl();
	}

	@Test
	public void test_Update() {
		UpdateTool.doUpdateTest("SeamCarverFelixTest", version);
	}
	
	@Test
	public void testPixelEnergySpecificationCompliance() {
		BufferedImage image = TestUtil.createImage(3, 3, 0);
		image.setRGB(1, 1, SeamCarverUtils.createRGBInt(0, 0, 1)); // middle-center
		
		int[][] localEnergy = prog.computeImageEnergy(image, true, true);
		int[][] localEnergyRef = {{0, 0, 0}, {0, 2, 1}, {0, 1, 0}};
		
		assertTrue("You failed to compute a correct energy array.\n" +
				"That's bad, and it also prevents this test from completing.\n\n" +
				"You gave:\n" + SeamCarverUtils.printEnergy(localEnergy) +
				"But expected was:\n" + SeamCarverUtils.printEnergy(localEnergyRef),
				java.util.Arrays.deepEquals(localEnergy, localEnergyRef));
		
		localEnergy = localEnergyRef;
		localEnergy[1][1] = 42;
		int out = prog.computePixelEnergy(image, 1, 1, localEnergy, true, false);
		
		if(out != 2) {
			if(out == 42) {
				fail("Oops. You calculated a wrong vertical energy.\n" +
						"I called computePixelEnergy with the following image:\n" + SeamCarverUtils.printImage(image) +
						"I gave you the following energy array:\n " + SeamCarverUtils.printEnergy(localEnergy) +
						"vertical was true, local was false.\n" +
						"You returned 42, which is wrong. I know that energy[1][1] is wrong, too,\n" +
						"but the specification says:\n" +
						"'...its local energy (given by evaluateEnergyFunction())...'\n" +
						"So, you should still have given the correct answer.\n");
			} else {
				fail("Oops. You calculated a wrong vertical energy.\n" +
						"However, you did not do an error I expected, so I cannot help you. Sorry.\n\n" +
						"Probably, you can help yourself with these informations:\n" +
						"I called computePixelEnergy with the following image:\n" + SeamCarverUtils.printImage(image) +
						"I gave you the following energy array:\n " + SeamCarverUtils.printEnergy(localEnergy) +
						"vertical was true, local was false.\n" +
						"See that energy[1][1] was wrong? You somehow got irritated by that.\n" +
						"That should not happen, becaus the specification says:\n" +
						"'...its local energy (given by evaluateEnergyFunction())...'\n" +
						"So, you should still have given the correct answer.\n" +
						"However, you gave neither 2 (the correct result) nor 42\n" +
						"(wich would be right if you could use energy[1][1]).\n" +
						"So, something is seriously broken here.");
			}
			
			localEnergy = localEnergyRef;
			localEnergy[0][0] = 42;
			localEnergy[1][0] = 42;
			localEnergy[2][0] = 42;
			out = prog.computePixelEnergy(image, 1, 1, localEnergy, true, false);
			
			if(out != 44) {
				if(out == 2) {
					fail("Oops. You calculated a correct vertical energy.\n" +
							"However, I gave you a wrong energy array, so your result should actually be incorrect!\n" +
							"I called computePixelEnergy with the following image:\n" + SeamCarverUtils.printImage(image) +
							"I gave you the following energy array:\n " + SeamCarverUtils.printEnergy(localEnergy) +
							"vertical was true, local was false.\n" +
							"See that energy[x][0] was wrong for all x? The specification says:\n" +
							"'This function should assume that the energy-array is already filled with the local energy of each pixel.'\n" +
							"So, you should have give an answer assuming the given local energy values are correct.\n" +
							"However, you gave 2, which is the correct result for the actual image.\n" +
							"That violates the specification.\n" +
							"Also, it suggests that your implementation is not efficient:\n" +
							"You (correctly) recalculated values that are already there.");
				} else {
					fail("Oops. You calculated a wrong vertical energy.\n" +
							"However, you did not do an error I expected, so I cannot help you. Sorry.\n\n" +
							"Probably, you can help yourself with these informations:\n" +
							"I called computePixelEnergy with the following image:\n" + SeamCarverUtils.printImage(image) +
							"I gave you the following energy array:\n " + SeamCarverUtils.printEnergy(localEnergy) +
							"vertical was true, local was false.\n" +
							"See that energy[x][0] was wrong for all x? You somehow got irritated by that.\n" +
							"That should not happen, becaus the specification says:\n" +
							"'This function should assume that the energy-array is already filled with the local energy of each pixel.'\n" +
							"So, you should have give an answer assuming the given local energy values.\n" +
							"However, you gave neither 2 (the actually correct result) nor 44\n" +
							"(wich you should have given, assuming enrgy[x][0] ist correct for all x).\n" +
							"So, something is seriously broken here.");
				}
			}
		}
	}
	
	@Test
	public void testComputeSeamThinErrorV() {
		BufferedImage image = TestUtil.createImage(1, 3, 0);
		
		try {
			prog.computeSeam(image, true);
		} catch (IllegalArgumentException e) {
			// Expecting this
			return;
		} catch (Exception e) {
			fail("Expected IllegalArgumentException to be thrown for image with width = 1, vertical = true\nYou threw something else.\n");
		}
		fail("Expected IllegalArgumentException to be thrown for image with width = 1, vertical = true\nYou threw nothing.\n");
	}

	@Test
	public void testComputeSeamThinErrorH() {
		BufferedImage image = TestUtil.createImage(3, 1, 0);
		
		try {
			prog.computeSeam(image, false);
		} catch (IllegalArgumentException e) {
			// Expecting this
			return;
		} catch (Exception e) {
			fail("Expected IllegalArgumentException to be thrown for image with height = 1, vertical = false\nYou threw something else.\n");
		}
		fail("Expected IllegalArgumentException to be thrown for image with height = 1, vertical = false\nYou threw nothing.\n");
	}

	@Test
	public void testComputeSeamThinV() {
		BufferedImage image = TestUtil.createImage(3, 1, 0);
		
		int[] testSeam = prog.computeSeam(image, true);
		
		int[] refSeam = new int[1];
		refSeam[0] = 0;
		
		assertTrue("Your computed seam (" + SeamCarverUtils.printSeamArray(testSeam) + ") did not match expected seam (" + SeamCarverUtils.printSeamArray(refSeam) + ").\n",
				TestUtil.compareSeamArrays(refSeam, testSeam));
	}

	@Test
	public void testComputeSeamThinH() {
		BufferedImage image = TestUtil.createImage(1, 3, 0);
		
		int[] testSeam = prog.computeSeam(image, false);
		
		int[] refSeam = new int[1];
		refSeam[0] = 0;
		
		assertTrue("Your computed seam (" + SeamCarverUtils.printSeamArray(testSeam) + ") did not match expected seam (" + SeamCarverUtils.printSeamArray(refSeam) + ").\n",
				TestUtil.compareSeamArrays(refSeam, testSeam));
	}

// Das im folgenden Test beschriebene Verhalten ist nur eine Empfehlung, keine Pflicht.
// Laut https://forum.st.cs.uni-saarland.de/boards/viewthread?thread=1490 sind alle Verhalten erlaubt.
// Deshalb ist der Test auskommentiert.
//	@Test
//    public void testEnergyOOB() {
//    		try {
//                prog.computePixelEnergy(TestUtil.createImage(7, 7, 0), 6, 6, new int[3][3], true, false);
//    		} catch (IndexOutOfBoundsException e) {
//    			// Expecting this
//    			return;
//    		} catch (Exception e) {
//    			fail("Expected IndexOutOfBoundsException to be thrown. See https://forum.st.cs.uni-saarland.de/boards/viewthread?thread=1490#8264. You threw something else.\n");
//    		}
//			fail("Expected IndexOutOfBoundsException to be thrown. See https://forum.st.cs.uni-saarland.de/boards/viewthread?thread=1490#8264. You threw nothing.\n");
//	}
//	
	@Test
    public void testEnergyOOBLocal() {
    		try {
    			prog.computePixelEnergy(TestUtil.createImage(7, 7, 0), 6, 6, new int[3][3], true, true);
    		} catch (Exception e) {
    			fail("Expected no exception to be thrown. See https://forum.st.cs.uni-saarland.de/boards/viewthread?thread=1490#8264. You threw something.\n");
    		}
    }

	@Test
    public void testEnergyNullLocal() {
    		try {
    			prog.computePixelEnergy(TestUtil.createImage(7, 7, 0), 6, 6, null, true, true);
    		} catch (Exception e) {
    			fail("Expected no exception to be thrown. You threw something.\n");
    		}
    }

	@Test
	public void testEnergyLocal() {
		String[] expectedHashes = new String[10];
		expectedHashes[0] = "d061478e4228f18ecc81bee29600147901d5cbdacdc0b21943f622b6ac96c3c459755f50870cf7d04a29b4ba874c40d4625d0e5c9df0f77571cc8177e0d557fb";
		expectedHashes[1] = "36f6d7ddba64cc4e426b8744a93865428d8ad4274ed29a31a4ffd44feafcfb6daedb1cb263e7e6ee6034211f7402549b260f6231522f6141f9d8ec48026138a7";
		expectedHashes[2] = "bd425a5d06e01addbc2e584b0411b2cd70141d1a0866913339fdd6391c5030c9f0dede207984810e3d05e21b5fa7d7897db380b2586fe737b37c86c822209454";
		expectedHashes[3] = "1d37d743c50b3bb836d9ee602070985b04d04eab2cff893e884c7609e54b77311445c1ca3cae65313b71b098d8b7680437e932ea6164d6b59a1cf077e581463a";
		expectedHashes[4] = "3a56ff8a0357e22b4aeea8aea35b6f086686718e97d2fb70fed2cf74547a6ab2bec895c65af16bd66d3599e23dcc0fb4a5d2d4a4d8a2ed0636f854a7d8c9d96c";
		expectedHashes[5] = "80400f4d7047e455bcb411c2def03ccc2973568b204af7255c22cd5c94322fb23a591f1f6279b9002cb112c1831f47874d61cf9da48ec66b3ee7919dfc7e970a";
		expectedHashes[6] = "819e0c3f6d58321bb12c6abe5f73ac39035367b26c80335fe8a2e5f0416dded65cf4378c9b59916df04540732312b0bf332b33575e2d13a3630038a79429ee2d";
		expectedHashes[7] = "7ec9cb67af245bc0b40c79eaa43ac91c2f600f1692a83bb7b6234e3e8a347b35e7e69518f1635a8dcd995250f3abaec507566fa7cfc04968bac52e601beaf194";
		expectedHashes[8] = "cc76dc8c6dd94a2d8a851598dd40a258debe4a342ea5b9fe86428cb6b67610443ec25d511da71de8524bbc04e0a4af1ff31f779ad35c2454207acbe8c2bcd99f";
		expectedHashes[9] = "1dfff27ee31bf8584781ab74ae664581e3fd2de4d1c5864c4ecf63ded4ca731becbfa38e3b115ae8f0a4488c4aee81da21f3b73ffb1189be872edbe2fc119fc4";
		
		for(int i = 0; i < 10; i++) {
			BufferedImage image = generateTestImage(100, 100, i);
			String hash = hashEnergy(prog.computeImageEnergy(image, false, true));
			assertEquals(expectedHashes[i], hash);
		}
	}

	@Test
	public void testEnergyVertical() {
		String[] expectedHashes = new String[10];
		expectedHashes[0] = "1beb9a5917d148065a4eff8ca2123d98d514877cc72a377e8d70d3b59a4c9f1853bc41b32291cf24924afa55a4a98e2dccf441a8ff32d7979ecdeb69c32e1097";
		expectedHashes[1] = "6a19b4f18ccc437ae5f64a83fe31fd7c81d21dc918affb98e159471b6fbcb8a825d9aa5ad9d6fec8fdc620b894541f0c440829801e0ac095146e85ed51456307";
		expectedHashes[2] = "0cc54b78009bd4102bbd930ca73b91939b850d1be96332b9e0f3ebfc5665a0303237504e3811eee45b184e651daef48c67cc501579d80c2b45091a2046b8f5f5";
		expectedHashes[3] = "da4e5e891a800c0a1060c10c8bdcf1ecbf7f0c244202965dbd422fe1aa998a116b899065c05d7e183b001005288a65dc6151010baae94808a9e54426abab9a4d";
		expectedHashes[4] = "b3d3edd0a6a3ff5216cc8b4414b261e6a90c5973ec317a058f1097f6fb1cb747ffad1883472d1ae972ddb7cdd46fb2e4bebc86bd070d70b09f25156b98845fff";
		expectedHashes[5] = "2cf1cc07a9d2c4e3831ff7900c7394f06756c912ce870d7af7d7aeed4cdf2fccd9e670530857fcadb7252a49a8f55710e5ed980b21c1ce5daebe00f4b7b868b3";
		expectedHashes[6] = "6318804de427bc113a33f0d64102acbbe1a1770bb446303e9a4c3809db1a3f3ef47f0f321b74174718455fde57f31820ba98b1aacda19aafd3cc356a7835c664";
		expectedHashes[7] = "56d2bc63877e9d8425fafd4b7dada2e13c61aff66b7fd29fb0374195780caff62b3708b6adff8fb862256eed17e54d7ac406a2613e288c0cd8167d9460867846";
		expectedHashes[8] = "ec02a0d5c98f4d7e204968bae2553a35e995e0a81388c735ad31df004da5781ac32a5a9379b1c3d226251d65cfc0bc7e07a43eb866431eb0d70e2b764bf28caa";
		expectedHashes[9] = "118123aecbec64d6a504020413c1cb5d0625f83e21095d05d90a9b98de6d183541b3b7241f7fe56b36b93cf06fec0a59ae3ecabf4ed81b6392c7788ecd2b662f";
		
		for(int i = 0; i < 10; i++) {
			BufferedImage image = generateTestImage(100, 100, i);
			String hash = hashEnergy(prog.computeImageEnergy(image, true, false));
			assertEquals(expectedHashes[i], hash);
		}
	}

	@Test
	public void testEnergyHorizontal() {
		String[] expectedHashes = new String[10];
		expectedHashes[0] = "717290691a4ac81f01247bc798cc8166b1d90fbc0b43cd2854455aa23b5ff8ab5e733efc1debc4c65674f2203b72d2b9af158421bd45b4fe350015bf4c90c156";
		expectedHashes[1] = "4f0119afa252e3c7c3e0deb1e64535fc7736f071ccec637f59b51ae31996ae0f34df7fe736337764fd305a07090a7fc2604e690fbc3c6c23fea3ab092cbd058f";
		expectedHashes[2] = "2f130bd84acd6ee9da459fe2ba9f3dfe3714662075aa770c3d4406a14faceb8309cd7ca1891abd103ecf4637d40c4bb06eeb1a85a0942b84513908f7e24b9ecf";
		expectedHashes[3] = "3739cf3358fa1cfcec40cd7a971b1a59bb0e574b155552441cf5c711799e3b2273f328ad75a96828cbf696489df52481e1997b8440b54bfeb6437ff6fea0a620";
		expectedHashes[4] = "58c43a230ff5d4b83bd953a77712e4d98b5caa1c529756708e4c5e52289d3bdfd7bb3376818df4aa0f58c83e3acc912c84460a2fe2b6d3b187f4f71c79369f5e";
		expectedHashes[5] = "2982af76e7780dbd3e5b52daf3e32ca63401c9df8f42b610858fd106f5a24efafc355caa4f39781198b1e29df26ac2a99fc2bced36b0efbdaabd0a4da1cc084e";
		expectedHashes[6] = "9e3f3b8b80358fd271510ee024dc351a525a439bb782bd590d5f6f7c67e2fae5efc8aa3159e7b16d264a37a146a31a53251da1fd2daab4403c4a5e1a5c5394f2";
		expectedHashes[7] = "b2f84a1ec752dded6269dbb59e54fd498b5523e8c17fbe822548e614322412146d9a502fc1e5ff23a557353f0e5cf9ace4070261d33eb125442dba55854a0c3d";
		expectedHashes[8] = "b4eb6c89a5b7030092978184443aed4f542b26c546510a243e9da10bc62d29cab0f8d5eeddf945f2e46387c221af392b837b3afbef8e934eedf4de55d0a00fed";
		expectedHashes[9] = "253ac78a4bab86934d420da2f50e904779645bc1fee24d8da28e1f2066577a38a0ba50a28d6e45faa5494566656e09586d52bf734b9f1cf60684b059fc1fe340";
		
		for(int i = 0; i < 10; i++) {
			BufferedImage image = generateTestImage(100, 100, i);
			String hash = hashEnergy(prog.computeImageEnergy(image, false, false));
			assertEquals(expectedHashes[i], hash);
		}
	}

	@Test
	public void testSeamVertical() {
		String[] expectedHashes = new String[100];
		expectedHashes[0] = "396936cfb344971bfb4cb4220dca9ce9a5437193ce68b84d8f82c529cf5d6b696e6b56af7ca8d67fceb6ce898e0d0565e31186af98ad3d0f8f4f9852efcf18b8";
		expectedHashes[1] = "a8f5221bb654bb294970b288209866aaf9203e7db3783a2eaf659b986df1c5739aa27976993bed78f2321aa680e964a5ee72479af1f4ab6bad4a7b99e2343ed5";
		expectedHashes[2] = "5312fc1bf5888afb129f138ce2bab0a7c4a7d07cc1bb51932d282e62c91e87a66e0537638017ac429457290cdf1ef48c501d4f2ffcf9a798d94c989fa8f2e2db";
		expectedHashes[3] = "a8f5221bb654bb294970b288209866aaf9203e7db3783a2eaf659b986df1c5739aa27976993bed78f2321aa680e964a5ee72479af1f4ab6bad4a7b99e2343ed5";
		expectedHashes[4] = "8f5574893b507cfc864156b1c7c38c0d81c817f6e32adf40e631ed660696e6643ccbe5e42fdb450b5f200cceebf6fadfa5c4932789009fd4381ce363600e7db6";
		expectedHashes[5] = "019c7cfd88d67681a696eaa4f1b4a49fab30717106dfb7c987154fb8509c3044d89f713bd9b05ba3370b2395cd40e900d9ca192e0e8506e16fa08a8605b8c579";
		expectedHashes[6] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[7] = "7999b5a0a121629ef66bcc5477d4e9114a8cdad2de15357339d842472b079270a3ac065f9a75775f524f6c342c973fcab7ccd80936a29e530e97123d3c383b70";
		expectedHashes[8] = "ca6102416e6ab173f172f6a34235598b011099b3d73377ffdecc443c33f10ba643f573f28544874bb0a022e4bfc733e6992eb4f8876561791d44a87b6195068b";
		expectedHashes[9] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[10] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[11] = "7999b5a0a121629ef66bcc5477d4e9114a8cdad2de15357339d842472b079270a3ac065f9a75775f524f6c342c973fcab7ccd80936a29e530e97123d3c383b70";
		expectedHashes[12] = "0bae22107ebf7cf5ed6e2a619061c3c0efa7a5d33090a0569e1bb31bd0dfe7abfa9a04aef168f32c4c06b703b14ec84caee9f738f2f11d79c1fa08bd7b931e70";
		expectedHashes[13] = "35e4d475134e82b92e42abe57e71edf55a0b89938d16b98c9cf2b070ce8c74520152bfc46c86e5004981181eed001d3f58f19635a308e398248624d9d2f0b190";
		expectedHashes[14] = "0ba98ffc9031cc1746aaba3484bbddcf456ab06707856e2e67f0c2209eaef519fdc95a043d37895736cae10d23f5dff1b05dae31c85c2a571ef8718a7b5e6c4a";
		expectedHashes[15] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[16] = "bdf507f30506e6e662ab987cd90bd223bf4c95394e8698e0dce07a6c4c6f81111a67c794da26539b60ed27ff476ebe229a2d833c3fa47f5fc9d5ba924c34d743";
		expectedHashes[17] = "906906a94e47b92ca505c68f2b4815b1b6362412f430a43ebe451a83632feafb355392bd44de349910968f12a2bb28159a25b42497440e374bb0d90681d604dc";
		expectedHashes[18] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[19] = "d673930f92455c4b2dfc7e5cb1297457b74bfb01d4fccda73fb1d6a0b2af0769b5aedb1984df479c972abb7b539d0920d928ca3f952072432d3397e2317ae54d";
		expectedHashes[20] = "0d2029bf58c331876b9c3ec490d66919d6c2c0f0d11b833476e1963a147f8d2f5ae035246e922eb3abfc6976282db884fa20ad2abce4260e62c6db11ecab94df";
		expectedHashes[21] = "be16e686c315128b22b61aead815a7c34ae78c98ddba3a6fea192ba40b287fede5b27a95bb1f57ecc6b74e21c4fe93dda20e3abe4cde814b75ab177692625850";
		expectedHashes[22] = "c7778641632d3ed44286a199fc9b8c3b67330a613bd1be823fd73e91c62c17c878a57a1007d34cd77de9e42d2e2990d00be8dfe5d08400d837e9011ae0b17e64";
		expectedHashes[23] = "f9f247c5f4f43e37e9c2ed802f9fad55beb35651f2dd6d450eb27ddd839ab9b0eafa4dc010c1a351225ad9a0ddf3e4104c083f3e4b40bef6ea7cda861692ff68";
		expectedHashes[24] = "aa181d6e080f23217e71a9e931346b3819e9a0c684577726b32b7772adae26571f0d3bea3951bb8783ab9f52c0870cf334f9138f4d3a7086e1f02c1310c2a413";
		expectedHashes[25] = "2074bc31a195f64445a6e60718ca8bc068f16e732059a0cb86604d8a34d8e7f7f11cdf0100acbef7389b97c7bd33cceac873fd56e5e6fe16e67d59b98920a372";
		expectedHashes[26] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[27] = "80199d7d04a7b1f44f13d9a8bbdbe8eedbd33d71af2c06e9f39c761fd580bc3b679eaa3850f5c09caa5a60cec5432f1dee0d8b4b9bcf6c5406931730c86bb1b2";
		expectedHashes[28] = "8f3ae2f8d171c131f1c31d4428ca2b89cfa0205875db4fc091cddbd52dad5f79131b5479583a0e12cbdecb90a5014a87b6aebf92c13ee72cc0c7f3e2de67b6f2";
		expectedHashes[29] = "90ccb983aad7ee39bbc447ba5ab293623ffbe1a5db1c67712ed2156c94856988e0defa349435f94252d7e80ae4187601f36602dbec1e4ab6dc99a1a4cd9e9019";
		expectedHashes[30] = "38c0b5ea4d1f1350793d508aa0d36954ebfed9823eaecba55cb0c22393c8854c05ce8ff633dd3714cf295d4a17166e244c6f49348f886c174aa5f0bb16782545";
		expectedHashes[31] = "29da3430024325298030d29f21fe4a618431fd58838cab5dc798764bc379fc1304be1581f2e04395a79b5cede1155a672bf3e2a117e4ac239a82eb09d333dea1";
		expectedHashes[32] = "0bb1ca8575ebed1fb808d2b1ee5a51690e5e12710c2f81a4eddbd7751666054659e5706f11a2ae9b8f4d6156b97c19255ec3dd558ecb3e6dd8f37309d3ff6253";
		expectedHashes[33] = "abb6424d72bd4906a87c776d357c9d5cac5d5cec77b5a1866d4de661acb4ddd7feaa62585ad26d289cbe8b876cd1fac533a908203c0abae42e9656dc9ba8071d";
		expectedHashes[34] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[35] = "044560b05a5f677b6ab46a946957bc457c24dc02801a6683e21f5183e4fdfd68cfacd12a38a7472cbccf19991ce024039d2b73a7b63a6e127283fa1588668e60";
		expectedHashes[36] = "7c9e8af282ee8e806f89ed5e4440cec87a87216ca1688accdf935d1da383c0f189ff9a0b8b78f583345516e3cbbe089519e37cd0a4fdfc9abe873880556e549b";
		expectedHashes[37] = "881a92b5e4e1760790f3377fe4f61663bbbe6bf6721c49ac771ca3ba9c126af8f815cc8b430aeac8b45ca06087bfc35d85b07186d0c42c072b12b9206cbb8ef0";
		expectedHashes[38] = "730d95c500614fcd04e073a4a160dac3e3e785b33d470a9ea5575b14eeba64a5dd950d1111df589efeeef5c656107b29721b73140bf6462785f99657f871bcc1";
		expectedHashes[39] = "238dd9c7d334fe7e726db76fefa2d0b436de27d701bd641f89fe45c7ea26ed3639a8bf9c614f3a1c2cf03beab343cdad44e3f4126b353262d4747ad5c9ae86d8";
		expectedHashes[40] = "8cda0bf89b2f4b2782c3ace7ff801fc6dc5a56722ff37be9d98454cd58fd21f7c8e576496abd2824603960966c6c2477befc078dc820fa4e9abed7163fc993fb";
		expectedHashes[41] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[42] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[43] = "33a18994e34a83b29576fd5bed57726d0c5123cff43202a8cecdec89650de549db90052bfd86dfd371afec4081c0b85eb8caf1e5f969e5409999a3125a0998be";
		expectedHashes[44] = "7d412e4f7e350848dbf4a07997d9eb93aa7092133159e93e67a7b56462cfeb9a2359d21cb86a0e1a0e35ab6f4e98950b611c03904c5d0ca1b7ef0f731e21be88";
		expectedHashes[45] = "fca91877089fb95ce06a48556d2443520b4b7cf8a4370359dfccdb69f077e3636b732afcb0823018ae9a83a5012005c51a396c1ed9bf6cc24a2369c0015d6a2c";
		expectedHashes[46] = "03792efdef44473dcc610c218041fb447ff9af1a4ab2bec73b55edc856b6c9aedf5d1faade75c1994f96eaa4b3e240ed182a2c2ad44580edec2a5b7bebbf770e";
		expectedHashes[47] = "1e95df0b9826f36b3858729aa2e3a93bfafa2250e41f475eee8e2a52027e615fd9b0f523a42ea96105e9fd76bf37f2542c3caa98e07e66bec382f05b7206aa8b";
		expectedHashes[48] = "5894d3cf59ab50f9571ecc56e2993a7fb150299267fb59094cd99316b68a79fbe06dea572ea44a117f99819359c27a0b151c6656bae71502457acd57e3dcf0d6";
		expectedHashes[49] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[50] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[51] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[52] = "9953eaf85c5f31fd9c1fcae259c96a558f9b5d95c62e2027c8b521395a71a99e5a2a2686323ea936c5062489ab9c89896c07f654c24f209c1ea1da5cfaf52e0d";
		expectedHashes[53] = "7c94591ff96999fe69acc3279a118a9a47844e97b08314d9ebbb8afeed3cd6aaaa760beefcfc35a6314ba680855569e01cce571e7664d36b7b2b3bc5eaba5e67";
		expectedHashes[54] = "5c3fb8c8ad79dc2bda7fea1ecb4469de0db498c26146cc0b213e394e752a2f2b3c2bccea99eab00a2cddaaf47e0c1ea05e79052cc4c07f39128e6eea5f28e8d3";
		expectedHashes[55] = "0ba98ffc9031cc1746aaba3484bbddcf456ab06707856e2e67f0c2209eaef519fdc95a043d37895736cae10d23f5dff1b05dae31c85c2a571ef8718a7b5e6c4a";
		expectedHashes[56] = "68991fd36187867eb7733641e5e01f88a8586a626c3ce3517ea4bc88e510d9fdb96124882c467d1d264441143f1ea08a4ecce18e9190436e49cecc58f452b02e";
		expectedHashes[57] = "d08b5646fbf2f2ab3520d6947bd3431715bff1fcf0f57168c7a1c63baa3cf761add791eb00e891eac2d63b19ca94471b1b9883ada29acd744a5ece4e56be9a9e";
		expectedHashes[58] = "f8f2fb21c762fac2c12222bbcf9f63796a95a57a356a0f707a289bf334e210924c3f5b194147bc16c698d50a373dff6ae68ebbdf976b9f7f217e4d9a2d558300";
		expectedHashes[59] = "9a41c2455fa2961eadbe2704c18cb4611157432bee666f221260cfae5016adce1d0095146e890068b9f1a1ceefefb98a88b447d18580bb52d38725efd8460f6f";
		expectedHashes[60] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[61] = "c33dbd780048fe731678917014611387d9686996c2c8680462ade82477380b7ab66ced99a10d893c4a285cfa1c9aa97a273f6d232b3e0012f14c02b3b6d58dfe";
		expectedHashes[62] = "0bade315e42acb03018b513adb5000078d3a3b5d0a0629de3c88235ea119b5ca42749087203ff64d6911002f7b442afd1a4ee5538a4b2670ef05e6d4df436db8";
		expectedHashes[63] = "ce19469f7ccf7ffbb4f7aa41aae37973b50b272e93a35805f750a61f530c1d77ad19cb1e1c9121c0bddbf5b9cee4b01f4b328b97c26a85ea272c80e01279399e";
		expectedHashes[64] = "0b0ac6bba2606df1af6dbede87446eacf1231821ed8538ae45e30539ee8ffbfb00b44719abacacb70c5be6f1a24f7dbce4af437b57f98774f9982943dbe66685";
		expectedHashes[65] = "684771e36dda3825e52e609c8a04a4fc55d4e170c1da7a8520b901ded6867ffdc3d857b6f810bc4ade4f98060159c98347930abefc20ed5bf50dc51a77173642";
		expectedHashes[66] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[67] = "680d403a3f26532122a5a67ae430858a0f8fe93c089c5d38837cb7a58c3d10c4bdbe435758049dd5bb8bc2ad52870b890e1ad4befeef2fc3cb061a13833284f7";
		expectedHashes[68] = "03729bc9f8fe04473c2016716245eb01609edb1e6600d489acb253d28b0defdbaedc6e3eefa50e1de56f63bc88dc28c608c84e9c8089f6a128689446a8860d89";
		expectedHashes[69] = "f4604669c69bbf284399e0799173c77d04c95bca083b23c5493ef9c76c5932f56f4269641bea28bf5902f1f29e87f92646c52d8f381ce225cdde41f2b5ac7f47";
		expectedHashes[70] = "0acc5735f53355524ab228f4fc86b066e5d6f461cf656b58ae07a18289cee675ee241966d216eedc89110e384731a5de10c2a4542e11d454da1ecefe55569e43";
		expectedHashes[71] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[72] = "6fd5d9d185a6cf894b30a232d45cb4c18a6c2640c45d11b82afb11b792ca5a8bb01fb034272b75915ae5ebbf156b2c6dd881f160143d9b7714f26c180ca102d9";
		expectedHashes[73] = "0acc5735f53355524ab228f4fc86b066e5d6f461cf656b58ae07a18289cee675ee241966d216eedc89110e384731a5de10c2a4542e11d454da1ecefe55569e43";
		expectedHashes[74] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[75] = "2b662feaaf285169dac82047221b1e36b742e53619cfddd605ad3c1f80ceedeeed8ae3a625994d69d9cc7e2c7ffa9aa81718abf11bb9d0787fef91543fba011f";
		expectedHashes[76] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[77] = "30863c107ce7c347915152eacd92a66740f0e2bb2f6194629015ef7754c6d48662dd44548a8f6551cedc852a07323d2a2d4af4292ce8b2056f9520b887887747";
		expectedHashes[78] = "9953eaf85c5f31fd9c1fcae259c96a558f9b5d95c62e2027c8b521395a71a99e5a2a2686323ea936c5062489ab9c89896c07f654c24f209c1ea1da5cfaf52e0d";
		expectedHashes[79] = "418f1a33a1a1a857bb9ad5e9896eb40db80eb4e0c75291c69e8e65429a1eecdcba16c359eef54e8577ec49af7c91afd5a3adcc81a625e681bce7c8157b81b392";
		expectedHashes[80] = "2825a070bc983cb44d1499756ae5b22109902db8adfe54a167e988b3a0d6cade1a8daddb69951fa046a25c4c0efa77bd6e01e8019f4557b94b4c23515a8a66ac";
		expectedHashes[81] = "7f333c4d7e3032dcab8546bea652c894df62858449a4d5824d574c24bc45301bab9f1b13cd34ad439a5b75b24575fa1c0351c7ad1e26df7402811f7b6072df03";
		expectedHashes[82] = "6a98e96198838960e653384ba5d0a800ba5cd56496dd72fc89e4b3b768b9fefc0db5480274c9cd61539ba67ffff09e7a4af1054100cc0365dfe21d0e35fbf50b";
		expectedHashes[83] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[84] = "29847aae4dadc0e60eb1f7537f0d3b1ea640407b729663f7619aba055c8d23f1bd505e4f0a2a5f440ddc141de2ba6f6c545c79e8c19549093ca87f04aa43bbbc";
		expectedHashes[85] = "dc8cf1db9b36761c96889fa00591659ac20eed303de1373f4f7cc40f5887ccb51f32c2c5ac80c0615854d5d80a825c9e628fd6c41a3692ae6af19241b2e66704";
		expectedHashes[86] = "36fcb5f913f182a402a916ac8c2c16acd92140b58058dfe5f4f17e6869e8c35f421d864be78a378408d48326ad8f139203d337f986824688931a9a808478333b";
		expectedHashes[87] = "9276829beea31994e65b18d784c437afed8884b7a88a793230b7a843b07f9ab4e17601cfa252c7cc75d740313cff02497523e2498819a0e9fc534bfeed493e7b";
		expectedHashes[88] = "56233b21c225b8bbc1a977b4104fa2faa19db68268337287764f1304f6a93d4db1b100d5887fa20c6c2106205d6227ca2086ce108bd1927b77fc3f192490f119";
		expectedHashes[89] = "631b976966a394470c2976578a6736efde0541809e48c1bf23c1eebd521065127dc3e7cdbee7d9a37116ef8844e0f232d6145667a611faebe87e3435b99d6937";
		expectedHashes[90] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[91] = "bda80ba9ca89e24fe1cfd101ccd446903c0d2e50c3bbe253a72cc7644422f90da2962043fb37472501239a96e10ce3f0285e7b37c5cb500fd51ee9d8b7060948";
		expectedHashes[92] = "21aaea0fd4272ec165a2fa89c8ce018e25c1d99d089da9c1adc37b18a26902bb5e988c4c00f7542544e1258dd71018107004c45d2355031af8fc1e3227a11100";
		expectedHashes[93] = "b64642cd52ecafc6e6ea8fb9cb15d84233f91bcabab99f4df9973addba064c5f62c9a4b6b76660286deeab9db4eefe53a04b5d128a7b6408de5850c8d1aabf68";
		expectedHashes[94] = "78d061e80d561dd6e034c6a7acd090d895970d6413f64a313ca71f6e873f8e0e6a61db7203038d20b17e85c998f7f371d51570a5683626ed4db7a65599d13554";
		expectedHashes[95] = "9476eb3a8c1e4999d98b1d0e61214432c10258f4f301232f7813a282d904da610e1e8ce7c7f5a4973748a90c34404b546caf6f9f6613a07370a7d171c549f79e";
		expectedHashes[96] = "5cb824c8a167b85409136f6c9237eca67b0c12950cac332730e11c8a23ba3a5b4e28a9adcc29742bca7345d8df7a29fe7b2e55861e43115f324ccc092c5220c0";
		expectedHashes[97] = "7b0c99bd16640c50a568e780b2df0ed9a7deb885ff562fb9dd549a149a23b0383a7a2de9e803ac13fb97c0320c7068b972e0d5dd2e54916ca6d180e92d140703";
		expectedHashes[98] = "100b42fab2f68a86d6712fc3180fcba851d3020a88cd2a2656c0f54028708d783af141600bf1f6000040a68dd1dc28b7a6dad0b4b7580db9cfce9d7315838e07";
		expectedHashes[99] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		
		for(int i = 0; i < 100; i++) {
			BufferedImage image = generateTestImage((i+1)*2, 100, i);
			String hash = hashSeam(prog.computeSeam(image, true));
			assertEquals(expectedHashes[i], hash);
		}
	}

	@Test
	public void testSeamHorizontal() {
		String[] expectedHashes = new String[100];
		expectedHashes[0] = "dc4d96b56fd4f09c2784ec47d875e57a7414d09d55aaaae4306f242a4cb1159f6bf38c9440e45c92d9e068dee98dcf1f8943101d4c3c5a6b30438c528d776eec";
		expectedHashes[1] = "3ad972bff29b1d2cd52a8ba9ba2e926870e9d2b690ad3a9c91a13bb0013e4b928ed7fd08a7c23691ed22fbca7b2e2171589922ae17ab47757d5f3b12964277ca";
		expectedHashes[2] = "b82e9a1a4789416fa6b3696e5995668e9376ce61f13a04cdb8e7418dd4d0a890a55b9edc5ac30b79c68f900a26452c456904139f0f14fcff925724aab0d11301";
		expectedHashes[3] = "667a23a77b61520d2574238179915c4dfaacc2afb29828c8161a05feec7498b010d11c595fec366de4fe213716f220131bd0fb41111a55f5c2adc87b63b7e5f2";
		expectedHashes[4] = "c284216e321d911c63a28949aa03894d2a90e889a3c8ce4100e56f4722d23a2cc59b819486b714fe79d7a828110fefe002fd09ec306ce39251dc7992f3c419b1";
		expectedHashes[5] = "74943942068d2172bd46cffe7ed6f1064d018c413afff33cb7ef39a679a2361945c978569a56aff2a29cf55e620bfb8a77ab3e08a5cd69a42ba791f9d88fa804";
		expectedHashes[6] = "26136c862193542816528550b6d6eb1efc185ec92e51baafd565b3063fcd1945baa8bc48fd99b7d4db761f0b79f55c9edc7b8b0a200b5c1693fd8477d66f453d";
		expectedHashes[7] = "040b5832bbc82a99f966b7c2e69ddf715111b1151e885593cacb4e8e039c5e8ba221edc586c61f5ed8c40c73c28594277774b69207b05d7d15f41c190fd1a9fa";
		expectedHashes[8] = "bc9ea5e575ed76b6751721b9b95d02fb8044af6139982619acde229d6ece23c92fbe1cb05fbe5297ae2c76938186b950317d147d890945e78deb8088f7ef6a21";
		expectedHashes[9] = "8124f38f6a084c7f55ebbdaa851dd80d0562a15aaf2f5cbb27dd9e7bdfe5ad563775fa5c99bd6b7d27511d0ed9964e31732c43d1af10da795ac4c5d38d377241";
		expectedHashes[10] = "5b707f9e86b40216abe7855eb676c2c53f31c8cb6f6ea268e113f9de1277c7553ca3634c29bfac53c676d9fdc4113f310b493df7a3749fd196d77e0162890b56";
		expectedHashes[11] = "f210f65bf07ca86ba9a4233b4f9e6dcc06cc4ce5bade941fa93d76bb97cc3ef3a6cfbd91792d561706de680fe217c08655c4ea0bd743f67a65f2b988f04e43b7";
		expectedHashes[12] = "a2cd314fcde6e714c68ab30d323a54af060eb1e4a64b084b6a50be6e40e7d915f9804cca98fec4804d8b375d6b7be064f5b67691002548e939bc72aa57428fc8";
		expectedHashes[13] = "21a4057d5ec0859c3b03f9df2f8c336c7385023d8e5c81109f367ebc23b8487cafb49e058044fa5377018f43d5ab42e45b8e248979bf961546107ec0e675c03b";
		expectedHashes[14] = "9aadd82e699bb810a49d284613f580f185269f1d9cd21fba3222e7986f537ef23efc1c3d5cfb1f8fcba642230d5fe5604804c1d24a2ffe2cc1bdf99d74454df8";
		expectedHashes[15] = "389a6fa59f3c8e95761abca1be44bb9a3755b97d8f3688fecff3b79be74fb538031ab54b52820d81793c6bf0541a224e076b0557aca02a1cdb479c92e68169f1";
		expectedHashes[16] = "db97a9fa24cbe24fa9553f213833d824b4cf1c40628d89c9df33d612e50b22d002f8288d337a51525f11650fda32c1e5b50568d8213e21830de8c359daf89c08";
		expectedHashes[17] = "dcf49214b8f5e1c19441b703a110577b4c205db7302a7791eb5777e8866184416940e74eb93c92ea2397a00d2f16982c8db4f8b001cf257f893558cc45f1e831";
		expectedHashes[18] = "4da680704438ec8c21a4a959baeb4a3158e116afc785b46ebee3458107c4f68a138fc3d580b19e986549e3d527aefcd5c32c513378dfffdbe95867aeca3d45fe";
		expectedHashes[19] = "2783daf5f5a841cba7077a4c624ecb5f2bef4608ebc4958f5cad7e40850762842bbc6d2e1b0fb4094c60c94fe7166fa73b1416d918d299d06302a2cdaab398e2";
		expectedHashes[20] = "1aa6a5f6939f811908db56747399230b69fd51be60c1cde5dff57eb2c4a034382e0611de57a196f45ac72a1f4fb5b18906212bf1c3a606fb15678ed30fbfe9ef";
		expectedHashes[21] = "9ba3df18c58c99717c44b24a5f3df56d40c796427aa95216dbe27f8055d1bef506c9ab90be1a49274f4615a917638705db8c6d9c2fad7d38a77027f50a49d3c2";
		expectedHashes[22] = "fb90d86a6b966c0f6fa93c0bd2a78a667d4bb19165997e101c191ee0bd1ef6fae225524d6be0f74531466cc147f32e0fb112107f64cab9113189b34d6715457a";
		expectedHashes[23] = "03b1d41fcfc4b5d5630284d7d640cd909c16217e7ecc64c7e732d8be4f6bb007ac11a8132a17178e008f676403a0854d12facf4033e152638ca410e3e39bdc39";
		expectedHashes[24] = "81a51f2535efcb9de6b041f503f2fb04d9d37ef006174c9ec37459521c47e84049bb9ce8855a35b2424a0cf5fbe9b2ebc79d34e42e14384870f44a382efb869b";
		expectedHashes[25] = "f6464753d66833887ea02d7f94004cc5c33ad7ebc17353b81fb07068e3274e7427d229a5a7e94e6a659cc0a8f2ec568bf83f0ff27ffa2e19c454da70d01faff6";
		expectedHashes[26] = "f18039370c351511a7d4c2166fc7b6758d921e72e3811dfeb734c5262fdf54062313eb5885968194a470bc77bed002a93b8bdb3f9941286e848278a8f76d1b24";
		expectedHashes[27] = "1108101068fbc1a369fe49fee2fbd6d705e57d66be89b2fd3555e9b4c17c4807dc32b301cb4b0eaefad3e699bafbd88ecb316faaf957c4f29c504c149886fc78";
		expectedHashes[28] = "e3b9ac140edd5ce1e021de6e845d90a6dbf4c50fb9537e2c4a21605b370dd8967bed96fc67df766ce9e23ac30f3403b34503d59cb923645ed55b4d6218579c0a";
		expectedHashes[29] = "07940414db7674f2f6dcd3d75922513bddff274fa63b739d20b79f9df386a4c8c585c23f584de2f79556169acd27fe8855ad8f96589fa701540a2156b93c1c42";
		expectedHashes[30] = "450832e1b914ae03021bff65997c529de2f74551605b08b0fad062a80cc09afff6d5b0234189c2b8db5f1abab147901b81b1f9e52f7ebe8b0b3af7d5b847c435";
		expectedHashes[31] = "d23b9c0c9a891ba9bf022546a01a4e5aff4030dc94e0a46489b39da37c5b404e2c3939ded864eff0e298499fb102db01399fdd163b10b5c88b68e72084f27a68";
		expectedHashes[32] = "cb7c451d0f0da3a0b3f9096841d1de4b19c8bca7fa6990793e5f6f97e32235324be3c5a89442eaf2f0ee4a99195dff9a7d9f37683a4a13a799929ae917a97383";
		expectedHashes[33] = "4f6f4df77be3e82a8c09b1b09bc3a822230ef938295bb20632649dcf0786b19dfe82d570953167c2f9c6774479ce2ba0758456c32852db8457adb4b861b3ca53";
		expectedHashes[34] = "22ce2bebf221cd4dc8ad27277070386effbb0932c6846c908aa3ce3729cf50381951312b636e5ba5b0b5d63380d98c917f31a57c49f1f5ddc67df1ada47a68a6";
		expectedHashes[35] = "c19ee417142ce0d8868e9ecaeb10d4e295b9c86188fa41db9461a1fc24ee56b5bb9909e584e2d7d8f26418ae2e0a28c310338e61faaffef6bcf6e091547ec3ce";
		expectedHashes[36] = "cb41d5c86fce1abc8e602dacadd4dfbfd1deca9db0048a5c0bba86275570e1eef8bf92215ddcd6d85df5ef7202c48ea4e1c569df2c03809eb62be0f0ea9bf8ec";
		expectedHashes[37] = "49514763b21feb98a23232a792f8562fb5ed4fde3514e575468fd436465358856ea10a5a85bc1513c2d4b4a53159b2c0067ec73f45f3c39c56c9ba4335b07d61";
		expectedHashes[38] = "9b0c87aca257ff0544a4497bde6f8d05daa6a4e4816de965b616a21869063256b11ceca2e67dc1a3547c3c4e6d02570f751779c8490ba7a56ea1863b0693d949";
		expectedHashes[39] = "0388c7912076c7eac7124bb562ebd2f77ab95fa88799f5e59eb073e7facbb5932eb7129e9ad3ca86e6612c84e9b3f29ecd561192943215a5d7c29389f82ef785";
		expectedHashes[40] = "881f132e7a0534d0eaa984eef71ed19342394044a978a8e14ad23d80f023e828be7fcfa27be37e54f4d6bc7afb1c3aead0a50d4cca73a81c9416ea9bf111b89d";
		expectedHashes[41] = "d9e4ba4436ed1e46cdf236e72d56b59ed5d6264acd953e710626775d22f47b24879083d99883ae17b04672c69381efff506528c1c9bdeb74208c295362d16c36";
		expectedHashes[42] = "ffd13dba30c5c69068fd31fad0af2b5aaea7ee6c4ece7d5e889d12082de58c3216a1cab90f1ee0b48962db134a1006041e3234e0c08854a9272dc71983db6f67";
		expectedHashes[43] = "ac3a8a56236dc47cc44376c82501c8924e3136a7278562c4e3a3f1c6d014329536540391c3b39a28b59945a9ab38efa424e4f485453babe5cc16753dedce71da";
		expectedHashes[44] = "08d4a2830c918723d809a7151b4d248eb4f66444f24b99dd0327b097f9be71f1eb15479d5e799802f8f83eb989ebf20d8539d88a091bc28865faed3e682e6843";
		expectedHashes[45] = "c3429abe7b7038d085989503734a9e9cbcc35a3535fb62423639b711147b3467177245842aa329e86ba8912b3d87883c4eb1096c40ea2e5078183e9f5303c133";
		expectedHashes[46] = "bb23db22a4e0b8e6abd98f14f8627774f1fc89a9bc39bd274fe4a3a46b6e41d1c16b9abb02fd473f00ba0fec34bff6a478a686e6c743707305603f5db1b092aa";
		expectedHashes[47] = "6000d0dc5b2a2fa79487095c04276104088ecc6656c1443afe3a74c3656a36cf4592f988af4e84b4370e00cf111331c6f3b845934e48ef4202b33d8954bb47d4";
		expectedHashes[48] = "d61b2fad3aa3fbcab69989a5b115ee011b603170d01ff1b6c3e9187b53ed10a27121e87958ce5e80e5a1ba45796b6e11eb754969865b57512740a6c31ea47732";
		expectedHashes[49] = "6564cd8a44cc7bd5fa820f5aec464d7ff92cf15f7180d41deeaf0beeff8c08b8abafd12a72d05f49d14ae29510847b56e5f9b991d0953628f6111284913b60c7";
		expectedHashes[50] = "96f48fc3febfdc7bcb1bf078c4b05a309bad31d340443e5d785e1d67dba8193fa320ae5298286002387d1b88063ffb1be2441427ec2972d0219d5db796434574";
		expectedHashes[51] = "4af2439080e6693db5daf117b34d7fe47b582a9ecbd225b70b95efa956617c85e7ce2e83b13704eea8ba7b41fc28d0ad50158ed42334cfe66f406d12637ece24";
		expectedHashes[52] = "e81e1b80c0b098f55eefbd488b3631477e2e521f41a3d0168d671f6d27bce986809ceb14443a4883d60030fc48e4a6bc50fd8bdacd95e3a076251e7eb78f4325";
		expectedHashes[53] = "42aec363800b3eb7bc7464a02e048c09d4e4a5b54db7a841f6806a6a4a7896c7d0acacaab03126d013a59448597bc79e90d4dc4e3fc598c7db6666a635530522";
		expectedHashes[54] = "7bebbbbc6bb3810deb825402c47e26153c828f24efb61af2b39079f898a2b909780d30777d578cf4eecf1ba9defded3e883f275036979e2cd8afa88342aa320c";
		expectedHashes[55] = "5a5c4af8368bd3d474763bbac11f4a4627ce2d123e90ab773e164ecba9d962f77cd86ddb1d40426635e86563522977556b25e6f5bae9615d26e9f0ff8d8f6c83";
		expectedHashes[56] = "5d12d974f25ae350e7aab4bbf04b07e21c7fb2b0eb990d21daaf05d84ddfb6b535be784dc1e51baa3a5107668217da76eabde0de5a289ed1ef21b7f4585e6378";
		expectedHashes[57] = "0788ecfbb551a88eed5fe254972f88f10fcfdc2cf3673821b5d97df4d5f9df3770d9126a8247e82b07fe10c24879f40b94cd14a817271b410fcde73353845085";
		expectedHashes[58] = "46b2ceeb1a5444404b99fcc6ea2450703a99f9e798f0c5433771b00d5a0c9d67a87f6fb37e2e23cbb72a91ffacdc56ddb82646389422d3f5143d738d79870366";
		expectedHashes[59] = "d2fc1f2d41c85fd8fc4eff6fd64a78189ba36da2bc1a4edae9e014fde82e614f694ef96e8b2adc5f9b71969fcf0c4e98a47ccb77193ccff794010b83501f3e9d";
		expectedHashes[60] = "95138391dacbebcd7ac8171a428dde8402216f2210d1265cd4369772625935c5abac9d9edf5f83316d0178eee2e49d7d44082bafbb687b732b429a93e2005041";
		expectedHashes[61] = "89ff6e8fe557b24cb5ecb6d54ccbeec75bedd98896ce1c2850ffa65ff01b8f0a3590b9d409d76e36adef3d9a74b5a38f648a5ac868ec120cfb1a421a3c386991";
		expectedHashes[62] = "74d119ed24559297e116c41fb876e83c0906b35cd77844152391cb20b992ecf0045f76c34e8aeb0f0078363022890873b64725b5eac636d4b4c7871c17846c8e";
		expectedHashes[63] = "dcaca49605f798b6e657d4b6b33a7e0582dde6166b74039914e3dd7abd8eee82ae292b66f032536855816d2339e44105ee1b681d45e50db385c568695148065c";
		expectedHashes[64] = "d727e3be445764c415e646c65c9cd88fd5e7556e068297507c63c25e750d207a601b34c1c04210e0ad02e83cac63352abd95210a46a3de790dc2ebb675c6399c";
		expectedHashes[65] = "3563dc1ca75cd9cf28b34a58a49d0c821c5e697802e521a3097a1d56055cf16c10b0436bdb0adc992ad6f400d3e92f7202b026f9e5b77d287583f8fe0194e34e";
		expectedHashes[66] = "9814934b4deb5e790f591d447bdac2f9ede19dabb6a8349bf64cad61e6a2bef66b2eb509b16f8049e26959674c6d075a6a5532aa5455ca8b8733ea83be9b86d0";
		expectedHashes[67] = "478eb18b4d06313d4d7bd6618650a708f66dfe8fe1fbd6aa6a2f3f55f95fed84b314ec2c1c860ccd34125343a74fab740c700d9ae517810a3629d81284877bde";
		expectedHashes[68] = "525586b12bd692d3d2fcd9fc2d746ba5a38c8e1dd3c57eb76bcfce87fe7112b0587d5ef43c7b99414d1e583405e3376fd5afaa599ceb568790e8c46aa19b13f5";
		expectedHashes[69] = "dfe241cbdecd9e5ab7c1a241fae7207398354d2f90da6ac404e123e99e513dd7bbfc43f52f425a00730213fd794be6a59895a85a4d909fc8a69e11d844814c3b";
		expectedHashes[70] = "8e76e93c5cc6eb71362d91ea6f12cf5944173d619348059fe8f69dd224279c55e0876dfc863e12f9a51698a99c8a5b331d2da3ca5a56c45caf56f97109f4583a";
		expectedHashes[71] = "3218e95a53f61411c74cda2bc36fe3fa53f01ef4cd806f890872c012b97cfd4fa53fb7998c953e069bb7569e3554fe6e57419a318d8b93fcbec44870721f6ba0";
		expectedHashes[72] = "bee6feaa7e7c7db4c2ce5ef32249b16499f44eb1bd36d79b24b3d1cbc662ef0a247a0b8f04b4802f366660d21706cc59db2adbf816a97dfdc828945b36d79a6b";
		expectedHashes[73] = "bc797384b4173188edf548d782fd16200640341add5a4716a1161c00237d2d9f4a4e405f9cfc61f027e10f5c1e21a4bd6e28998e261435df410fcf3e866c7477";
		expectedHashes[74] = "21b727684dbad107d6f324326154243dacb7a4519f4389be08e86d9e8a92ce0e7781aa0c5adfa29b7a77bfe2efeed4a4bb60b9f851216399d56c1097d13d853d";
		expectedHashes[75] = "f9231a7e58ae9a460f8352a5b1ef6996f9fe85823a16fd08efad6f0eff25992e40ca5cf36204607980ccf0b0e44a415cb80f4984c74ea041b9de284c02aba35d";
		expectedHashes[76] = "9c4abe28a6ff41060e465df5bf6c3def01602771711d89bda596763292e0dc574266070c5d946ff5efa78c0ab4e331572974cdc83ea4fdec31448c92a2742aad";
		expectedHashes[77] = "136a277d1c9f5a838893755d8835b61ce98a747ab0c221bd5c6a1393ed78e28a22d4cb13aa07ea0136e731e0f577af177a09d6ddfb739756253bc2fb330c98e7";
		expectedHashes[78] = "ff825936181d666b90cc8760958fc2326dbfeab5c9c412eb3dd1157256d0658b38af5c8aec93af4cd6412ed11daade87f1adf5884ca9df141e9f71e7168c7f99";
		expectedHashes[79] = "e2a4201f5e9312d3135334f2a968690c0470150ca15b820ed354fbdac86db54bfb98756617b6d044da070c04a77b084528ca117963b37355db4244ef126b42f7";
		expectedHashes[80] = "2c9f49ba6985b1910f627dcc9b8e16c5f4f9c3985c2243a41ddeb5f6f40a924e8c6891104f2a58fb35b200cc5afb1eefc623937b21df4c56190a06046ea647ea";
		expectedHashes[81] = "c9f7fd755b029a184d1c737da39f25c4ed81326f648817abc1d2e2b33d14c94b60514c6ea0b4ed0307653d785d15eec9739ba67265db6de1b3b71ac9759d1db8";
		expectedHashes[82] = "a55113dcaaf1d0a5bd18cdab84474277cec36de70b995286d301085e6df48743a2ffa6b13058eae4051594c3bc3b0ab3c0489372b5bcc669534765910fa2813f";
		expectedHashes[83] = "ca0939bcbb16278b6c659f3e384fb3d78a84592c14ab0787dd52725bb37368aa77cffc71d72da061d5e98bddb68cfd24cefe63b037e846d828ac163686dc5fd1";
		expectedHashes[84] = "824f600307bf9187c038ebe7988ce62985171522a058c25e1d00d8bdb721faba7130ed748d9ed940c80ee698a89562b6a1c5871f863aa8d945b34da70dff9997";
		expectedHashes[85] = "0b3d210e655bf55761094e8839e434fb4e2a8acc661ca2cc7afc6414c788dc980948f7cfc062fcb0fff8aabd62ea01ae85bae892557c61c3b0c968a8b1830329";
		expectedHashes[86] = "f83b592aa20bf9469cb206c0de9eeecc44bed686c6b73f39764eab05bc05232ed82697b90042f9111a2acb3c1d48732364b10f9f11762aca78b14158956586fe";
		expectedHashes[87] = "c50eeabc6a615cb594634dbd5615a7ff4833bbac04e418b04152d2eee1d3ce1feac0ad73deb352e3cb1bce582908ee4e817bd7906542d42ccd4120b96a3603af";
		expectedHashes[88] = "6abb554a51badfcb57763d148455bbf2e21de04d9323f5527c3a3dfd7154f952a888c56721266fc121ce76e6ff8ec77071eb616f27cdbf174b228ee33d492e5c";
		expectedHashes[89] = "48629ae3b6d98836bff0238beba539cb2e25103469ae714445f555da02cb60a5b3158e00d6d6951b7b5056162f65a2613c146720b26ca02df6b52f432cb70da6";
		expectedHashes[90] = "113dd2d9a96c239ff915415f5181fe0257bb53669289caf9327a03c540e09fb776efa35d862231df2d28adbc61d098b836c432e7565b05a737a0ac1040499f5d";
		expectedHashes[91] = "4ff004f0311f56e50f58eaca96379253ca6393191af81c0cdb5a147dd17ed5b570f9f59a1a86ba04af020f45782ce2cf6bb3b907e476677637ceec3a7355a48e";
		expectedHashes[92] = "052f5d108e1dab9c5f387968d746cfca6ea9e0f2c5779c18c67473840627b7a369c0035511a405b67829ac43e7ed34b77178f25a775c8dbeafec36c159a0a7e4";
		expectedHashes[93] = "5fd7da66990220306b59992fb465a008bb515846f2f414bee0eed63121e77dad608d028a3e4e2e8b51fe4259cbfc8a4fc8562b4d97bc071439be0b90c0121509";
		expectedHashes[94] = "649cc982847634e1c222cfe113797c10173c6de78ea360eb91e4e9b20f86ba44bc29dba47b6d1a9683aa31fc3962fc65793fc55afa171ff8c505392f7ca71f3f";
		expectedHashes[95] = "4bf30413116bc80f39d995ec04c7b9f28ae210a4f451c8bdaf7319be43aa4a7a0741ce648fdb9d35d26dbd1ce292ead36a55855136ef4a18b7d77a677dd90d87";
		expectedHashes[96] = "f7cc8e67a1e35e2dd5f4e839be331b43b115980386b9b9a223a4e8b5682e02a29dc793650ad0ec2d2af3120f3c406030701ef56700bb2e82361ffd310a3eae0a";
		expectedHashes[97] = "ebd3cc47997df9dc4cf77d15a52363394c90c2e9205f1c48d01892b4a85cd7751f069a0a75290c3447820d9f231e1127e7cf16142e21bfec325182afbcec506a";
		expectedHashes[98] = "7905609e802e4720cfc842bb63b529e55ee04032a6ccee541094b831681110dc73bb5dd6c154f5a26ca18e0a31b8ac7ab624d019702891702cd680bb1c8ce43a";
		expectedHashes[99] = "5cad73885dcc153b3818fa0ee7ee3ea46c9c35309c76b6f9d0070356fd07f2a5709e4f8ce597c979fe370631d80f08f3703cd24788bbecf485d5a42617203be8";
		
		for(int i = 0; i < 100; i++) {
			BufferedImage image = generateTestImage((i+1)*2, 100, i);
			String hash = hashSeam(prog.computeSeam(image, false));
			assertEquals(expectedHashes[i], hash);
		}
	}
	
	// 			System.out.println("expectedHashes[" + i + "] = \"" + hash + "\";");
}
