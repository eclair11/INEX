package org.inex.Utils;

import java.util.ArrayList;

import org.inex.Model.Doc;

public class UtilFrequencyCompute {

    public static ArrayList<Integer> docFreq(ArrayList<Doc> docList, ArrayList<String> terms) {
        ArrayList<Integer> list = new ArrayList<>();
        for (String term : terms) {
            int df = 0;
            for (Doc d : docList) {
                for (String t : d.getContentList()) {
                    if (term.equals(t)) {
                        df++;
                        break;
                    }
                }
            }
            list.add(df);
        }
        return list;
    }

    public static ArrayList<Integer> termFreq(Doc d, ArrayList<String> terms) {
        ArrayList<Integer> list = new ArrayList<>();
        for (String term : terms) {
            int tf = 0;
            for (String t : d.getContentList()) {
                if (term.equals(t)) {
                    tf++;
                }
            }
            list.add(tf);
        }
        return list;
    }

}
