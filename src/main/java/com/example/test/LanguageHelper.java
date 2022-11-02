package com.example.test;
import opennlp.tools.langdetect.*;

import java.io.IOException;
import java.io.InputStream;

public class LanguageHelper {

    static LanguageDetector myCategorizer = null;
    LanguageHelper()
    {

    }

    public static LanguageDetector getLanguageHelper() throws IOException {
        if(myCategorizer == null)
        {
            InputStream is = LanguageHelper.class.getClassLoader().getResource("langdetect-183.bin").openStream();
            LanguageDetectorModel m = new LanguageDetectorModel(is);
            myCategorizer = new LanguageDetectorME(m);
        }
        return myCategorizer;
    }

}
