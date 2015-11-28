import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Class for Parsing and Processing the query,and computing the vector
 * representation for queries and documents.
 * 
 * @author Akshay
 */
public class QueryProcessor {

	TreeMap<String, IndexWrapper> indexMap;
	TreeMap<Integer, DocumentInfoWrapper> documentMap;
	TreeMap<Integer, TreeMap<String, Integer>> queryMap;
	HashSet<String> stopWordsSet;
	ArrayList<String> queriesList;
	DocumentParser docParser;
	HashMap<Integer, HashMap<String, Double>> W1QueryVectorMap = new HashMap<>();
	HashMap<Integer, HashMap<String, Double>> W2QueryVectorMap = new HashMap<>();
	HashMap<String, HashMap<Integer, Double>> W1DocumentVectorMap = new HashMap<>();
	HashMap<String, HashMap<Integer, Double>> W2DocumentVectorMap = new HashMap<>();

	public QueryProcessor(TreeMap<String, IndexWrapper> indexMap,
			TreeMap<Integer, DocumentInfoWrapper> docInfoMap,
			HashSet<String> stopWordsSet, ArrayList<String> queryList,
			TreeMap<Integer, TreeMap<String, Integer>> queryInfoMap) {
		this.indexMap = indexMap;
		this.documentMap = docInfoMap;
		this.stopWordsSet = stopWordsSet;
		this.queriesList = queryList;
		this.queryMap = queryInfoMap;
		docParser = new DocumentParser();
	}

	/**
	 * Method for calculating the Vector representation of the query using
	 * different schemes.
	 * 
	 * @param cnt
	 *            : counter indicating the query number
	 */
	public void computeQueryVector(int cnt) {
		int collectionSize = 0, queryLenSum = 0, querySize = 0, queryLen = 0;
		double queryAverage = 0;

		// ArrayList<Double>W1Vector=new ArrayList<>();
		// ArrayList<Double>W2Vector=new ArrayList<>();

		/* Calculating the total query size */
		collectionSize = queryMap.size();

		/* Calculating average Query Length */
		queryAverage = computeAverageQueryLength(collectionSize);

		TreeMap<String, Integer> currentQueryMap = queryMap.get(cnt);

		/* Calculating query length */
		for (int len : currentQueryMap.values()) {
			queryLen += len;
		}

		for (String term : currentQueryMap.keySet()) {
			int qTF = currentQueryMap.get(term);
			int qMaxTF = computeQueryMaxTF(currentQueryMap);
			int qDF = computeQueryDF(term);
			double W1 = computeVectorByW1(qTF, qMaxTF, qDF, collectionSize);
			double W2 = computeVectorByW2(qTF, queryLen, queryAverage, qDF,
					collectionSize);
			populateW1QueryVector(term, W1, cnt);
			populateW2QueryVector(term, W2, cnt);

		}

		/*
		 * for (QueryWrapper qwrapper : queryMap.values()) { queryLenSum =
		 * queryLenSum + qwrapper.queryLength; }
		 */

		/* Calculating the average query length */
		// queryAverage = (queryLenSum / collectionSize);

		/*
		 * QueryWrapper qwrap = queryMap.get(cnt);
		 * 
		 * for(String qTerm : qwrap.perQueryTFMap.keySet()) { IndexWrapper
		 * iwrapper = indexMap.get(qTerm); if (iwrapper != null) { int DF =
		 * iwrapper.documentFrequency; int TF = qwrap.perQueryTFMap.get(qTerm);
		 * int maxTF = qwrap.queryMaxTf; int queryLen = qwrap.queryLength;
		 * double W1 = computeVectorByW1(TF, maxTF, DF, collectionSize); double
		 * W2 = computeVectorByW2(TF,queryLen, queryAverage, DF,
		 * collectionSize); W1Vector.add(W1); W2Vector.add(W2);
		 * W1QueryVectorMap.put(cnt,W1Vector);
		 * W2QueryVectorMap.put(cnt,W2Vector); } }
		 * 
		 * System.out.println("\nQuery : " + cnt); for(Entry<Integer,
		 * ArrayList<Double>> entry:W1QueryVectorMap.entrySet()){
		 * System.out.println(entry.getValue()); }
		 */
		
		System.out.println("\nQuery:"+cnt);
		for(Entry<Integer,HashMap<String,Double>>entry:W1QueryVectorMap.entrySet()){
			HashMap<String,Double>currentMap=entry.getValue();
			for(Entry<String,Double>currEntry:currentMap.entrySet()){
				System.out.println("["+currEntry.getValue()+"]");
			}
		}
	}

