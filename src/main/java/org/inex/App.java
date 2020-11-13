package org.inex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class App {

	private static final String PATH_QUERY = "./topics_M2WI7Q_2020_21.txt";
	private static final String PATH_COLLECTION = "./Text_Only_Ascii_Coll_MWI_NoSem.gz";

	private static ArrayList<Request> extractRequests() throws FileNotFoundException, IOException {
		ArrayList<Request> requestList = new ArrayList<>();
		File q = new File(PATH_QUERY);
		Scanner reader = new Scanner(q);
		while (reader.hasNextLine()) {
			// read the request
			String query = reader.nextLine().trim();
			// split the request content
			String[] input = query.split(" ", 2);
			// get the request id
			String code = input[0];
			// get the request words
			String[] terms = input[1].split(" ");
			// add the request to the list of requests
			requestList.add(new Request(code, terms));
		}
		reader.close();
		return requestList;
	}

	private static void computeOccurenceByWordDisplay(ArrayList<Document> docList, ArrayList<Request> requestList) {
		// Pour chaque requ�te
		for (int i = 0; i < requestList.size(); i++) {
			System.out.print("\nRequest n�" + requestList.get(i).getId() + " => ");
			// Pour chaque document
			for (int j = 0; j < docList.size(); j++) {
				System.out.print("\n\tDoc n� n�=> " + docList.get(j).getId() + " ");
				// Pour chacun des termes d'une requ�te
				for (int k = 0; k < requestList.get(i).getTermList().size(); k++) {
					String currenTermRequest = requestList.get(i).getTermList().get(k);
					System.out.print("\n\t\tTerm NB -> " + requestList.get(i).getTermList().get(k).toString() + ": ");
					int nbOcc = 0;
					// Pour chacun des mots contenus dans un document identifi�, on compare avec le
					// terme courant
					for (int l = 0; l < docList.get(j).getContentList().size(); l++) {
						String currenTermVerif = docList.get(j).getContentList().get(l);
						if (currenTermRequest.equals(currenTermVerif)) {
							// si les 2 concordent, on incr�mente
							nbOcc++;
						}
					}
					// On affcihe
					System.out.print(nbOcc);
				}
			}
		}
	}

	private static ArrayList<Result> computeOccurenceByWord(ArrayList<Document> docList, ArrayList<Request> requestList) {
		ArrayList<Result> resultList = new ArrayList<>();
		// Pour chaque requ�te
		for (int i = 0; i < requestList.size(); i++) {
			String requestId = "";
			String documentId = "";
			String requestCurrentTermName = "";
			String requestCurrentTermOccurrence = "";
			requestId = requestList.get(i).getId();
			// Pour chaque document
			for (int j = 0; j < docList.size(); j++) {
				documentId = docList.get(j).getId();
				// Pour chacun des termes d'une requ�te
				for (int k = 0; k < requestList.get(i).getTermList().size(); k++) {
					requestCurrentTermName = requestList.get(i).getTermList().get(k);
					int nbOcc = 0;
					// Pour chacun des mots contenus dans un document identifi�, on compare avec le
					// terme courant
					for (int l = 0; l < docList.get(j).getContentList().size(); l++) {
						String currenTermVerif = docList.get(j).getContentList().get(l);
						if (requestCurrentTermName.equals(currenTermVerif)) {
							// si les 2 concordent, on incr�mente
							nbOcc++;
						}
					}
					requestCurrentTermOccurrence = Integer.toString(nbOcc);
					resultList.add(new Result(requestId, documentId, requestCurrentTermName, requestCurrentTermOccurrence));
				}
			}
		}
		return resultList;
	}

	private static void displayResultList(ArrayList<Result> resultList) {
		for (int i = 0; i < resultList.size(); i++) {
			String requestId = resultList.get(i).getRequestId();
			String documentId = resultList.get(i).getDocumentId();
			String requestCurrentTermName = resultList.get(i).getRequestCurrentTermName();
			String requestCurrentTermOccurrence = resultList.get(i).getRequestCurrentTermOccurrence();
			System.out.println("Request n�" + requestId + " - Document n�" + documentId + " - Term: "
					+ requestCurrentTermName + " - Nb Occurrences => " + requestCurrentTermOccurrence);
		}
	}

	private static void read() throws IOException {

		ArrayList<Document> docList = new ArrayList<>();
		String docId = "";
		boolean tagOpen = false;
		boolean enCours = false;
		String line = "";
		String lineFinal = "";

		File f = new File(PATH_COLLECTION);
		InputStream fileStream = new FileInputStream(f);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream, "US-ASCII");
		BufferedReader buffered = new BufferedReader(decoder);

		while ((line = buffered.readLine()) != null) {
			if (line.contains("<doc><docno>")) {
				tagOpen = true;
				docId = line.replaceAll("[^0-9]", "");
			}
			if (tagOpen && !enCours) {
				enCours = true;
			}
			if (tagOpen && enCours && !line.isEmpty() && !line.isBlank()) {
				lineFinal = lineFinal.concat(line + " ");
			}
			if (line.contains("</doc>")) {
				docList.add(new Document(docId, lineFinal));
				lineFinal = "";
				tagOpen = false;
				enCours = false;
			}
		}

		buffered.close();

		// computeOccurenceByWord(docList, requestList);
		// ArrayList<Result> resultList = computeOccurenceByWord(docList, requestList);
		// displayResultList(resultList);

		algo(docList);

	}

	public static void algo(ArrayList<Document> docList) throws IOException {

		ArrayList<Request> requestList = extractRequests();
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
				term = term.replaceAll("[^0-9a-zA-Z]", "").toLowerCase();
				// compute term frequency in the request
				for (String t : terms) {
					if (term.equals(t.replaceAll("[^0-9a-zA-Z]", "").toLowerCase())) {
						tf++;
					}
				}
				// compute the number of documents including the term
				for (Document d : docList) {
					for (String w : d.getContentList()) {
						if (term.equals(w)) {
							df++;
							break;
						}
					}
				}
				// compute weight in the request
				//Double weight = weightBNN();
				//Double weight = weightNTN(tf, df, docList);
				//Double weight = weightLTN(tf, df, docList);
				//Double weight = weightBTN(df, docList);
				Double weight = weightNNN(tf);
				// add the weight to the list
				weights.add(weight);
				// save the df of the term
				dfs.add(df);
				// reset counters
				tf = 0.0;
				df = 0.0;
			}

			for (Document d : docList) {
				double score = 0.0;
				for (String term : terms) {
					// delete special characters
					term = term.replaceAll("[^0-9a-zA-Z]", "").toLowerCase();
					// compute term frequency in the document
					for (String w : d.getContentList()) {
						if (term.equals(w)) {
							tf++;
						}
					}
					// compute weight in the document
					//Double weight = weightBNN();
					//Double weight = weightNTN(tf, dfs.get(j), docList);
					//Double weight = weightLTN(tf, dfs.get(j), docList);
					//Double weight = weightBTN(dfs.get(j), docList);
					Double weight = weightNNN(tf);
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

			Stream<Map.Entry<String, Double>> sortedScores = scores.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
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

		write(s);

	}

	private static Double weightBNN() {
		return 1.0;
	}

	private static Double weightNTN(Double tf, Double df, ArrayList<Document> docList) {
		Double weight = 0.0;
		if (df != 0) {
			weight = tf * Math.log10(docList.size() / df);
		}
		return weight;
	}

	private static Double weightLTN(Double tf, Double df, ArrayList<Document> docList) {
		Double weight = 0.0;
		if (tf != 0.0 && df != 0.0) {
			weight = (1 + Math.log10(tf)) * Math.log10(docList.size() / df);
		}
		return weight;
	}

	private static Double weightBTN(Double df, ArrayList<Document> docList) {
		Double weight = 0.0;
		if (df != 0.0) {
			weight = Math.log10(docList.size() / df);
		}
		return weight;
	}

	private static Double weightNNN(Double tf) {
		return tf;
	}

	private static void write(String s) throws IOException {
		FileWriter writer = new FileWriter("./EliasNicolas_01_05_NNN_articles.txt");
		writer.write(s);
		writer.close();
	}

	public static void main(String[] args) throws IOException {
		read();
	}

}
