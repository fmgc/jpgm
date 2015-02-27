package jpgm;


/**
* Lambda in an interface to define anonymous functions (while Java 8+ doesn't get mainstream).
*
* @author Francisco Coelho
* @version 0.1
*/
public interface Lambda<A,B> {
	/**
	* Anonymous function evaluation
	*/
	public B at(A x);
}