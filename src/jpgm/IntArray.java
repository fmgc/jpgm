package jpgm;

import java.lang.Math;
import java.lang.StringBuffer;
import java.util.Arrays;
import java.util.Iterator;

import jpgm.Lambda;

/**
* IntArray is the class for operations with symbolic names of variables or values.
* An IntArray wraps an int[] and has common operations on lists or sets of integers, 
* as well as specific methods to deal with potentials.
*
* @author Francisco Coelho
* @version 0.1
*/
public class IntArray {


	
	//************************************
	//
	//	static utility methods. 
	//
	//************************************

	public static Lambda<Integer,Boolean> areNegative() {
		return new Lambda<Integer,Boolean>() {
			public Boolean at(Integer x) {
				return x < 0;
			}
		};
	}
	//************************************
	//
	//	attributes, basic constructor and access methods. 
	//
	//************************************

	//
	//	wrapped array
	//
	protected int[] arr;

	/**
	* Default constructor. Uses variadic arguments.
	*/
	public IntArray(int... x) {
		arr = Arrays.copyOf(x, x.length);
	}

	/**
	* Clone constructor.
	*/
	public IntArray(IntArray x) {
		arr = Arrays.copyOf(x.arr, x.arr.length);
	}

	/**
	* Returns the element at given position.
	* No bondary check is performed (change this?).
	*/
	public int get(int i) {
		return arr[i];
	}

	public int[] values() {
		return Arrays.copyOf(arr, arr.length);
	}

	/**
	* Replaces the element at the given position (i) by the given value (x).
	* No bondary check is performed (change this?).
	*/
	public void set(int i, int x) {
		arr[i] = x;
	}

	/**
	* Returns the number of elements in this instance.
	*/
	public int size() {
		return arr.length;
	}

	/**
	* Returns true iff both IntArrays have the same elements in the same positions.
	*/
	public boolean equals(IntArray b) {
		return Arrays.equals(arr, b.arr);
	}

