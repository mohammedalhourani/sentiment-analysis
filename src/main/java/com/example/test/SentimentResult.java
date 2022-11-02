package com.example.test;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public class SentimentResult implements Serializable {
    public String getOriginalText() {
        return OriginalText;
    }

    public void setOriginalText(String originalText) {
        OriginalText = originalText;
    }

    String OriginalText;
    String OverallSentimentClass;
    int OverallSentimentClassIndex;

    public double getOverallSentimentClassIndexScore() {
        return OverallSentimentClassIndexScore;
    }

    public void setOverallSentimentClassIndexScore(double overallSentimentClassIndexScore) {
        OverallSentimentClassIndexScore = overallSentimentClassIndexScore;
    }

    double OverallSentimentClassIndexScore;
    double OverallSentimentScore;
    Collection<SentenceResult> sentences ;
    public SentimentResult()
    {
        this.OriginalText = "";
        this.OverallSentimentClass = "Neutral";
        this.OverallSentimentClassIndex = 0;
        this.OverallSentimentClassIndexScore = 0;
        this.OverallSentimentScore = 0;
        this.sentences = new LinkedList<>();
    }

    public SentimentResult(String originalText, String overallSentimentClass, int overallSentimentClassIndex, double overallSentimentClassIndexScore, double overallSentimentScore, Collection<SentenceResult> sentences) {
        OriginalText = originalText;
        OverallSentimentClass = overallSentimentClass;
        OverallSentimentClassIndex = overallSentimentClassIndex;
        OverallSentimentClassIndexScore = overallSentimentClassIndexScore;
        OverallSentimentScore = overallSentimentScore;
        this.sentences = sentences;
    }


    public String getOverallSentimentClass() {
        return OverallSentimentClass;
    }

    public void setOverallSentimentClass(String overallSentimentClass) {
        OverallSentimentClass = overallSentimentClass;
    }

    public int getOverallSentimentClassIndex() {
        return OverallSentimentClassIndex;
    }

    public void setOverallSentimentClassIndex(int overallSentimentClassIndex) {
        OverallSentimentClassIndex = overallSentimentClassIndex;
    }

    public double getOverallSentimentScore() {
        return OverallSentimentScore;
    }

    public void setOverallSentimentScore(double overallSentimentScore) {
        OverallSentimentScore = overallSentimentScore;
    }

    public Collection<SentenceResult> getSentences() {
        return sentences;
    }

    public void setSentences(Collection<SentenceResult> sentences) {
        this.sentences = sentences;
    }
}
