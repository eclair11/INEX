package org.inex.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Contains every stats of a specific run 
 */
public class Run {

	/***************/
	/** VARIABLES **/
	/***************/
	
	private String name;

	private Date date;

	private String type;

	private int nbDocument;

	private ArrayList<Term> termList;
	
	
	/******************/
	/** CONSTRUCTORS **/
	/******************/
	
	public Run() {
		
	}

	public Run(String name, String type, int nbDocument, ArrayList<Term> termList) {
		super();
		this.name = name;
		this.date = new Date();
		this.type = type;
		this.nbDocument = nbDocument;
		this.termList = termList;
	}
	
	
	/***************/
	/** FUNCTIONS **/
	/***************/
	
	private static int countDocumentByRun(ArrayList<Document> docList) {				
		return docList.size();
	}
	
	private static ArrayList<String> generateTotalStringList(ArrayList<Document> docList) {
		ArrayList<String> totalStringList = new ArrayList<String>();
		
		for (int i = 0; i < docList.size(); i++) {
			
			for (String string : docList.get(i).getContentList()) {
				totalStringList.add(string);
				//System.out.println(string);
			}
			
		}
		
		return totalStringList;
	}
	
	private static ArrayList<String> extractDistinctTermList(ArrayList<Document> docList, ArrayList<String> totalStringList){
		List<String> stringList = totalStringList.stream().distinct().sorted().collect(Collectors.toList());
		ArrayList<String> resultList = new ArrayList<>(stringList);
		return resultList;
	}
	
	private static ArrayList<Term> countDistinctTermList(ArrayList<String> distinctTermList, ArrayList<String> totalStringList){
		ArrayList<Term> countedTermList = new ArrayList<>();
		
		for (String distinctTerm : distinctTermList) {
			int currentTermCount = 0;
			
			for (String totalTerm : totalStringList) {
				if(distinctTerm.equals(totalTerm)) {
					currentTermCount++;
				}
			}
			countedTermList.add(new Term(distinctTerm, currentTermCount));
			// System.out.println(distinctTerm + " - " + currentTermCount);
			System.out.println(countedTermList.get(countedTermList.size()-1));
		}
		
		return countedTermList;
	}
	
	// Génère la totalité des statistiques d'un run dont le compte des occurrences totales des termes
	public static Run generateRunStat(String name, String type, ArrayList<Document> docList) {
		
		ArrayList<String> totalStringList = generateTotalStringList(docList);
		ArrayList<String> distinctTermList = extractDistinctTermList(docList, totalStringList);	
		ArrayList<Term> countedDistinctTermList = countDistinctTermList(distinctTermList, totalStringList);
		
		return new Run(name, type, countDocumentByRun(docList), countedDistinctTermList);
	}
	
	public static void displayRun(Run run) {
		System.out.println("Nb document => " + run.getNbDocument());
	}
	
	
	/*************************/
	/** GETTERS AND SETTERS **/
	/*************************/
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getNbDocument() {
		return nbDocument;
	}

	public void setNbDocument(int nbDocument) {
		this.nbDocument = nbDocument;
	}

	public ArrayList<Term> getTermList() {
		return termList;
	}

	public void setTermList(ArrayList<Term> termList) {
		this.termList = termList;
	}

}
