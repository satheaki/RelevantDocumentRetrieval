import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Program for tokenizing and lemmatizing words from Cranfield Database
 * 
 * @author Akshay
 */
public class DocumentParser {

	/* TreeMap containing tokens as key with their frequency as value */
	private static TreeMap<String, Integer> tokenMap;
	StanfordLemmatizer lemmatizer;

	public DocumentParser() {
		lemmatizer = new StanfordLemmatizer();
		tokenMap = new TreeMap<String, Integer>();
	}

	/**
	 * Method for parsing each file from the cranfield collection and creating
	 * lemmas
	 * 
	 * @param file
	 *            :single file from the cranfield dataset
	 * @param stopWords
	 *            :Set of stopwords
	 * @return :Returns a TreeMap containing tokens as lemmas
	 */
	public TreeMap<String, Integer> parseSingleFile(File file,
			HashSet<String> stopWords) {
		String token = "";
		String lemmatizedToken = "";
		List<String> lemmaList = new LinkedList<String>();
		try {
			tokenMap = new TreeMap<String, Integer>();
			Scanner wordScanner = new Scanner(file);
			while (wordScanner.hasNext()) {
				token = wordScanner.next();
				token = token.replaceAll("\\<.*?>", "").replaceAll("\\.", "")
						.trim().toLowerCase();

				// TODO:check for regular expression condition
				token = token.replaceAll("[^a-zA-Z]+", "");

				lemmaList = lemmatizer.lemmatize(token);

				for (String lemma : lemmaList) {
					if (stopWords.contains(lemma)) {
						continue;
					} else {
						lemmatizedToken = lemma;

						parseSimpleToken(lemmatizedToken);
					}
				}

			}
			wordScanner.close();
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		}
		return tokenMap;
	}
	
	TreeMap<String,Integer> queryParser(String query,HashSet<String>stopWords){
		String token = "";
		String lemmatizedToken = "";
		List<String> lemmaList = new LinkedList<String>();
		try {
			tokenMap = new TreeMap<String, Integer>();
			Scanner wordScanner = new Scanner(query);
			while (wordScanner.hasNext()) {
				token = wordScanner.next();
				token = token.replaceAll("\\<.*?>", "").replaceAll("\\.", "")
						.trim().toLowerCase();

				// TODO:check for regular expression condition
				token = token.replaceAll("[^a-zA-Z]+", "");

				lemmaList = lemmatizer.lemmatize(token);

				for (String lemma : lemmaList) {
					if (stopWords.contains(lemma)) {
						continue;
					} else {
						lemmatizedToken = lemma;

						parseSimpleToken(lemmatizedToken);
					}
				}

			}
			wordScanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tokenMap;
	}

	/**
	 * Method for Parsing token and calculating token frequency
	 * 
	 * @param token
	 *            :Single token from each file
	 */
	private static void parseSimpleToken(String token) {
		if (!token.isEmpty()) {
			if (tokenMap.containsKey(token)) {
				tokenMap.put(token, tokenMap.get(token) + 1);
			} else {
				tokenMap.put(token, 1);
			}
		}
	}

}
