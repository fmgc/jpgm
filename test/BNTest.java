import java.util.ArrayList;
import java.util.Arrays;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;

import jpgm.IntArray;
import jpgm.Lambda;
import jpgm.Factor;
import jpgm.BN;

public class BNTest {

	public static void main(String[] args) {
		TestUtils.test("constructors and access methods", testConstructors());
		TestUtils.test("distribution methods", testDistribution());
	}


	public static boolean testDistribution() {
		BN alarm;
		try {
			alarm = BN.fromFactors( makeAlarmFactors() );
			TestUtils.message("Alarm network: compute P(~b,~e,a,j,m):");
			TestUtils.output("P(~b,~e,a,j,m) = " + String.format("%5.4g",alarm.get(new IntArray(0,0,1,1,1)) ));

			TestUtils.message("Alarm network: query P(J|b)");
			//alarm.setSumProductOrder(new IntArray(55,44,33,11,22));
			TestUtils.output("P(J|b) = \n" + alarm.query(
				new IntArray(44),
				new IntArray(11),
				new IntArray(1)
				));

			TestUtils.message("Alarm network: query P(B|j,m)");
			TestUtils.output("P(B|j,m) = \n" + alarm.query(
				new IntArray(11),
				new IntArray(44,55),
				new IntArray(1,1)
				));
		} catch (Exception e) {
			TestUtils.output("NOK:" + e);
		};

		int CLOUDY = 10;
		int SPRINKLER = 20;
		int RAIN = 30;
		int WETGRASS = 40;
		
		BN wetgrass;
		try {
			wetgrass = BN.fromFactors( makeWetgrassFactors() );
			//wetgrass.setSumProductOrder(new IntArray(WETGRASS,RAIN,SPRINKLER,CLOUDY));
			TestUtils.message("WetGrass network: compute P(S|w):");
			TestUtils.output("P(S|w) = \n"+wetgrass.query(
				new IntArray(SPRINKLER),
				new IntArray(WETGRASS),
				new IntArray(1)
				));
			TestUtils.message("WetGrass network: compute P(R|w):");
			TestUtils.output("P(R|w) = \n"+wetgrass.query(
				new IntArray(RAIN),
				new IntArray(WETGRASS),
				new IntArray(1)
				));

		} catch (Exception e) {
			TestUtils.output("NOK:" + e);
		}
		return true;
	}
	public static boolean testConstructors() {
		DirectedAcyclicGraph<Integer,DefaultEdge> dag = makeAlarmDAG();

		BN bn = new BN(dag, new IntArray(11,22,33,44,55), new IntArray(2,2,2,2,2));

		TestUtils.message("Expected exception for adding factor with wrong number of vars.");
		try {
			bn.addFactor( new Factor(new IntArray(11,22), new IntArray(2,3)));
		} catch (Exception e) {
			TestUtils.output(e.toString());
		}

		TestUtils.message("Expected exception for adding factor with wrong domain.");
		try {
			bn.addFactor( new Factor(new IntArray(11), new IntArray(3)));
		} catch (Exception e) {
			TestUtils.output(e.toString());
		}

		TestUtils.message("Create 'alarm' network.");
		BN alarm;
		try {
			alarm = BN.fromFactors( makeAlarmFactors() );
			TestUtils.output("OK.");
		} catch (Exception e) {
			TestUtils.output("NOK:" + e);
		};

		TestUtils.message("Create 'wetgrass' network.");
		BN wetgrass;
		try {
			wetgrass = BN.fromFactors( makeWetgrassFactors() );
			TestUtils.output("OK.");
		} catch (Exception e) {
			TestUtils.output("NOK:" + e);
		};

		TestUtils.message("Create 'student' network.");
		BN student;
		try {
			student = BN.fromFactors( makeStudentFactors() );
			TestUtils.output("OK.");
		} catch (Exception e) {
			TestUtils.output("NOK:" + e);
		};
		
		return true;
	}

	public static ArrayList<Factor> makeStudentFactors() {

		Factor[] f = new Factor[5];

		int DIFFICULTY = 10;
		int INTELLIGENCE = 20;
		int GRADE = 30;
		int SAT = 40;
		int LETTER = 50;

		f[0] = new Factor(
			new IntArray(DIFFICULTY),
			new IntArray(2),
			new int[][] {{0}, {1}},
			new double[] {0.6, 0.4}
			);

		f[1] = new Factor(
			new IntArray(INTELLIGENCE),
			new IntArray(2),
			new int[][] {{0}, {1}},
			new double[] {0.7, 0.3}
			);

		f[2] = new Factor(
			new IntArray(GRADE,INTELLIGENCE,DIFFICULTY),
			new IntArray(3,2,2),
			new int[][] {
				{0,0,0}, {1,0,0}, {2,0,0},
				{0,0,1}, {1,0,1}, {2,0,1},
				{0,1,0}, {1,1,0}, {2,1,0},
				{0,1,1}, {1,1,1}, {2,1,1} },
			new double[] {
				0.30, 0.40, 0.30,
				0.05, 0.25, 0.70,
				0.90, 0.08, 0.02,
				0.50, 0.30, 0.20 }
			);

		f[3] = new Factor(
			new IntArray(SAT, INTELLIGENCE),
			new IntArray(2,2),
			new int[][] {
				{0,0}, {1,0},
				{0,1}, {1,1} },
			new double[] {
				0.95, 0.05,
				0.2, 0.8 }
			);

		f[4] = new Factor(
			new IntArray(LETTER,GRADE),
			new IntArray(2,3),
			new int[][] {
				{0,0}, {1,0},
				{0,1}, {1,1},
				{0,2}, {1,2} },
			new double[] {
				0.1, 0.9,
				0.4, 0.6,
				0.99, 0.01 }
			);
		return new ArrayList<Factor>(Arrays.asList(f));
	}

