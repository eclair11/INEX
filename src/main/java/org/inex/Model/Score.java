package org.inex.Model;

public class Score implements Comparable<Score> {

    private String node;
    private double value;
    private double norm;

    public Score() {
    }

    /**
     * Constructor used to save the score of a document
     * 
     * @param node  Path of the element (for ranking by elements)
     * @param value Value of the score
     * @param norm  Value of the norm for LTC
     */
    public Score(String node, double value, double norm) {
        this.node = node;
        this.value = value;
        this.norm = norm;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getNorm() {
        return norm;
    }

    public void setNorm(double norm) {
        this.norm = norm;
    }

    /**
     * Override used to compare score documents based on value attribut
     * 
     * @param score Object to compare with this object
     */
    @Override
    public int compareTo(Score score) {
        if (this.value > score.getValue()) {
            return 1;
        } else if (this.value < score.getValue()) {
            return -1;
        }
        return 0;
    }

}
