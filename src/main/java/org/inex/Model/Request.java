package org.inex.Model;

import java.io.IOException;
import java.util.ArrayList;

import org.inex.Utils.UtilTextTransformation;

public class Request {

	private String id;
	private ArrayList<String> termList;

	public Request() {
	}

	/**
	 * Constructor used for parsing the request
	 * 
	 * @param id            Identifier of the request
	 * @param terms         Terms of the request
	 * @param applyStemming Boolean to choose using stemming during parsing
	 * @throws IOException
	 */
	public Request(String id, String[] terms, boolean applyStemming) throws IOException {
		this.id = id;
		if (applyStemming) {
			String content = fromArrayToString(terms);
			this.termList = UtilTextTransformation.cleanContentList(content, applyStemming);
		} else {
			this.termList = new ArrayList<String>();
			for (String term : terms) {
				this.termList.add(term.replaceAll("[^0-9a-zA-Z']", ""));
			}
		}
	}

	/**
	 * Transform the array of terms to a String
	 * 
	 * @param terms Terms of the request
	 * @return Terms as a String object
	 */
	private String fromArrayToString(String[] terms) {
		String content = "";
		for (String term : terms) {
			content = content.concat(term) + " ";
		}
		return content;
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

	@Override
	public String toString() {
		return "Request [id=" + id + ", termList=" + termList + "]";
	}

}
