package org.inex.Parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.inex.Model.Request;

public class ParseRequest {

	/**
	 * Parse the content of the request file
	 * 
	 * @param pathRequest Path to the file that contain the requests
	 * @param applyStemming Boolean to choose using stemming during parsing
	 * @return Parsed content as an array of Request
	 * @throws IOException
	 */
	public static ArrayList<Request> extractRequests(String pathRequest, boolean applyStemming) throws IOException {
		ArrayList<Request> requestList = new ArrayList<>();
		File q = new File(pathRequest);
		Scanner reader = new Scanner(q);
		while (reader.hasNextLine()) {
			String query = reader.nextLine().trim();
			String[] input = query.split(" ", 2);
			String code = input[0];
			String[] terms = input[1].split(" ");
			requestList.add(new Request(code, terms, applyStemming));
		}
		reader.close();
		return requestList;
	}

}
