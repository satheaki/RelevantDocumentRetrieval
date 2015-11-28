import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Class for index creation and compression.
 * 
 * @author Akshay
 *
 */
public class IndexProcesser {
	private static int phase = 0;
	private static boolean flag = true;
	private static long startTime, endTime, elapsedTime;
	/* A set containing all the stop words */
	private static HashSet<String> stopWordsSet = new HashSet<String>();

	/* An index map having term against df and posting list mapping */
	private static TreeMap<String, IndexWrapper> indexMap = new TreeMap<>();

	/* A TreeMap to hold information about the documents */
	private static TreeMap<Integer, DocumentInfoWrapper> docInfoMap = new TreeMap<>();

	private static TreeMap<Integer, TreeMap<String, Integer>> queryInfoMap = new TreeMap<>();

	/**
	 * 
	 * @param args
	 * @throws IOException
	 *             :Input/Output Exception
	 */
	public static void main(String[] args) throws IOException {

		String stopWordFilePath = "";
		String dataSetFilePath = "";
		String queryFilePath = "";
		int qId = 0;
		ArrayList<String> queryList = new ArrayList<>();
		TreeMap<String, Integer> lemmatizedQueryMap = new TreeMap<>();
		String fileName = "DataOutputFile.txt";
		FileWriter fileObj = new FileWriter(fileName, true);

		if (args.length <3) {
			System.out
					.println("Incorrect input format.Enter <stopwords,dataset,queries> file path");
		}
		stopWordFilePath = args[0];
		dataSetFilePath = args[1];
		queryFilePath = args[2];

//		stopWordFilePath = "C:\\My files\\Information Retrieval\\stopwordlist.txt";
//		dataSetFilePath = "C:\\My files\\Information Retrieval\\CranfieldDatabase";
//		queryFilePath = "C:\\My files\\Information Retrieval\\Queries\\queries.txt";
		
		parseStopWords(stopWordFilePath);
		

		IndexBuilder iBuilder = new IndexBuilder(stopWordsSet, dataSetFilePath);
		timer();
		indexMap = iBuilder.buildDictionaryIndex();
		docInfoMap = iBuilder.documentInfoMap;
		timer();

		byte[] encodedQueryData = Files.readAllBytes(Paths.get(queryFilePath));
		String queries = new String(encodedQueryData).trim();
		String[] splitter = queries.split("[Q0-9:]+");
		// System.out.print("Size of Query:" + splitter.length);
		for (String part : splitter) {
			part = part.trim().replaceAll("\\r\\n", " ");
			if (!part.isEmpty()) {
				queryList.add(part);
			}
		}

		double docAverageLen = computeDocumentAverage(docInfoMap);
		QueryDocumentVectorGenerator vectorGen = new QueryDocumentVectorGenerator(
				indexMap, docInfoMap, stopWordsSet, queryList, dataSetFilePath,
				fileObj);

		for (int i = 0; i < queryList.size(); i++) {
			fileObj = vectorGen.genrateVectors(i, docAverageLen);
		}

		System.out.println("\nQuery Vector Representation:");
		fileObj.write("\nQuery Vector Representation:");

		for (int i = 0; i < queryList.size(); i++) {
			List<QueryWrapper> queryWrapperList = QueryDocumentVectorGenerator.queryVectorMap
					.get(i);
			System.out.println("Query : " + i + " ");
			fileObj.write("Query : " + i + " ");
			System.out.print("[");
			fileObj.write("[");
			for (QueryWrapper queryObj : queryWrapperList) {
				// System.out.println("Term: " + queryObj.queryTerm + "\tW1 : "
				// + queryObj.W1 + "\tW2 : " + queryObj.W2);

				System.out.print(queryObj.W1 + ",");
				fileObj.write(queryObj.W1 + ",");
			}
			System.out.print("]\n");
			fileObj.write("]\n");

			System.out.print("[");
			fileObj.write("[");
			for (QueryWrapper queryObj : queryWrapperList) {
				// System.out.println("Term: " + queryObj.queryTerm + "\tW1 : "
				// + queryObj.W1 + "\tW2 : " + queryObj.W2);
				System.out.print(queryObj.W2 + ",");
				fileObj.write(queryObj.W2 + ",");
			}
			System.out.print("]\n");
			fileObj.write("]\n");

		}
		fileObj.close();

	}

	/**
	 * 
	 * @param docInfoMap2
	 * @return
	 */
	private static double computeDocumentAverage(
			TreeMap<Integer, DocumentInfoWrapper> docInfoMap1) {
		int docLen = 0;
		for (DocumentInfoWrapper d : docInfoMap1.values()) {
			docLen = docLen + d.documentLength;
		}
		return (docLen / docInfoMap1.size());
	}

	/**
	 * Method for calculating the total time required for the program
	 */
	public static void timer() {
		if (phase == 0) {
			startTime = System.currentTimeMillis();
			phase = 1;
		} else {
			endTime = System.currentTimeMillis();
			elapsedTime = endTime - startTime;
			System.out.println("\nTime taken to build the index: "
					+ elapsedTime + " msec.");
			phase = 0;
		}
	}

	/**
	 * Method for parsing file containing stop words and storing the stop words
	 * in a Set
	 * 
	 * @param stopWordFilePath
	 *            :Path of file containing stopwords
	 */
	private static void parseStopWords(String stopWordFilePath) {
		String[] words;
		String stop = "";
		try {
			// TODO:Use Scanner

			BufferedReader buffReader = new BufferedReader(new FileReader(
					stopWordFilePath));
			for (String eachline = buffReader.readLine().toLowerCase(); eachline != null; eachline = buffReader
					.readLine()) {
				// words = eachline.split("");
				// for (String sword : eachline) {
				stopWordsSet.add(eachline);
				// }
			}
			buffReader.close();

		} catch (Exception e) {

		}

	}

}
