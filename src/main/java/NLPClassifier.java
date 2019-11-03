/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.*;

import java.io.*;
import java.net.URL;

// add new function

/**
 * makeDataTrainingModel()  - generate POS model for detect category
 * sentDetect()             - sentence detector
 * tokenize()               - tokinizer for split sentance on words
 * makeDataTrainingModel()  -
 *
 * @edit by alexeytitovwork, BlackPoint LLC
 * @last modyfy by alexeytitovwork 13.02.2017
 * alexeytitovwork@mail.com
 * @author Alex Titov
 * @update Alex Titov 29/09/2017
 * @since 0.1.0 added tokinizer 11/12/16
 * @since 0.0.1 created 27/06/2017
 */

/**
 *
 * @author Alex Titov
 * @since 0.0.1 created 27/06/2017
 * @update Alex Titov 29/09/2017
 */

/**
 * Data model generator based on gen_data.txt
 */
public class NLPClassifier {
    private final SentenceDetector sentenceDetector;
    private final InputStream inSentenceStream;
    private final SentenceModel sentenceModel;
    private final Tokenizer tokenizer;
    private final InputStream inTokenStream;
    private final TokenizerModel tokenModel;
    private final InputStream inPosStream;
    private final POSModel posModel;
    private final POSTaggerME posTagger;
    private double[] probs;
    private POSModel model;             //postagger model
    private InputStream inputStream;

    public NLPClassifier() throws IOException {

        URL url = getClass().getClassLoader().getResource("en-sent.bin");
        inSentenceStream = url.openStream();
        sentenceModel = new SentenceModel(inSentenceStream);
        inSentenceStream.close();
        sentenceDetector = new SentenceDetectorME(sentenceModel);
//
        inTokenStream = getClass().getClassLoader().getResourceAsStream("en-token.bin");
        tokenModel = new TokenizerModel(inTokenStream);
        inTokenStream.close();
        tokenizer = new TokenizerME(tokenModel); // New tokinizer maximum estimation

        inPosStream = getClass().getClassLoader().getResourceAsStream("en-pos.dat");

        posModel = new POSModel(inPosStream);
        inPosStream.close();
        posTagger = new POSTaggerME(posModel);

    }


    public void makeDataTrainingModel() {
        model = null;
        System.out.println("POS model started");
//        InputStream dataIn = null;
        InputStreamFactory dataIn = null;
        try {
            dataIn = new InputStreamFactory() {
                public InputStream createInputStream() throws IOException {
//                    return NLPClassifier.class.getResourceAsStream("/home/interceptor/src/main/resources/en-pos.txt");
                     return NLPClassifier.class.getResourceAsStream("en-pos.txt");

                }
            };

            ObjectStream<String> lineStream = new PlainTextByLineStream((InputStreamFactory) dataIn, "UTF-8");
            ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream);

            model = POSTaggerME.train("en", sampleStream, TrainingParameters.defaultParams(), null);
        } catch (IOException e) {
            // Failed to read or parse training data, training failed
            e.printStackTrace();
        } finally {
            if (dataIn != null) {
                //                    dataIn.close();
                System.out.println("InputStreamFactory not create!");
            }
        }
        System.out.println("POS model done...");
        System.out.println("Success generate model...");
        //write Data model
        OutputStream modelOut = null;
        try {
            String currentDir = new File("").getAbsolutePath();
            modelOut = new BufferedOutputStream(new FileOutputStream(currentDir + "//src//main//resources//example-bad-model.dat"));


            model.serialize(modelOut);
        } catch (IOException e) {
            // Failed to save model
            e.printStackTrace();
        } finally {
            if (modelOut != null) {
                try {
                    modelOut.close();
                } catch (IOException e) {
                    // Failed to correctly save model.
                    // Written model might be invalid.
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Model generated and treated successfully...");
    }


    public String[] sentenceDetect(String message) {

        System.out.println("-> OpenNLP: sentence detector");
        return sentenceDetector.sentDetect(message);
    }

    public String[] tokenize(String message) {

        System.out.println("-> OpenNLP: token detector");
        return tokenizer.tokenize(message);
    }

    public String[] tag(String[] message) {

        System.out.println("-> OpenNLP: tag detector");
        String[] tags = posTagger.tag(message);
        probs = posTagger.probs();
        return tags;
    }

    public double[] viewProb() {

        System.out.println("-> OpenNLP: probability");
        return this.probs;
    }


    public Sequence[] topKSequences(String[] message) {

        System.out.println("-> OpenNLP: topKSequences");
        Sequence sequences[] = posTagger.topKSequences(message);
        for (Sequence sequence : sequences) {
            System.out.println(sequence);
        }
        return sequences;
    }

}



