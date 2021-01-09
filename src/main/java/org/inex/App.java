package org.inex;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.inex.Model.Doc;
import org.inex.Model.Request;
import org.inex.Model.Score;
import org.inex.Parser.ParseRequest;
import org.inex.Parser.ParseTxt;
import org.inex.Parser.ParseXML;
import org.inex.Utils.UtilFrequencyCompute;
import org.inex.Utils.UtilWeightCompute;
import org.xml.sax.SAXException;

public class App {

	/**
	 * @param PATH_QUERY     Path to the requests file
	 * @param PATH_INPUT_TXT Path to the txt format documents
	 * @param PATH_INPUT_XML Path to the xml format documents
	 * @param PATH_OUTPUT    Path to the run file
	 */
	private static final String PATH_QUERY = "./files/request/topics_M2WI7Q_2020_21.txt";
	private static final String PATH_INPUT_TXT = "./files/input/txt/Text_Only_Ascii_Coll_MWI_NoSem.gz";
	private static final String PATH_INPUT_XML = "./files/input/xml/XML_Coll_MWI_withSem.tar.gz";
	private static final String PATH_OUTPUT = "./files/output/EliasNicolas_02_12_BM25_articles_k0.5b0.3.txt";

	/**
	 * Custom enum class to define weighting types
	 */
	public enum Weight {
		LTN, LTC, BM25;
	}

	/**
	 * Custom enum class to define input type
	 */
	public enum Input {
		TXT, XML_ARTICLES, XML_ELEMENTS;
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		read(false, Weight.BM25, Input.XML_ARTICLES);
	}

	/**
	 * Main program to generate a run from the input documents
	 * 
	 * @param applyStemming Boolean to choose using stemming during parsing
	 * @param weighting     Type of weighting (LTN, LTC, BM25)
	 * @param input         Type of the documents (TXT or XML)
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void read(boolean applyStemming, Weight weighting, Input input)
			throws IOException, ParserConfigurationException, SAXException {
		ArrayList<Doc> docList = new ArrayList<>();
		ArrayList<Request> requestList = ParseRequest.extractRequests(PATH_QUERY, applyStemming);
		if (input.equals(Input.TXT)) {
			docList = ParseTxt.extractTxt(PATH_INPUT_TXT, applyStemming);
			createRun(docList, requestList, weighting);
		} else if (input.equals(Input.XML_ARTICLES) || input.equals(Input.XML_ELEMENTS)) {
			ParseXML.extractTarGzXmlFiles(PATH_INPUT_XML);
			List<String> files = ParseXML.getXmlPathList();
			for (String path : files) {
				Doc doc = ParseXML.parseXmlFile(path, input, applyStemming);
				docList.add(doc);
			}
			if (input.equals(Input.XML_ARTICLES)) {
				createRun(docList, requestList, weighting);
			} else {
				createRunElements(docList, requestList, weighting);
			}
			// ParseXML.deleteTmpXmlFolder();
		}
	}

	/**
	 * Compute the score (articles) of the documents for each request
	 * 
	 * @param docList     List containing all the documents in the input file
	 * @param requestList List containing all the requests in the request file
	 * @param weighting   Type of weighting (LTN, LTC, BM25)
	 */
	public static void createRun(ArrayList<Doc> docList, ArrayList<Request> requestList, Weight weighting) {
		String inex = "";
		int docListSize = docList.size();
		double avg = UtilWeightCompute.avg(docList);
		for (int i = 0; i < requestList.size(); i++) {
			String id = requestList.get(i).getId();
			Map<String, Score> scores = new HashMap<>();
			ArrayList<String> terms = requestList.get(i).getTermList();
			ArrayList<Integer> dfs = UtilFrequencyCompute.docFreq(docList, terms);
			for (Doc d : docList) {
				Score score = new Score("/article[1]", 0, 0);
				int docSize = d.getContentList().size();
				ArrayList<Integer> tfs = UtilFrequencyCompute.termFreq(d, terms);
				for (int j = 0; j < terms.size(); j++) {
					int df = dfs.get(j);
					int tf = tfs.get(j);
					UtilWeightCompute.weight(score, df, tf, docSize, docListSize, avg, weighting);
				}
				if (weighting.equals(Weight.LTC)) {
					if (score.getNorm() != 0) {
						score.setValue(score.getValue() / Math.sqrt(score.getNorm()));
					}
				}
				scores.put(d.getId(), score);
			}
			inex = inex + writeRequestResult(id, scores);
		}
		writeRunResult(inex);
	}

