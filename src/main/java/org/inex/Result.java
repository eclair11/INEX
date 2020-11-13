package org.inex;

public class Result {
	
	private String requestId;
	
	private String documentId;
	
	private String requestCurrentTermName;
	
	private String requestCurrentTermOccurrence;
	
	
	public Result() {
		
	}
	
	public Result(String requestId, String documentId, String requestCurrentTermName, String requestCurrentTermOccurrence) {
		this.requestId = requestId;
		this.documentId = documentId;
		this.requestCurrentTermName = requestCurrentTermName;
		this.requestCurrentTermOccurrence = requestCurrentTermOccurrence;
	}

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
	

}
