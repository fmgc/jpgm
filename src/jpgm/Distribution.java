package jpgm;

import java.util.HashMap;
import java.util.Random;

import jpgm.IntArray;

/**
* Distribution is an abstract class for discrete distributions.
* It implements a set of methods for the variables, respective domains and atomic events.
* Furthermore, abstract methods define a common set of operations: 
* - double .get(Event)
* - Distribution .marginal(Variables);
* - Distribution .query(Query Variables, Evidence Variables, Evidence Values);
* - Event .draw();
* - Distribution .cond(Variables);
* 
*/
public abstract class Distribution {

	protected IntArray dom;
	protected IntArray var;
	HashMap<Integer,Integer> vmap;

	public Random rand;

	/**
	* Default constructor. Sets the symbolic varaible names and respective domain ranges.
	*/
	public Distribution(IntArray var, IntArray dom) {
		this.var = new IntArray(var);
		this.dom = new IntArray(dom);
		rand = new Random();
		indexVars();
	}

	/**
	* Returns the variables.
	*/
	public IntArray vars() {
		return new IntArray(var);
	}

	/**
	* Returs the domains of the variables in the same order than vars().
	*
	* @see #vars()
	*/
	public IntArray domains() {
		return new IntArray(dom);
	}

	/**
	* Returns the domain of variable v.
	*/
	public int dom(int v) {
		return dom.get(vmap.get(v));
	}


	/**
	* Returns the probability of event e.
	*/
	public abstract double get(IntArray e);

	public abstract Distribution marginal(IntArray vars);

	/**
	* Marginalizes given variables. Variables not in this distribution are ignored.
	*/
	public Distribution marginal(int... v) {
		return marginal(new IntArray(v));
	}


	/**
	* Returns the result of the probabilistic query P(qvars|evars,evals) where P is the factor represented by this instance.
	* Evidence is given by two lists, evars, of variables and evals, of respective values.
	*
	*/
	public abstract Distribution query(IntArray qvars, IntArray evars, IntArray evals);

	/** 
	* Draws a single sample from this factor.
	*/
	public abstract IntArray draw();

	/**
	* Conditions a (expected) join distribution on given variables.
	* If this is P(V,U), cond(V) return P(U|V).
	*/
	public abstract Distribution cond(IntArray v);

	/**
	* Updates the map of variables names to respective local indexes
	*/
	private void indexVars() {
		vmap = new HashMap<Integer,Integer>();
		for (int i = 0; i < var.size(); i++) {
			vmap.put(var.get(i), i);
		}
	}
}