	public IntArray match(IntArray b) {
		int c = 0;
		int w = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == b.get(i)){
				c++;
			} else {
				w++;
			}
		}
		return new IntArray(c,w);
	}

	/**
	* Returns a string representation of this instance.
	* This is the method used in the context of string operations as println.
	*/
	public String toString() {
		return Arrays.toString(arr);
	}

	/**
	* Returns a string representation of this instance with formated values.
	*/
	public String format(String fmt) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			s.append(String.format(fmt, arr[i]));
		}
		return s.toString();
	}

	//************************************
	//
	//	Static constructors. 
	//
	//************************************

	/**
	* Returns { a[1][i], a[2][i], ...}.
	*/
	public static IntArray project(IntArray[] a, int i) {
		int[] x = new int[a.length];
		for (int k = 0; k < a.length; k++) {
			x[k] = a[k].get(i);
		}
		return new IntArray(x);
	}

	/**
	*	Returns num copies of k
	*/
	public static IntArray fill(int k, int num) {
		int[] a = new int[num];
		for (int i = 0; i < num; i++) a[i] = k;
		return new IntArray(a);
	}

	/**
	* Returns { from, from+step, from+2*step} up to to.
	*/
	public static IntArray range(int from, int to, int step) {
		int n = (to - from + 1) / step;
		int[] a = new int[n+1];
		int i = 0;
		for (int x = from; x < to; x += step) {
			a[i] = x;
			i++;
		}
		a = Arrays.copyOf(a,i);
		return new IntArray(a);
	}

	/**
	* Syntatic sugar for range(from, to, 1).
	*/
	public static IntArray range(int from, int to) {
		return range(from, to, 1);
	}

	/**
	* Syntatic sugar for range(0, to, 1).
	*/
	public static IntArray range(int to) {
		return range(0, to, 1);
	}


	//************************************
	//
	//	multi-variable encoding/deconding
	//
	//************************************

	/**
	* Returns the <em>event</em> that is encoded by i in the domain defined by this instance.
	*
	* @see #enc(IntArray)
	*/
	public IntArray dec(int i) {
		int n = size();
		int[] c = new int[n];
		int x = i;
		int dk;
		for (int k = 0; k < n; k++) {
			dk = get(k);
			c[k] = x % dk;
			x = x / dk;
		}
		return new IntArray(c);
	}

	/**
	* Returns the <em>index</em> that encodes x in the domain defined by this instance.
	*
	* @see #dec(int)
	*/
	public int enc(IntArray x) {
		int n = size() - 1;
		int i = x.get(n);
		for (int k = n - 1; k >= 0; k--) {
			i = get(k) * i + x.get(k);
		}
		return i;
	}

	/**
	* Returns the <em>product</em> of the elements in this instance.
	*/	
	public int mul() {
		int x = 1;
		for (int i = 0; i < arr.length; i++) {
			x *= arr[i];
		}
		return x;
	}

	//************************************
	//
	//	index operations
	//
	//************************************

	/**
	* Returns the <em>position</em> of x in this instance.
	* If x is not found, returns -1.
	*
	* @see #search(IntArray)
	*/
	public int search(int x) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == x) {
				return i;
			}
		}
		return -1;
	}

	/**
	* Returns the <em>positions</em> of the elements of x in this instance.
	* Not found elements have "position" -1.
	*
	* @see #search(int)
	*/
	public IntArray search(IntArray x) {
		int xs = x.size();
		int[] s = new int[xs];
		for (int i = 0; i < xs; i++) {
			s[i] = search(x.get(i));
		}
		return new IntArray(s);
	}

	/**
	* Returns an ArrayInt with elements of this instance in the positions listed in i.
	* Elements in i that do not correspond to positions in this instance are ignored;
	*
	* @see #search(int)
	*/
	public IntArray compose(IntArray i) {
		int[] b = new int[i.size()];		
		int bs = 0;
		for (int k = 0; k < i.size(); k++) {
			int ik = i.get(k);
			if (0 <= ik && ik < arr.length) {
				b[bs] = arr[ik];
				bs++;
			}
		}
		b = Arrays.copyOf(b,bs);
		return new IntArray(b);
	}

	/**
	* Syntatic sugar for compose.
	*
	* @see #compose(IntArray)
	*/
	public IntArray c(IntArray i) {
		return compose(i);
	}

	//************************************
	//
	//	Array operations
	//
	//************************************

	public IntArray filterIndexes() {
		int[] a = new int[size()];
		int k = 0;
		for (int x : arr) {
			if (x >= 0) {
				a[k] = x;
				k++;
			}
		}

		a = Arrays.copyOf(a,k);
		return new IntArray(a);
	}

	/**
	* Returns the <em>concatenation</em> of the elements in this instance with the elements of b.
	*
	*/
	public IntArray append(IntArray b) {
		int as = size();
		int bs = b.size();
		int[] x = new int[as+bs];
		for (int i = 0; i < as; i++) {
			x[i] = arr[i];
		}
		for(int i = 0; i < bs; i++) {
			x[as + i] = b.get(i);
		}
		return new IntArray(x);
	}

	/**
	* Returns a copy of this instance with elements sorted.
	*
	*/
	public IntArray sorted() {
		int[] x = Arrays.copyOf(arr, arr.length);
		Arrays.sort(x);
		return new IntArray(x);
	}

	/**
	* Returns a copy of this instance with elements in reverse order.
	*
	*/
	public IntArray reversed() {
		int[] x = new int[arr.length];
		for (int i = 0; i < arr.length; i++) x[arr.length - i - 1] = arr[i];
		return new IntArray(x);
	}

	/**
	* Returns the <em>pairing</em> of the elements of in this instance with the elements of b.
	* The result is {{a1,b1} ... {an,bn}} where n = min(this.size(), b.size());
	*
	* @see #project(IntArray[], int)
	*/
	public IntArray[] zip(IntArray b) {
		int n = Math.min(size(), b.size());
		IntArray[] x = new IntArray[n];
		for (int i = 0; i < n; i++) {
			x[i] = new IntArray( get(i), b.get(i) );
		}
		return x;
	}
	

	//************************************
	//
	//	Set operations
	//
	//************************************

	/**
	* Returns a copy of this instance with no duplicate elements.
	*
	*/
	public IntArray unique() {
		if (arr.length > 0) {
			int[] u = new int[arr.length];
			u[0] = arr[0];
			int k = 1;
			for (int i = 0; i < arr.length; i++) {
				boolean repeated = false;
				for(int j = 0; j < k; j++) {
					repeated  = arr[i] == u[j];
					if (repeated) break;
				}
				if (!repeated) {
					u[k] = arr[i];
					k++;
				}
			}
			u = Arrays.copyOf(u,k);
			return new IntArray(u);
		} else {
			return new IntArray();
		}
	}

	/**
	* Returns the intersection of this instance with b.
	* Duplicate elements are removed.
	*
	*/
	public IntArray intersection(IntArray b) {
		return compose( search(b) ).unique();
	}

	/**
	* Returns the union of this instance with b.
	* Duplicate elements are removed.
	*
	* @see #append(IntArray)
	*/
	public IntArray union(IntArray b) {
		return append(b).unique();
	}

	/**
	* Returns the <em>removal</em> of the elements of b in this instance.
	*
	*/
	public IntArray exclude(IntArray b) {
		IntArray mask = b.search(this);
		int[] x = new int[arr.length];
		int k = 0;
		for (int i = 0; i < mask.size(); i++) {
			if (mask.get(i) < 0) {
				x[k] = arr[i];
				k++;
			}
		}
		x = Arrays.copyOf(x,k);
		return new IntArray(x);
	}
	

	//************************************
	//
	//	Functional operations
	//
	//************************************


	/**
	* Returns the <em>image by f</em> of the elements in this instance.
	*
	*/
	public IntArray map(Lambda<Integer,Integer> f) {
		int[] y = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			y[i] = f.at(arr[i]);
		}

		return new IntArray(y);
	}

	/**
	* Returns the <em>elements</em> of this instance that satisfy condition f.
	*
	*/
	public IntArray filter(Lambda<Integer,Boolean> f) {
		int[] y = new int[arr.length];
		int k = 0;
		for (int i = 0; i < arr.length; i++) {
			if (f.at(arr[i])) {
				y[k] = arr[i];
				k++;
			}
		}
		y = Arrays.copyOf(y,k);
		return new IntArray(y);
	}

	/**
	* Returns true if all elements of this instance satisfy condition f.
	*
	*/
	public boolean all(Lambda<Integer,Boolean> f) {
		for (int i = 0; i < arr.length; i++) {
			if (!f.at(arr[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	* Returns true if any element of this instance satisfies condition f.
	*
	*/
	public boolean any(Lambda<Integer,Boolean> f) {
		for (int i = 0; i < arr.length; i++) {
			if (f.at(arr[i])) {
				return true;
			}
		}
		return false;
	}
}