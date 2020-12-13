package org.inex;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.inex.Model.Doc;
import org.inex.Model.Request;
import org.inex.Parser.ParseRequest;
import org.inex.Parser.ParseTxt;
import org.inex.Parser.ParseXML;
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
	private static final String PATH_OUTPUT = "./files/output/EliasNicolas_02_02_bm25_articles_k0.5b0.3stemming.txt";

	/**
	 * Custom enum class to define weighting types
	 */
	public enum Weight {
		LTN, BM25;
	}

	public static void main(String[] args) {
		// readTxt(true, Weight.BM25);
		readXml(true, Weight.BM25);
	}

	/**
	 * Parse the input file using the txt parser
	 * 
	 * @param applyStemming Boolean to choose using stemming during parsing
	 * @param weighting     Type of weighting (LTN, LTC, BM25, ...)
	 */
	public static void readTxt(boolean applyStemming, Weight weighting) {
		ArrayList<Doc> docList = new ArrayList<>();
		try {
			docList = ParseTxt.extractTxt(PATH_INPUT_TXT, applyStemming);
			createRun(docList, applyStemming, weighting);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parse the input files using the xml parser
	 * 
	 * @param applyStemming Boolean to choose using stemming during parsing
	 * @param weighting     Type of weighting (LTN, LTC, BM25, ...)
	 */
	public static void readXml(boolean applyStemming, Weight weighting) {
		ArrayList<Doc> docList = new ArrayList<>();
		try {
			ParseXML.extractTarGzXmlFiles(PATH_INPUT_XML);
			List<String> files = ParseXML.getXmlPathList();
			for (String path : files) {
				Doc doc = ParseXML.parseXmlFile(path, applyStemming);
				docList.add(doc);
			}
			createRun(docList, applyStemming, weighting);
			ParseXML.deleteTmpXmlFolder();
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Compute the score of the documents in the list for each request
	 * 
	 * @param docList       List containing all the documents in the file(s)
	 * @param applyStemming Boolean to choose using stemming during parsing
	 * @param weighting     Type of weighting (LTN, LTC, BM25, ...)
	 */
	public static void createRun(ArrayList<Doc> docList, boolean applyStemming, Weight weighting) {
		ArrayList<Request> requestList = ParseRequest.extractRequests(PATH_QUERY, applyStemming);
		int docListSize = docList.size();
		double avg = UtilWeightCompute.avg(docList);
		Map<String, Double> scores = new HashMap<>();
		String inex = "";
		for (int i = 0; i < requestList.size(); i++) {
			String id = requestList.get(i).getId();
			ArrayList<String> terms = requestList.get(i).getTermList();
			ArrayList<Integer> listfq = new ArrayList<>();
			ArrayList<Integer> listcd = new ArrayList<>();
			for (String term : terms) {
				int fq = 0;
				for (String t : terms) {
					if (term.equals(t)) {
						fq++;
					}
				}
				listfq.add(fq);
				int cd = 0;
				for (Doc d : docList) {
					for (String t : d.getContentList()) {
						if (term.equals(t)) {
							cd++;
							break;
						}
					}
				}
				listcd.add(cd);
			}
			for (Doc d : docList) {
				double score = 0;
				int docSize = d.getContentList().size();
				ArrayList<Integer> listfd = new ArrayList<>();
				for (String term : terms) {
					int fd = 0;
					for (String t : d.getContentList()) {
						if (term.equals(t)) {
							fd++;
						}
					}
					listfd.add(fd);
				}
				for (int j = 0; j < terms.size(); j++) {
					int fq = listfq.get(j);
					int cd = listcd.get(j);
					int fd = listfd.get(j);
					score = score + UtilWeightCompute.weight(fq, cd, fd, docSize, docListSize, avg, weighting);
				}
				scores.put(d.getId(), score);
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
	private static String writeRequestResult(String id, Map<String, Double> scores) {
		String ranking = "";
		Stream<Map.Entry<String, Double>> sortedScores = scores.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
		Iterator<Map.Entry<String, Double>> it = sortedScores.iterator();
		int rank = 1;
		while (rank <= 1500 && it.hasNext()) {
			ranking = ranking + id;
			ranking = ranking + " " + "Q0";
			ranking = ranking + " " + it.next().toString().replace("=", " " + rank + " ");
			ranking = ranking + " " + "EliasNicolas";
			ranking = ranking + " " + "/article[1]" + "\n";
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
