import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * @author Akshay
 *
 */
public class QueryDocumentVectorGenerator {
	private TreeMap<String, IndexWrapper> indexMap = new TreeMap<>();
	private TreeMap<Integer, DocumentInfoWrapper> docMap = new TreeMap<>();
	private HashSet<String> stopWords = new HashSet<>();
	private ArrayList<String> queriesList = new ArrayList<>();
	private String cranFieldPath = " ";
	FileWriter fileObj = null;

	public static HashMap<Integer, List<QueryWrapper>> queryVectorMap = new HashMap<>();
	public static HashMap<Integer, List<QueryWrapper>> documentVectorMap = new HashMap<>();
	// public static HashMap<Integer, Double> W1ScoreMap = new HashMap<Integer,
	// Double>();
	// public static HashMap<Integer, Double> W2ScoreMap = new HashMap<Integer,
	// Double>();
	DocumentParser parser;

	public QueryDocumentVectorGenerator(
			TreeMap<String, IndexWrapper> indexedMap,
			TreeMap<Integer, DocumentInfoWrapper> docInfoMap,
			HashSet<String> stopWordsSet, ArrayList<String> queryList,
			String dataSetFilePath, FileWriter fileObj1) {
		this.indexMap = indexedMap;
		this.docMap = docInfoMap;
		this.stopWords = stopWordsSet;
		this.queriesList = queryList;
		parser = new DocumentParser();
		this.cranFieldPath = dataSetFilePath;
		this.fileObj = fileObj1;
	}

