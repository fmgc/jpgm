package jpgm;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;

import jpgm.Factor;
import jpgm.IntArray;
import jpgm.Distribution;

/**
* BN instances represent distributions encoded by bayesian networks.
* The query method can be computed by one of common algorithms, either exact (sum-product) or approximate (particle-filter)
* <br/>
* <strong>NOT FULLY IMPLEMENTED</strong>
*/
public class BN extends Distribution {
	public enum QueryAlgorithm {
		SUM_PRODUCT,
		MESSAGE_PASSING,
		PARTICLE_FILTER,
		ENUMERATION
	};


	/**
	* Analyses the list of factors to create a dag, a factors map, a set of variables and the respective domains.
	* If the factors don't create a dag an exception is thrown.
	*
	* @throws Exception if the list of factors induces a cyclic graph.
	*/
	public static BN fromFactors(List<Factor> f) throws Exception {
		DirectedAcyclicGraph<Integer,DefaultEdge> dag = new DirectedAcyclicGraph<Integer,DefaultEdge>(DefaultEdge.class);	// What a f@#!ing long name!
		HashMap<Integer,Factor> factor = new HashMap<Integer,Factor>();	// init factors map
		int n = f.size();												// there should be as many vars as factors
		int[] v = new int[n];										// array to gather variables
		int[] d = new int[n];										// array to gather variable domains
		int k = 0;															// variable counter
		for (Factor fv : f) {										// for each factor in the input list
			IntArray fvars = fv.vars();						// 	get the variables in that factor
			int v0 = fvars.get(0);								// 	the first is the var in the node
			dag.addVertex(v0);										// 	add as vertex
			factor.put(v0,fv);										// 	associate the var with the factor
			v[k] = v0;														//	gather this variable,
			d[k] = fv.dom(v0);										//	the respective domain
			k++;																	//	and increase the counter
			for (int i : fvars.values()) {				// 	for each variable in this factor
				if (i != v0) {											//		if not the first variable
					dag.addVertex(i);									//			add as vertex
					dag.addEdge(i,v0);								//			add an edge to the forst variable
				}
			}
		}

		return new BN(dag, factor, new IntArray(v), new IntArray(d));
	}

	DirectedAcyclicGraph<Integer,DefaultEdge> dag;	
	HashMap<Integer, Factor> factor;
	HashMap<Integer, IntArray> idx;
	QueryAlgorithm qalg;
	IntArray sporder; // the order of the variables be used in sumproduct;

	/**
	* Javadoc comment this.
	*/
	public BN(IntArray var, IntArray dom) {
		super(var,dom);
		idx = new HashMap<Integer, IntArray>();
		qalg = QueryAlgorithm.SUM_PRODUCT;
	}

	/**
	* Javadoc comment this.
	*/
	public BN(DirectedAcyclicGraph<Integer,DefaultEdge> dag, IntArray var, IntArray dom) {
		this(var,dom);
		this.dag = dag;
		setSumProductOrder(new IntArray());
		initFactors();
	}

	/**
	* Javadoc comment this.
	*/
	public BN(
		DirectedAcyclicGraph<Integer,DefaultEdge> dag,
		HashMap<Integer,Factor> factor,
		IntArray var, IntArray dom) {
		this(dag,var,dom);
		for (int v : factor.keySet()) {
			this.factor.put(v, factor.get(v));
		}
	}

	/**
	* Javadoc comment this.
	*
	* @throws Exception		if the factor defines variables not in the dag nodes or if the domains don't match.
	*/
	public void addFactor(Factor f) throws Exception {
		int v = f.vars().get(0);
		Factor g = factor.get(v);

		if (!g.vars().equals(f.vars())) {
			throw new Exception(String.format(
				"Setting incompatible factor: expected vars: %s; actual vars: %s;",g.vars(),f.vars()
				));
		}

		if (!g.domains().equals(f.domains())) {
			throw new Exception(String.format(
				"Setting incompatible factor: expected domains: %s; actual domains: %s;",g.domains(),f.domains()
				));
		}
	}

	/**
	* Selects the argorithm to be used for query evaluation.
	*/
	public void setQueryAlgorithm(QueryAlgorithm qalg) {
		this.qalg = qalg;
	}

	/**
	* Sets the order that variables are processed in the sum-product algorithm.
	*/
	public void setSumProductOrder(IntArray sporder) {
		if (sporder.size() == 0) {
			int[] vs = new int[dag.vertexSet().size()];
			Iterator<Integer> viter = dag.iterator();
			for (int i = 0; viter.hasNext(); i++) {
				vs[i] = viter.next();
			}
			sporder = new IntArray(vs);
			sporder = sporder.reversed();
		}
		this.sporder = sporder;
	}

	//************************************
	//
	//	Distribution interface methods
	//
	//************************************


	/**
	* Javadoc comment this.
	*/
	public double get(IntArray e) {
		double p = 1.0;
		for (int i = 0; i < var.size(); i++) {
			int n = var.get(i);
			p *= factor.get(n).get( e.compose( idx.get(n) ) );
		}
		return p;
	}

	/**
	* Not implemented.
	*/
	public Distribution marginal(IntArray vars) { // FIXME: implement
		return null;
	}

