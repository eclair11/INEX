package org.inex.Model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.inex.Utils.UtilTextTransformation;

public class Doc {

	/***************/
	/** VARIABLES **/
	/***************/

	private String id;

	private String content;

	private ArrayList<String> contentList;

	/***************/
	/** FUNCTIONS **/
	/***************/

	private String contentTransform(String content) {

		return content;
	}

	private ArrayList<String> generateContentList() {

		ArrayList<String> contentList = new ArrayList<>();

		return contentList;
	}

	/******************/
	/** CONSTRUCTORS **/
	/******************/

	public Doc() {
	}

	public Doc(String id, String content, boolean applyStemming) throws IOException {
		this.setId(id);
		this.setContent(content);

		this.contentList = UtilTextTransformation.cleanContentList(content, applyStemming);

		/*
		content = content.replaceAll("[^a-zA-Z]", " ").toLowerCase().concat(" ");
		String[] words = content.split(" ");
		this.contentList = new ArrayList<String>();
		for (String word : words) {
			if (Pattern.matches(".*[a-zA-Z]+.*", word)) {
				this.contentList.add(word);
			}

		}
		*/

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public ArrayList<String> getContentList() {
		return contentList;
	}

}
