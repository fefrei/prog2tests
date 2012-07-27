package prog2.project4.tests.prog2tests;

import org.junit.Test;

import prog2.project4.tests.TestBase;

public class EvilFelixTest extends TestBase {

	public final String VERSION = "1.0.1";
	
	@Test
	public void test_Update(){
		CompilerTestUpdateTool.doUpdateTest("EvilFelixTest", VERSION);
	}

	@Test
	public void testReallyBigExpression() throws Exception {
		final int depth = 15; 

		String badExp = "x";
		
		for (int i = 0; i < depth; i++) {
			badExp = "(" + badExp + " * " + badExp + ")";
		}

		try {
			assertResult("int evil(int x) { return " + badExp + "; }", 1, 1);
		} catch (Exception e) {
			System.out.println("Your Compiler threw an exception while compiling my program.\n" +
					"This *may* be because ist has a *really* big expression.\n" +
					"Since there is a surrounding function, there is *no* guarantee that 12 registers are enough.\n" +
					"This is not a severe problem, because it is really mean*, but you should still consider fixing it.\n" +
					"* Tobias hinted that they will not test this.\n");
			throw new Exception("Your compiler threw an exception. See console.", e);
		}
	}

}