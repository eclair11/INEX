package org.inex.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.inex.Model.Doc;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.traverse.DepthFirstIterator;

public class UtilArticleLink {

    public static Graph<String, DefaultWeightedEdge> createGraph() {
        Graph<String, DefaultWeightedEdge> multiGraph = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        return multiGraph;
    }

    /** Just some tests */
    public static void testGraph(Graph<String, DefaultWeightedEdge> multiGraph) {
        multiGraph.addVertex("v1");
        multiGraph.addVertex("v2");
        multiGraph.addVertex("v3");
        multiGraph.addVertex("v4");

        multiGraph.addEdge("v1", "v2");
        multiGraph.addEdge("v1", "v3");
        multiGraph.addEdge("v1", "v4");
        multiGraph.addEdge("v1", "v1");
        multiGraph.addEdge("v4", "v1");
        multiGraph.addEdge("v1", "v1");

        multiGraph.getAllEdges("v1", "v4");
        multiGraph.containsEdge("v1", "v4");
        multiGraph.vertexSet();
        System.out.println(multiGraph.containsEdge("v1", "v2"));
        System.out.println("Degres entrants de V1 : " + multiGraph.inDegreeOf("v1"));
        System.out.println("Degres sortants de V1 : " + multiGraph.outDegreeOf("v1"));
        System.out.println("Liste des degres sortants de V1 : " + multiGraph.outgoingEdgesOf("v1"));
    }

    /**
     * Receive the list of the articles used in the run and build all the vertexs of
     * the multigraph.
     * 
     * @return Graph<String, DefaultWeightedEdge> multiGraph
     */
    public static Graph<String, DefaultWeightedEdge> fillGraphVertex(Graph<String, DefaultWeightedEdge> multiGraph,
            ArrayList<String> articleList) {
        articleList.forEach(article -> multiGraph.addVertex(article));
        return multiGraph;
    }

    /**
     * Receive the String content of an article used in the run and build all the
     * edges from the link of the other articles.
     * 
     * @return Graph<String, DefaultWeightedEdge> multiGraph
     */
    public static Graph<String, DefaultWeightedEdge> fillArticleGraphEdge(Graph<String, DefaultWeightedEdge> multiGraph,
            String currentArticle, ArrayList<String> linkedArticleList) {
        linkedArticleList.forEach(linkedArticle -> {
            if (multiGraph.containsVertex(linkedArticle)) {
                multiGraph.addEdge(currentArticle, linkedArticle);
            }
        });
        return multiGraph;
    }

    public static void printGraph(Graph<String, DefaultWeightedEdge> multiGraph) {
        System.out.println("\nCartographie du graphe des liens entre les articles :");
        Iterator<String> iter = new DepthFirstIterator<>(multiGraph);
        while (iter.hasNext()) {
            String vertex = iter.next();
            if (multiGraph.edgesOf(vertex) != null) {
                System.out.println("[" + vertex + "] => " + multiGraph.edgesOf(vertex).toString());
            }
        }
    }

    /**
     * Extract all of the linked articles from the content of a given article
     * 
     * @return ArrayList<String> linkedArticleList
     */
    public static ArrayList<String> getAllLinkedArticle(ArrayList<String> linkList) {
        ArrayList<String> linkedArticleList = new ArrayList<>();

        // Regex to extract only the file id of the article from the link pointing to it
        String regex = "(?<=\\/)([0-9]*)(?=\\.xml)";
        Pattern pattern = Pattern.compile(regex);

        linkList.forEach(link -> {
            Matcher matcher = pattern.matcher(link);

            while (matcher.find()) {
                linkedArticleList.add(matcher.group());
            }
        });

        return linkedArticleList;
    }

    /**
     * @param docList List of documents
     * @return ArrayList<String> articleList
     */
    public static ArrayList<String> createArticleList(ArrayList<Doc> docList) {
        ArrayList<String> articleList = new ArrayList<>();
        docList.forEach(doc -> articleList.add(doc.getId()));
        return articleList;
    }

}
