import java.io.IOException;

public class NlpProductClassifier {

     public static void main(String[] args) throws IOException {

         NLPClassifier CL = new NLPClassifier();
         /**
          * Sentence detector test
          */
         String[] sentences = CL.sentenceDetect("My text about boots and dress. It is next sentence. It is last sentence.");
         for (String sent:sentences) {
             System.out.println(sent);
         }

         /**
          * Detecting tokens in first sentence
          */
         String[] tokens = CL.tokenize(sentences[0]);
         for (String tok:tokens) {
             System.out.println(tok);
         }
         /**
          * Detecting  category for intrest tokens
          */
         String[] tags = CL.tag(tokens);
         /**
          * Focusing on 3 and 5 elements of sentence
          *
          */
         System.out.println(tokens[3] + " it is " + tags[3]);
         System.out.println(tokens[5] + " it is " + tags[5]);


         /**
          * For example veiw model proccess generating, model it is not good for data analisys, but illustrated tools usage.
          * For getting good model your data set must be larger than 5000 elements, better it will be about 50000 elements.
          */
          CL.makeDataTrainingModel();

     }
}