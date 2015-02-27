package jpgm;

import java.util.HashMap;
import java.util.Set;
import java.util.Arrays;
import java.lang.StringBuffer;
import java.util.Locale;
import java.util.Random;

import jpgm.IntArray;
import jpgm.Distribution;
/**
* Factor is the class for basic multivariate discrete statistics operations.
* A Factor wraps a map from atomic events to probabilities.
* Events not in this map have probability zero.
*
* @author Francisco Coelho
* @version 0.1
*/

public class Factor extends Distribution {
	//************************************
	//
	//	attributes, basic constructor and access methods. 
	//
	//************************************

	HashMap<Integer,Double> pot;
	double z;

	/**
	* Default constructor. Sets the symbolic varaible names and respective domain ranges.
	*/
	public Factor(IntArray var, IntArray dom) {
		super(var,dom);
		pot = new HashMap<Integer,Double>();
	}

	/**
	* Constructor with explicit potential given.
	* 
	* @see #Factor(IntArray,IntArray)
	*/
	public Factor(IntArray var, IntArray dom, HashMap<Integer,Double> pot) {
		this(var,dom);
		this.pot = new HashMap<Integer,Double>(pot);
		sumz();
	}

	/**
	* Constructor with explicit support given. Events are defined in x and the respective probabilities in p.
	*
	* @see #Factor(IntArray,IntArray)
	*/
	public Factor(IntArray var, IntArray dom, int[][] x, double[] p) {
		this(var,dom);
		for (int i = 0; i < x.length; i++) {
			IntArray xi = new IntArray(x[i]);
			pot.put(dom.enc(xi), p[i]);
			z += p[i];
		}
	}

	/**
	* Sets the value of event #i in the domain.
	*/
	public void set(int i, double p) {
		pot.put(i, p);
		z += p;
	}

	/**
	* Sets the value of event e.
	*/
	public void set(IntArray e, double p) {
		set(dom.enc(e), p);
	}

	/**
	* Adds the given value p to event e.
	*/
	public void addTo(IntArray e, double p) {
		int i = dom.enc(e);
		double pi = get(i);
		set(i, pi+p);
		z -= pi; // otherwise pi would be added twice to z;
	}

	/**
	* Returns the indexes of events in the support.
	*/
	public Set<Integer> supportKeys() {
		return pot.keySet();
	}

	/**
	* Returns the value of event #i in the domain. If there is no value assigned to such event, returns 0.0.
	*/
	public double get(int i) {
		return pot.containsKey(i) ? pot.get(i) : 0.0;
	}

	/**
	* Returns the value of event e in the domain. If there is no value assigned to such event, returns 0.0.
	*
	* @see #get(int)
	*/
	public double get(IntArray e) {
		return get(dom.enc(e));
	}

	//************************************
	//
	//	factor algebra
	//
	//************************************

