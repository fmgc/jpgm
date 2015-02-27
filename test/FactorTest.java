import jpgm.IntArray;
import jpgm.Lambda;
import jpgm.Factor;

import java.util.Random;
import java.util.HashMap;
import java.lang.StringBuffer;

public class FactorTest {

	public static void main(String[] args) {
		//TestUtils.test("constructors and access methods", testConstructors());
		TestUtils.test("algebra methods", testAlgebra());
		TestUtils.test("distribution methods", testDistribution());
		//TestUtils.test("gold miners factors: sensor model", testGMSensor());
	}

	//
	//	TEST GOLD MINERS
	//
	/*
	vars in S: 1XX
	vars in S': 2XX
	vars in Y: 3XX

	values of sensor: m:0, o:1, e:2, g:3

	perception error prob: Theta1
	*/
	public static boolean testGMSensor() {

		int M = 0;
		int O = 1;
		int E = 2;
		int G = 3;

		double THETA1 = 0.2;
		double cp = 1.0 - THETA1;
		double wp = cp/3.0;

		TestUtils.message("Factor for perception of m:");
		Factor mY = new Factor(
			new IntArray(300,200),
			new IntArray(4,4),
			new int[][] {
				{M,M}, 
				{M,O},
				{M,E},
				{M,G}
			},
			new double[] {
				cp,
				wp,
				wp,
				wp
			} );
		TestUtils.output("P(Y_i=m|S'_i)\n" + mY);

		TestUtils.message("Factor for perception of o:");
		Factor oY = new Factor(
			new IntArray(300,200),
			new IntArray(4,4),
			new int[][] {
				{O,M}, 
				{O,O},
				{O,E},
				{O,G}
			},
			new double[] {
				wp,
				cp,
				wp,
				wp
			} );		
		TestUtils.output("P(Y_i=o|S'_i)\n" + oY);

		TestUtils.message("Factor for perception of e:");
		Factor eY = new Factor(
			new IntArray(300,200),
			new IntArray(4,4),
			new int[][] {
				{E,M}, 
				{E,O},
				{E,E},
				{E,G}
			},
			new double[] {
				wp,
				wp,
				cp,
				wp
			} );
		TestUtils.output("P(Y_i=e|S'_i)\n" + eY);

		TestUtils.message("Factor for perception of g:");
		Factor gY = new Factor(
			new IntArray(300,200),
			new IntArray(4,4),
			new int[][] {
				{G,M}, 
				{G,O},
				{G,E},
				{G,G}
			},
			new double[] {
				wp,
				wp,
				wp,
				cp
			} );
		TestUtils.output("P(Y_i=g|S'_i)\n" + gY);

		TestUtils.message("Perception factor:");
		Factor Y = mY.extend(oY).extend(eY).extend(gY);
		TestUtils.output("P(Y_i|S'_i)\n" + Y);
		return true;
	}

