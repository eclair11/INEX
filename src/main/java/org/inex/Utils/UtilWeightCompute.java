package org.inex.Utils;

import java.util.ArrayList;

import org.inex.App.Weight;
import org.inex.Model.Doc;
import org.inex.Model.Score;

public class UtilWeightCompute {

	/**
	 * @param K           Adjust term frequency saturation for BM25
	 * @param B           Adjust size normalization for BM25
	 * @param ALPHA_SEC   Adjust importance of sec tags
	 * @param ALPHA_TITLE Adjust importance of title tags
	 */
	private static final double K = 0.5;
	private static final double B = 0.3;
	private static final int ALPHA_SEC = 1;
	private static final int ALPHA_TITLE = 2;

	/**
	 * @param docList List containing all the documents in the file(s)
	 * @return Average size of the documents in the list
	 */
	public static double avg(ArrayList<Doc> docList) {
		double total = 0;
		for (Doc d : docList) {
			total = total + d.getContentList().size();
		}
		return total / docList.size();
	}

	/**
	 * @param docList List containing all the documents in the file(s)
	 * @return Average size of the elements in the documents list
	 */
	public static double avgElements(ArrayList<Doc> docList) {
		double total = 0;
		for (Doc d : docList) {
			for (String k : d.getElements().keySet()) {
				total = total + d.getElements().get(k).size();
			}
		}
		return total / docList.size();
	}

	/**
	 * @param score       Score object of the document
	 * @param df          Number of documents that contain the term
	 * @param tf          Term frequency in the document
	 * @param docSize     Size of the document
	 * @param docListSize Total size of the documents in the list
	 * @param avg         Average size of the documents in the list
	 * @param weighting   Type of weighting (LTN, LTC, BM25)
	 * @param node        Name of the node (XML tag)
	 * @return Computed weight following the selected type
	 */
	public static void weight(Score score, int df, int tf, int docSize, int docListSize, double avg, Weight weighting,
			String node) {
		if (weighting.equals(Weight.LTN) || weighting.equals(Weight.LTC)) {
			if (tf != 0 && df != 0) {
				double tfd = 1 + Math.log10(tf);
				double idf = Math.log10(docListSize / df);
				double weight = tfd * idf;
				score.setValue(score.getValue() + weight);
				if (weighting.equals(Weight.LTC)) {
					score.setNorm(score.getNorm() + Math.pow(weight, 2));
				}
			}
		}
		if (weighting.equals(Weight.BM25) || weighting.equals(Weight.BM25F_ROBER)
				|| weighting.equals(Weight.BM25F_WILKI)) {
			int alpha = ALPHA_SEC;
			if (node.endsWith("title[1]")) {
				alpha = ALPHA_TITLE;
			}
			if (weighting.equals(Weight.BM25F_ROBER)) {
				tf = alpha * tf;
			}
			double tfd1 = tf * (K + 1);
			double tfd2 = K * ((1 - B) + B * docSize / avg) + tf;
			double idf = Math.log10((docListSize - df + 0.5) / (df + 0.5));
			double weight = tfd1 / tfd2 * idf;
			if (weighting.equals(Weight.BM25F_WILKI)) {
				weight = alpha * weight;
			}
			score.setValue(score.getValue() + weight);
		}
	}

}