	/**
	* Returns the factor multiplication of this instance with b.
	*/
	public Factor mul(Factor b) {
		//
		//	Variables information of this instance
		//
		IntArray avars = vars();
		IntArray adoms = domains();
		//
		//	Variables information of other instance
		//
		IntArray bvars = b.vars();		
		IntArray bdoms = b.domains();
		//
		//	Separation of common and solo variables and domains
		//
		IntArray commonvars = avars.intersection(bvars);
		//
		//	Common variables in this instance
		//
		IntArray aci = avars.search(commonvars);	// this common vars index
		IntArray acv = avars.compose(aci);			// this common vars
		IntArray acd = adoms.compose(aci);			// this common doms
		//
		//	Solo variables in this instance
		//
		IntArray asv = avars.exclude(acv);	// this solo vars
		IntArray asi = avars.search(asv);	// this solo vars index;
		IntArray asd = adoms.compose(asi);	// this solo doms
		//
		//	Common variables in other instance
		//
		IntArray bci = bvars.search(commonvars);	// other common vars index
		IntArray bcv = bvars.compose(bci);			// other common vars
		IntArray bcd = bdoms.compose(bci);			// other common doms
		//
		//	Solo variables in other instance
		//
		IntArray bsv = bvars.exclude(bcv);	// other solo vars
		IntArray bsi = bvars.search(bsv);	// other solo vars index	
		IntArray bsd = bdoms.compose(bsi);	// other solo doms
		//
		//	Resulting factor instantiation
		//
		IntArray cvars = asv.append( acv.append(bsv) );	// result vars
		IntArray cdoms = asd.append( acd.append(bsd) );	// result doms
		Factor c = new Factor(cvars,cdoms);				// result instance
		//
		//	Fill relevant values in resulting instance
		//
		for (int ak : supportKeys() ) {	// for each event in this instance
			IntArray ax = adoms.dec(ak);		// get the event
			IntArray axs = ax.compose(asi);	// solo part
			IntArray axc = ax.compose(aci);	// common part

			for (int bk: b.supportKeys()) {	// for each event in other instance
				IntArray bx = bdoms.dec(bk);		// get the event
				IntArray bxs = bx.compose(bsi);	// solo part
				IntArray bxc = bx.compose(bci);	// common part
				if (axc.equals(bxc)) {	// if the common part is equal...
					c.set( axs.append( axc.append(bxs) ), get(ax) * b.get(bx) );	// operate
				}
			}
		}
		return c;		
	}

	/**
	* Computes the factor sum of this instance with b.
	*/
	public Factor add(Factor b) {
		//
		//	Variables information of this instance
		//
		IntArray avars = vars();
		IntArray adoms = domains();
		//
		//	Variables information of other instance
		//
		IntArray bvars = b.vars();		
		IntArray bdoms = b.domains();
		//
		//	Separation of common and solo variables and domains
		//
		IntArray commonvars = avars.intersection(bvars);
		//
		//	Common variables in this instance
		//
		IntArray aci = avars.search(commonvars);	// this common vars index
		IntArray acv = avars.compose(aci);			// this common vars
		IntArray acd = adoms.compose(aci);			// this common doms
		//
		//	Solo variables in this instance
		//
		IntArray asv = avars.exclude(acv);	// this solo vars
		IntArray asi = avars.search(asv);	// this solo vars index;
		IntArray asd = adoms.compose(asi);	// this solo doms
		//
		//	Common variables in other instance
		//
		IntArray bci = bvars.search(commonvars);	// other common vars index
		IntArray bcv = bvars.compose(bci);			// other common vars
		IntArray bcd = bdoms.compose(bci);			// other common doms
		//
		//	Solo variables in other instance
		//
		IntArray bsv = bvars.exclude(bcv);	// other solo vars
		IntArray bsi = bvars.search(bsv);	// other solo vars index	
		IntArray bsd = bdoms.compose(bsi);	// other solo doms
		//
		//	Resulting factor instantiation
		//
		IntArray cvars = asv.append( acv.append(bsv) );	// result vars
		IntArray cdoms = asd.append( acd.append(bsd) );	// result doms
		Factor c = new Factor(cvars,cdoms);				// result instance
		//
		//	Fill relevant values in resulting instance
		//
		for (int ak : supportKeys() ) {	// for each event in this instance
			IntArray ax = adoms.dec(ak);		// get the event
			IntArray axs = ax.compose(asi);	// solo part
			IntArray axc = ax.compose(aci);	// common part

			for (int bk: b.supportKeys()) {	// for each event in other instance
				IntArray bx = bdoms.dec(bk);		// get the event
				IntArray bxs = bx.compose(bsi);	// solo part
				IntArray bxc = bx.compose(bci);	// common part
				if (axc.equals(bxc)) {	// if the common part is equal...
					c.set( axs.append( axc.append(bxs) ), get(ax) + b.get(bx) );	// operate
				}
			}
		}
		return c;		
	}

