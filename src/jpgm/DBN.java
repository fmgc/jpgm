package jpgm;

import java.util.HashMap;
import java.util.Set;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;

import jpgm.Factor;
import jpgm.IntArray;



/**
* Javadoc comment this.
*/
public class DBN {
	DirectedAcyclicGraph<Integer,DefaultEdge> dag;	
	Factor prior;
	HashMap<Integer, Factor> transition;
	IntArray var;
	IntArray dom;


	/**
	* Javadoc comment this.
	*/
	public DBN(DirectedAcyclicGraph<Integer,DefaultEdge> dag, IntArray var, IntArray dom) {
		this.dag = dag;
		this.var = var;
		this.dom = dom;
		initTransition();
	}

	/**
	* Javadoc comment this.
	* Compute 
	*/
	public Factor update(IntArray xvars, IntArray xvals) { // FIXME: I don't know what I'm doing here!
		//
		//	Prepare factor **next** to hold the update.
		//
		IntArray xi = var.search(xvars);				// index of evars in var
		IntArray xv = var.compose(xi);					// evars in var
		IntArray xd = dom.compose(xi);					// evars domains
		IntArray xx = xvals.compose(xvars.search(xv));	// evals updated
		//IntArray nx = var.exclude()
		Factor next = new Factor(xv, xd);				// create factor
		//
		//
		//
		return next;
	}

	/**
	* Javadoc comment this.
	* Compute P(evars = evals | X = state)
	*/
	public Factor likelihood(IntArray evars, IntArray evals, IntArray state) { // FIXME: 
		return null;
	}

	/**
	* Creates the transition factors after the dag is defined.
	*/
	private void initTransition() {
		transition = new HashMap<Integer,Factor>();
		for (Integer v : dag.vertexSet()) {
			Set<DefaultEdge> inEdges = dag.incomingEdgesOf(v);
			int[] vp = new int[inEdges.size() + 1];
			vp[0] = v;
			int k = 1;
			for (DefaultEdge e : inEdges) {
				vp[k] = (Integer)dag.getEdgeSource(e);
				k++;
			}
			IntArray vars = new IntArray(vp);
			IntArray vi = var.search(vars);
			IntArray doms = dom.compose(vi);
			transition.put(v, new Factor(vars,doms));
		}
	}


}