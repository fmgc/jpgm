import jpgm.IntArray;
import jpgm.Lambda;
import java.util.Random;
import java.lang.StringBuffer;

public class IntArrayTest {

	public static void main(String[] args) {
		test("start testing IntArray", true);
		test("basic operations", testBasicOps());
		test("codec operations", testCodecOps());
		test("index operations", testIndexOps());
		test("array operations", testArrayOps());
		test("set operations", testSetOps());
		test("constructors", testConstructors());
		test("functional", testFunctional());
	}
	
	public static boolean testFunctional() {
		IntArray a = IntArray.range(10);

		message("map");
		message("apply lambda.x(2x+1) to each element of " + a);
		output("2*a+1 = "+a.map(
			new Lambda<Integer,Integer>() {
				public Integer at(Integer x) {
					return 2*x + 1;
				}
			}
		));

		message("filter");
		message("select even elements of " + a);
		output("a | even = "+a.filter(
			new Lambda<Integer,Boolean>() {
				public Boolean at(Integer x) {
					return (x % 2 == 0);
				}
			}
		));

		message("all");
		message("test all elements of " + a);
		output("all a < 100 ? "+a.all(
			new Lambda<Integer,Boolean>() {
				public Boolean at(Integer x) {
					return (x < 100);
				}
			}
		));
		output("all a < 5 ? "+a.all(
			new Lambda<Integer,Boolean>() {
				public Boolean at(Integer x) {
					return (x < 5);
				}
			}
		));

		message("any");
		message("test some elements of " + a);
		output("any a < 5 ? "+a.any(
			new Lambda<Integer,Boolean>() {
				public Boolean at(Integer x) {
					return (x < 5);
				}
			}
		));
		output("any a < 0 ? "+a.any(
			new Lambda<Integer,Boolean>() {
				public Boolean at(Integer x) {
					return (x < 0);
				}
			}
		));

		return true;
	}
	
	public static boolean testConstructors() {

		message("range(a,b,c)");
		output("range(3,12,4):" + IntArray.range(3,12,4));
		output("range(3,11,4):" + IntArray.range(3,11,4));
		output("range(3,10,4):" + IntArray.range(3,10,4));

		output("range(3,12):" + IntArray.range(3,12));
		output("range(3,11):" + IntArray.range(3,11));
		output("range(3,10):" + IntArray.range(3,10));

		output("range(12):" + IntArray.range(12));
		output("range(11):" + IntArray.range(11));
		output("range(10):" + IntArray.range(10));

		message("project");
		IntArray[] a = {
			IntArray.range(10),
			IntArray.range(1,11),
			IntArray.range(2,33,3)
		};
		message("a[0]: " + a[0]);
		message("a[1]: " + a[1]);
		message("a[2]: " + a[2]);
		output("IntArray.project(a,4)" + IntArray.project(a,4));

		return true;
	}
	
	public static boolean testSetOps() {
		IntArray a = new IntArray(1,4,5,4,5,1,5,1,4);

		message("unique elemens in a = " + a);
		output("a.unique() = " + a.unique());

		a = new IntArray(1,9, 4,5,7,2);
		IntArray b = new IntArray(10,5,44,1);

		message("exclude from a = " + a + " elements in b = " + b);
		IntArray c = a.exclude(b);
		output("a.exclude(b) = " + c);

		message("exclude from b = " + b + " elements in a = " + a);
		c = b.exclude(a);
		output("b.exclude(a) = " + c);

		message("intersection of a = " + a + " with b = " + b);
		output("a.intersection(b) = " + a.intersection(b));

		message("union of a = " + a + " with b = " + b);
		output("a.union(b) = " + a.union(b));

		return true;
	}

	public static boolean testArrayOps() {
		IntArray a = new IntArray(1,9, 4,5,7,2);
		IntArray b = new IntArray(10,5,44,1);

		message("sorting " + a);
		output("sorted: " + a.sorted());

		message("reversing " + a);
		output("reversed: " + a.reversed());

		message("append a = " + a + " with b = " + b);
		IntArray c = a.append(b);
		output("a.append(b) = " + c);

		message("append b = " + b + " with a = " + a);
		c = b.append(a);
		output("b.append(a) = " + c);

		message("zip of a with b");
		IntArray[] z = a.zip(b);
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < z.length; i++) {
			s.append(z[i].format("%2d ") +";");
		}
		output(s.toString());

		message("zip of b with a");
		z = b.zip(a);
		s = new StringBuffer();
		for (int i = 0; i < z.length; i++) {
			s.append(z[i].format("%2d ") +";");
		}
		output(s.toString());

		return true;
	}

	public static boolean testIndexOps() {
		IntArray a = new IntArray(1,4,5,7,2,8,3,9,0);

		message("searching in a = " + a);
		boolean testOk = true;
		for (int i = 0; i < a.size(); i++) {
			int x = a.get(i);
			testOk = i == a.search(x);
			if (!testOk) {
				break;
			}
		}
		output("search of all elements: " + (testOk ? "PASS" : "FAIL"));

		IntArray b = new IntArray(10,2,4,11,-1,8);
		message("searching elements of " + b);
		IntArray ai = a.search(b);
		output("indexes:" + ai);

		IntArray i = new IntArray(1,1,-1,30,3,2,4,1);
		message("composition of a = " + a + " with i = " + i);
		output("a.c(i) = " + a.c(i));

		return true;
	}

	public static boolean testCodecOps() {
		IntArray d = new IntArray(3,2,4,2);
		message("domain dimentions");
		output("d = " + d);

		message("multiplication of elements");
		output("d.mul() = " + d.mul());

		message("coordinate decode/encode");
		Random rand = new Random();
		int numTests = 1000;
		boolean testOk = true;
		for (int i = 0; i < numTests; i++) {
			int index = rand.nextInt(d.mul());
			IntArray x = d.dec(index);
			int j = d.enc(x);
			testOk = j == index;
			if (!testOk) {
				break;
			}
		}
		output(numTests + " tests 'dec -> enc -> dec': " + (testOk ? "PASS" : "FAIL"));

		return true;
	}

	public static boolean testBasicOps() {

		message("constructor");
		IntArray a = new IntArray(2,4,6,8);

		message("size");
		output("a.size() =\t"+a.size());

		message("convert to string");
		output("a =\t" + a);

		message("get element");
		output("a[2] =\t" + a.get(2));

		message("set element");
		a.set(2, 100);
		output("a.set(2,100):\t" + a);

		message("format");
		output("a.format(\"%4d\")=\t" + a.format("%4d"));

		message("equals");
		IntArray b = new IntArray(2,4,100,8);
		output(""+a+" == "+b+": "+ a.equals(b));		
		b = new IntArray(2,100,4,8);
		output(""+a+" == "+b+": "+ a.equals(b));
		b = new IntArray(2,4);
		output(""+a+" == "+b+": "+ a.equals(b));
		b = new IntArray(2,4,100,8,1,2,3);
		output(""+a+" == "+b+": "+ a.equals(b));

		message("values iterator");
		for (int i : a.values()) {
			output(""+i);
		}

		return true;
	}

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