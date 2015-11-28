import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Class for building the dictionary index containing term,document frequency
 * and the posting list for the term
 * 
 * @author Akshay
 *
 */
public class IndexBuilder {

	private HashSet<String> stopWordsSet = new HashSet<>();
	String cranfieldDataSetPath = "";
	TreeMap<String, IndexWrapper> indexerMap = null;
	TreeMap<Integer, DocumentInfoWrapper> documentInfoMap = null;

	public IndexBuilder(HashSet<String> stopWordsSet, String dataSetFilePath) {
		this.stopWordsSet = stopWordsSet;
		this.cranfieldDataSetPath = dataSetFilePath;
		indexerMap = new TreeMap<String, IndexWrapper>();
		documentInfoMap = new TreeMap<Integer, DocumentInfoWrapper>();
	}

	/**
	 * Method for building the dictionary index by parsing each file from the
	 * dataset
	 * 
	 * @return :Returns a uncompressed TreeMap containing term,document
	 *         frequency and posting list for each term
	 */
	public TreeMap<String, IndexWrapper> buildDictionaryIndex() {
		TreeMap<String, Integer> lemmatizedTokenMap;
		int docId = 0;
		DocumentParser docParser = new DocumentParser();

		File folder = new File(cranfieldDataSetPath);
		if (folder.exists() && folder.isDirectory()) {
			File[] cranfieldDatabaseFiles = folder.listFiles();
			Arrays.sort(cranfieldDatabaseFiles);
			for (File file : cranfieldDatabaseFiles) {
				++docId;
				if (file.isFile()) {
					lemmatizedTokenMap = docParser.parseSingleFile(file,
							stopWordsSet);

					addToDictionaryIndex(lemmatizedTokenMap, file, docId);
				}
			}
		} else {
			System.out.println("Folder Path cannot be found");
		}

		return indexerMap;

	}

	/**
	 * Method for adding the term to the dictionary index
	 * 
	 * @param lemmatizedTokenMap
	 *            :A map containing terms as lemmas
	 * @param file
	 *            :Single file from the cranfield dataset
	 * @param docId
	 *            :Unique Id identifying each document
	 */
	private void addToDictionaryIndex(
			TreeMap<String, Integer> lemmatizedTokenMap, File file, int docId) {

		int termFrequency = 0, maximumTermFrequency = 0;
		String maxTerm = "";
		for (String lemma : lemmatizedTokenMap.keySet()) {
			termFrequency = lemmatizedTokenMap.get(lemma);
			if (termFrequency > maximumTermFrequency) {
				maximumTermFrequency = termFrequency;
				maxTerm = lemma;
			}

			constructPostingList(lemma, lemmatizedTokenMap.get(lemma), file,
					docId);
		}

		int doclen = 0;
		for (Entry<String, Integer> map : lemmatizedTokenMap.entrySet()) {
			doclen = doclen + map.getValue();
		}

		// TODO:check for stemmedtokenMap size-->included/excluded stop words
		documentInfoMap.put(docId,
				new DocumentInfoWrapper(maximumTermFrequency,
						lemmatizedTokenMap.size(), maxTerm, file.getName(),
						doclen));

	}

	/**
	 * Method for creating the posting list for each term in the lemmatized
	 * token map
	 * 
	 * @param term
	 *            :Individual lemma
	 * @param termFrequency
	 *            :total occurrance of term in the cranfield dataset
	 * @param file
	 *            :Single file from the cranfield dataset
	 * @param docId
	 *            :Unique Id identifying each document
	 */
	private void constructPostingList(String term, Integer termFrequency,
			File file, int docId) {

		IndexWrapper indexEntryWrapper = indexerMap.get(term);
		if (indexEntryWrapper == null) {

			// indexEntryWrapper=new IndexWrapper(term,0,0,new
			// LinkedList<PostingListWrapper>());
			PostingListWrapper listWrapper = new PostingListWrapper(docId,
					termFrequency);
			indexEntryWrapper = new IndexWrapper(term, 1, termFrequency,
					new LinkedList<PostingListWrapper>());

			// indexEntryWrapper = new IndexWrapper(term, 1,
			// new LinkedList<PostingListWrapper>());

			indexEntryWrapper.postingList.add(listWrapper);
			indexerMap.put(term, indexEntryWrapper);
		}

		else {
			indexEntryWrapper.documentFrequency += 1;
			indexEntryWrapper.totalTermFrequency += termFrequency;
			PostingListWrapper listWrapper = new PostingListWrapper(docId,
					termFrequency);
			indexEntryWrapper.postingList.add(listWrapper);
			indexerMap.put(term, indexEntryWrapper);
		}

	}

}
