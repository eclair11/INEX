package org.inex.Utils;

import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.EnglishStemmer;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class UtilTextTransformation {

    /***************/
    /** CONSTANTS **/
    /***************/

    private final static ArrayList<String> STOP_WORDS_MINIMAL = new ArrayList<String>(Arrays.asList("a", "an",
            "another", "any", "certain", "each", "every", "her", "his", "its", "its", "my", "no", "our", "some", "that",
            "the", "their", "this", "and", "but", "or", "yet", "for", "nor", "so", "as", "aboard", "about", "above",
            "across", "after", "against", "along", "around", "at", "before", "behind", "below", "beneath", "beside",
            "between", "beyond", "but", "by", "down", "during", "except", "following", "for", "from", "in", "inside",
            "into", "like", "minus", "minus", "near", "next", "of", "off", "on", "onto", "onto", "opposite", "out",
            "outside", "over", "past", "plus", "round", "since", "since", "than", "through", "to", "toward", "under",
            "underneath", "unlike", "until", "up", "upon", "with", "without"));

    private static ArrayList<String> deleteStopWords(String content) {

        ArrayList<String> contentList = Stream.of(content.toLowerCase().split(" "))
                .collect(Collectors.toCollection(ArrayList<String>::new));

        contentList.removeAll(STOP_WORDS_MINIMAL);

        return contentList;

    }

    /***************/
    /* StanfordNLP */
    /***************/

    public static ArrayList<String> normalizeContentList(String content) {

        ArrayList<String> contentList = new ArrayList<String>();

        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument coreDocument = pipeline.processToCoreDocument(content);
        pipeline.annotate(coreDocument);

       
        contentList = coreDocument.tokens().stream().map(coreLabel -> coreLabel.lemma())
                .collect(Collectors.toCollection(ArrayList<String>::new));

        return contentList;
    }

    
    /**********/
    /* Lucene */
    /**********/

    public static ArrayList<String> tokenizeContent(String content, Analyzer analyzer) throws IOException {

        ArrayList<String> result = new ArrayList<String>();
        TokenStream tokenStream = analyzer.tokenStream("", content);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();

        while (tokenStream.incrementToken()) {
            result.add(attr.toString());
        }

        return result;
    }

    public static ArrayList<String> normalizeContentList(ArrayList<String> contentList) {

        String currentTerm = "";

        for (int i = 0; i < contentList.size(); i++) {
            currentTerm = contentList.get(i);
            currentTerm = Normalizer.normalize(currentTerm, Form.NFD);
            currentTerm = currentTerm.replaceAll("[^\\p{ASCII}]", "");
            contentList.set(i, currentTerm);
        }

        return contentList;
    }

    /************/
    /* Tartarus */
    /************/

    public static ArrayList<String> stemmingWord(ArrayList<String> contentList) {

        contentList.forEach(term -> {
            EnglishStemmer stemmer = new EnglishStemmer();
            stemmer.setCurrent(term);
            if (stemmer.stem()) {
                term = stemmer.getCurrent();
            }
        });

        return contentList;
    }

    public static void displayContentList(ArrayList<String> contentList) {
        contentList.forEach(System.out::println);
    }

    public static ArrayList<String> cleanContentList(String content) throws IOException {

        ArrayList<String> contentList = tokenizeContent(content, new StopAnalyzer());
        contentList = normalizeContentList(contentList);
        // contentList = stemmingWord(contentList);

        return contentList;

    }

}
