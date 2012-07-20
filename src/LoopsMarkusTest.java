package prog2.project4.tests.prog2tests;

import static org.junit.Assert.*;

import org.junit.*;


/**
 * 
 * @author Markus
 * 
 * 
 * 
 * Extensive loop testing: 
 * - while and do-while loops
 * - multiple and nested loops
 * - break and continue
 * - break and continue in multiple and nested loops
 * - also checks naming and typing
 *
 */

public class LoopsMarkusTest extends TestBaseMarkus {
	public final String VERSION = "1.0";
	
	// If you don't want that console output, go to tests/TestBase.java, line 39
	// and replace true with false. 
	
	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("LoopsMarkusTest", VERSION);
	}
	
	
	@Test
	public void LoopsNaming(){ 
		// SUBTEST 1
		subtest = 1; code = CTS(/*
			int f(){
				int a=-1;
				while (a < 0){
					int a = 5;
				}
				return a;
			}
		*/);
		assertNoNamingException(code, "no naming exception expected. Check you create a new scope for while body");
		
		
		// SUBTEST 2
		subtest = 2; code = CTS(/*
			int f(){
				int a=-1;
				do{
					int a = 5;
				} while (a < 0);
				return a;
			}
		 */);
		assertNoNamingException(code, "no naming exception expected. Check you create a new scope for do-while body");
		
		
		// SUBTEST 3
		subtest = 3; code = CTS(/*
			int f(){
				int a = 0;
				while(false){
					int a = 0;
					do{
						int a = 0;
						while(true){ int a = 1;}
					} while(true);
				}
				return a;
			}
		*/);
		assertNoNamingException(code, "no naming exception expected. ");
		
		
		
		// SUBTEST 4
		subtest = 4; code = CTS(/*
			int f(){
				int a = 0;
				while(false){
					int a = 0;
					do{}while(true);
					int a = 0;
				}
				return a;
			}
		*/);
		assertNamingException(code, "Naming exception expected, a is defined 2x in the while loop");
		
		
		// SUBTEST 5
		subtest = 5; code = CTS(/*
			int f(){
				int a = 0;
				do{
					int a = 0;
					do{}while(true);
					int a = 0;
				}while(false);
				return a;
			}
		*/);
		assertNamingException(code, "Naming exception expected, a is defined 2x in the do-while loop");
		
		
		// SUBTEST 6
		subtest = 6; code = CTS(/*
			int f(){
				int a = 0;
				while(false){ int a = 0;}
				do{int a = 0;} while (false);
				int a = 0;
				return a;
			}
		*/);
		assertNamingException(code, "Naming exception expected, a is defined 2x in the function body");	
	}
	
	
	
	
	
	
	
	@Test
	public void LoopsTyping(){
		// SUBTEST 1
		//make sure while condition only accepts booleans
		subtest = 1;
		assertTypingException("int f(){ while(0){} return 0;}", 
				"Type exception expected. While condition should only accept boolean values");
		assertNoTypingException("int f(){ while(true){} return 0;}", 
				"No Type exception expected. While condition must accept boolean values");
		
		// SUBTEST 2
		//make sure do-while condition only accepts booleans
		subtest = 2;
		assertTypingException("int f(){ do {} while(0); return 0;}", 
				"Type exception expected. Do-While condition should only accept boolean values");
		assertNoTypingException("int f(){ do {} while(true); return 0;}", 
				"No Type exception expected. Do-While condition must accept boolean values");
		
		
		// SUBTEST 3
		//bindings
		subtest = 3;
		assertNoTypingException(CTS(/*
			int f(){
				int a = 0;
				while(false){
					boolean a = false;
					do{} while(a);
				}
				return a;
			}
		*/), "No Typing exception expected. Check if you use the right scope for bindings in while body. ");
		
		// the same with do-while
		assertNoTypingException(CTS(/*
			int f(){
				int a = 0;
				do{
					boolean a = false;
					do{} while(a);
				}while(false);
				return a;
			}
		 */), "No Typing exception expected. Check if you use the right scope for bindings in do-while body. ");
		
		
		// SUBTEST 4
		//make sure the do-while condition isn't executed in body scope
		subtest = 4;
		assertTypingException(CTS(/*
			int f(){
				int a = 0;
				do{
					boolean a = false;
				} while(a);  // a from function body
				return 0;
			}
		*/), "Typing Exception expected. I guess you executed the do-while condition in the body scope?");
	}
	
	
	
	
	
	
	
	@Test
	public void SimpleLoops(){ try{
		// Checks simple loops - SUBTEST 1
		subtest = 1;
		code = CTS(/*
			int f(){
				int i = 0;
				while (i<5) { i = i+1; }
				return i;
			}
		*/);
		int res = getResult(code, null);
		assertEquals("simple while loop fails (Subtest 1)", 5, res);
		
		
		//SUBTEST 2
		subtest = 2;
		code = CTS(/*
			int f(){
				int i = 0;
				do { i = i+1; } while (i<5);
				return i;
			}
		*/);
		res = getResult(code, null);
		assertEquals("simple do-while loop fails (Subtest 2)", 5, res);
		
		
		// SUBTEST 3
		subtest = 3;
		code = CTS(/*
				int f(){
					int i = 1;
					while (false) { i = i+1; }
					return i;
				}
		 */);
		res = getResult(code, null);
		assertEquals("simple while loop fails (Subtest 3)", 1, res);

		// SUBTEST 4
		subtest = 4;
		code = CTS(/*
				int f(){
					int i = 1;
					do { i = i+1; } while (false);
					return i;
				}
		 */);
		res = getResult(code, null);
		assertEquals("simple do-while loop fails (Subtest 4)", 2, res);
		
		}catch(AssertionError e){
			System.err.println(geterrhead());
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}
	
	
	
	
	
	
	@Test 
	public void MultipleLoops(){
		assertPrint(); try{
		//Checks multiple loops
		
		// SUBTEST 1
		subtest = 1;
		code = CTS(/*
			int f(){
				int a = 0;
				 while (a <= 2){
				 	a = a+1;
				 	print(0);
				 }
				 a = a-10;
				 while (a < 0){
				 	a = a+3;
				 	print(1);
				 }
				 print(a);
				 return a;
			}
		*/);
		int[] out = getOutput(code, null);
		
		assertOutput(0, "You don't correctly enter the first loop.", 0, out);
		assertOutput(1, "You don't repeat first loop after one passing.", 0, out);
		assertOutput(2, 0, out);
		assertOutput(3, "You don't correctly enter second loop.", 1, out);
		assertOutput(4, "You don't repeat second loop after one passing - check the loops have different labels.", 1, out);
		assertOutput(5, 1, out);
		assertOutput(6, "Your result after both loops is incorrect.", 2, out);
		
		
		//Same thing with do-while
		// SUBTEST 2
		subtest = 2;
		code = CTS(/*
			int f(){
				int a = 0;
				 do{
				 	a = a+1;
				 	print(0);
				 } while(a <= 2);
				 a = a-10;
				 do{
				 	a = a+3;
				 	print(1);
				 } while(a<0);
				 print(a);
				 return a;
			}
		*/);
		out = getOutput(code, null);
			
		assertOutput(0, "You don't correctly enter the first loop.", 0, out);
		assertOutput(1, "You don't repeat first loop after one passing.", 0, out);
		assertOutput(2, 0, out);
		assertOutput(3, "You don't correctly enter second loop.", 1, out);
		assertOutput(4, "You don't repeat second loop after one passing - check the loops have different labels.", 1, out);
		assertOutput(5, 1, out);
		assertOutput(6, "Your result after both loops is incorrect.", 2, out);
		
		
		//Mix it!
		// SUBTEST 3
		subtest = 3;
		code = CTS(/*
			int f(){
				int a = 0;
				 while (a <= 2){
				 	a = a+1;
				 	print(0);
				 }
				 a = a-10;
				 do{
				 	a = a+3;
				 	print(1);
				 } while(a<0);
				 print(a);
				 return a;
			}
		 */);
		out = getOutput(code, null);

		assertOutput(0, "You don't correctly enter the first loop.", 0, out);
		assertOutput(1, "You don't repeat first loop after one passing.", 0, out);
		assertOutput(2, 0, out);
		assertOutput(3, "You don't correctly enter second loop.", 1, out);
		assertOutput(4, "You don't repeat second loop after one passing - check the loops have different labels.", 1, out);
		assertOutput(5, 1, out);
		assertOutput(6, "Your result after both loops is incorrect.", 2, out);



		// SUBTEST 4
		subtest = 4;
		code = CTS(/*
			int f(){
				int a = 0;
				do{
					a = a+1;
				 	print(0);
				} while(a <= 2);
				a = a-10;
				while(a<0){
					a = a+3;
					print(1);
				}
				print(a);
				return a;
			}
		 */);
		out = getOutput(code, null);

		assertOutput(0, "You don't correctly enter the first loop.", 0, out);
		assertOutput(1, "You don't repeat first loop after one passing.", 0, out);
		assertOutput(2, 0, out);
		assertOutput(3, "You don't correctly enter second loop.", 1, out);
		assertOutput(4, "You don't repeat second loop after one passing - check the loops have different labels.", 1, out);
		assertOutput(5, 1, out);
		assertOutput(6, "Your result after both loops is incorrect.", 2, out);
		
		
		}catch(AssertionError e){
			System.err.println(geterrhead());
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}
	
	
	
	
	
	
	@Test
	public void LoopsInLoops(){
		assertPrint();
		
		try{
		
		//While in while - SUBTEST 1
		subtest = 1; 
		code = CTS(/*
		int f(){
			int a = 10;
			while (a < 50){
				a = a+10;
				int b = 1;
				while (b < 3){
					b = b+1;
					print(a+b);
				}
				a = a+10;
			}
			return a;
		}
		*/);
		int[] out = getOutput(code, null);
		int[] exp = {22, 23, 42, 43};
		assertArrayEquals("while loop in while loop", exp, out);
		
		
		//While in do-while - SUBTEST 2
		subtest = 2;
		code = CTS(/*
				int f(){
					int a = 10;
					do{
						a = a+10;
						int b = 1;
						while (b < 3){
							b = b+1;
							print(a+b);
						}
						a = a+10;
					}while (a < 50);
					return a;
				}
		 */);
		out = getOutput(code, null);
		assertArrayEquals("while loop in do-while loop", exp, out);


		//Do-while in while - SUBTEST 3
		subtest = 3;
		code = CTS(/*
				int f(){
					int a = 10;
					while (a < 50){
						a = a+10;
						int b = 1;
						do {
							b = b+1;
							print(a+b);
						} while (b < 3);
						a = a+10;
					}
					return a;
				}
		 */);
		out = getOutput(code, null);
		assertArrayEquals("do-while loop in while loop", exp, out);

		
		//do-while in do-while - SUBTEST 4
		subtest = 4;
		code = CTS(/*
				int f(){
					int a = 10;
					do{
						a = a+10;
						int b = 1;
						do{
							b = b+1;
							print(a+b);
						} while (b<3);
						a = a+10;
					} while (a < 50);
					return a;
				}
		 */);
		out = getOutput(code, null);
		assertArrayEquals("do-while loop in do-while loop", exp, out);
		
		
		}catch(AssertionError e){
			System.err.println(geterrhead());
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
		
	}
	
	
	
	
	
	@Test
	public void BreakContinueSimple(){ try{
		subtest = 1;
		code = CTS(/*
			int f(){
				int i = 0;
				int r = 0;
				while (i < 2){
					r = r+1;
					i = i+1;
					break;
					r = r+2;
				}
				r = r+8;
				return r;
			}
		*/);
		assertEquals("break doesn't work", 9, getResult(code, null));
		
		
		subtest = 2;
		code = CTS(/*
				int f(){
					int i = 0;
					int r = 0;
					while (i < 2){
						r = r+1;
						i = i+1;
						continue;
						r = r+4;
					}
					r = r+16;
					return r;
				}
		 */);
		assertEquals("continue doesn't work", 18, getResult(code, null));

		subtest = 3;
		code = CTS(/*
				int f(){
					int i = 0;
					int r = 0;
					do{
						r = r+1;
						i = i+1;
						break;
						r = r+2;
					}while(i < 2);
					r = r+8;
					return r;
				}
		 */);
		assertEquals("break doesn't work with do-while", 9, getResult(code, null));
			
		
		subtest = 4;
		code = CTS(/*
				int f(){
					int i = 0;
					int r = 0;
					do {
						r = r+1;
						i = i+1;
						continue;
						r = r+4;
					} while (i < 2);
					r = r+16;
					return r;
				}
		 */);
		assertEquals("continue doesn't work with do-while", 18, getResult(code, null));
		
		}catch(AssertionError e){
			System.err.println(geterrhead());
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}
	
	
	
	
	
	@Test
	public void BreakContinueMultipleLoops(){ try{
		
		// SUBTEST 1
		//continue with multiple while loops
		subtest = 1;
		code = CTS(/*	
			int f(){
				int r = 0;
				int c1 = 0;     // Counter first loop
				while (c1 < 2){
					c1 = c1+1;
					r = r+1;
					continue;
					r = r+10;
				}
				if (r < 10000) c1 = 0;
				r = r+10000;
				int c2 = 0;     // Counter second loop
				while (c2 < 3){
					c2 = c2+1;
					r = r+100;
					continue;
					r = r+1000;
				}
				return r;
			}
		*/);
		int res = getResult(code, null);
		if (res == 301) assertEquals("Continue in first loop seems to jump to second loop", 10302, res);
		if (res % 100 == 4) assertEquals("Continue in second loop seems to jump back to first loop", 10302, res);
		assertEquals("First loop is passed too often", 2, res % 10);
		assertEquals("Continue in first loop doesn't terminate execution", 0, (res/10) % 10);
		assertEquals("Second loop is passed too often", 3, (res/100) % 10);
		assertEquals("Continue in second loop doesn't terminate execution", 0, (res/1000) % 10);
		assertEquals("Code between the two loops is executed too often", 1, (res/10000));
		assertEquals("Continue doesn't work with two while loops", 10302, res);
		
		
		// SUBTEST 2
		//continue with multiple do-while loops
		subtest = 2;
		code = CTS(/*	
			int f(){
				int r = 0;
				int c1 = 0;   // Counter first loop
				do{
					c1 = c1+1;
					r = r+1;
					continue;
					r = r+10;
				} while(c1<2);
				
				if (r < 10000) c1 = 0;
				r = r+10000;
				int c2 = 0;    // Counter second loop
				
				do{
					c2 = c2+1;
					r = r+100;
					continue;
					r = r+1000;
				} while (c2<3);
				return r;
			}
		 */);
		res = getResult(code, null);
		if (res == 301) assertEquals("Continue in first loop seems to jump to second loop", 10302, res);
		if (res % 100 == 4) assertEquals("Continue in second loop seems to jump back to first loop", 10302, res);
		assertEquals("First loop is passed too often", 2, res % 10);
		assertEquals("Continue in first loop doesn't terminate execution", 0, (res/10) % 10);
		assertEquals("Second loop is passed too often", 3, (res/100) % 10);
		assertEquals("Continue in second loop doesn't terminate execution", 0, (res/1000) % 10);
		assertEquals("Code between the two loops is executed too often", 1, (res/10000));
		assertEquals("Continue doesn't work with two do-while loops", 10302, res);
		
		
		
		
		// SUBTEST 3
		//continue with while and do-while loops
		subtest = 3;
		code = CTS(/*	
			int f(){
				int r = 0;
				int c1 = 0;   // Counter first loop
				while (c1 < 2){
					c1 = c1+1;
					r = r+1;
					continue;
					r = r+10;
				} 
					if (r < 10000) c1 = 0;
				r = r+10000;
				int c2 = 0;    // Counter second loop
					do{
					c2 = c2+1;
					r = r+100;
					continue;
					r = r+1000;
				} while (c2<3);
				return r;
			}
		 */);
		res = getResult(code, null);
		if (res == 301) assertEquals("Continue in first loop seems to jump to second loop", 10302, res);
		if (res % 100 == 4) assertEquals("Continue in second loop seems to jump back to first loop", 10302, res);
		assertEquals("First loop is passed too often", 2, res % 10);
		assertEquals("Continue in first loop doesn't terminate execution", 0, (res/10) % 10);
		assertEquals("Second loop is passed too often", 3, (res/100) % 10);
		assertEquals("Continue in second loop doesn't terminate execution", 0, (res/1000) % 10);
		assertEquals("Code between the two loops is executed too often", 1, (res/10000));
		assertEquals("Continue doesn't work with mixed loops", 10302, res);
		
		
		
		
		// SUBTEST 4
		//break with multiple while loops
		subtest = 4;
		code = CTS(/*
			int f(){
				int r = 0;
				int c1 = 0;   // Counter first loop
				while (c1 < 2){
					c1 = c1+1;
					r = r+1;
					break;
					r = r+10;
				} 
				
				if (r < 10000) c1 = 0;
				r = r+10000;
				int c2 = 0;    // Counter second loop
				
				while (c2 < 3){
					c2 = c2+1;
					r = r+100;
					break;
					r = r+1000;
				} 
				return r;
			}
		*/);
		res = getResult(code, null);
		if (res == 10302) assertEquals("Your break works like continue!", 10101, res);
		if (res == 1) assertEquals("Frist break jumps to end of second loop", 10101, res);
		if (res == 40301) assertEquals("Second break jumps to end of first loop", 10101, res);
		assertEquals("First loop executed too often", 1, res % 10);
		assertEquals("break in first loop doesn't terminate loop execution", 0, (res / 10) % 10);
		assertEquals("Second loop executed too often", 1, (res / 100) % 10);
		assertEquals("break in second loop doesn't terminate loop execution", 0, (res / 1000) % 10);
		assertEquals("code between the loops executed too often", 1, (res / 10000) % 10);
		assertEquals("break doesn't work with two while loops", 10101, res);
		
		
		
		// SUBTEST 5
		//break with multiple do-while loops
		subtest = 5;
		code = CTS(/*
			int f(){
				int r = 0;
				int c1 = 0;   // Counter first loop
				do{
					c1 = c1+1;
					r = r+1;
					break;
					r = r+10;
				} while (c1 < 2);
				
				if (r < 10000) c1 = 0;
				r = r+10000;
				int c2 = 0;    // Counter second loop
				
				do{
					c2 = c2+1;
					r = r+100;
					break;
					r = r+1000;
				} while (c2 < 3);
				return r;
			}
		 */);
		res = getResult(code, null);
		if (res == 10302) assertEquals("Your break works like continue!", 10101, res);
		if (res == 1) assertEquals("Frist break jumps to end of second loop", 10101, res);
		if (res == 40301) assertEquals("Second break jumps to end of first loop", 10101, res);
		assertEquals("First loop executed too often", 1, res % 10);
		assertEquals("break in first loop doesn't terminate loop execution", 0, (res / 10) % 10);
		assertEquals("Second loop executed too often", 1, (res / 100) % 10);
		assertEquals("break in second loop doesn't terminate loop execution", 0, (res / 1000) % 10);
		assertEquals("code between the loops executed too often", 1, (res / 10000) % 10);
		assertEquals("break doesn't work with two do-while loops", 10101, res);
		
		
		
		
		// SUBTEST 6
		//break with mixed loops
		subtest = 6;
		code = CTS(/*
			int f(){
				int r = 0;
				int c1 = 0;   // Counter first loop
				while (c1 < 2){
					c1 = c1+1;
					r = r+1;
					break;
					r = r+10;
				} 

				if (r < 10000) c1 = 0;
				r = r+10000;
				int c2 = 0;    // Counter second loop

				do{
					c2 = c2+1;
					r = r+100;
					break;
					r = r+1000;
				} while (c2 < 3);
				return r;
			}
		 */);
		res = getResult(code, null);
		if (res == 10302) assertEquals("Your break works like continue!", 10101, res);
		if (res == 1) assertEquals("Frist break jumps to end of second loop", 10101, res);
		if (res == 40301) assertEquals("Second break jumps to end of first loop", 10101, res);
		assertEquals("First loop executed too often", 1, res % 10);
		assertEquals("break in first loop doesn't terminate loop execution", 0, (res / 10) % 10);
		assertEquals("Second loop executed too often", 1, (res / 100) % 10);
		assertEquals("break in second loop doesn't terminate loop execution", 0, (res / 1000) % 10);
		assertEquals("code between the loops executed too often", 1, (res / 10000) % 10);
		assertEquals("break doesn't work with mixed loops", 10101, res);
		
		
		}catch(AssertionError e){
			System.err.println(geterrhead());
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}
	
	
	
	
	
	
	
	@Test
	public void BreakContinueNestedLoops(){
		assertPrint(); try{
			

		// SUBTEST 1
		//break in while loops
		subtest = 1;
		code = CTS(/*
			int f(){
				int c = 0;
				int i = 0;
				while (c < 20){
					c = c+4;
					while (i < c){
						i = i+1;
						if (i == 2) break;
					}
					print(c*100 + i);
					if (c == 12) break;
				}
				print(c*100+i);
				return 0;
			}
		*/);
		int[] out = getOutput(code, null);
		if (out.length == 0){
			throw new AssertionError("program terminates before print commands");
		}
		int res = out[out.length-1];
		int[] exp = {402, 808, 1212, 1212};
		if (res == 402) assertEquals("break in inner loop jumps out of outer loop. make sure you use different labels", 1212, res);
		if (res / 100 > 12) assertEquals("outer break doesn't work", 1212, res);
		assertArrayEquals("break doesn't work in multiple loops. Make sure you use different labels for nested loops and break jumps to the right one", exp, out);
		
		
		// SUBTEST 2
		//break in do-while loops
		subtest = 2;
		code = CTS(/*
			int f(){
				int c = 0;
				int i = 0;
				do{
					c = c+4;
					do{
						i = i+1;
						if (i == 2) break;
					} while (i < c);
					print(c*100 + i);
					if (c == 12) break;
				} while (c < 20);
				print(c*100+i);
				return 0;
			}
		 */);
		out = getOutput(code, null);
		if (out.length == 0){
			throw new AssertionError("program terminates before print commands");
		}
		res = out[out.length-1];
		if (res == 402) assertEquals("break in inner loop jumps out of outer loop. make sure you use different labels", 1212, res);
		if (res / 100 > 12) assertEquals("outer break doesn't work", 1212, res);
		assertArrayEquals("break doesn't work in multiple loops. Make sure you use different labels for nested loops and break jumps to the right one", exp, out);
		
		
		
		
		// SUBTEST 3
		//break in mixed loops
		subtest = 3;
		code = CTS(/*
			int f(){
				int c = 0;
				int i = 0;
				do{
					c = c+4;
					while (i < c){
						i = i+1;
						if (i == 2) break;
					}
					print(c*100 + i);
					if (c == 12) break;
				} while (c < 20);
				print(c*100+i);
				return 0;
			}
		 */);
		out = getOutput(code, null);
		if (out.length == 0){
			throw new AssertionError("program terminates before print commands are executed");
		}
		res = out[out.length-1];
		if (res == 402) assertEquals("break in inner loop jumps out of outer loop. make sure you use different labels", 1212, res);
		if (res / 100 > 12) assertEquals("outer break doesn't work", 1212, res);
		assertArrayEquals("break doesn't work in multiple loops. Make sure you use different labels for nested loops and break jumps to the right one", exp, out);
		
		
		
		// SUBTEST 4
		//continue in nested while loops
		subtest = 4;
		code = CTS(/*
			int f(){
				int r = 0;
				int c1 = 0;
				int c2 = 0;
				
				while (c1 < 2){
					r = r+1;
					c1 = c1+1;
					while (c2 < c1*3){
						c2 = c2+1;
						r = r + 100;
						continue;
						r = r+1000;
					}
					r = r+1;
					if (r == 8) break; //avoid endless loops if continue jumps to inner loop
					continue;
					r = r+10;
				}
				return r;				
			}
		*/);
		res = getResult(code, null);
		if (res % 10 == 2) assertEquals("inner continue aborts and goes to outer loop. make sure you use the correct labels", 0604, res);
		if (res % 10 == 8) assertEquals("outer continue goes to condition of inner loop. make sure you use the correct labels", 604, res);
		assertEquals("outer loop is executed too often or too few", 4, res % 10);
		assertEquals("outer continue has no effect", 0, (res / 10) % 10);
		assertEquals("inner loop is executed too often or too few", 6, (res / 100) %10);
		assertEquals("inner continue has no effect", 0, (res / 1000) % 10);
		assertEquals("continue doesn't work for nested loops", 604, res);
		
		
		// SUBTEST 5
		//continue in nested do-while loops
		subtest = 5;
		code = CTS(/*
			int f(){
				int r = 0;
				int c1 = 0;
				int c2 = 0;

				do{
					r = r+1;
					c1 = c1+1;
					do{
						c2 = c2+1;
						r = r + 100;
						continue;
						r = r+1000;
					} while (c2 < c1*3);
					r = r+1;
					if (r == 8) break; //avoid endless loops if continue jumps to inner loop
					continue;
					r = r+10;
				} while (c1 < 2);
				return r;				
			}
		 */);
		res = getResult(code, null);
		if (res % 10 == 2) assertEquals("inner continue aborts and goes to outer loop. make sure you use the correct labels", 0604, res);
		if (res % 10 == 8) assertEquals("outer continue goes to condition of inner loop. make sure you use the correct labels", 604, res);
		assertEquals("outer loop is executed too often or too few", 4, res % 10);
		assertEquals("outer continue has no effect", 0, (res / 10) % 10);
		assertEquals("inner loop is executed too often or too few", 6, (res / 100) %10);
		assertEquals("inner continue has no effect", 0, (res / 1000) % 10);
		assertEquals("continue doesn't work for nested loops", 604, res);
		
		
		// SUBTEST 6
		//continue in nested, mixed loops
		subtest = 6;
		code = CTS(/*
			int f(){
				int r = 0;
				int c1 = 0;
				int c2 = 0;

				do{
					r = r+1;
					c1 = c1+1;
					while (c2 < c1*3){
						c2 = c2+1;
						r = r + 100;
						continue;
						r = r+1000;
					} 
					r = r+1;
					if (r == 8) break; //avoid endless loops if continue jumps to inner loop
					continue;
					r = r+10;
				} while (c1 < 2);
				return r;				
			}
		 */);
		res = getResult(code, null);
		if (res % 10 == 2) assertEquals("inner continue aborts and goes to outer loop. make sure you use the correct labels", 0604, res);
		if (res % 10 == 8) assertEquals("outer continue goes to condition of inner loop. make sure you use the correct labels", 604, res);
		assertEquals("outer loop is executed too often or too few", 4, res % 10);
		assertEquals("outer continue has no effect", 0, (res / 10) % 10);
		assertEquals("inner loop is executed too often or too few", 6, (res / 100) %10);
		assertEquals("inner continue has no effect", 0, (res / 1000) % 10);
		assertEquals("continue doesn't work for nested loops", 604, res);	
		
		
		}catch(AssertionError e){
			System.err.println(geterrhead());
			System.out.println(e.getMessage());
			printCode();
			printAsm();
			throw e;
		}
	}
	
}
