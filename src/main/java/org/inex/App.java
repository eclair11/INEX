package org.inex;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	private static final String PATH_OUTPUT = "./files/output/EliasNicolas_XX_XX_LTN_articles.txt";

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
		TXT, XML;
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		read(false, Weight.LTN, Input.TXT);
	}

	public static void read(boolean applyStemming, Weight weighting, Input input)
			throws IOException, ParserConfigurationException, SAXException {
		ArrayList<Doc> docList = new ArrayList<>();
		ArrayList<Request> requestList = ParseRequest.extractRequests(PATH_QUERY, applyStemming);
		switch (input) {
			case TXT:
				docList = ParseTxt.extractTxt(PATH_INPUT_TXT, applyStemming);
				break;
			case XML:
				ParseXML.extractTarGzXmlFiles(PATH_INPUT_XML);
				List<String> files = ParseXML.getXmlPathList();
				for (String path : files) {
					Doc doc = ParseXML.parseXmlFile(path, applyStemming);
					docList.add(doc);
				}
				break;
			default:
				break;
		}
		createRun(docList, requestList, weighting);
		if (input.equals(Input.XML)) {
			// ParseXML.deleteTmpXmlFolder();
		}
	}

	/**
	 * Compute the score of the documents in the list for each request
	 * 
	 * @param docList     List containing all the documents in the input file
	 * @param requestList List containing all the requests in the request file
	 * @param weighting   Type of weighting (LTN, LTC, BM25, ...)
	 */
	public static void createRun(ArrayList<Doc> docList, ArrayList<Request> requestList, Weight weighting) {
		String inex = "";
		ArrayList<Score> scores = new ArrayList<>();
		Map<String, Double> norm = new HashMap<>();
		int docListSize = docList.size();
		double avg = UtilWeightCompute.avg(docList);
		for (int i = 0; i < requestList.size(); i++) {
			String id = requestList.get(i).getId();
			ArrayList<String> terms = requestList.get(i).getTermList();
			ArrayList<Integer> dfs = UtilFrequencyCompute.docFreq(docList, terms);
			for (Doc d : docList) {
				double score = 0;
				norm.put("value", 0.0);
				int docSize = d.getContentList().size();
				ArrayList<Integer> tfs = UtilFrequencyCompute.termFreq(d, terms);
				for (int j = 0; j < terms.size(); j++) {
					int df = dfs.get(j);
					int tf = tfs.get(j);
					score = score + UtilWeightCompute.weight(df, tf, docSize, docListSize, avg, norm, weighting);
				}
				if (weighting.equals(Weight.LTC)) {
					if (norm.get("value") != 0) {
						score = score / Math.sqrt(norm.get("value"));
					}
				}
				scores.add(new Score(d.getId(), score, "/article[1]"));
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
	private static String writeRequestResult(String id, ArrayList<Score> scores) {
		String ranking = "";
		int rank = 1;
		scores.sort(Comparator.comparing(Score::getValue).reversed());
		Iterator<Score> it = scores.iterator();
		while (rank <= 1500 && it.hasNext()) {
			Score score = it.next();
			ranking = ranking + id;
			ranking = ranking + " " + "Q0";
			ranking = ranking + " " + score.getId();
			ranking = ranking + " " + rank;
			ranking = ranking + " " + score.getValue();
			ranking = ranking + " " + "EliasNicolas";
			ranking = ranking + " " + score.getNode() + "\n";
			rank++;
		}
		return ranking;
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