	/**
	 * 
	 * @param term
	 * @param w2
	 * @param cnt
	 */
	private void populateW2QueryVector(String term, double W2, int cnt) {
		if (W2QueryVectorMap.containsKey(cnt)) {
			HashMap<String, Double> currentVectorMap = W2QueryVectorMap
					.get(cnt);
			if (currentVectorMap.containsKey(term)) {
				currentVectorMap.put(term, currentVectorMap.get(term) + W2);
			} else {
				currentVectorMap.put(term, W2);
			}
		} else {
			HashMap<String, Double> nextQueryVectorMap = new HashMap<>();
			nextQueryVectorMap.put(term, W2);
			W2QueryVectorMap.put(cnt, nextQueryVectorMap);
		}

	}

	/**
	 * 
	 * @param term
	 * @param w1
	 * @param cnt
	 */
	private void populateW1QueryVector(String term, double W1, int cnt) {
		if (W1QueryVectorMap.containsKey(cnt)) {
			HashMap<String, Double> currentVectorMap = W1QueryVectorMap
					.get(cnt);
			if (currentVectorMap.containsKey(term)) {
				currentVectorMap.put(term, currentVectorMap.get(term) + W1);
			} else {
				currentVectorMap.put(term, W1);
			}
		} else {
			HashMap<String, Double> nextQueryVectorMap = new HashMap<>();
			nextQueryVectorMap.put(term, W1);
			W1QueryVectorMap.put(cnt, nextQueryVectorMap);
		}
	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	private int computeQueryDF(String term) {
		int queryDF = 0;
		for (Entry<Integer, TreeMap<String, Integer>> entry : queryMap
				.entrySet()) {
			TreeMap<String, Integer> currentQueryMap = entry.getValue();
			if (currentQueryMap.containsKey(term)) {
				queryDF++;
			}
		}

		return queryDF;
	}

	/**
	 * 
	 * @param currentQueryMap
	 * @return
	 */
	private int computeQueryMaxTF(TreeMap<String, Integer> currentQueryMap) {
		int maxTF = 0;
		for (Entry<String, Integer> entry : currentQueryMap.entrySet()) {
			if (maxTF < entry.getValue())
				maxTF = entry.getValue();
		}

		return maxTF;

	}

	/**
	 * 
	 * @param collectionSize
	 * @return
	 */
	private double computeAverageQueryLength(int collectionSize) {
		int queryLen = 0;
		for (Entry<Integer, TreeMap<String, Integer>> queryEntry : queryMap
				.entrySet()) {
			TreeMap<String, Integer> singleQueryMap = queryEntry.getValue();
			for (Entry<String, Integer> eachTermEntry : singleQueryMap
					.entrySet()) {
				queryLen = queryLen + eachTermEntry.getValue();
			}
		}
		return (queryLen / collectionSize);
	}

	/**
	 * 
	 * @param tf
	 * @param queryLen
	 * @param queryAverage
	 * @param df
	 * @param collectionSize
	 * @return
	 */
	private double computeVectorByW2(int tf, int queryLen, double queryAverage,
			int df, int collectionSize) {
		double part1 = tf;
		double depart1 = (tf + 0.5 + 1.5 * (queryLen / queryAverage));
		double numerator = 0.6 * (part1 / depart1);
		double part2 = Math.log(collectionSize / df);
		double depart2 = Math.log(collectionSize);
		double denominator = (part2 / depart2);
		double partVector = (numerator * denominator) + 0.4;
		// Check if final value is infinity
		if (Double.isNaN(partVector))
			return 0;
		else
			return partVector;

	}

	/**
	 * 
	 * @param tf
	 * @param maxtf
	 * @param df
	 * @param collectionSize
	 * @return
	 */
	private double computeVectorByW1(int tf, int maxtf, int df,
			int collectionSize) {
		if(df==0)
			return 0;
		double part1 = (Math.log(tf + 0.5) * 0.6);
		double depart1 = Math.log(maxtf + 1.0);
		double numerator = (part1 / depart1) + 0.4;
		double part2 = Math.log(collectionSize / df);
		double depart2 = Math.log(collectionSize);
		double denominator = (part2 / depart2);
		double partVector = numerator * denominator;
		// Check if final value is infinity
		if (Double.isNaN(partVector))
			return 0;
		else
			return partVector;
	}

	/**
	 * 
	 */
	public void computeDocumentVectors(int cnt) {
		int documentCollectionSize = 0;
		double avgDocumentLen = 0;

		documentCollectionSize = documentMap.size();
		avgDocumentLen = computeAverageDocumentLength(documentCollectionSize);

		TreeMap<String, Integer> currentWordMap = queryMap.get(cnt);

		for (String term : currentWordMap.keySet()) {
			HashMap<Integer, Integer> tf = computeDocumentTF(term);
			HashMap<Integer, Integer> maxTF = computeDocumentMaxTF();
			int df = computeDocumentDF(term);
			HashMap<Integer, Double> innerDocVectorW1 = new HashMap<Integer, Double>();
			HashMap<Integer, Double> innerDocVectorW2 = new HashMap<Integer, Double>();

			for (int i =1 ; i <= documentCollectionSize; i++) {
				double W1 = computeVectorByW1(tf.get(i), maxTF.get(i), df,
						documentCollectionSize);
				innerDocVectorW1.put(i, W1);
				double W2 = computeVectorByW2(tf.get(i),
						documentMap.get(i).documentLength, avgDocumentLen, df,
						documentCollectionSize);
				innerDocVectorW2.put(i, W2);
			}

			W1DocumentVectorMap.put(term, innerDocVectorW1);
			W2DocumentVectorMap.put(term, innerDocVectorW2);

		}
		
		

	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	private int computeDocumentDF(String term) {
		if(indexMap.get(term)==null)
		return 0;
	 return indexMap.get(term).postingList.size();
	}

	/**
	 * 
	 * @return
	 */
	private HashMap<Integer, Integer> computeDocumentMaxTF() {
		HashMap<Integer,Integer>maxTF=new HashMap<>();
		for(Entry<Integer, DocumentInfoWrapper>entry:documentMap.entrySet()){
			maxTF.put(entry.getKey(),entry.getValue().maxTermFrequency);
		}
		return maxTF;
	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	private HashMap<Integer, Integer> computeDocumentTF(String term) {
		HashMap<Integer,Integer>docTF=new HashMap<>();
		IndexWrapper iwrapper=indexMap.get(term);
		LinkedList<PostingListWrapper>pList=(LinkedList<PostingListWrapper>) iwrapper.postingList;
		for(PostingListWrapper pl:pList){
			docTF.put(pl.docId, pl.termFrequencyPerDocument);
		}
//		TreeMap<Integer,Integer> postings=(TreeMap<Integer, Integer>) iwrapper.postingList;
//		for(Entry<Integer,Integer>entry:postings.entrySet()){
//			docTF.put(entry.getKey(), entry.getValue());
//		}
		
		return docTF;
	}

	/**
	 * 
	 * @param documentCollectionSize
	 * @return
	 */
	private double computeAverageDocumentLength(int documentCollectionSize) {
		int docLen=0;
		for(DocumentInfoWrapper docWrapper:documentMap.values()){
			docLen+=docWrapper.documentLength;
		}
		return(docLen/documentCollectionSize);
	}

}
