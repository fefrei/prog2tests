package prog2.project4.tests.prog2tests;

import static org.junit.Assert.*;
import java.util.concurrent.TimeoutException;
import org.junit.*;
import prog2.project4.driver.ParseEntity;
import prog2.project4.tests.prog2tests.CompilerTestUpdateTool;
import prog2.project4.tree.Type;


/**
 * 
 * @author Markus
 * 
 * 
 * Tests IF and CONDITIONALS: 
 * - simple tests
 * - naming and typing tests
 * - multiple if and multiple conditional tests
 * - nested if and nested conditionals tests
 * - mixed if and conditional testing
 *
 */

public class IfCondMarkusTest extends TestBaseMarkus {
	public final String VERSION = "1.0";
	
	@Test
	public void test_Update(){
		if (workingcopy) return;
		CompilerTestUpdateTool.doUpdateTest("IfCondMarkusTest", VERSION);
	}
	
	
	
	
	@Test
	public void ConditionalNaming(){
		// SUBTEST 1
		//Check some valid conditionals
		subtest = 1;
		code = CTS(/*
			int f(){
				int a = 0; int b = 0;
				return true ? a : b;
			}
		*/);
		assertNoNamingException(code, "No naming exception expected!");
		
		code = CTS(/*
			int f(){
				boolean b = true;
				return b ? 1:0;
			}
		*/);
		assertNoNamingException(code, "No naming exception expected!");
		
		
		// SUBTEST 2
		//Check some non-valid conditionals
		subtest = 2;
		code = CTS(/*
			int f(){
				return b ? 0:1;
			}
		*/);
		assertNamingException(code, "You have to throw a naming exception if there's an unknown variable in your condition!");
		
		code = CTS(/*
			int f(){
				return true ? a : 0;
			}
		 */);
		assertNamingException(code, "You have to throw a naming exception if there's an unknown variable in your false expression!");
		
		code = CTS(/*
			int f(){
				return true ? 0 : a;
			}
		 */);
		assertNamingException(code, "You have to throw a naming exception if there's an unknown variable in your false expression!");
				
	}
	
	
	