	/**
	 * 
	 * @param qID
	 * @param docAverageLen
	 * @throws IOException
	 */
	public FileWriter genrateVectors(int qID, double docAverageLen)
			throws IOException {
		int qTF = 0, qDF = 0, qMaxTF = 0, collectionSize = 0, queryLength = 0;
		HashMap<Integer, Double> W1ScoreMap = new HashMap<Integer, Double>();
		HashMap<Integer, Double> W2ScoreMap = new HashMap<Integer, Double>();
		String query = queriesList.get(qID);
		TreeMap<String, Integer> lemmatizedQueryMap = parser.queryParser(query,
				stopWords);

		/* Calculate the total collection size */
		collectionSize = docMap.size();

		/* Calculating total length of query */
		queryLength = lemmatizedQueryMap.size();

		TreeSet<Integer> commonDocsSet = findCommonDocs(lemmatizedQueryMap);

		/* Computing the max term frequency for each query */
		qMaxTF = calculateQueryMaxTF(lemmatizedQueryMap);
		List<QueryWrapper> queryInfoList = new ArrayList<>();
		for (String queryTerm : lemmatizedQueryMap.keySet()) {
			if (indexMap.containsKey(queryTerm)) {
				qTF = lemmatizedQueryMap.get(queryTerm);
				qDF = indexMap.get(queryTerm).documentFrequency;
				double W1 = computeQueryWeight1(qTF, qMaxTF, qDF,
						collectionSize);
				double W2 = computeQueryWeight2(qTF, qDF, queryLength,
						docAverageLen, collectionSize);

				QueryWrapper qWrapper = new QueryWrapper(queryTerm, W1, W2);
				queryInfoList.add(qWrapper);
			}

		}

		queryVectorMap.put(qID, queryInfoList);
		CosineSimilarity cos = new CosineSimilarity();
		double W1NormalizedQuery = cos.normalizeComponent(queryInfoList, 0);
		double W2NormalizedQuery = cos.normalizeComponent(queryInfoList, 1);

		for (int dID : commonDocsSet) {
			List<QueryWrapper> docInfoList = new ArrayList<>();

			for (String term : lemmatizedQueryMap.keySet()) {
				int flag = 0;
				IndexWrapper iWrapper = indexMap.get(term);
				if (iWrapper != null) {
					int df = iWrapper.documentFrequency;
					for (PostingListWrapper pl : iWrapper.postingList) {
						int tf = 0;

						if (pl.docId == dID) {
							tf = pl.termFrequencyPerDocument;
							DocumentInfoWrapper dWrapper = docMap.get(dID);
							int maxTF = dWrapper.maxTermFrequency;
							int docLen = dWrapper.documentLength;
							double W1 = computeQueryWeight1(tf, maxTF, df,
									collectionSize);
							double W2 = computeQueryWeight2(tf, df, docLen,
									docAverageLen, collectionSize);
							QueryWrapper docWrapperObj = new QueryWrapper(term,
									W1, W2);
							docInfoList.add(docWrapperObj);
							flag = 1;
						}
					}
				}
				if (flag == 0) {
					QueryWrapper docWrapperObj = new QueryWrapper(term, 0, 0);
					docInfoList.add(docWrapperObj);
				}
			}
			documentVectorMap.put(dID, docInfoList);

			double W1NormalizedDocument = cos
					.normalizeComponent(docInfoList, 0);
			double W2NormalizedDocument = cos
					.normalizeComponent(docInfoList, 1);

			double cosineSimilarityW1 = cos.computeCosineSimilarity(
					queryInfoList, docInfoList, W1NormalizedQuery,
					W1NormalizedDocument, 0);
			W1ScoreMap.put(dID, cosineSimilarityW1);
			double cosineSimilarityW2 = cos.computeCosineSimilarity(
					queryInfoList, docInfoList, W2NormalizedQuery,
					W2NormalizedDocument, 1);
			W2ScoreMap.put(dID, cosineSimilarityW2);
		}

		// Top 5 by W1 schema
		fileObj.write("Query is : \n" + queriesList.get(qID));
		System.out.println("\n Query is : \n" + queriesList.get(qID));
		System.out
				.println("\nTop 5 documents by W1 scheme are by cosine similarity:");
		fileObj.write("\nTop 5 documents by W1 scheme are by cosine similarity:");

		TreeMap<Integer, Double> W1TopDocumentMap = calculateTopDocs(W1ScoreMap);
		int cnt = 1;
		for (Entry<Integer, Double> entry : W1TopDocumentMap.entrySet()) {
			if (cnt != 6) {
				System.out.println("Rank : " + cnt + "  " + "DocID : "
						+ entry.getKey() + " " + "Score: "
						+ W1ScoreMap.get(entry.getKey()));
				fileObj.write("\nRank : " + cnt + "  " + "DocID : "
						+ entry.getKey() + " " + "Score : "
						+ W1ScoreMap.get(entry.getKey()));
				printHeadLine(entry.getKey(), fileObj);
				cnt++;
			} else {
				break;
			}
		}

		// Top 5 by W2 schema
		System.out
				.println("\nTop 5 documents by W2 scheme are by cosine similarity:");
		fileObj.write("\n\nTop 5 documents by W2 scheme are by cosine similarity:");

		TreeMap<Integer, Double> W2TopDocumentMap = calculateTopDocs(W2ScoreMap);
		int cnt2 = 1;
		for (Entry<Integer, Double> entry : W2TopDocumentMap.entrySet()) {
			if (cnt2 != 6) {
				System.out.println("Rank : " + cnt2 + "  " + "DocID : "
						+ entry.getKey() + " " + "Score: "
						+ W2ScoreMap.get(entry.getKey()));
				fileObj.write("\nRank : " + cnt2 + "  " + "DocID : "
						+ entry.getKey() + " " + "Score: "
						+ W2ScoreMap.get(entry.getKey()));
				printHeadLine(entry.getKey(), fileObj);
				// System.out.println("\n");
				cnt2++;
			} else {
				break;
			}
		}

		fileObj.write("\nDocument Vector Representation:");
		
		fileObj.write("\nDocument Vector--->W1:\n");

		System.out.println("\nDocument Vector Representation:\n");
		int docCntW1 = 1;
		System.out.println("\nDocument Vector--->W1:\n");

		for (Entry<Integer, Double> topEntry : W1TopDocumentMap.entrySet()) {
			if (docCntW1 != 6) {
				List<QueryWrapper> documentWrapperList = documentVectorMap
						.get(topEntry.getKey());
				System.out.println("Document : " + topEntry.getKey() + " ");
				System.out.print("[");
				fileObj.write("Document : " + topEntry.getKey() + " ");
				fileObj.write("[");
				for (QueryWrapper documentObj : documentWrapperList) {
					System.out.print(documentObj.W1 + ",");
					fileObj.write(documentObj.W1 + ",");
				}
				System.out.print("]\n");
				fileObj.write("]\n");
				docCntW1++;
			} else {
				break;
			}
		}

		System.out.println("\nDocument Vector--->W2:");
		fileObj.write("\nDocument Vector--->W2:\n");
		int docCntW2 = 1;
		for (Entry<Integer, Double> topEntry2 : W2TopDocumentMap.entrySet()) {
			if (docCntW2 != 6) {
				List<QueryWrapper> documentWrapperList2 = documentVectorMap
						.get(topEntry2.getKey());
				System.out.println("Document : " + topEntry2.getKey() + " ");
				System.out.print("[");
				fileObj.write("Document : " + topEntry2.getKey() + " ");
				fileObj.write("[");
				for (QueryWrapper documentObj2 : documentWrapperList2) {

					System.out.print(documentObj2.W2 + ",");
					fileObj.write(documentObj2.W2 + ",");
				}
				System.out.print("]\n");
				fileObj.write("]\n");
				docCntW2++;
			} else {
				break;
			}

		}
		// fileObj.close();
		return fileObj;

	}

