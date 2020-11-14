package org.inex.Model;

public class Term {
	
	/***************/
	/** VARIABLES **/
	/***************/
	
	private String value;
	
	private int occurence;
	
	
	/******************/
	/** CONSTRUCTORS **/
	/******************/
	
	public Term() {
		
	}

	public Term(String value, int occurence) {
		super();
		this.value = value;
		this.occurence = occurence;
	}
	
	
	/*************************/
	/** GETTERS AND SETTERS **/
	/*************************/

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getOccurence() {
		return occurence;
	}

	public void setOccurence(int occurence) {
		this.occurence = occurence;
	}

	@Override
	public String toString() {
		return "Term [value=" + value + ", occurence=" + occurence + "]";
	}

	
	
}
