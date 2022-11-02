package com.example.test;

import org.ejml.simple.SimpleMatrix;

import java.io.Serializable;

public class SentenceResult implements Serializable {
    String Sentiment;
    int SentimentClassIndex;
    double SentimentScore;

    public SentenceResult(String sentiment, int sentimentClassIndex, double sentimentScore, SimpleMatrix sentimentMatrix) {
        Sentiment = sentiment;
        SentimentClassIndex = sentimentClassIndex;
        SentimentScore = sentimentScore;
        this.sentimentMatrix = sentimentMatrix;
    }

    public SimpleMatrix getSentimentMatrix() {
        return sentimentMatrix;
    }

    public void setSentimentMatrix(SimpleMatrix sentimentMatrix) {
        this.sentimentMatrix = sentimentMatrix;
    }

    SimpleMatrix sentimentMatrix;

    public String getSentiment() {
        return Sentiment;
    }

    public void setSentiment(String sentiment) {
        Sentiment = sentiment;
    }

    public int getSentimentClassIndex() {
        return SentimentClassIndex;
    }

    public void setSentimentClassIndex(int sentimentClassIndex) {
        SentimentClassIndex = sentimentClassIndex;
    }

    public double getSentimentScore() {
        return SentimentScore;
    }

    public void setSentimentScore(double sentimentScore) {
        SentimentScore = sentimentScore;
    }
}
