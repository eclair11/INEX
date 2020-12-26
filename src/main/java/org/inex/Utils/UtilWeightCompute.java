package org.inex.Utils;

import java.util.ArrayList;
import java.util.Map;

import org.inex.App.Weight;
import org.inex.Model.Doc;

public class UtilWeightCompute {

	/**
	 * @param K Adjust term frequency saturation for BM25
	 * @param B Adjust size normalization for BM25
	 */
	private static final double K = 0.5;
	private static final double B = 0.3;

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
	 * @param fq          Term frequency in the query
	 * @param cd          Number of documents containing the term
	 * @param fd          Term frequency in the document
	 * @param docSize     Size of the document
	 * @param docListSize Total size of the documents in the list
	 * @param avg         Average size of the documents in the list
	 * @param norm        Document normalization
	 * @param weighting   Type of weighting (LTN, LTC, BM25, ...)
	 * @return Computed weight following the selected type
	 */
	public static double weight(int cd, int fd, int docSize, int docListSize, double avg, Map<String, Double> norm,
			Weight weighting) {
		double weight = 0;
		switch (weighting) {
			case LTN:
				if (fd != 0 && cd != 0) {
					double tfd = 1 + Math.log10(fd);
					double idf = Math.log10(docListSize / cd);
					weight = tfd * idf;
				}
				break;
			case LTC:
				if (fd != 0 && cd != 0) {
					double tfd = 1 + Math.log10(fd);
					double idf = Math.log10(docListSize / cd);
					weight = tfd * idf;
					norm.put("value", norm.get("value") + Math.pow(weight, 2));
				}
				break;
			case BM25:
				double p1 = fd * (K + 1);
				double p2 = K * ((1 - B) + B * docSize / avg) + fd;
				double p3 = Math.log10((docListSize - cd + 0.5) / (cd + 0.5));
				weight = p1 / p2 * p3;
				break;
			default:
				break;
		}
		return weight;
	}

}