	/**
	* Computes the extention of this instance with b.
	* The "extention" is a factor c such that
	* c(uwv) = this(uw) if wv not in b;
	* c(uwv) = b(wv) if uw not in this instance;
	* c(uwv) = 0 if uw in this instance and wv in b; (thus this event is skiped)
	*/
	public Factor extend(Factor b) {
		//
		//	Variables information of this instance
		//
		IntArray avars = vars();
		IntArray adoms = domains();
		//
		//	Variables information of other instance
		//
		IntArray bvars = b.vars();		
		IntArray bdoms = b.domains();
		//
		//	Separation of common and solo variables and domains
		//
		IntArray commonvars = avars.intersection(bvars);
		//
		//	Common variables in this instance
		//
		IntArray aci = avars.search(commonvars);	// this common vars index
		IntArray acv = avars.compose(aci);			// this common vars
		IntArray acd = adoms.compose(aci);			// this common doms
		//
		//	Solo variables in this instance
		//
		IntArray asv = avars.exclude(acv);	// this solo vars
		IntArray asi = avars.search(asv);	// this solo vars index;
		IntArray asd = adoms.compose(asi);	// this solo doms
		//
		//	Common variables in other instance
		//
		IntArray bci = bvars.search(commonvars);	// other common vars index
		IntArray bcv = bvars.compose(bci);			// other common vars
		IntArray bcd = bdoms.compose(bci);			// other common doms
		//
		//	Solo variables in other instance
		//
		IntArray bsv = bvars.exclude(bcv);	// other solo vars
		IntArray bsi = bvars.search(bsv);	// other solo vars index	
		IntArray bsd = bdoms.compose(bsi);	// other solo doms
		//
		//	Resulting factor instantiation
		//
		IntArray cvars = asv.append( acv.append(bsv) );	// result vars
		IntArray cdoms = asd.append( acd.append(bsd) );	// result doms
		Factor c = new Factor(cvars,cdoms);				// result instance
		//
		//	Fill relevant values in resulting instance
		//
		for (int ak : supportKeys() ) {	// for each event in this instance
			IntArray ax = adoms.dec(ak);		// get the event
			IntArray axs = ax.compose(asi);	// solo part
			IntArray axc = ax.compose(aci);	// common part

			for (int bk: b.supportKeys()) {	// for each event in other instance
				IntArray bx = bdoms.dec(bk);		// get the event
				IntArray bxs = bx.compose(bsi);	// solo part
				IntArray bxc = bx.compose(bci);	// common part
				if (axc.equals(bxc)) {	// if the common part is equal...
					//c.set( axs.append( axc.append(bxs) ), get(ax) + b.get(bx) );	// operate
				} else {
					c.set( axs.append( axc.append(bxs)), get(ax));
					c.set( axs.append( bxc.append(bxs)), b.get(bx));
				}
			}
		}
		return c;		
	}

	//************************************
	//
	//	distribution operations.
	//
	//************************************

	/**
	* Returns the sum of all values in this instance.
	*/
	public double Z() {
		return z;
	}

	/**
	* Changes the values in this instance such that the sum is 1.0.
	*/
	public void normalize() {
		double s = 1.0/Z();
		for (int l: supportKeys()) {
			pot.put(l, pot.get(l)*s);
		}
		z = 1.0;
	}

	/**
	* Marginalizes given variables. Variables not in this factor are ignored.
	*/
	public Factor marginal(IntArray v) {
		IntArray mv = var.exclude(v);
		IntArray mi = var.search(mv);
		IntArray md = dom.compose(mi);

		if (mv.size() == 0) {
			return new Factor(var,dom,pot);
		} else {
			Factor m = new Factor(mv,md);
			for (int k : supportKeys()) {
				IntArray x = dom.dec(k);
				IntArray mx = x.compose(mi);
				m.addTo(mx, pot.get(k));
			}
			return m;
		}
	}

