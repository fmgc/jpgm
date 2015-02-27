import jpgm.IntArray;
import jpgm.Lambda;
import jpgm.Factor;
import jpgm.DBN;


import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;

public class DBNTest {

	public static void main(String[] args) {
		test("constructors and access methods", testConstructors());

	}


	public static boolean testConstructors() {
		DirectedAcyclicGraph<Integer,DefaultEdge> dag = makeAlarmDAG();
		DBN dbn = new DBN(dag, new IntArray(11,22,33,44,55), new IntArray(2,2,2,2,2));
		return true;
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