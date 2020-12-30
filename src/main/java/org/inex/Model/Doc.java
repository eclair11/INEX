package org.inex.Model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.inex.Utils.UtilTextTransformation;

public class Doc {

	private String id;
	private String content;
	private ArrayList<String> contentList;
	private Map<String, ArrayList<String>> elements;

	public Doc() {
	}

	/**
	 * Constructor used for parsing text file
	 * 
	 * @param id            Identifier of the document
	 * @param content       Parsed content of the document
	 * @param applyStemming Boolean to choose using stemming during parsing
	 * @throws IOException
	 */
	public Doc(String id, String content, boolean applyStemming) throws IOException {
		this.id = id;
		this.content = content;
		this.contentList = UtilTextTransformation.cleanContentList(content, applyStemming);
	}

	/**
	 * Constructor used for parsing XML files
	 * 
	 * @param id       Identifier of the document
	 * @param elements Parsed elements of the document
	 */
	public Doc(String id, Map<String, ArrayList<String>> elements) {
		this.id = id;
		this.elements = elements;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public ArrayList<String> getContentList() {
		return contentList;
	}

	public void setContentList(ArrayList<String> contentList) {
		this.contentList = contentList;
	}

	public Map<String, ArrayList<String>> getElements() {
		return elements;
	}

	public void setElements(Map<String, ArrayList<String>> elements) {
		this.elements = elements;
	}

}