	//
	//	TEST DISTRIBUTION
	//
	public static boolean testDistribution() {
		Factor a = new Factor(
			new IntArray(11,12),
			new IntArray(4,2),
			new int[][] {
				{2,0}, 
				{1,1}
			},
			new double[] {
				10.0,
				100.0
			}
		);

		Factor b = new Factor(
			new IntArray(21,23,12),
			new IntArray(2,3,2),
			new int[][] {
				{0,0,1},
				{1,2,0},
				{0,1,0},
				{1,0,1}
			},
			new double[] {
				0.1,
				0.2,
				0.4,
				0.6,
			}
		);

		Factor d = a.mul(b);
		TestUtils.message("unnormalized factor with Z = " + d.Z());
		TestUtils.output("F\n" + d);
		d.normalize();
		TestUtils.message("normalized factor with Z = " + d.Z());
		TestUtils.output("D\n" + d);

		Factor m = (Factor)d.marginal(21,23);
		TestUtils.message("marginal of vars 21,23");
		TestUtils.output("M = D.marginal(21,23)\n"+m);

		Factor n = (Factor)d.marginal(11,12);
		TestUtils.message("marginal of vars 11,12");
		TestUtils.output("N = D.marginal(11,12)\n"+n);

		TestUtils.message("query D\n"+d);
		TestUtils.output("D(11|21=0)\n" + d.query(
			new IntArray(11), 
			new IntArray(21),
			new IntArray(0)
		));
		TestUtils.output("D(21|11=2)\n" + d.query(
			new IntArray(21), 
			new IntArray(11),
			new IntArray(2)
		));
		TestUtils.output("D(21|11=2, 23 = 1)\n" + d.query(
			new IntArray(21), 
			new IntArray(11,23),
			new IntArray(2,1)
		));
		TestUtils.output("D(21|11=2, 23 = 0)\n" + d.query(
			new IntArray(21), 
			new IntArray(11,23),
			new IntArray(2,0)
		));
		TestUtils.output("D(21,12|11=2, 23 = 1)\n" + d.query(
			new IntArray(21,12), 
			new IntArray(11,23),
			new IntArray(2,1)
		));
		TestUtils.output("D(21,12|11=2)\n" + d.query(
			new IntArray(21,12), 
			new IntArray(11),
			new IntArray(2)
		));
		TestUtils.output("D(21,44|11=2)\n" + d.query(
			new IntArray(21,44), 
			new IntArray(11),
			new IntArray(2)
		));
		TestUtils.output("D(44|11=2)\n" + d.query(
			new IntArray(44), 
			new IntArray(11),
			new IntArray(2)
		));
		TestUtils.output("D(|11=2)\n" + d.query(
			new IntArray(), 
			new IntArray(11),
			new IntArray(2)
		));
		TestUtils.output("D(|44=2)\n" + d.query(
			new IntArray(), 
			new IntArray(44),
			new IntArray(2)
		));
		TestUtils.output("D(|)\n" + d.query(
			new IntArray(), 
			new IntArray(),
			new IntArray()
		));

		TestUtils.message("draw 1000 samples");
		d.normalize();

		HashMap<String,Integer> count = new HashMap<String,Integer>();
		HashMap<String,IntArray> samples = new HashMap<String,IntArray>();
		for (int k = 0; k < 1000; k++) {
			IntArray sample = d.draw();
			String s = sample.toString();
			if (count.containsKey(s)) count.put(s, count.get(s)+1);
			else {
				count.put(s,1);
				samples.put(s,sample);
			}
		}
		for (String k : count.keySet()) {
			TestUtils.output("\tsample frequency of " + k + ": " +
				((double)count.get(k))/1000.0 + "; \t real: " + d.get(samples.get(k)));
		}

		TestUtils.output("Original join\nD\n" +  d);
		TestUtils.message("conditionalize on 11");
		TestUtils.output("D(|11)\n"+d.cond(new IntArray(11)));

		TestUtils.message("conditionalize on 21");
		TestUtils.output("D(-|21)\n"+d.cond(new IntArray(21)));

		TestUtils.message("conditionalize on 21,12");
		TestUtils.output("D(-|21,12)\n"+d.cond(new IntArray(21,12)));

		TestUtils.message("conditionalize on 21,23");
		TestUtils.output("D(-|21,23)\n"+d.cond(new IntArray(21,23)));

		TestUtils.message("conditionalize on 11,12");
		TestUtils.output("D(-|11,12)\n"+d.cond(new IntArray(11,12)));

		return true;
	}


	//
	//	TEST ALGEBRA
	//
	public static boolean testAlgebra() {
		Factor a = new Factor(
			new IntArray(11,12),
			new IntArray(4,2),
			new int[][] {
				{2,0}, 
				{1,1}
			},
			new double[] {
				10.0,
				100.0
			}
		);

		Factor b = new Factor(
			new IntArray(12,23,21),
			new IntArray(2,3,2),
			new int[][] {
				{0,0,1},
				{1,2,0},
				{0,1,0}
			},
			new double[] {
				0.1,
				0.2,
				0.4
			}
		);

		TestUtils.output("A:\n"+a);
		TestUtils.output("B:\n"+b);

		Factor c = a.mul(b);
		TestUtils.output("C = AxB:\n"+c);

		Factor d = a.add(b);
		TestUtils.output("D = A+B:\n"+d);

		TestUtils.message("Testing example in AIMA");

		a = new Factor(
			new IntArray(11,12),
			new IntArray(2,2),
			new int[][] {
				{1,1}, {1,0},
				{0,1}, {0,0},
			},
			new double[] {
				0.3, 0.7,
				0.9, 0.1
			}
		);

		b = new Factor(
			new IntArray(12,13),
			new IntArray(2,2),
			new int[][] {
				{1,1}, {1,0},
				{0,1}, {0,0},
			},
			new double[] {
				0.2, 0.8,
				0.6, 0.4
			}
		);

		TestUtils.output("A:\n"+a);
		TestUtils.output("B:\n"+b);

		c = a.mul(b);
		TestUtils.output("C = AxB:\n"+c);

		d = a.add(b);
		TestUtils.output("D = A+B:\n"+d);

		return true;
	}


	//
	//	TEST CONSTRUCTORS
	//
	public static boolean testConstructors() {

		TestUtils.message("from instances");
		Factor a = new Factor(
			new IntArray(11,12),
			new IntArray(4,2),
			new int[][] {
				{2,0}, 
				{1,1}
			},
			new double[] {
				0.6,
				0.3
			}
		);

		TestUtils.output("vars: " + a.vars());
		TestUtils.output("domains: " + a.domains());
		StringBuffer s = new StringBuffer("\n\t\t");
		for (int v : a.vars().values()) {
			s.append( "v:" + v + " d: " + a.dom(v) + ";");
		}
		TestUtils.message("dom/var: " + s.toString());
		TestUtils.message("set: _en passant_");
		TestUtils.message("addTo");
		a.addTo(new IntArray(3,0), 0.1);
		TestUtils.message("to string");
		TestUtils.output("factor:\n"+a);


		return true;
	}
}