public class TestUtils {
	public static void test(String header, boolean testResult) {
		System.out.println("===== " + (testResult ? "   OK  " : "  FAIL ") + " [" + header + "] =====");
	}

	public static void message(String m) {
		System.out.println("\n\ttest: " + m);
	}

	public static void output(String m) {
		System.out.println("\toutput\n\t\t" + m);
	}
}