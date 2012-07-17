package prog2.project4.tests.prog2tests;

//import org.junit.Test;

import java.util.Iterator;

import org.junit.Test;

import prog2.project4.parser.Token;
import prog2.project4.tree.Tree;

public class TestUtilBewied {
	// ===== Constants

	private static final String NULL = "null";

	// ===== Object To String conversion

	public static final String toString(Token t) {
		StringBuilder sb = new StringBuilder();
		append(sb, t);
		return sb.toString();
	}

	public static final String toString(Tree t) {
		StringBuilder sb = new StringBuilder();
		append(sb, t);
		return sb.toString();
	}

	public static final void append(StringBuilder sb, Token t) {
		if (t == null) {
			System.out.println("============= NULL token! =============");
			sb.append(NULL);
			return;
		}
		sb.append(t.getType());
		sb.append(':');
		sb.append(t.getText());
	}

	public static final void append(StringBuilder sb, Tree t) {
		if (t == null) {
			sb.append(NULL);
			return;
		}
		sb.append(t.getClass().getSimpleName());
		sb.append('[');
		append(sb, t.getToken());
		sb.append(']');
		if (t instanceof CustomizedAppendable) {
			sb.append('{');
			((CustomizedAppendable) t).customizedAppend(sb);
			sb.append('}');
		}
		sb.append('(');
		Iterator<Tree> it = t.iterator();
		if (it.hasNext()) {
			append(sb, it.next());
			while (it.hasNext()) {
				sb.append(',');
				sb.append(' ');
				append(sb, it.next());
			}
		}
		sb.append(')');
	}

	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("TestUtilBewied", "1.0.0.0.1");
	}

	public static interface CustomizedAppendable {
		void customizedAppend(final StringBuilder sb);
	}
}