	@Test
	public void ConditionalTyping(){
		// We will check all combinations of types and make sure you return the right type. 
		subtest = 1;
		
		String[] options = {"true", "false", "0", "-5"};
		String msg;
		
		for (String a: options) for (String b: options) for (String c: options){
			code = a+" ? "+b+" : "+c;
			//check if you should throw an exception
			if (a.length() < 4 || Math.abs(b.length()-c.length())>1){
				if (a.length() < 4) msg = "You have to throw a typing exception if the condition isn't bool.";
				else msg = "You have to throw a typing exception if two values have different types.";
				assertTypingExceptionExpr(code, msg);
			}else{
				//We found a correct combination
				assertNoTypingExceptionExpr(code, "You throw a typing exception for correct types. Only make sure condition is boolean and values have equal types.");
				if (b.length() < 4) assertExprType(Type.INT, code, "If both values are int, you have to return an int.");
				else assertExprType(Type.BOOL, code, "If both values are bool, you have to return bool as type. ");
			}
		}
		
	}
	
	
	@Test
	public void ConditionalSimple(){ try{
		// SUBTEST 1
		subtest = 1;
		code = "true ? false : true";
		assertResult(ParseEntity.EXPR, code, 0);
		
		// SUBTEST 2
		subtest = 2;
		code = "false ? false : true";
		assertResult(ParseEntity.EXPR, code, 1);
		
		// SUBTEST 3
		subtest = 3;
		code = "true ? 12 : 10";
		assertResult(ParseEntity.EXPR, code, 12);
		
		// SUBTEST 4
		subtest = 4;
		code = "false ? 12 : 10";
		assertResult(ParseEntity.EXPR, code, 10);
		
		
		}catch(AssertionError e){
			printerrhead();
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}
	
	
	
	@Test
	public void IfNaming(){  
		// SUBTEST 1
		subtest = 1;
		code = CTS(/*
		int f(){
			int a=1;
			if (a == 0)	 int b = a;
			else         int c = b;			
			return c;
		}
		*/);
		assertNoNamingException(code, "You threw naming exception for a valid if clause. If clauses don't create a new scope. \n"+
									  "See https://forum.st.cs.uni-saarland.de/boards/viewthread?thread=1569");
		
		
		// SUBTEST 2
		subtest = 2;
		code = CTS(/*
			int f(){
				int a = 1;
				if (a == 0){ int b = a; } else { int c = a; }
				return b;
			}
		*/);
		assertNamingException(code, "You didn't throw a naming exception. Remember a block (then stmt) has to create a new scope.");
		
		
		// SUBTEST 3
		subtest = 3;
		code = CTS(/*
			int f(){
				int a = 1;
				if (a == 0){ int b = a; } else { int c = a; }
				return c;
			}
		 */);
		assertNamingException(code, "You didn't throw a naming exception. Remember a block (else stmt) has to create a new scope.");
		
		
		// SUBTEST 4
		subtest = 4;
		code = CTS(/*
			int f(){
				int a = 0;
				if (true){ int a = 1; }else{ int a = 2; }
				return a;
			}
		*/);
		assertNoNamingException(code, "You threw a naming exception. Remember a block needs a new scope which may redefine used variables. ");
		
		
		// SUBTEST 5
		subtest = 5;
		code = CTS(/*
			int f(){
				if (true){
					int a = 0;
					int a = 1;
				}else{
					int a = 0;
				}
				return 0;
			}
		*/);
		assertNamingException(code, "You have to make sure the then-block is named correctly!");
		
		
		// SUBTEST 6
		subtest = 6;
		code = CTS(/*
			int f(){
				if (true){
					int a = 0;
				}else{
					int a = 0;
					int a = 1;
				}
				return 0;
			}
		 */);
		assertNamingException(code, "You have to make sure the else-block is named correctly!");
		
		
		// SUBTEST 7
		subtest = 7;
		code = CTS(/*
			int f(){
				int a = 0;
				if(true){ a = 1;}
				return a;
			}
		*/);
		try{
			assertNoNamingException(code, "You throw a naming exception if there's only a then part. Remember not every if clause has an else.");
		}catch(NullPointerException e){
			printerrhead();
			System.out.println("Naming fails with NullPointerException if there is no true part.");
			System.out.println("REMEMBER: in your treefactory, makeIf gets elsePart=null in this case. Seems that you tried to do something with a non existing else part. Check that.");
			System.out.println(e.getClass().getName()+": "+e.getMessage());
			printCode();
			throw e;
		}
		
		
		// SUBTEST 8
		subtest = 8;
		code = CTS(/*
			int f(){
				int a = 0;
				if(true) int a = 1;
				return a;
			}
		*/);
		assertNamingException(code, "You don't throw a naming exception if there's only a then part. Remember not every if clause has an else.");
	}
	
	
	
	
	@Test
	public void IfTyping(){
		// SUBTEST 1
		subtest = 1;
		code = CTS(/*
			int f(){
				int a = 2;
				if (true) a = 0; else a = 1;
				return a;
			}
		*/);
		assertNoTypingException(code, "No typing exception expected. You only have to make sure the condition is boolean");
		
		
		// SUBTEST 2
		subtest = 2;
		code = CTS(/*
			int f(){
				int a = 2;
				if (a) a = 0; else a = 1;
				return a;
			}
		*/);
		assertTypingException(code, "Typing exception expected. You mustn't accept an int as condition, only bool.");
		
		
		// SUBTEST 3
		subtest = 3;
		code = CTS(/*
			int f(){
				int a = 0;
				if (a == 0){
					boolean a = true;
				}else{
					boolean a = false;
				}
				return a;
			}
		*/);
		assertNoTypingException(code, "This is typed correctly, because the two bool variables are only valid in their block.");
		
		
		// SUBTEST 4
		subtest = 4;
		code = CTS(/*
			int f(){
				int a = 0;
				if (true){ a = true; }else{ a = 0; }
				return a;
			}
		*/);
		assertTypingException(code, "You should throw a typing exception because the then-clause isn't typed correctly.");
		
		
		// SUBTEST 5
		subtest = 5;
		code = CTS(/*
			int f(){
				int a = 0;
				if (true){ a = 1; }else{ a = false; }
				return a;
			}
		 */);
		assertTypingException(code, "You should throw a typing exception because the else-clause isn't typed correctly.");
		
		
		// SUBTEST 6
		subtest = 6;
		code = CTS(/*
			int f(){
				int a = 0;
				if (false) { a = false; }
				return a;
			}
		*/);
		assertTypingException(code, "You have to throw a typing exception here, even if the if clause hasn't an else part.");
		
		
		// SUBTEST 7
		subtest = 7;
		code = CTS(/*
			int f(){
				boolean a = true;
				if(true){
					int a = 0;
				}
				return 0;
			}
		*/);
		assertNoTypingException(code, "You don't have to throw a typing exception here, even if the if clause hasn't an else part.");
	}
	
	
	
	
	
	
	
	
	@Test
	public void IfSimple(){ try{
		// Basis cases for if: true/false condition, block/statements, with/without else
		
		// SUBTEST 1
		subtest = 1;
		code = CTS(/*
			int f(){
				int a = 0;
				if (true){
					a = 1;
				}else{
					a = 2;
				}
				return a;
			}
		*/);
		assertEquals("You failed a very simple if test. Make sure you handle it correct.", 1, getResult(code, null));
		
		
		
		// SUBTEST 2
		subtest = 2;
		code = CTS(/*
			int f(){
				int a = 0;
				if (false){
					a = 1;
				}else{
					a = 2;
				}
				return a;
			}
		*/);
		assertEquals("You fail very simple if tests if the condition is false.", 2, getResult(code, null));

		
		
		// SUBTEST 3
		subtest = 3;
		code = CTS(/*
			int f(){
				int a = 0;
				if (true) a = 1; else a = 2;
				return a;
			}
		*/);
		assertEquals("You fail simple if tests if the then/else part is a statement instead of a block.", 1, getResult(code, null));

		
		
		// SUBTEST 4
		subtest = 4;
		code = CTS(/*
			int f(){
				int a = 0;
				if (false) a = 1; else a = 2;
				return a;
			}
		*/);
		assertEquals("You fail simple if tests if the then/else part is a statement instead of a block and the condition is false.", 2, getResult(code, null));

		
		
		// SUBTEST 5
		subtest = 5;
		code = CTS(/*
			int f(){
				int a = 0;
				if (true) a = 1;
				return a;
			}
		*/);
		try{
			assertEquals("You fail simple if tests if there is no false part. Remember in this cases: elsePart is null in your treefactory.", 1, getResult(code, null));
		}catch(NullPointerException e){
			printerrhead();
			System.out.println("You fail simple if tests if there is no true part.");
			System.out.println("REMEMBER: in your treefactory, makeIf gets elsePart=null in this case. It seems you tried to do something with a non existing else part. Check that.");
			System.out.println(e.getClass().getName()+": "+e.getMessage());
			printCode();
			throw e;
		}

		
		
		// SUBTEST 6
		subtest = 6;
		code = CTS(/*
			int f(){
				int a = 0;
				if (false){ a = 1; }
				return a;
			}
		*/);
		try{
			assertEquals("You fail simple if tests if there is no false part and the condition is false. Remember in this cases: elsePart is null in your treefactory.", 0, getResult(code, null));
		}catch(NullPointerException e){
			printerrhead();
			System.out.println("You fail simple if tests if there is no true part.");
			System.out.println("REMEMBER: in your treefactory, makeIf gets elsePart=null in this case. It seems you tried to do something with a non existing else part. Check that.");
			System.out.println(e.getClass().getName()+": "+e.getMessage());
			printCode();
			throw e;
		}
		
		
		}catch(AssertionError e){
			printerrhead();
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}
	
	


	
	
	
	@Test
	public void MultipleIf(){ try{
		// SUBTEST 1
		subtest = 1;
		code = CTS(/*
			int f(){
				int res = 0;
				if (true) res = res + 1;
				  else res = res + 10;
				if (true) res = res + 100;
				  else res = res + 1000;
				return res;
			}
		*/);
		
		try {
			int res = getResultTimeout(code, null);
			if (res == 100) assertEquals("Your first <if> branches to the second <if>'s then statement. " +
					"Make sure every if-then-else construct gets own, individual label names.", 101, res);
			assertEquals("First <then> statement has not/too often been executed. \n"
				    +"Maybe your first <if> branches to the <then>-Statement of the second <if>.\n" +
				    "Check your labels!", 1, res % 10);
			assertEquals("First <else> statement has been executed even if the condition is true!", 0, (res/10)%10);
			assertEquals("Second <then> statement has not/too often been executed. \n"
				    +"Maybe you branch incorrectly. Check your labels.", 1, (res/100) % 10);
			assertEquals("Second <else> statement has been executed even through the condition is true!", 0, (res/1000));
		} catch (TimeoutException e) {
			throw new AssertionError("Timeout, commonly cause of an endless loop. \n" +
					"Maybe your second <if> branches to the <then> statement of your first <if>. \n" +
					"Make sure every <if> gets its own, unique labels.");
		}
		
		
		
		// SUBTEST 2
		subtest = 2;
		code = CTS(/*			
			int f(){
				int res = 0;
				if (true) res = res + 1;
				  else res = res + 10;
				if (false) res = res + 100;
				  else res = res + 1000;
				return res;
			}
		*/);
		
		try {
			int res = getResultTimeout(code, null);
			if (res == 100) assertEquals("Your first <if> branches to the second <if>'s then statement. " +
					"Make sure every if-then-else construct gets own, individual label names.", 1001, res);
			assertEquals("First <then> statement has not/too often been executed. \n"
				    +"Maybe your first <if> branches to the <then>-Statement of the second <if>.\n" +
				    "Check your labels!", 1, res % 10);
			assertEquals("First <else> statement has been executed even if the condition is true!", 0, (res/10)%10);
			assertEquals("Second <then> statement has been executed even through the condition is false!", 0, (res/100) % 10);
			assertEquals("Second <else> statement has not/too often been executed. \n"
				    +"Maybe you branch incorrectly. Check your labels.", 1, (res/1000));			
		} catch (TimeoutException e) {
			throw new AssertionError("Timeout, commonly cause of an endless loop. \n" +
					"Maybe your second <if> branches to the <else> statement of your first <if>. \n" +
					"Make sure every <if> gets its own, unique labels.");
		}
		
			
		
		// SUBTEST 3
		subtest = 3;
		code = CTS(/*
			int f(){
				int res = 0;
				if (false) res = res + 1;
				  else res = res + 10;
				if (true) res = res + 100;
				  else res = res + 1000;
				return res;
			}
		*/);
		
		try {
			int res = getResultTimeout(code, null);
			if (res == 1000) assertEquals("Your first <if> branches to the second <if>'s else statement. " +
					"Make sure every if-then-else construct gets own, individual label names.", 110, res);
			assertEquals("First <then> statement has been executed even if the condition is false!", 0, (res)%10);
			assertEquals("First <else> statement has not/too often been executed. \n"
				    +"Maybe your first <if> branches to the <else>-Statement of the second <if>.\n" +
				    "Check your labels!", 1, (res/10) % 10);
			assertEquals("Second <then> statement has not/too often been executed. \n"
				    +"Maybe you branch incorrectly. Check your labels.", 1, (res/100) % 10);
			assertEquals("Second <else> statement has been executed even through the condition is true!", 0, (res/1000));
		} catch (TimeoutException e) {
			throw new AssertionError("Timeout, commonly cause of an endless loop. \n" +
					"Maybe your second <if> branches to the <then> statement of your first <if>. \n" +
					"Make sure every <if> gets its own, unique labels.");
		}
		
		
			
		// SUBTEST 4
		subtest = 4;
		code = CTS(/*
			int f(){
				int res = 0; 
				if (false) res = res + 1;
				  else res = res + 10;
				if (false) res = res + 100;
				  else res = res + 1000;
				return res;
			}
		*/);
		
		try {
			int res = getResultTimeout(code, null);
			if (res == 1000) assertEquals("Your first <if> branches to the second <if>'s else statement. " +
					"Make sure every if-then-else construct gets own, individual label names.", 1010, res);
			assertEquals("First <then> statement has been executed even if the condition is false!", 0, (res)%10);
			assertEquals("First <else> statement has not/too often been executed. \n"
				    +"Maybe your first <if> branches to the <else>-Statement of the second <if>.\n" +
				    "Check your labels!", 1, (res/10) % 10);
			assertEquals("Second <then> statement has been executed even through the condition is false!", 0, (res/100) % 10);
			assertEquals("Second <else> statement has not/too often been executed. \n"
				    +"Maybe you branch incorrectly. Check your labels.", 1, (res/1000));	
		} catch (TimeoutException e) {
			throw new AssertionError("Timeout, commonly cause of an endless loop. \n" +
					"Maybe your second <if> branches to the <else> statement of your first <if>. \n" +
					"Make sure every <if> gets its own, unique labels.");
		}
		
		
		
		// SUBTEST 5
		subtest = 5;
		code = CTS(/*
			int f(){
				int res = 0;
				if (true) res = res+1;
				if (false) res = res + 10;
				return res;
			}
		*/);
		try {
			int res = getResultTimeout(code, null);
			assertEquals("Multiple <if> fail if you haven't got any <else> parts.", 1, res);
		} catch (TimeoutException e) {
			throw new AssertionError("Timeout! Make sure your labels are correct and every <if> get's a different one.");
		}
		
		
	}catch(RuntimeException e){
		printerrhead();
		System.out.println(e.getClass().getName()+": "+e.getMessage());
		printCode();
		printAsm();
		throw e;
	}catch(AssertionError e){
		printerrhead();
		System.out.println(e.getMessage());
		printCode();
		printAsm();
		throw e;
	}
	}
	
	
	
	
	
	
	@Test
	public void MultipleConditional(){ try{
		// SUBTEST 1
		subtest = 1;
		code = "(true ? 1:2) + (true ? 10:20)";
		int res = getResultExprTimeout(code, null);
		assertEquals("Multiple Conditions in one expression fail.", 11, res);

		
		// SUBTEST 2
		subtest = 2;
		code = "(true ? 1:2) + (false ? 10:20)";
		res = getResultExprTimeout(code, null);
		assertEquals("Multiple Conditions in one expression fail if one is false.", 21, res);

		
		// SUBTEST 3
		subtest = 3;
		code = "(false ? 1:2) + (true ? 10:20)";
		res = getResultExprTimeout(code, null);
		assertEquals("Multiple Conditions in one expression fail if first is false.", 12, res);

		
		// SUBTEST 4
		subtest = 4;
		code = "(false ? 1:2) + (false ? 10:20)";
		res = getResultExprTimeout(code, null);
		assertEquals("Multiple Conditions in one expression fail if both are false.", 22, res);
		
		
		// SUBTEST 5 - Mulitple Conditionals in a function
		subtest = 5;
		code = CTS(/*
			int f(){
				int res = 0;
				res = res + (true ? 1 : 2);
				res = res + (false ? 10 : 20);
				return res;
			}
		*/);
		res = getResultTimeout(code, null);
		assertEquals("Multiple Conditions fail if they are in seperate statements of a function", 21, res);

		
		
	}catch(TimeoutException e){
		printerrhead();
		System.out.println("Timeout! (mostly caused by an endless loop in your code. Check your labels.");
		printCode();
		printAsm();
		throw new AssertionError("Timeout in subtest "+subtest);
	}catch(RuntimeException e){
		printerrhead();
		System.out.println(e.getClass().getName()+": "+e.getMessage());
		printCode();
		printAsm();
		throw e;
	}catch(AssertionError e){
		printerrhead();
		System.out.println(e.getMessage());
		printCode();
		printAsm();
		throw e;
	}
	}

	
	
	
	
	@Test
	public void NestedCondition(){ try{
		// SUBTEST 1
		subtest = 1;
		code = "true ? (true ? 1:2) : 0";
		int res = getResultExprTimeout(code, null);
		assertEquals("Conditionals fail if they are nested!", 1, res);
		
		
		// SUBTEST 2 - Conditional in conditional - Mass test
		// something like "true ? (true ? 1 : 2) : (true ? 3 : 4)"
		subtest = 2;
		for (int i = 0; i < 8; i++){
			boolean a = i%2 == 1;
			boolean b = (i/2)%2 == 1;
			boolean c = (i/3)%2 == 1;
			code = a+" ? ("+b+" ? 1 : 2) : ("+c+" ? 3 : 4)";
			res = getResultExprTimeout(code, null);
			assertEquals("Condition in Condition fails.", a ? (b ? 1 : 2) : (c ? 3 : 4), res);
		}
		
		
	}catch(TimeoutException e){
		printerrhead();
		System.out.println("Timeout! (mostly caused by an endless loop in your code. Check your labels.");
		printCode();
		printAsm();
		throw new AssertionError("Timeout in subtest "+subtest);
	}catch(RuntimeException e){
		printerrhead();
		System.out.println(e.getClass().getName()+": "+e.getMessage());
		printCode();
		printAsm();
		throw e;
	}catch(AssertionError e){
		printerrhead();
		System.out.println(e.getMessage());
		printCode();
		printAsm();
		throw e;
	}
	}
	
	
	
	@Test
	public void NestedIf(){ try{
		assertPrint();
		
		// SUBTEST 1 - Mass test
		subtest = 1;
		code = CTS(/*
			int f(boolean a, boolean b){
				if (a){				// Nr. 1
					print(1);
					if (b){			// Nr. 2
						print(2);
					}else{
						print(3);
					}
					print(4);
				} else {
					print(5);
					if (b){			// Nr. 3
						print(6);
					} else {
						print(7);
					}
					print(8);
				}
				print(9);
				return 0;
			}
		*/);
		
		int[] input = new int[2];
		for (int a = 0; a < 2; a++) for (int b = 0; b < 2; b++){
			input[0] = a;
			input[1] = b;
			int[] out = getOutputTimeout(code, input);
			
			if (a==1){
				assertOutput(0, "You didn't jump correctly in first <if> statement. a="+a+" b="+b, 1, out);
				assertOutput(1, "You didn't choose the correct statement in <if> 2. Condition was "+(b==1?"true":"false")+". a="+a+" b="+b, b==1 ? 2:3, out);
				assertOutput(2, "You didn't jump out of <if> 2(inner <if> in outer <then> part). a="+a+" b="+b, 4, out);
			}else{
				assertOutput(0, "You didn't jump correctly in first <if> statement. a="+a+" b="+b, 5, out);
				assertOutput(1, "You didn't choose the correct statement in <if> 3. Condition was "+(b==1?"true":"false")+" a="+a+" b="+b, b==1 ? 6:7, out);
				assertOutput(2, "You didn't jump out of <if> 2(inner <if> in outer <then> part). a="+a+" b="+b, 8, out);
			}
			assertOutput(3, "You didn't jump out of <if> 1 (outer <if>).", 9, out);
		}		
		
		
		// SUBTEST 2 - Mass test
		// The same thing, but use if statements instead of if blocks
		subtest = 2;
		code = CTS(/*
			int f(boolean a, boolean b){
				if (a){				// Nr. 1
					print(1);
					if (b)	print(2);  // Nr. 2
					 else print(3);
					print(4);
				} else {
					print(5);
					if (b) print(6);  // Nr. 3
					 else print(7);
					print(8);
				}
				print(9);
				return 0;
			}
		 */);

		for (int a = 0; a < 2; a++) for (int b = 0; b < 2; b++){
			input[0] = a;
			input[1] = b;
			int[] out = getOutputTimeout(code, input);

			if (a==1){
				assertOutput(0, "(Statements instead blocks) You didn't jump correctly in first <if> statement. a="+a+" b="+b, 1, out);
				assertOutput(1, "(Statements instead blocks) You didn't choose the correct statement in <if> 2. Condition was "+(b==1?"true":"false")+". a="+a+" b="+b, b==1 ? 2:3, out);
				assertOutput(2, "(Statements instead blocks) You didn't jump out of <if> 2(inner <if> in outer <then> part). a="+a+" b="+b, 4, out);
			}else{
				assertOutput(0, "(Statements instead blocks) You didn't jump correctly in first <if> statement. a="+a+" b="+b, 5, out);
				assertOutput(1, "(Statements instead blocks) You didn't choose the correct statement in <if> 3. Condition was "+(b==1?"true":"false")+" a="+a+" b="+b, b==1 ? 6:7, out);
				assertOutput(2, "(Statements instead blocks) You didn't jump out of <if> 2(inner <if> in outer <then> part). a="+a+" b="+b, 8, out);
			}
			assertOutput(3, "(Statements instead blocks) You didn't jump out of <if> 1 (outer <if>).", 9, out);
		}
		
		
		// SUBTEST 3 - Mass test
		// The same thing again, but use statements instead of if blocks even for the outer if
		subtest = 3;
		code = CTS(/*
			int f(boolean a, boolean b){
				if (a)	if (b)	print(2);  // Nr. 1 - Nr. 2
					    else print(3);
				else    if (b) print(6);   // Nr. 3
				 		else print(7);
				print(9);
				return 0;
			}
		 */);

		for (int a = 0; a < 2; a++) for (int b = 0; b < 2; b++){
			input[0] = a;
			input[1] = b;
			int[] out = getOutputTimeout(code, input);

			if (a==1){
				assertOutput(0, "(Statements instead blocks) You didn't choose the correct statement in <if> 2. Condition was "+(b==1?"true":"false")+". a="+a+" b="+b, b==1 ? 2:3, out);
			}else{
				assertOutput(0, "(Statements instead blocks) You didn't choose the correct statement in <if> 3. Condition was "+(b==1?"true":"false")+" a="+a+" b="+b, b==1 ? 6:7, out);
			}
			assertOutput(1, "(Statements instead blocks) You didn't jump out of <if> 1 (outer <if>).", 9, out);
		}
		
		
		// SUBTEST 4 - Mass test
		// Code already working if we drop an else statement?
		subtest = 4;
		code = CTS(/*
			int f(boolean a, boolean b){
				if (a){				// Nr. 1
					print(1);
					if (b){			// Nr. 2
						print(2);
					}
					print(4);
				} else {
					print(5);
					if (b){			// Nr. 3
						print(6);
					}
					print(8);
				}
				print(9);
				return 0;
			}
		 */);

		input = new int[2];
		for (int a = 0; a < 2; a++) for (int b = 0; b < 2; b++){
			input[0] = a;
			input[1] = b;
			int[] out = null;
			try{
				out = getOutputTimeout(code, input);
			}catch(NullPointerException e){
				printerrhead();
				System.out.println(e.getClass().getName()+": "+e.getMessage());
				System.out.println("You throw a null pointer exception if an <else> statement is missing. " +
						"Remember: the <else> node you get is null if there's no <else> part. Check that.");
				printCode();
				throw e;
			}

			if (a==1){
				assertOutput(0, "You didn't jump correctly in first <if> statement. a="+a+" b="+b, 1, out);
				if (b == 1) assertOutput(1, "You didn't choose the correct statement in <if> 2. Condition was true. a="+a+" b="+b, 2, out);
				assertOutput((b == 1) ? 2 : 1, "You didn't jump out of <if> 2(inner <if> in outer <then> part). a="+a+" b="+b, 4, out);
			}else{
				assertOutput(0, "You didn't jump correctly in first <if> statement. a="+a+" b="+b, 5, out);
				if (b == 1) assertOutput(1, "You didn't choose the correct statement in <if> 3. Condition was true a="+a+" b="+b, 6, out);
				assertOutput((b == 1) ? 2 : 1, "You didn't jump out of <if> 2(inner <if> in outer <then> part). a="+a+" b="+b, 8, out);
			}
			assertOutput((b==1) ? 3 : 2, "You didn't jump out of <if> 1 (outer <if>).", 9, out);
		}
		
		
		
	}catch(TimeoutException e){
		printerrhead();
		System.out.println("Timeout! (mostly caused by an endless loop in your code. Check your labels.");
		printCode();
		printAsm();
		throw new AssertionError("Timeout in subtest "+subtest);
	}catch(RuntimeException e){
		printerrhead();
		System.out.println(e.getClass().getName()+": "+e.getMessage());
		printCode();
		printAsm();
		throw e;
	}catch(AssertionError e){
		printerrhead();
		System.out.println(e.getMessage());
		printCode();
		printAsm();
		throw e;
	}
		
	}



	
	
	@Test
	public void CombinatedIfConditional(){ try{
		subtest = 1;
		code = CTS(/*
			int f(boolean a, boolean b){
				int res = 0;
				if (a) res = res + (b ?   1 :   10);
				 else  res = res + (b ? 100 : 1000);
				return res;
			}
		*/);
		
		//Do a mass test, pass a and b as parameters
		int[] input = new int[2];
		for (int a = 0; a < 2; a++) for (int b = 0; b < 2; b++){
			input[0] = a;
			input[1] = b;
			int res = getResultTimeout(code, input);
			assertEquals(a==1 ? (b==1 ? 1:10) : (b==1 ? 100:1000), res);
		}
		
	}catch(TimeoutException e){
		printerrhead();
		System.out.println("Timeout! (mostly caused by an endless loop in your code. Check your labels.");
		printCode();
		printAsm();
		throw new AssertionError("Timeout in subtest "+subtest);
	}catch(RuntimeException e){
		printerrhead();
		System.out.println(e.getClass().getName()+": "+e.getMessage());
		printCode();
		printAsm();
		throw e;
	}catch(AssertionError e){
		printerrhead();
		System.out.println(e.getMessage());
		printCode();
		printAsm();
		throw e;
	}
	}

}
