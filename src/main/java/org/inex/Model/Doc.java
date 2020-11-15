package org.inex.Model;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Doc {

	private String id;
	private String content;
	private ArrayList<String> contentList;

	public Doc() {
	}

	public Doc(String id, String content) {
		this.setId(id);
		this.setContent(content);

		content = content.replaceAll("[^a-zA-Z]", " ").toLowerCase().concat(" ");
		String[] words = content.split(" ");
		this.contentList = new ArrayList<String>();
		for (String word : words) {
			if (Pattern.matches(".*[a-zA-Z]+.*", word)) {
				this.contentList.add(word);
			}

		}

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

}
