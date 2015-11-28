
/**
 * 
 * @author Akshay
 *
 */
public class QueryWrapper {
	String queryTerm;
	Double W1;
	Double W2;
	
	public QueryWrapper(String qTerm,double weight1,double weight2) {
		this.queryTerm=qTerm;
		this.W1=weight1;
		this.W2=weight2;
	}
	
	@Override
	public String toString() {
		return "Term : " + queryTerm + " W1 value: " + W1+" W2 value: " +W2;
	}
}
