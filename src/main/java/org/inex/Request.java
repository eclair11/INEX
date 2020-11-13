package org.inex;

import java.util.ArrayList;

public class Request {

	private String id;
	
	private ArrayList<String> termList;
	
	public Request(){
		
	}
	
	public Request(String id, String[] terms ) {
		this.id = id;
		this.termList = new ArrayList<String>();
		for(String term : terms) {
			this.termList.add(term);
		}
	}

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
