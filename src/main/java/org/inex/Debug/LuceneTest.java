package org.inex.Debug;

import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.EnglishStemmer;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class LuceneTest {

    private static final String TEXT = " 2008-08-21T18:38:31Z\nTo éné Promote probabilistic rankings are the Progress of . . . Useful Arts,  Report of the President's Commission on the Patent System (1966). as cited in Gottschalk v Benson.zazaz";

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

    public static ArrayList<String> normalizeStanfordNLPContent(String content) {

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


    public static void displayContentList(ArrayList<String> contentList) {
        contentList.forEach(System.out::println);
    }

    public static void main(String[] args) throws IOException {

        ArrayList<String> contentList = tokenizeContent(TEXT, new StopAnalyzer());
        contentList = normalizeContentList(contentList);

        // ArrayList<String> contentList = normalizeStanfordNLPContent(TEXT);

        displayContentList(contentList);

    }

}
