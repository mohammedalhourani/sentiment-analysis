package com.example.test;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.ejml.simple.SimpleMatrix;

import java.util.List;
import java.util.Properties;

public class StanfordSentimentAnalyzerProcessor {
    static StanfordCoreNLP pipeline;


    static {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
        props.setProperty("parse.maxlen", "80");
        props.setProperty("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.caseless.distsim.crf.ser.gz");
       props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");

        pipeline = new StanfordCoreNLP(props);
    }

    public static double populateOverallSentimentIndexScore(String text, SentimentResult sentimentResult) {
        Annotation nlpDocument = new Annotation(text);
        pipeline.annotate(nlpDocument);


        double sum = 0;
        SimpleMatrix sumScore = new SimpleMatrix(5, 1, false, new double[]{0, 0, 0, 0, 0});
        int numberOfSentences = 0;
        List<CoreMap> sentences = nlpDocument.get(CoreAnnotations.SentencesAnnotation.class);
        if (text.trim() != "")
            for (CoreMap sentence : sentences) {
                String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                Tree sentiments = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int predictedClass = RNNCoreAnnotations.getPredictedClass(sentiments);
                SimpleMatrix matrix = RNNCoreAnnotations.getPredictions(sentiments);
                sentimentResult.getSentences().add(new SentenceResult(sentiment, predictedClass, matrix.get(predictedClass), matrix));
                if (predictedClass == 2) { // neutral sentiment
                    continue;
                }
                sum += predictedClass;
                sumScore = sumScore.plus(matrix);
                numberOfSentences++;
            }
        double overallClassIndexScore = numberOfSentences == 0 ? 0 : (sum / numberOfSentences - 2) / 2;
        int matrixIndex =
                overallClassIndexScore < -0.5 ? 0  // very negative
                        : overallClassIndexScore < 0.0 ? 1  // negative
                        : overallClassIndexScore == 0.0 ? 2  // neutral
                        : overallClassIndexScore < 0.5 ? 3  // positive
                        : 4;
        String overallClass = overallClassIndexScore < -0.5 ? "Very Negative"  // very negative
                : overallClassIndexScore < 0.0 ? "Negative"  // negative
                : overallClassIndexScore == 0.0 ? "Neutral"  // neutral
                : overallClassIndexScore < 0.5 ? "Positive"  // positive
                : "Very Positive";    // very positive

        sentimentResult.setOverallSentimentClassIndex(matrixIndex);
        sentimentResult.setOverallSentimentClassIndexScore(overallClassIndexScore);
        sentimentResult.setOverallSentimentClass(overallClass);
        sentimentResult.setOverallSentimentScore(numberOfSentences == 0 ? 0 : (sumScore.get(matrixIndex) / numberOfSentences));
        return overallClassIndexScore;
    }
}
