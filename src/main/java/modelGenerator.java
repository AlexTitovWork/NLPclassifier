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
import java.util.ArrayList;
import java.util.Arrays;

// add new function

/**
 * makeTokinizerModel()     - generate tokinizer model for split sentance on words
 * makeDataTrainingModel()  - generate POS model for detect category
 *
 * @edit by alexeytitovwork, BlackPoint LLC
 * @last modyfy by alexeytitovwork 13.02.2017
 * alexeytitovwork@mail.com
 * @author Alex Titov
 * @update Alex Titov 29/09/2017
 * @since 0.1.0 added 11/12/16
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
public class modelGenerator {
    private String textContainer;

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
    public TokenizerModel modelT;       //tokinizer model
    private InputStream inputStream;

    public modelGenerator() throws IOException {

        URL url = getClass().getClassLoader().getResource("en-sent.bin");
        inSentenceStream = url.openStream();
//			inSentenceStream = this.class.getResourceAsStream("/resources/en-sent.bin");
        sentenceModel = new SentenceModel(inSentenceStream);
        inSentenceStream.close();
        sentenceDetector = new SentenceDetectorME(sentenceModel);
//
        //println "-> Nimbler: NlpService: loading tokenizer model";
        inTokenStream = getClass().getClassLoader().getResourceAsStream("en-token.bin");
        tokenModel = new TokenizerModel(inTokenStream);
        inTokenStream.close();
        //tokenizer = new SimpleTokenizer() 	//Old tokinizer;
        tokenizer = new TokenizerME(tokenModel); // New tokinizer maximum estimation
        // TODO change model for correct recognition

        //println "-> Nimbler: NlpService: loading POS model";
//			inPosStream = getClass().getClassLoader().getResourceAsStream("ru-buy-model.bin");
        inPosStream = getClass().getClassLoader().getResourceAsStream("gen_POS_model.dat");

        posModel = new POSModel(inPosStream);
        inPosStream.close();
        posTagger = new POSTaggerME(posModel);

    }


    public void makeDataTrainingModel() {

        model = null;
        System.out.println("POS model started");
        InputStream dataIn = null;
        try {
            String currentDir = new File("").getAbsolutePath();
            dataIn = new FileInputStream(currentDir + "\\src\\main\\resources\\gen_data.txt");  //training data
            ObjectStream<String> lineStream = new PlainTextByLineStream((InputStreamFactory) dataIn, "UTF-8");
            ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream);

            model = POSTaggerME.train("en", sampleStream, TrainingParameters.defaultParams(), null);
        } catch (IOException e) {
            // Failed to read or parse training data, training failed
            e.printStackTrace();
        } finally {
            if (dataIn != null) {
                try {
                    dataIn.close();
                } catch (IOException e) {
                    // Not an issue, training already finished.
                    // The exception should be logged and investigated
                    // if part of a production system.
                    e.printStackTrace();
                }
            }
        }
        System.out.println("POS model done...");
        System.out.println("Success generate model...");
        //write Data model
        OutputStream modelOut = null;
        try {
            String currentDir = new File("").getAbsolutePath();
            modelOut = new BufferedOutputStream(new FileOutputStream(currentDir + "//src//main//resources//gen_POS_model.dat"));


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
        System.out.println("Success generate and write model...");
    }


    public String[] sentenceDetect(String message) {
        //println "-> Nimbler: NlpService: sentence detect";
        return sentenceDetector.sentDetect(message);
    }

    public String[] tokenize(String message) {
        System.out.println("-> Nimbler: NlpService: tokenize");

        return tokenizer.tokenize(message);
    }

    public String[] tag(String[] message) {
        System.out.println("-> Nimbler: NlpService: tagging");
        String[] tags = posTagger.tag(message);
        probs = posTagger.probs();
        //probs =  (float*)posTagger.probs();

        //tags.each {tag->println tag}

        return tags;
    }

    public double[] viewProb() {

        System.out.println("-> Nimbler: NlpService: probability");
        //print  after first tagging

        return this.probs;
    }


    public Sequence[] topKSequences(String[] message){

        System.out.println( "-> Nimbler: NlpService: topKSequences");
        //print  after first tagging

        Sequence sequences[] = posTagger.topKSequences(message);
        for (Sequence sequence : sequences) {
            System.out.println(sequence);
        }
        return sequences;
    }

    public ArrayList textAnalisis(String inputdata) {


        System.out.println("-> Nimbler: RestController: current message:\n " + inputdata);
        /**
         * Detector of sentence and divide text on independent sentence.
         */
        String[] sentences = sentenceDetect(inputdata);
        System.out.println(Arrays.toString(sentences));
        /**
         * Split sentence on words and numbers(tokens)
         */
        String[] tokens = tokenize(inputdata);
        System.out.println(Arrays.toString(tokens));
        /**
         * Classyfy all tokens by category (POS - tagger).
         * Set tag to any token.
         */
        String[] tags = tag(tokens);
        System.out.println("Detected tag:\n");
        System.out.println(Arrays.toString(tags));

        double[] probsCurr = viewProb();
        System.out.println(Arrays.toString(probs));

        System.out.println("ProbThresholdFilter start...");
        /**
         * Deleted all duplicate category with small probability.
         * This method get only one maxProb category.
         * Method delete all objects smaller than TH.
         */
        double TH = 0.5; //bots ->boots
        System.out.println(Arrays.toString(tags));
        /**
         * If the probability of detecting a category is low,
         * choose the most likely one from the list
         */

        ArrayList containerResult = new ArrayList();

        for (int i = 0; i < tokens.length; i++) {
            containerResult.add(tokens[i]);
            containerResult.add(tags[i]);
        }


        System.out.println("Most probability phrase, get first phrase if" + "\nprob > " + TH + " ...");
        Sequence tagTree[] = topKSequences(tokens);

        return containerResult;
    }


}



