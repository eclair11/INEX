package org.inex;

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
import org.inex.Utils.UtilAlgoCalculation;
import org.xml.sax.SAXException;

public final class App {

	/***************/
	/** CONSTANTS **/
	/***************/

	private static final String PATH_QUERY = "./files/request/topics_M2WI7Q_2020_21.txt";
	private static final String PATH_INPUT_TXT = "./files/input/txt/Text_Only_Ascii_Coll_MWI_NoSem.gz";
	private static final String PATH_INPUT_XML = "./files/input/xml/XML_Coll_MWI_withSem.tar.gz";
	private static final String PATH_OUTPUT = "./files/output/EliasNicolas_01_03_ltn_articles.txt";

	/******************/
	/** CONSTRUCTORS **/
	/******************/

	private App() {
	}

	/***************/
	/** FUNCTIONS **/
	/***************/

	public static void readTxt(boolean applyStemming) throws IOException {
		// Extraction du fichier texte de la liste de documents
		ArrayList<Doc> docList = ParseTxt.extractTxt(PATH_INPUT_TXT, applyStemming);

		// computeOccurenceByWord(docList, requestList);
		// ArrayList<Result> resultList = computeOccurenceByWord(docList, requestList);
		// displayResultList(resultList);
		// Run run = Run.generateRunStat("FirstRun", "txt", docList);
		// Run.displayRun(run);

		// Lancement de la construction du run du fichier
		algo(docList, applyStemming);
	}

	public static void readXml(boolean applyStemming) throws IOException, ParserConfigurationException, SAXException {
		ArrayList<Doc> docList = new ArrayList<>();

		// Extraction de tous les fichiers dans un répertoire temporaire
		ParseXML.extractTarGzXmlFiles(PATH_INPUT_XML);
		List<String> files = ParseXML.getXmlPathList();
		for (String path : files) {
			Doc doc = ParseXML.parseXmlFile(path, applyStemming);
			docList.add(doc);
		}

		// Lancement de la construction du run du fichier
		algo(docList, applyStemming);
		// ParseXML.deleteTmpXmlFolder();
	}

	public static void algo(ArrayList<Doc> docList, boolean applyStemming) throws IOException {
		ArrayList<Request> requestList = ParseRequest.extractRequests(PATH_QUERY, applyStemming);
		System.out.println(requestList.toString());
		String s = "";

		for (int i = 0; i < requestList.size(); i++) {
			ArrayList<String> terms = requestList.get(i).getTermList();
			Map<String, Double> scores = new HashMap<>();
			ArrayList<Double> weights = new ArrayList<>();
			ArrayList<Double> dfs = new ArrayList<>();
			Double tf = 0.0;
			Double df = 0.0;
			int j = 0;

			for (String term : terms) {
				// compute term frequency in the request
				for (String t : terms) {
					if (term.equals(t)) {
						tf++;
					}
				}
				// compute the number of documents including the term
				for (Doc d : docList) {
					for (String w : d.getContentList()) {
						if (term.equals(w)) {
							df++;
							break;
						}
					}
				}
				// compute weight in the request
				Double weight = UtilAlgoCalculation.weightLTN(tf, df, docList);
				// add the weight to the list
				weights.add(weight);
				// save the df of the term
				dfs.add(df);
				// reset counters
				tf = 0.0;
				df = 0.0;
			}

			for (Doc d : docList) {
				Double score = 0.0;
				for (String term : terms) {
					// compute term frequency in the document
					for (String w : d.getContentList()) {
						if (term.equals(w)) {
							tf++;
						}
					}
					// compute weight in the document
					Double weight = UtilAlgoCalculation.weightLTN(tf, dfs.get(j), docList);
					// compute the score
					score = score + weight * weights.get(j);
					// reset counter
					tf = 0.0;
					// pass to the next term's df and weight
					j++;
				}
				// save the score with the document id
				scores.put(d.getId(), score);
				// reset counter
				j = 0;
			}

			Stream<Map.Entry<String, Double>> sortedScores = scores.entrySet().stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
			Iterator<Map.Entry<String, Double>> it = sortedScores.iterator();
			int rank = 1;
			while (rank <= 1500 && it.hasNext()) {
				s = s + requestList.get(i).getId();
				s = s + " " + "Q0";
				s = s + " " + it.next().toString().replace("=", " " + rank + " ");
				s = s + " " + "EliasNicolas";
				s = s + " " + "/article[1]" + "\n";
				rank++;
			}
		}

		// Generation du fichier de resultats d'un run
		ParseTxt.writeRunResult(s, PATH_OUTPUT);
	}

	/**********/
	/** MAIN **/
	/**********/

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		readTxt(false);
		// readXml(false);
	}

}
