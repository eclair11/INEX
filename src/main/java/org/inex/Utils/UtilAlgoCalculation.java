package org.inex.Utils;

import java.util.ArrayList;

import org.inex.Model.Document;

public class UtilAlgoCalculation {
	
	/***************/
	/** FUNCTIONS **/
	/***************/
	
	public static Double weightBNN() {
		return 1.0;
	}

	public static Double weightNTN(Double tf, Double df, ArrayList<Document> docList) {
		Double weight = 0.0;
		if (df != 0) {
			weight = tf * Math.log10(docList.size() / df);
		}
		return weight;
	}

	public static Double weightLTN(Double tf, Double df, ArrayList<Document> docList) {
		Double weight = 0.0;
		if (tf != 0.0 && df != 0.0) {
			weight = (1 + Math.log10(tf)) * Math.log10(docList.size() / df);
		}
		return weight;
	}

	public static Double weightBTN(Double df, ArrayList<Document> docList) {
		Double weight = 0.0;
		if (df != 0.0) {
			weight = Math.log10(docList.size() / df);
		}
		return weight;
	}

	public static Double weightNNN(Double tf) {
		return tf;
	}

}
