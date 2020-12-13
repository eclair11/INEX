package org.inex.Parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.inex.Model.Request;

public class ParseRequest {
	
	public static ArrayList<Request> extractRequests(String pathRequest, boolean applyStemming) {
		ArrayList<Request> requestList = new ArrayList<>();
		try {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return requestList;
	}

}
