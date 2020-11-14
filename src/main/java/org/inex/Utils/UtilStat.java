package org.inex.Utils;

import java.util.ArrayList;

import org.inex.Model.Document;
import org.inex.Model.Request;
import org.inex.Model.Result;

public class UtilStat {
	
	/***************/
	/** FUNCTIONS **/
	/***************/
	
	public static void computeOccurenceByWordDisplay(ArrayList<Document> docList, ArrayList<Request> requestList) {
		// Pour chaque requete
		for (int i = 0; i < requestList.size(); i++) {
			System.out.print("\nRequest numero " + requestList.get(i).getId() + " => ");
			// Pour chaque document
			for (int j = 0; j < docList.size(); j++) {
				System.out.print("\n\tDoc numero => " + docList.get(j).getId() + " ");
				// Pour chacun des termes d'une requete
				for (int k = 0; k < requestList.get(i).getTermList().size(); k++) {
					String currenTermRequest = requestList.get(i).getTermList().get(k);
					System.out.print("\n\t\tTerm NB -> " + requestList.get(i).getTermList().get(k).toString() + ": ");
					int nbOcc = 0;
					// Pour chacun des mots contenus dans un document identifie, on compare avec le terme courant
					for (int l = 0; l < docList.get(j).getContentList().size(); l++) {
						String currenTermVerif = docList.get(j).getContentList().get(l);
						if (currenTermRequest.equals(currenTermVerif)) {
							// si les 2 concordent, on incremente
							nbOcc++;
						}
					}
					// On affiche
					System.out.print(nbOcc);
				}
			}
		}
	}
	
	public static ArrayList<Result> computeOccurenceByWord(ArrayList<Document> docList, ArrayList<Request> requestList) {
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
					resultList.add(new Result(requestId, documentId, requestCurrentTermName, requestCurrentTermOccurrence, Integer.toString(0)));
				}
			}
		}
		return resultList;
	}

	public static void displayResultList(ArrayList<Result> resultList) {
		for (int i = 0; i < resultList.size(); i++) {
			String requestId = resultList.get(i).getRequestId();
			String documentId = resultList.get(i).getDocumentId();
			String requestCurrentTermName = resultList.get(i).getRequestCurrentTermName();
			String requestCurrentTermOccurrence = resultList.get(i).getRequestCurrentTermOccurrence();
			System.out.println("Request n�" + requestId + " - Document n�" + documentId + " - Term: "
					+ requestCurrentTermName + " - Nb Occurrences => " + requestCurrentTermOccurrence);
		}
	}

}
