package org.inex.Model;

import java.io.IOException;
import java.util.ArrayList;

import org.inex.Utils.UtilTextTransformation;

public class Request {

	/***************/
	/** VARIABLES **/
	/***************/

	private String id;

	private ArrayList<String> termList;

	/******************/
	/** CONSTRUCTORS **/
	/******************/

	public Request() {

	}

	/* if 'applyStemming is true' we apply stemming on requests*/
	public Request(String id, String[] terms, boolean applyStemming) throws IOException {
		this.id = id;

		if (applyStemming) {
			String content = fromArrayToString(terms);
			this.termList = UtilTextTransformation.cleanContentList(content, applyStemming);
		} else {
			this.termList = new ArrayList<String>();
			for (String term : terms) {
				this.termList.add(term);
			}
		}

	}


	/***************/
	/** FUNCTIONS **/
	/***************/

	private String fromArrayToString(String[] terms) {
		String content = "";

		for (String term : terms) {
			content = content.concat(term) + " ";
		}

		return content;
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


	/**************/
	/** TOSTRING **/
	/**************/

	@Override
	public String toString() {
		return "Request [id=" + id + ", termList=" + termList + "]";
	}

	

}
