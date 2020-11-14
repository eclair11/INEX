package org.inex.Model;

public class Result {
	
	/***************/
	/** VARIABLES **/
	/***************/
	
	private String requestId;
	
	private String documentId;
	
	private String requestCurrentTermName;
	
	private String requestCurrentTermOccurrence;
	
	private String requestCurrentTermOccurrenceTotal;
	
	
	/******************/
	/** CONSTRUCTORS **/
	/******************/
	
	public Result() {
		
	}
	
	public Result(
			String requestId, 
			String documentId, 
			String requestCurrentTermName, 
			String requestCurrentTermOccurrence, 
			String requestCurrentTermOccurrenceTotal
			) {
		this.requestId = requestId;
		this.documentId = documentId;
		this.requestCurrentTermName = requestCurrentTermName;
		this.requestCurrentTermOccurrence = requestCurrentTermOccurrence;
		this.requestCurrentTermOccurrenceTotal = requestCurrentTermOccurrenceTotal;
	}

	
	/*************************/
	/** GETTERS AND SETTERS **/
	/*************************/
	
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getRequestCurrentTermName() {
		return requestCurrentTermName;
	}

	public void setRequestCurrentTermName(String requestCurrentTermName) {
		this.requestCurrentTermName = requestCurrentTermName;
	}

	public String getRequestCurrentTermOccurrence() {
		return requestCurrentTermOccurrence;
	}

	public void setRequestCurrentTermOccurrence(String requestCurrentTermOccurrence) {
		this.requestCurrentTermOccurrence = requestCurrentTermOccurrence;
	}

	public String getRequestCurrentTermOccurrenceTotal() {
		return requestCurrentTermOccurrenceTotal;
	}

	public void setRequestCurrentTermOccurrenceTotal(String requestCurrentTermOccurrenceTotal) {
		this.requestCurrentTermOccurrenceTotal = requestCurrentTermOccurrenceTotal;
	}
	

}
