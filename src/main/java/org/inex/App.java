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
	private static final String PATH_OUTPUT = "./files/output/EliasNicolas_02_05_bm25_articles_k2b0.9stemming.txt";

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
		// algo(docList, applyStemming);
		bm25(docList, applyStemming);
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
		// algo(docList, applyStemming);
		// ParseXML.deleteTmpXmlFolder();
		bm25(docList, applyStemming);
	}

	public static void bm25(ArrayList<Doc> docList, boolean applyStemming) throws IOException {
		double k1 = 2;
		double k2 = 100;
		double b = 0.9;
		double avg = 0;
		for (Doc d : docList) {
			avg = avg + d.getContentList().size();
		}
		avg = avg / docList.size();
		ArrayList<Request> requestList = ParseRequest.extractRequests(PATH_QUERY, applyStemming);
		Map<String, Double> scores = new HashMap<>();
		String inex = "";
		for (int i = 0; i < requestList.size(); i++) {
			ArrayList<String> terms = requestList.get(i).getTermList();
			System.err.println(terms);
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
				double k = k1 * ((1 - b) + b * (d.getContentList().size() / avg));
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
					double p1 = Math.log10((0.5) / (0.5) / (cd + 0.5) / (docList.size() - cd + 0.5));
					double p2 = fd * (k1 + 1) / (fd + k);
					double p3 = fq * (k2 + 1) / (fq + k2);
					score = score + Math.abs(p1) * p2 * p3;
				}
				scores.put(d.getId(), score);
			}
			Stream<Map.Entry<String, Double>> sortedScores = scores.entrySet().stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
			Iterator<Map.Entry<String, Double>> it = sortedScores.iterator();
			int rank = 1;
			while (rank <= 1500 && it.hasNext()) {
				inex = inex + requestList.get(i).getId();
				inex = inex + " " + "Q0";
				inex = inex + " " + it.next().toString().replace("=", " " + rank + " ");
				inex = inex + " " + "EliasNicolas";
				inex = inex + " " + "/article[1]" + "\n";
				rank++;
			}
		}
		ParseTxt.writeRunResult(inex, PATH_OUTPUT);
	}

	public static void algo(ArrayList<Doc> docList, boolean applyStemming) throws IOException {
		ArrayList<Request> requestList = ParseRequest.extractRequests(PATH_QUERY, applyStemming);
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
		//readTxt(false);
		readXml(true);
	}

}