	/**
	 * 
	 * @param docID
	 * @param fileObj
	 * @throws IOException
	 */
	private void printHeadLine(Integer docID, FileWriter fileObj)
			throws IOException {

		String fileName = getFileName(docID);
		System.out.print(" " + "FileName : " + "cranfield" + fileName + "\n");
		fileObj.write(" FileName : " + "cranfield" + fileName + "\n");
		BufferedReader br = null;
		try {
			// br = new BufferedReader(new FileReader(cranFieldPath + "\\"
			// + "cranfield" + fileName));
			br = new BufferedReader(new FileReader(cranFieldPath + "cranfield"
					+ fileName));
			String line = null;
			boolean flag = false;
			System.out.print("Title :");
			fileObj.write("Title :");
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				if (line.toLowerCase().contains("<title>")) {
					flag = true;
				} else if (line.toLowerCase().contains("</title>")) {
					flag = false;
					break;
				} else if (flag) {
					System.out.println(line);
					fileObj.write(line);
					// System.out.println("\n");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param docID
	 * @return
	 */
	private String getFileName(Integer docID) {
		if (docID < 10) {
			return "000" + docID;
		} else if (docID < 100) {
			return "00" + docID;
		} else if (docID < 1000) {
			return "0" + docID;
		} else {
			return Integer.toString(docID);
		}
	}

	/**
	 * 
	 * @param weightMap
	 * @return
	 */
	private TreeMap<Integer, Double> calculateTopDocs(
			HashMap<Integer, Double> weightMap) {
		ValueComparator vc = new ValueComparator(weightMap);
		TreeMap<Integer, Double> sortedTopMap = new TreeMap<>(vc);
		sortedTopMap.putAll(weightMap);
		return sortedTopMap;
	}

	/**
	 * 
	 * @param qTF
	 * @param qDF
	 * @param queryLength
	 * @param docAverageLen
	 * @param collectionSize
	 * @return
	 */
	private double computeQueryWeight2(int qTF, int qDF, int queryLength,
			double docAverageLen, int collectionSize) {

		double part1Num = qTF;
		double part1Deno = (qTF + 0.5 + 1.5 * (queryLength / docAverageLen));
		double part1Res = 0.6 * (part1Num / part1Deno);
		double part2Num = Math.log(collectionSize / qDF);
		double part2Deno = Math.log(collectionSize);
		double part2Result = (part2Num / part2Deno);
		double weight2Result = (part1Res * part2Result) + 0.4;
		if (Double.isNaN(weight2Result))
			return 0;
		else
			return weight2Result;

	}

	/**
	 * 
	 * @param qTF
	 * @param qMaxTF
	 * @param qDF
	 * @param collectionSize
	 * @return
	 */
	private double computeQueryWeight1(int qTF, int qMaxTF, int qDF,
			int collectionSize) {
		double part1num = (Math.log(qTF + 0.5) * 0.6);
		double part1deno = Math.log(qMaxTF + 1.0);
		double part1Res = (part1num / part1deno) + 0.4;
		double part2num = Math.log((double) collectionSize / (double) qDF);
		double part2deno = Math.log(collectionSize);
		double part2Res = (part2num / part2deno);
		double weight1Result = part1Res * part2Res;
		if (Double.isNaN(weight1Result))
			return 0;
		else
			return weight1Result;
	}

	/**
	 * 
	 * @param lemmatizedQueryMap
	 * @return
	 */
	private int calculateQueryMaxTF(TreeMap<String, Integer> lemmatizedQueryMap) {
		int maxTF = Integer.MIN_VALUE;

		for (String term : lemmatizedQueryMap.keySet()) {
			if (maxTF < lemmatizedQueryMap.get(term)) {
				maxTF = lemmatizedQueryMap.get(term);
			}
		}

		return maxTF;
	}

	/**
	 * 
	 * @param lemmatizedQueryMap
	 * @return
	 */
	private TreeSet<Integer> findCommonDocs(
			TreeMap<String, Integer> lemmatizedQueryMap) {
		TreeSet<Integer> commonDocs = new TreeSet<>();
		for (String term : lemmatizedQueryMap.keySet()) {
			if (indexMap.containsKey(term)) {
				List<PostingListWrapper> pList = indexMap.get(term).postingList;

				for (PostingListWrapper pl : pList) {
					commonDocs.add(pl.docId);
				}
			}
		}

		return commonDocs;
	}

}

/**
 * 
 * @author Akshay
 *
 */
class ValueComparator implements Comparator<Integer> {

	HashMap<Integer, Double> topMap;

	public ValueComparator(HashMap<Integer, Double> top5Map) {
		this.topMap = top5Map;
	}

	@Override
	public int compare(Integer a, Integer b) {
		if (topMap.get(a) >= topMap.get(b)) {
			return -1;
		} else {
			return 1;
		}
	}

}