	/**
	* Javadoc comment this.
	*/
	public Distribution query(IntArray qvars, IntArray evars, IntArray evals) {
		Distribution q = null;
		switch (qalg) {
			case SUM_PRODUCT:
				q = sumproduct(qvars,evars,evals);
				break;
			case MESSAGE_PASSING:
				q =  messagepassing(qvars,evars,evals);
				break;
			case PARTICLE_FILTER:
				q = particlefilter(qvars,evars,evals);
				break;
			case ENUMERATION:
				q = enumeration(qvars,evars,evals);
				break;
		}
		return q;
	}

	/**
	* Not implemented.
	*/
	public IntArray draw()  { // FIXME: implement
		return new IntArray(0);
	}

	/**
	* Not implemented.
	*/
	public Distribution cond(IntArray v) { // FIXME: implement
		return null;
	}

	//************************************
	//
	//	more statistical inference methods
	//
	//************************************	


	/**
	* Javadoc comment this.
	*/
	public Factor update(IntArray xvars, IntArray xvals) { // FIXME: I don't know what I'm doing here!
		//
		//	Prepare factor **next** to hold the update.
		//
		IntArray xi = var.search(xvars);								// index of evars in var
		IntArray xv = var.compose(xi);									// evars in var
		IntArray xd = dom.compose(xi);									// evars domains
		IntArray xx = xvals.compose(xvars.search(xv));	// evals updated
		Factor next = new Factor(xv, xd);								// create factor
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

	//************************************
	//
	//	utility methods
	//
	//************************************

	//
	//	SUM-PRODUCT, aka VARIABLE-ELIMINATION, aka ELIMINATION-ASK (in aima)
	//

	private Factor makeFactor(int v, IntArray evars, IntArray evals) {
		return factor.get(v).filter(evars, evals);
	}

	private List<Factor> sumOut(int v, List<Factor> factors) {
		ArrayList<Factor> s = new ArrayList<Factor>();
		ArrayList<Factor> m = new ArrayList<Factor>();
		for (Factor f : factors) {
			if (f.vars().search(v) >= 0) {
				m.add(f);
			} else {
				s.add(f);
			}
		}

		Factor p = (Factor)m.get(0);
		for (int i = 1; i < m.size(); i++) {
			p = p.mul( (Factor)m.get(i) );
		}
		s.add( (Factor)p.marginal(v) );
		return s;
	}

	private Distribution sumproduct(IntArray qvars, IntArray evars, IntArray evals, IntArray order) { // FIXME: implement
		//
		//	Prepare main cycle:
		//	- find hidden vars (yvars)
		//	- restrict vars to ancestors of qvars+evars
		//	- sort restricted variables (usually reversed topological)
		//
		IntArray yvars = var.exclude(qvars.append(evars));
		IntArray neededvars = ancestors(qvars.append(evars));
		IntArray vorder = neededvars.compose(neededvars.search(order));
		//
		// Run the factor gathering step
		//
		List<Factor> factors = new ArrayList<Factor>();
		for (int i = 0; i < vorder.size(); i++) {
			int vi = vorder.get(i);
			factors.add(0, makeFactor(vi, evars, evals));
			if (yvars.search(vi) >= 0) {
				factors = sumOut(vi, factors);
			}
		}
		//
		//	Multiply the gathered factors
		//
		Factor p = (Factor)factors.get(0);
		for (int i = 1; i < factors.size(); i++) {
			Factor pf = (Factor)factors.get(i);
			p = p.mul( pf );
		}
		//
		//	Leave only the qvars and normalize.
		//
		p = p.marginal(p.vars().exclude(qvars));
		p.normalize();
		//
		//	Ended.
		//
		return p;
	}
	
	private Distribution sumproduct(IntArray qvars, IntArray evars, IntArray evals) {
		return sumproduct(qvars,evars,evals,sporder);
	}

	//
	//	MESSAGE-PASSING, aka CLUSTERING
	//
	private Distribution messagepassing(IntArray qvars, IntArray evars, IntArray evals) { // FIXME: implement
		return null;
	}

	//
	//	PARTICLE-FILTER
	//
	private Distribution particlefilter(IntArray qvars, IntArray evars, IntArray evals) { // FIXME: implement
		return null;
	}


	private Distribution enumeration(IntArray qvars, IntArray evars, IntArray evals) {
		return sumproduct(qvars,evars,evals);
	}

	/**
	* Creates the factor factors after the dag is defined.
	*/
	private void initFactors() {
		factor = new HashMap<Integer,Factor>();
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
			Factor f =  new Factor(vars,doms);
			factor.put(v,f);
			idx.put(v, vi);
		}
	}

	/**
	* Finds the antecesors in this dag of the given vertexes.
	*/
	private IntArray ancestors(IntArray vset) {
		HashSet<Integer> closed = new HashSet<Integer>();
		ArrayDeque<Integer> open = new ArrayDeque<Integer>();
		for (int v : vset.values()) {
			open.addLast(v);
		}

		while(open.size()  > 0) {
			int v = (int)open.removeFirst();
			closed.add(v);
			for (DefaultEdge e : dag.incomingEdgesOf(v)) {
				open.addLast(dag.getEdgeSource(e));
			}
		}

		Integer[] anc_ = (Integer[])(closed.toArray(new Integer[0]));
		int[] anc = new int[anc_.length];
		for (int i = 0; i < anc_.length; i++) {
			anc[i] = anc_[i];
		}
		return new IntArray( anc );

	}
}