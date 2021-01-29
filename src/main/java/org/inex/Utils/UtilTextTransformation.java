package org.inex.Utils;

import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * 
     * Removal of stop-words from the content of a document.
     * 
     * @param content
     * @return the list whose stop-words have been removed.
     */
    private static ArrayList<String> deleteStopWords(String content) {

        ArrayList<String> contentList = Stream.of(content.toLowerCase().split(" "))
                .collect(Collectors.toCollection(ArrayList<String>::new));

        contentList.removeAll(STOP_WORDS_MINIMAL);

        return contentList;

    }

    /***************/
    /* StanfordNLP */
    /***************/

    /**
     * 
     * Testing the StanfordNLP library for tokenization and Normalization.
     * 
     * @see<a href="https://stanfordnlp.github.io/CoreNLP/api.html">
     * 
     * @param content
     * @return the list of cleaned and normalized tokens
     */
    public static ArrayList<String> normalizeContentList(String content) {

        ArrayList<String> contentList = new ArrayList<String>();

        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        CoreDocument coreDocument = pipeline.processToCoreDocument(content);
        pipeline.annotate(coreDocument);

        contentList = coreDocument.tokens().stream().map(coreLabel -> coreLabel.lemma())
                .collect(Collectors.toCollection(ArrayList<String>::new));

        return contentList;
    }

    /**********/
    /* Lucene */
    /**********/

    /**
     * 
     * Tokenization and deletion of stop-words, from the Lucene Apache library.
     * 
     * @see <a href="https://lucene.apache.org/core/7_3_1/core/org/apache/lucene/analysis/package-summary.html">
     * 
     * @param content
     * @param analyzer
     * @return a list of tokenized terms
     * @throws IOException
     */
    public static ArrayList<String> tokenizeContent(String content, Analyzer analyzer) throws IOException {

        ArrayList<String> result = new ArrayList<String>();
        TokenStream tokenStream = analyzer.tokenStream("", content);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                result.add(attr.toString());
            }
            tokenStream.end();

        } finally {
            tokenStream.close();
        }
        return result;
    }

    /**
     * 
     * Normalization of the token list.
     * 
     * @param contentList
     * @return a list of normaized tokens
     */
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

    /**
     * 
     * Stemmatization of the list of terms, from the Tartarus library.
     * 
     * @see <a href="https://usermanual.wiki/Document/Instructions.1836733729/help">
     * @see <a href="https://www.programcreek.com/java-api-examples/?api=org.tartarus.snowball.ext.EnglishStemmer">    
     * 
     * @param contentList
     * @return a list of terms passed through the stemming filter.
     */
    public static ArrayList<String> stemmingWord(ArrayList<String> contentList) {

        String currentTerm = "";

        for (int i = 0; i < contentList.size(); i++) {

            EnglishStemmer stemmer = new EnglishStemmer();

            currentTerm = contentList.get(i);

            stemmer.setCurrent(currentTerm);
            if (stemmer.stem()) {
                currentTerm = stemmer.getCurrent();
            }

            contentList.set(i, currentTerm);
        }

        return contentList;
    }

    /**
     *
     * Simple function to display the contents of a list of tokens.
     * 
     * @param contentList
     */
    public static void displayContentList(ArrayList<String> contentList) {
        contentList.forEach(System.out::println);
    }

    /**
     * 
     * Main function of tokenization and normalization of the textual content of a
     * document.
     * 
     * @param content
     * @param applyStemming
     * @return the list of terms cleaned and passed to the stemming filter.
     * @throws IOException
     */
    public static ArrayList<String> cleanContentList(String content, boolean applyStemming) throws IOException {

        ArrayList<String> contentList = tokenizeContent(content, new StopAnalyzer());
        contentList = normalizeContentList(contentList);
        if (applyStemming) {
            contentList = stemmingWord(contentList);
        }

        return contentList;

    }

}
