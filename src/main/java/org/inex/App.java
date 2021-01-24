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
import org.inex.Model.GraphLink;
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
	 * @param ALPHA_POPULAR  Adjust the importance of the popularity bonus
	 */
	private static final String PATH_QUERY = "./files/request/topics_M2WI7Q_2020_21.txt";
	private static final String PATH_INPUT_TXT = "./files/input/txt/Text_Only_Ascii_Coll_MWI_NoSem.gz";
	private static final String PATH_INPUT_XML = "./files/input/xml/XML_Coll_MWI_withSem.tar.gz";
	private static final String PATH_OUTPUT = "./files/output/EliasNicolas_05_01_BM25_articles_k0.6b0.3.txt";
	private static final int ALPHA_POPULAR = 64;

	/**
	 * Custom enum class to define weighting type
	 * 
	 * @param LTN  Weighting request terms using LTN
	 * @param LTC  Weighting request terms using LTC
	 * @param BM25 Weighting request terms using BM25
	 */
	public enum Weight {
		LTN, LTC, BM25;
	}

	/**
	 * Custom enum class to define input type
	 * 
	 * @param TXT          Return content of a text document
	 * @param XML_ARTICLES Return content of article tag in XML documents
	 * @param XML_ELEMENTS Return content of specific tags in XML documents
	 */
	public enum Input {
		TXT, XML_ARTICLES, XML_ELEMENTS;
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		read(false, Weight.BM25, Input.XML_ARTICLES, false);
	}

	/**
	 * Main program to generate a run from the input documents
	 * 
	 * @param applyStemming   Boolean to choose using stemming during parsing
	 * @param weight          Type of weighting
	 * @param input           Type of the documents
	 * @param applyPopularity Boolean to choose using popularity of links
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void read(boolean applyStemming, Weight weight, Input input, boolean applyPopularity)
			throws IOException, ParserConfigurationException, SAXException {
		ArrayList<Doc> docList = new ArrayList<>();
		GraphLink linkList = null;
		ArrayList<Request> requestList = ParseRequest.extractRequests(PATH_QUERY, applyStemming);
		if (input.equals(Input.TXT)) {
			docList = ParseTxt.extractTxt(PATH_INPUT_TXT, applyStemming);
			createRun(docList, linkList, requestList, weight);
		} else if (input.equals(Input.XML_ARTICLES) || input.equals(Input.XML_ELEMENTS)) {
			ParseXML.extractTarGzXmlFiles(PATH_INPUT_XML);
			List<String> files = ParseXML.getXmlPathList();
			for (String path : files) {
				Doc doc = ParseXML.parseXmlFile(path, input, applyStemming);
				docList.add(doc);
			}
			if (input.equals(Input.XML_ARTICLES)) {
				if (applyPopularity) {
					linkList = new GraphLink(docList);
				}
				createRun(docList, linkList, requestList, weight);
			} else if (input.equals(Input.XML_ELEMENTS)) {
				createRunElements(docList, requestList, weight);
			}
			ParseXML.deleteTmpXmlFolder();
		}
	}

	/**
	 * Compute the score (articles) of the documents for each request
	 * 
	 * @param docList     List containing all the documents in the input file
	 * @param linkList    List containing the links pointed by all documents
	 * @param requestList List containing all the requests in the request file
	 * @param weight      Type of weighting
	 */
	public static void createRun(ArrayList<Doc> docList, GraphLink linkList, ArrayList<Request> requestList,
			Weight weight) {
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
					UtilWeightCompute.weight(score, df, tf, docSize, docListSize, avg, weight, "/article[1]");
				}
				if (weight.equals(Weight.LTC)) {
					if (score.getNorm() != 0) {
						score.setValue(score.getValue() / Math.sqrt(score.getNorm()));
					}
				}
				if (linkList != null) {
					double bonus = linkList.getArticleVertexList().stream().filter(doc -> doc.getId().equals(d.getId()))
							.collect(Collectors.toList()).get(0).getPopularity();
					if (bonus > 0) {
						score.setValue(score.getValue() + bonus * ALPHA_POPULAR);
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
	 * @param weight      Type of weighting
	 */
	public static void createRunElements(ArrayList<Doc> docList, ArrayList<Request> requestList, Weight weight) {
		String inex = "";
		int docListSize = docList.size();
		double avg = UtilWeightCompute.avgElements(docList);
		for (int i = 0; i < requestList.size(); i++) {
			String id = requestList.get(i).getId();
			Map<String, ArrayList<Score>> scores = new HashMap<>();
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
						UtilWeightCompute.weight(score, df, tf, nodeSize, docListSize, avg, weight, node);
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
					ArrayList<Score> scoreByDoc = new ArrayList<>();
					double total = 0;
					for (String node : scoreByNode.keySet()) {
						Score score = scoreByNode.get(node);
						if (weight.equals(Weight.LTC)) {
							if (score.getNorm() != 0) {
								score.setValue(score.getValue() / Math.sqrt(score.getNorm()));
							}
						}
						total = total + score.getValue();
					}
					double avgScore = total / scoreByNode.size();
					/**
					 * Take elements with a score higher or equal to the average score of the
					 * elements in the document
					 */
					for (String node : scoreByNode.keySet()) {
						Score score = scoreByNode.get(node);
						if (score.getValue() >= avgScore) {
							scoreByDoc.add(score);
						}
					}
					Collections.sort(scoreByDoc, Collections.reverseOrder());
					/**
					 * Remove overlapping elements
					 */
					for (int j = 0; j < scoreByDoc.size() - 1; j++) {
						String node1 = scoreByDoc.get(j).getNode();
						for (int k = j + 1; k < scoreByDoc.size(); k++) {
							String node2 = scoreByDoc.get(k).getNode();
							if (node1.contains(node2) || node2.contains(node1)) {
								scoreByDoc.remove(k);
								k = k - 1;
							}
						}
					}
					/**
					 * Uncomment the below code to take the top 50% of the elements above the
					 * average score
					 */
					// scoreByDoc.subList((scoreByDoc.size() - 1) / 2, scoreByDoc.size() - 1).clear();
					/**
					 * Uncomment the below code to take the top 25% of the elements above the
					 * average score
					 */
					// scoreByDoc.subList(((scoreByDoc.size() - 1) / 2) / 2, scoreByDoc.size() - 1).clear();
					scores.put(d.getId(), scoreByDoc);
				}
			}
			inex = inex + writeRequestResultElements(id, scores);
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
		int limit = 1500;
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
			if (rank > limit) {
				break;
			}
		}
		return result;
	}

	/**
	 * Save the ranking result for one request
	 * 
	 * @param id     Request identifier
	 * @param scores Score of the elements in the documents for this request
	 * @return Ranking of the top 1500 documents with the best score
	 */
	private static String writeRequestResultElements(String id, Map<String, ArrayList<Score>> scores) {
		String result = "";
		int rank = 1;
		int limit = 1500;
		Map<String, Double> scoreByDoc = new HashMap<>();
		Comparator<Entry<String, Double>> comparator = Collections.reverseOrder(Map.Entry.comparingByValue());
		/**
		 * Uncomment the below code to rank documents using the element with the best
		 * score in each document
		 */
		for (String node : scores.keySet()) {
			scoreByDoc.put(node, scores.get(node).get(0).getValue());
		}
		/**
		 * Uncomment the below code to rank documents using the best average score of
		 * elements in each document
		 */
		/*
		for (String node : scores.keySet()) {
			double total = 0;
			for (int i = 0; i < scores.get(node).size(); i++) {
				total = total + scores.get(node).get(i).getValue();
			}
			scoreByDoc.put(node, total / scores.get(node).size());
		}
		*/
		Map<String, Double> ranking = scoreByDoc.entrySet().stream().sorted(comparator).collect(Collectors
				.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		for (String key : ranking.keySet()) {
			for (int i = 0; i < scores.get(key).size(); i++) {
				Score score = scores.get(key).get(i);
				result = result + id;
				result = result + " " + "Q0";
				result = result + " " + key;
				result = result + " " + rank;
				result = result + " " + (limit - rank + 1);
				result = result + " " + "EliasNicolas";
				result = result + " " + score.getNode() + "\n";
				rank++;
				if (rank > limit) {
					break;
				}
			}
			if (rank > limit) {
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
