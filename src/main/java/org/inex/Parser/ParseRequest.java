package org.inex.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.inex.Model.Request;

public class ParseRequest {
	
	public static ArrayList<Request> extractRequests(String pathRequest) throws FileNotFoundException, IOException {
		ArrayList<Request> requestList = new ArrayList<>();
		File q = new File(pathRequest);
		Scanner reader = new Scanner(q);
		while (reader.hasNextLine()) {
			// read the request
			String query = reader.nextLine().trim();
			// split the request content
			String[] input = query.split(" ", 2);
			// get the request id
			String code = input[0];
			// get the request words
			String[] terms = input[1].split(" ");
			// add the request to the list of requests
			requestList.add(new Request(code, terms));
		}
		reader.close();
		return requestList;
	}

}