	public Factor filter(IntArray evars, IntArray evals) {
		IntArray eidx = var.search(evars); 			// indexes of evidence vars
		if (eidx.all(IntArray.areNegative())) {
			return new Factor(var,dom,pot);
		}
		IntArray ridx = evars.search(var);
		IntArray rvals = evals.compose( ridx );
		//
		//	select the evidence cases
		//
		int[] lines = new int[pot.size()]; 			// matching lines in this potential
		IntArray[] events = new IntArray[pot.size()]; // matching events in this potential (storage)
		int k = 0;									// match counter
		for (int i : supportKeys()) {				// for each event in this instance
			IntArray event = dom.dec(i);			// 		the event in this instance
			IntArray eevent = event.compose(eidx);	// 		the event restricted to evidence vars
			if (rvals.equals(eevent)) {				// 		if matches the evidence...
				lines[k] = i;						//			store the "line"
				events[k] = event;					//			store the event
				k++;								// 			increase match counter
			}
		}
		lines = Arrays.copyOf(lines, k);
		//
		//	fill-in the qy potential
		//
		Factor qy = new Factor(var, dom);		
		for (int i = 0; i < lines.length; i++) {	// for each matching case found above...
			IntArray event = events[i];				// 		the matching event in this instance
			int line = lines[i];					// 		the corresponding index
			qy.addTo(event, get(line));			// 		the same qyevent can join from many lines
		}
		return qy;
	}

	public Factor filter(IntArray[] e) {
		IntArray evars = IntArray.project(e,0);
		IntArray evals = IntArray.project(e,1);
		return filter(evars,evals);
	}

	/**
	* Returns the result of the probabilistic query P(qvars|e) where P is the factor represented by this instance.
	* Variables not defined here are ignored.
	* If qvars doesn't contains variables in this factor the resulting factor contains all variables of this factor.
	* Evidence is defined by a list of (variable,value) pairs.
	*/
	public Factor query(IntArray qvars, IntArray[] e) {
		//
		//	relate evidence with this variables
		//
		Factor q = filter(e).marginal(var.exclude(qvars));
		q.normalize();
		return q;
	}

	/**
	* Alternate presentation of the evidence in a probabilistic query.
	* Evidence is given by two lists, evars and evals.
	* The former list contains variables and the later respective values.
	*
	* @see #query(IntArray,IntArray[])
	*/
	public Factor query(IntArray qvars, IntArray evars, IntArray evals) {
		return query(qvars, evars.zip(evals));
	}

	/** 
	* Draws a sample from this factor.
	*/
	public IntArray draw() {
		double threshold = rand.nextDouble() * Z();
		for (int index : pot.keySet()) {
			threshold -= pot.get(index);
			if (threshold < 0.0) {
				return dom.dec(index);
			}
		}		
		return dom.dec(0);
	}

	/**
	* Conditions a (expected) join distribution on given variables.
	* If this is P(V,U), cond(V) return P(U|V).
	*/
	public Factor cond(IntArray v) {

		IntArray yvar = var.exclude(v);
		IntArray cvar = yvar.append( v );
		IntArray ic = var.search(cvar);
		IntArray cdom = dom.compose(ic);

		Factor c = new Factor(cvar,cdom);

		Factor pe = marginal(yvar);
		IntArray ei = var.search(pe.vars());
		IntArray ed = pe.domains();

		for (int index : supportKeys()) {
			IntArray x = dom.dec(index);
			int eindex = ed.enc( x.compose(ei) );
			int cindex = cdom.enc( x.compose(ic) );
			c.set(cindex, get(index) / pe.get( eindex ));
		}

		return c;
	}
	//************************************
	//
	//	utility methods
	//
	//************************************

	/**
	* Updates the value of Z (the sum of all values in this factor).
	*/
	private void sumz() {
		z = 0.0;
		for (double p : pot.values()) {
			z += p;
		}
	}

	//************************************
	//
	//	export/import methods
	//
	//************************************

	/**
	* Generates the default presentation of this factor, in a CSV format.
	*/
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(var.format("%3d, ") + "#\n");
		Integer[] k = supportKeys().toArray(new Integer[0]);
		Arrays.sort(k);
		for(int i : k) {
			s.append(dom.dec(i).format("%3d, ")+String.format(Locale.US,"%5.4f",get(i)) + "\n");
		}
		return s.toString();
	}

	
}