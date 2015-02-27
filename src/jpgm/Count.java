package jpgm;

public class Count extends Distribution {
	int symbol;

	public Count(IntArray var, IntArray dom, int symbol) {
		super(var,dom);
		this.symbol = symbol;
	}

	public int getSymbol() {
		return symbol;
	}

	public double get(IntArray e) {
		int count = 0;
		for(int i = 1; i < e.size(); i++) {
			if (e.get(i) == symbol) {
				count++;
			}
		}

		return count == e.get(0) ? 1.0 : 0.0;
	}

	public Distribution marginal(IntArray vars) {
		return null;
	}

	public Distribution query(IntArray qvars, IntArray evars, IntArray evals) {
		return null;
	}

	public IntArray draw() {
		return null;
	}

	public Distribution cond(IntArray v) {
		return null;
	}
}