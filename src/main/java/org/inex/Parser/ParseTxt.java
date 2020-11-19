package org.inex.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.inex.Model.Doc;

public class ParseTxt {

	/***************/
	/** FUNCTIONS **/
	/***************/

	public static ArrayList<Doc> extractTxt(String pathInputTxt, boolean applyStemming) throws IOException {

		ArrayList<Doc> docList = new ArrayList<>();

		String docId = "";
		boolean tagOpen = false;
		boolean enCours = false;
		String line = "";
		String lineFinal = "";

		File f = new File(pathInputTxt);
		InputStream fileStream = new FileInputStream(f);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream, "US-ASCII");
		BufferedReader buffered = new BufferedReader(decoder);

		while ((line = buffered.readLine()) != null) {
			if (line.contains("<doc><docno>")) {
				tagOpen = true;
				docId = line.replaceAll("[^0-9]", "");
			}
			if (tagOpen && !enCours) {
				enCours = true;
			}
			if (tagOpen && enCours && !line.isEmpty() && !line.isBlank()) {
				lineFinal = lineFinal.concat(line + " ");
			}
			if (line.contains("</doc>")) {
				docList.add(new Doc(docId, lineFinal, applyStemming));
				lineFinal = "";
				tagOpen = false;
				enCours = false;
			}
		}

		buffered.close();

		return docList;
	}

	public static void writeRunResult(String s, String pathRunOutput) throws IOException {
		FileWriter writer = new FileWriter(pathRunOutput);
		writer.write(s);
		writer.close();
	}

}
