package prog2.project4.tests.prog2tests;

import static org.junit.Assert.fail;

import org.junit.Test;

import prog2.project4.tests.prog2tests.StatisticsReporterTool;
import prog2.project4.tests.prog2tests.StatisticsReporterTool.Reporter;

public class StatisticsReporterToolTest {
	@Test
	public void test_Update() {
		CompilerTestUpdateTool.doUpdateTest("StatisticsReporterToolTest", "1.0");
	}

	@Test
	public void testWithFail() {
		// Usual header
		StatisticsReporterTool.withReport("StatisticsReportToolTest.test()",
				new Reporter() {
					@Override
					public void run(StatisticsReporterTool report) {
						// Here would your code go
						fail("Not yet implemented");
					}
				});
	}

	@Test
	public void testEspeciallyComplex() {
		// Usual header
		StatisticsReporterTool.withReport(
				"StatisticsReportToolTest.testEspeciallyComplex()",
				new Reporter() {
					@Override
					public void run(StatisticsReporterTool report) {
						System.out.println("Running...");
						// Actual testing
						report.put("pi", Math.PI);
						long piLong = Math.round(Math.PI);
						report.put("piLong", piLong);
						if (piLong == 3) {
							report.put("Pi ist genau drei!!!!", "Oh shi-");
						}
					}
				});
	}
}