	public static ArrayList<Factor> makeWetgrassFactors() {
		int CLOUDY = 10;
		int SPRINKLER = 20;
		int RAIN = 30;
		int WETGRASS = 40;

		Factor[] f = new Factor[4];

		f[0] = new Factor(
			new IntArray(CLOUDY),
			new IntArray(2),
			new int[][] {{1}, {0}},
			new double[]  {0.5, 0.5}
			);

		f[1] = new Factor(
			new IntArray(SPRINKLER,CLOUDY),
			new IntArray(2,2),
			new int[][] {
				{0,0}, {1,0},
				{0,1}, {1,1}},
			new double[]  {
				0.5, 0.5,
				0.9, 0.1}
			);

		f[2] = new Factor(
			new IntArray(RAIN,CLOUDY),
			new IntArray(2,2),
			new int[][] {
				{0,0}, {1,0},
				{0,1}, {1,1}},
			new double[]  {
				0.8, 0.2,
				0.2, 0.8}
			);

		f[3] = new Factor(
			new IntArray(WETGRASS, SPRINKLER, RAIN),
			new IntArray(2,2,2),
			new int[][] {
				{0,0,0}, {1,0,0},
				{0,1,0}, {1,1,0},
				{0,0,1}, {1,0,1},
				{0,1,1}, {1,1,1},
			},
			new double[]  {
				1.0, 0.0, 
				0.1, 0.9, 
				0.1, 0.9, 
				0.01, 0.99}
			);

		return new ArrayList<Factor>(Arrays.asList(f));
	}

	public static ArrayList<Factor> makeAlarmFactors() {
			int BURGLARY = 11;
			int EARTHQUAKE = 22;
			int ALARM = 33;
			int JOHNCALLS = 44;
			int MARYCALLS = 55;

			Factor[] f = new Factor[5];

			f[0] = new Factor(
				new IntArray(BURGLARY),
				new IntArray(2),
				new int[][] {{0}, {1}},
				new double[] {0.999, 0.001} );

			f[1] = new Factor(
				new IntArray(EARTHQUAKE),
				new IntArray(2),
				new int[][] {{0}, {1}},
				new double[] {0.998, 0.002} );

			f[2] = new Factor(
				new IntArray(ALARM,BURGLARY,EARTHQUAKE),
				new IntArray(2,2,2),
				new int[][] {
					{1,1,1},
					{1,1,0},
					{1,0,1},
					{1,0,0},
					{0,1,1},
					{0,1,0},
					{0,0,1},
					{0,0,0}},
				new double[] {
					0.95,
					0.94,
					0.29,
					0.001,
					0.05,
					0.06,
					0.73,
					0.999} );

			f[3] = new Factor(
				new IntArray(JOHNCALLS, ALARM),
				new IntArray(2,2),
				new int[][] {{1,1}, {1,0}, {0,1}, {0,0}},
				new double[] {0.9, 0.05, 0.1, 0.95} );

			f[4] = new Factor(
				new IntArray(MARYCALLS, ALARM),
				new IntArray(2,2),
				new int[][] {{1,1}, {1,0}, {0,1}, {0,0}},
				new double[] {0.7, 0.01, 0.3, 0.99} );

			return new ArrayList<Factor>(Arrays.asList(f));
		}

	public static DirectedAcyclicGraph<Integer,DefaultEdge> makeAlarmDAG() {
		DirectedAcyclicGraph<Integer,DefaultEdge> dag = 
			new DirectedAcyclicGraph<Integer,DefaultEdge>(DefaultEdge.class);

			int BURGLARY = 11;
			int EARTHQUAKE = 22;
			int ALARM = 33;
			int JOHNCALLS = 44;
			int MARYCALLS = 55;

			dag.addVertex(BURGLARY);
			dag.addVertex(EARTHQUAKE);
			dag.addVertex(ALARM);
			dag.addVertex(JOHNCALLS);
			dag.addVertex(MARYCALLS);

			try {
				dag.addDagEdge(BURGLARY, ALARM);
				dag.addDagEdge(EARTHQUAKE, ALARM);
				dag.addDagEdge(ALARM, JOHNCALLS);
				dag.addDagEdge(ALARM, MARYCALLS);
			} catch (Exception e) {				
			}
			
			return dag;
	}


	// public static ArrayList<Factor> makeHMMFactors() {
	// 	Factor[] f = new Factor[11];
	// 	return f;
}