	/**
	 * Compute the score (elements) of the documents for each request
	 * 
	 * @param docList     List containing all the documents in the input file
	 * @param requestList List containing all the requests in the request file
	 * @param weighting   Type of weighting (LTN, LTC, BM25)
	 */
	public static void createRunElements(ArrayList<Doc> docList, ArrayList<Request> requestList, Weight weighting) {
		String inex = "";
		int docListSize = docList.size();
		double avg = UtilWeightCompute.avgElements(docList);
		for (int i = 0; i < requestList.size(); i++) {
			String id = requestList.get(i).getId();
			Map<String, Score> scores = new HashMap<>();
			ArrayList<String> terms = requestList.get(i).getTermList();
			ArrayList<Map<String, Integer>> dfs = UtilFrequencyCompute.docFreqElements(docList, terms);
			for (Doc d : docList) {
				Map<String, Score> scoreByNode = new HashMap<>();
				ArrayList<Map<String, Integer>> tfs = UtilFrequencyCompute.termFreqElements(d, terms);
				for (int j = 0; j < terms.size(); j++) {
					for (String node : tfs.get(j).keySet()) {
						Score score = new Score(node, 0, 0);
						int nodeSize = d.getElements().get(node).size();
						int df = dfs.get(j).get(node);
						int tf = tfs.get(j).get(node);
						UtilWeightCompute.weight(score, df, tf, nodeSize, docListSize, avg, weighting);
						if (scoreByNode.containsKey(node)) {
							score.setValue(score.getValue() + scoreByNode.get(node).getValue());
							score.setNorm(score.getNorm() + scoreByNode.get(node).getNorm());
							scoreByNode.put(node, score);
						} else {
							scoreByNode.put(node, score);
						}
					}
				}
				if (scoreByNode.size() > 0) {
					String node = Collections.max(scoreByNode.entrySet(), Map.Entry.comparingByValue()).getKey();
					Score score = scoreByNode.get(node);
					if (weighting.equals(Weight.LTC)) {
						if (score.getNorm() != 0) {
							score.setValue(score.getValue() / Math.sqrt(score.getNorm()));
						}
					}
					scores.put(d.getId(), score);
				}
			}
			inex = inex + writeRequestResult(id, scores);
		}
		writeRunResult(inex);
	}

	/**
	 * Save the ranking result for one request
	 * 
	 * @param id     Request identifier
	 * @param scores Score of the documents in the list for this request
	 * @return Ranking of the top 1500 documents with the best score
	 */
	private static String writeRequestResult(String id, Map<String, Score> scores) {
		String result = "";
		int rank = 1;
		Comparator<Entry<String, Score>> comparator = Collections.reverseOrder(Map.Entry.comparingByValue());
		Map<String, Score> ranking = scores.entrySet().stream().sorted(comparator).collect(Collectors
				.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		for (String key : ranking.keySet()) {
			Score score = ranking.get(key);
			result = result + id;
			result = result + " " + "Q0";
			result = result + " " + key;
			result = result + " " + rank;
			result = result + " " + score.getValue();
			result = result + " " + "EliasNicolas";
			result = result + " " + score.getNode() + "\n";
			rank++;
			if (rank > 1500) {
				break;
			}
		}
		return result;
	}

	/**
	 * Save the ranking result for all the requests
	 * 
	 * @param inex Ranking of the top 1500 documents for all the requests
	 */
	private static void writeRunResult(String inex) {
		try {
			FileWriter writer = new FileWriter(PATH_OUTPUT);
			writer.write(inex);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
