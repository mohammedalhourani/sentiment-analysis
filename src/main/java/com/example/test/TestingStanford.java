package com.example.test;


import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;

public class TestingStanford {
    public static void main(String[] args) throws IOException {
        System.clearProperty("GOOGLE_APPLICATION_CREDENTIALS");
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "./json-key.json");
        InputStream is = TestingStanford.class.getClassLoader().getResource("langdetect-183.bin").openStream();
        LanguageDetectorModel m = new LanguageDetectorModel(is);
        LanguageDetector myCategorizer = new LanguageDetectorME(m);
        String text = "bad token , etherum is very bad so don't buy it. I had very bad experience! ADA is very good, buy ADA instead!";
        //Language bestLanguage = myCategorizer.predictLanguage(text);

        DateTime start = DateTime.now();

        for (int i = 0; i < 1000; i++) {
           SentimentResult sentimentResult = new SentimentResult();
            sentimentResult = new StanfordSentimentAnalyzerProcessor().populateOverallSentimentIndexScore(text );
            //Language bestLanguage = myCategorizer.predictLanguage(text);
            //System.out.println("overall score\t" + sentimentResult.getOverallSentimentClass());
        }
        DateTime end = DateTime.now();

        System.out.println("time in seconds\t" + (end.minus(start.getMillis()).toString() ));
    }


}
