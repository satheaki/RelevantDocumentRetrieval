import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper class for dictionary Index storing term,document frequency and
 * pointers for posting list
 * 
 * @author Akshay
 */
public class IndexWrapper  {

	String term;
	Integer documentFrequency;
	 Integer totalTermFrequency;
	List<PostingListWrapper> postingList;

	 public IndexWrapper(String term, int docFrequency, int termFreq,
	 LinkedList<PostingListWrapper> documentList) {

//	public IndexWrapper(String term, int docFrequency,
//			LinkedList<PostingListWrapper> documentList) {

		this.term = term;
		this.documentFrequency = docFrequency;
		 this.totalTermFrequency = termFreq;
		this.postingList = documentList;
	}

	@Override
	public String toString() {
		return "[term:" + term + ", docFreq=" + documentFrequency
				+ ", postingList:" + postingList + "]";
	}
}

/**
 * Wrapper class for storing posting lists for documents,having document Id and
 * frequency of term per document
 * 
 * @author Akshay
 */
class PostingListWrapper{

	Integer docId;
	Integer termFrequencyPerDocument;

	public PostingListWrapper(int docId, int termFrequency) {
		this.docId = docId;
		this.termFrequencyPerDocument = termFrequency;
	}

	@Override
	public String toString() {
		return "(docId:" + docId + "--> termFreq:" + termFrequencyPerDocument
				+ ")";
	}

}

/**
 * Wrapper class for storing the information of each document,containing maximum
 * term frequency in each document and document length
 * 
 * @author Akshay
 */
class DocumentInfoWrapper {

	String term;
	Integer maxTermFrequency;
	Integer documentLength;
	String docName;
	Integer totalWords;
	

	public DocumentInfoWrapper(Integer maximumTermFrequency, Integer size,
			String maxTerm,String documentName,Integer doclen) {

		this.maxTermFrequency = maximumTermFrequency;
		this.totalWords = size;
		this.term = maxTerm;
		this.docName=documentName;
		this.documentLength=doclen;

	}

	@Override
	public String toString() {
		return "DocInfo [maxTermFreq=" + maxTermFrequency + ", docLen="
				+ documentLength + ", term=" + term + "]";
	}

}


