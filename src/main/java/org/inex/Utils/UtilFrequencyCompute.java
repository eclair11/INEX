package org.inex.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public static ArrayList<Map<String, Integer>> docFreqElements(ArrayList<Doc> docList, ArrayList<String> terms) {
        ArrayList<Map<String, Integer>> list = new ArrayList<>();
        for (String term : terms) {
            Map<String, Integer> map = new HashMap<>();
            for (Doc d : docList) {
                for (String k : d.getElements().keySet()) {
                    for (String t : d.getElements().get(k)) {
                        if (term.equals(t)) {
                            if (map.containsKey(k)) {
                                map.put(k, map.get(k) + 1);
                            } else {
                                map.put(k, 1);
                            }
                            break;
                        }
                    }
                }
            }
            list.add(map);
        }
        return list;
    }

    public static ArrayList<Map<String, Integer>> termFreqElements(Doc d, ArrayList<String> terms) {
        ArrayList<Map<String, Integer>> list = new ArrayList<>();
        for (String term : terms) {
            Map<String, Integer> map = new HashMap<>();
            for (String k : d.getElements().keySet()) {
                for (String t : d.getElements().get(k)) {
                    if (term.equals(t)) {
                        if (map.containsKey(k)) {
                            map.put(k, map.get(k) + 1);
                        } else {
                            map.put(k, 1);
                        }
                    }
                }
            }
            list.add(map);
        }
        return list;
    }

}
