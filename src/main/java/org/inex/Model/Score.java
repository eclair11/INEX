package org.inex.Model;

public class Score {

    private String id;
    private double value;
    private String node;

    public Score() {
    }

    /**
     * Constructor used to save the score of a document
     * 
     * @param id    Identifier of the document
     * @param value Value of the score
     * @param node  Path of the element (for ranking by elements)
     */
    public Score(String id, double value, String node) {
        this.id = id;
        this.value = value;
        this.node = node;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

}
