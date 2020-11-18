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
	private static final String PATH_INPUT_SINGLE_XML = "./files/input/xml/612.xml";
	private static final String PATH_OUTPUT = "./files/output/EliasNicolas_03_XX_XXX_articles.txt";

	/******************/
	/** CONSTRUCTORS **/
	/******************/

	private App() {

	}

	/***************/
	/** FUNCTIONS **/
	/***************/

	private static void readTxt() throws IOException {

		// Extraction du fichier texte de la liste de documents
		ArrayList<Doc> docList = ParseTxt.extractTxt(PATH_INPUT_TXT);

		/*
		 * Generation et affichage d'une serie de statistique sur les occurences des
		 * termes des requetes
		 */
		// computeOccurenceByWord(docList, requestList);
		// ArrayList<Result> resultList = computeOccurenceByWord(docList, requestList);
		// displayResultList(resultList);

		/* Generation des statistiques d'un run */
		// Run run = Run.generateRunStat("FirstRun", "txt", docList);
		// Run.displayRun(run);

		/* Lancement de la construction du run du fichier */
		algo(docList);

	}

	public static void readXml() throws IOException, ParserConfigurationException, SAXException {

		ArrayList<Doc> docList = new ArrayList<>();

		// Extraction de tous les fichiers dans un répertoire temporaire
		ParseXML.extractTarGzXmlFiles(PATH_INPUT_XML);

		List<String> files = ParseXML.getXmlPathList();
		for (String path : files) {
			Doc doc = ParseXML.parseXmlFile(path);
			docList.add(doc);
		}

		/* Lancement de la construction du run du fichier */
		algo(docList);

		// ParseXML.deleteTmpXmlFolder();

	}

	public static void algo(ArrayList<Doc> docList) throws IOException {

		ArrayList<Request> requestList = ParseRequest.extractRequests(PATH_QUERY);
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

				// delete special characters
				// term = term.replaceAll("[^0-9a-zA-Z]", "").toLowerCase();

				// compute term frequency in the request
				for (String t : terms) {
					if (term.equals(t.replaceAll("[^0-9a-zA-Z]", "").toLowerCase())) {
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
				// Double weight = UtilAlgoCalculation.weightBNN();
				// Double weight = UtilAlgoCalculation.weightNTN(tf, df, docList);
				// Double weight = UtilAlgoCalculation.weightLTN(tf, df, docList);
				// Double weight = UtilAlgoCalculation.weightBTN(df, docList);
				Double weight = UtilAlgoCalculation.weightNNN(tf);

				// add the weight to the list
				weights.add(weight);

				// save the df of the term
				dfs.add(df);

				// reset counters
				tf = 0.0;
				df = 0.0;
			}

			for (Doc d : docList) {
				double score = 0.0;
				for (String term : terms) {

					// delete special characters
					//term = term.replaceAll("[^0-9a-zA-Z]", "").toLowerCase();

					// compute term frequency in the document
					for (String w : d.getContentList()) {
						if (term.equals(w)) {
							tf++;
						}
					}

					// compute weight in the document
					// Double weight = UtilAlgoCalculation.weightBNN();
					// Double weight = UtilAlgoCalculation.weightNTN(tf, dfs.get(j), docList);
					// Double weight = UtilAlgoCalculation.weightLTN(tf, dfs.get(j), docList);
					// Double weight = UtilAlgoCalculation.weightBTN(dfs.get(j), docList);
					Double weight = UtilAlgoCalculation.weightNNN(tf);

					// compute the score
					score = score + weight * weights.get(j);

					// reset counter
					tf = 0.0;

					// pass to the next term's df and weight
					j++;
				}

				// save the score with the document id
				scores.put(d.getId(), score / d.getContentList().size());

				// reset counter of term's df and weight
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
		
		readTxt();
		// readXml();
	
		/******************/
		/* Extraction xml */
		/******************/
		/* 
		// Extration du fichier 612.xml
		Doc document = ParseXML.parseXmlFile(PATH_INPUT_SINGLE_XML);
		System.out.println(document.getContentList());
		System.out.println("Id : " + document.getId());

		// Extraction de tous les fichiers dans un répertoire temporaire
		ParseXML.extractTarGzXmlFiles(PATH_INPUT_XML);
		ParseXML.displayList(50);
		 */
		
		// ParseXML.deleteTmpXmlFolder();

		
	}

}
