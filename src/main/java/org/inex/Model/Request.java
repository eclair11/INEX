package org.inex.Model;

import java.util.ArrayList;

public class Request {

	/***************/
	/** VARIABLES **/
	/***************/
	
	private String id;
	
	private ArrayList<String> termList;
	
	
	/******************/
	/** CONSTRUCTORS **/
	/******************/
	
	public Request(){
		
	}
	
	public Request(String id, String[] terms ) {
		this.id = id;
		this.termList = new ArrayList<String>();
		for(String term : terms) {
			this.termList.add(term);
		}
	}
	
	
	/*************************/
	/** GETTERS AND SETTERS **/
	/*************************/

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<String> getTermList() {
		return termList;
	}

	public void setTermList(ArrayList<String> termList) {
		this.termList = termList;
	}
	
}
