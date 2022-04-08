import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import javax.xml.crypto.Data;
import java.util.Collections;
import java.util.stream.*;

public class OnlineEM {


  private static void print2D(double[][] mat) {
    // Loop through all rows
    for (double[] row : mat){
      System.out.println();
      // Loop through all columns of current row
      for (double x : row)
        System.out.print(x + " ");
    }
    System.out.println();
  }

  private static double[][] multiply2D(double[][] a, double[][] b){
    double[][] res = new double[a.length][a[0].length];

    if(a.length != b.length){
      System.out.println("Elementwise multiplication is not possible, because the number of rows differ");
      System.out.println("a length: "+ a.length + "b lenght"+ b.length);
    }
    else if(a[0].length != b[0].length){
      System.out.println("Elementwise multiplication is not possible, because the number of cols differ");
      System.out.println("a[0] length: "+ a[0].length + "b[0] lenght"+ b[0].length);
    }
    else{
      for (int i=0; i<a.length; i++){
        for (int j=0; j<a[0].length; j++){
          res[i][j] = a[i][j]*b[i][j];
        }
      }
    }
    return res;
  }

  private static double[][] multiply2Dscalar(double[][] a, int scalar){
    double[][] res = new double[a.length][a[0].length];
      for (int i=0; i<a.length; i++){
        for (int j=0; j<a[0].length; j++){
          res[i][j] = a[i][j]*scalar;
        }
      }
    return res;
  }

  private static double[][] divide2D1D(double[][] a, double[] b){
    double[][] res = new double[a.length][a[0].length];

    if(a[0].length != b.length){ //Columns in a should be the same number as elements in b
      System.out.println("Elementwise division is not possible, because the number of rows differ");
      System.out.println("a length: "+ a.length + "b lenght"+ b.length);
    }
    else{
      for (int i=0; i<a.length; i++){
        for (int j=0; j<a[0].length; j++){
          res[i][j] = a[i][j]/b[j];
        }
      }
    }
    return res;
  }

  private static double[][][] divide3D2D(double[][][] a, double[][] b){
    double[][][] res = new double[a.length][a[0].length][a[0][0].length];

    if(a[0].length != b.length){ //Columns in a should be the same number as elements in b
      System.out.println("Elementwise multiplication is not possible, because the number of rows differ");
      System.out.println("a length: "+ a.length + "b lenght"+ b.length);
    }
    else{
      for (int i=0; i<a.length; i++){
        for (int j=0; j<a[0].length; j++){
          for (int k = 0; k < a[0][0].length; k++) {
            res[i][j][k] = a[i][j][k]/b[j][k];
          }
        }
      }
    }
    return res;
  }

  private static double[] divide1D(double[] a, double[] b){
    double[] res = new double[a.length];

    if(a.length != b.length){
      System.out.println("Elementwise division is not possible, because the number of elements differ");
      System.out.println("a length: "+ a.length + "b lenght"+ b.length);
    }

    else{
      for (int i=0; i<a.length; i++){
        res[i] = a[i]/b[i];
      }
    }
    return res;
  }

  private static double[][] add2D2D(double[][] a, double[][] b){
    double[][] res = new double[a.length][a[0].length];

    if(a.length != b.length){
      System.out.println("Elementwise addition is not possible, because the number of rows differ");
    }
    else if(a[0].length != b[0].length){
      System.out.println("Elementwise addition is not possible, because the number of cols differ");
    }
    else{
      for (int i=0; i<a.length; i++){
        for (int j=0; j<a[0].length; j++){
          res[i][j] = a[i][j]+b[i][j];
        }
      }
    }
    return res;
  }

  private static class ReturnBM{
    private double[][] tp;
    private HashMap<String, ArrayList<Double>> ep;

    protected ReturnBM(double[][] tp, HashMap<String, ArrayList<Double>> ep) {
      this.tp = tp;
      this.ep = ep;
    }
  }

  public static double[][] forward(ArrayList<String> seq, HashMap<String, ArrayList<Double>> emission_p, double[][] transition_p, double[] start_p){
    int number_of_states = start_p.length;
    int seq_len = seq.size();

    double[][] alpha = new double[number_of_states][seq_len];

    for(int s=0; s<number_of_states; s++){
      double ep = emission_p.get(seq.get(0)).get(s); //emission probability of the first string in the sequence from state s
      alpha[s][0] = start_p[s]*ep;
    }

    for (int token_index = 1; token_index<seq_len; token_index++){
      for (int current_state =0; current_state<number_of_states; current_state++){
//        previous_states =

        double[] prob_prev_state_current_state = new double[number_of_states];
        for (int prev_state =0; prev_state<number_of_states; prev_state++){

          prob_prev_state_current_state[prev_state] = alpha[prev_state][token_index-1]*transition_p[prev_state][current_state];
        }
        double sum_prob_prev_state_current_state = Arrays.stream(prob_prev_state_current_state).sum();
        alpha[current_state][token_index] = sum_prob_prev_state_current_state*(emission_p.get(seq.get(token_index)).get(current_state));
      }
    }

    double forwardprob = 0;
    for (int state =0; state<number_of_states; state++){
      forwardprob+=alpha[state][seq_len-1];
    }

//    System.out.println("Forward probability: " + forwardprob);

    return alpha;
  }

  public static double[][] backward(ArrayList<String> seq, HashMap<String, ArrayList<Double>> emission_p, double[][] transition_p, double[] start_p){
    int number_of_states = start_p.length;
    int seq_len = seq.size();

    double[][] beta = new double[number_of_states][seq_len];

    for(int s=0; s<number_of_states; s++){
      beta[s][seq_len-1] = 1;
    }

    for (int token_index = seq_len-2; token_index>-1; token_index--){
      for (int current_state =0; current_state<number_of_states; current_state++){

        double[] prob_current_state_next_state = new double[number_of_states];
        for (int next_state =0; next_state<number_of_states; next_state++){

          prob_current_state_next_state[next_state] = beta[next_state][token_index+1]*transition_p[current_state][next_state]*emission_p.get(seq.get(token_index+1)).get(next_state);
        }

        double sum_prob_current_state_next_state = Arrays.stream(prob_current_state_next_state).sum();
        beta[current_state][token_index] = sum_prob_current_state_next_state;
      }
    }

    double backwardprob = 0; //observation given model
    for (int state =0; state<number_of_states; state++){
      backwardprob+=beta[state][0]*emission_p.get(seq.get(0)).get(state)*start_p[state];
    }

//    System.out.println("Backward probability: " + backwardprob);

    return beta;
  }

  private static ReturnBM baumWelch(
      ArrayList<String> seq,
      HashMap<String, ArrayList<Double>> emission_p,
      double[][] transition_p,
      double[] start_p) {
//    System.out.println("Baum Welch");
    int number_of_states = start_p.length;
    int seq_len = seq.size();

    double[][] alpha = forward(seq, emission_p, transition_p, start_p);
    double[][] beta = backward(seq, emission_p, transition_p, start_p);

    // Expectaion

    double[][] alpha_beta = multiply2D(alpha, beta);

    double[] gamma_normalization = new double[seq_len];
    for (int i = 0; i <seq_len; i++) {
      double column_sum = 0;
      for (int j = 0; j <number_of_states; j++) {
        column_sum += alpha_beta[j][i];
      }
      gamma_normalization[i] = column_sum;
    }

    double[][] gamma = divide2D1D(alpha_beta, gamma_normalization); // Normalization: cols should sum to 1

    //normalization over all counts i->j (in all possible terminals)
    double[][][] xi =
        new double[number_of_states][number_of_states]
            [seq_len
                - 1]; // Probs for transition from current_state at position t to next_state for all
                      // current state, next state and t
    for (int i = 0; i < number_of_states; i++) {
      for (int j = 0; j < number_of_states; j++) {
        for (int t = 0; t < seq_len - 1; t++) {
          xi[i][j][t] =
              alpha[i][t] * transition_p[i][j] * emission_p.get(seq.get(t)).get(j) * beta[j][t + 1];
        }
      }
    }

    double[][] xi_normalization = new double[number_of_states][seq_len];

      for (int j = 0; j <number_of_states; j++) {
        for (int k = 0; k < seq_len-1; k++) {
          double column_sum = 0;
          for (int i = 0; i <number_of_states; i++) {
            column_sum += xi[i][j][k];
          }
          xi_normalization[j][k]=column_sum;
      }
    }

    xi = divide3D2D(xi, xi_normalization);

    // Maximisation

    // emission probs
    HashMap<String, ArrayList<Double>> b = (HashMap<String, ArrayList<Double>>) emission_p.clone();

    double[] denominators = new double[number_of_states];
    double[] numerators = new double[number_of_states];

    // Calculating numerators
    for (int state = 0; state < number_of_states; state++) {
      double sum_gamma = 0;
      for (int token_index = 0; token_index < seq_len; token_index++) {
        sum_gamma += gamma[state][token_index];
      }
      denominators[state] = sum_gamma;
    }

    // Caluclating denominators
    for (String token : b.keySet()) {
      for (int state = 0; state < number_of_states; state++) {
        double sum_gamma = 0;
        for (int token_index = 0; token_index < seq_len; token_index++) {
          if (seq.get(token_index).equals(token)) {
            sum_gamma += gamma[state][token_index];
          }
        }
        numerators[state] = sum_gamma;
      }
      double[] eps_token = divide1D(numerators, denominators);
      b.put(
          token, new ArrayList<>(DoubleStream.of(eps_token).boxed().collect(Collectors.toList())));
    }

    // transition probs
    double[][] a = new double[number_of_states][number_of_states];
    for (int i = 0; i < number_of_states; i++) {

      double denominator = 0;
      for (int j = 0; j < number_of_states; j++) {
        denominator+= Arrays.stream(xi[i][j]).sum();
      }

      for (int j = 0; j < number_of_states; j++) {
        double numerator = Arrays.stream(xi[i][j]).sum();
        a[i][j] = numerator/denominator;
      }
    }

    return new ReturnBM(a, b);
  }


  public static double[] random1D(int size){
    double[] res = new double[size];
    for (int i = 0; i <size ; i++) {
      res[i] = Math.random();
    }
    return res;
  }
  public static double[][] random2D(int size1, int size2){
    double[][] res = new double[size1][size2];
    for (int i = 0; i <size1 ; i++) {
      for (int j = 0; j < size2; j++) {
        res[i][j] = Math.random();
      }
    }
    return res;
  }

  public static double[][] tpNormalization(double[][] transitionProbs){
    int clustersNumber = transitionProbs.length;
    double[] sumsTransitionProbs = new double[clustersNumber];
    for (int i = 0; i <clustersNumber; i++) {
      for (int j = 0; j <clustersNumber ; j++) {
        sumsTransitionProbs[i]+=transitionProbs[i][j];
      }
    }
    for (int i = 0; i < clustersNumber; i++) {
      for (int j = 0; j <clustersNumber; j++) {
        double numerator = transitionProbs[i][j];
        transitionProbs[i][j] = numerator/sumsTransitionProbs[i];
      }
    }

    return transitionProbs;
  }

  public static void simpleTest(){
    ArrayList<String> seq = new ArrayList<String>(Arrays.asList("C", "C","G", "A", "A", "G", "T", "G"));
    HashMap<String, ArrayList<Double>> emission_p = new HashMap<String, ArrayList<Double>>(){{
      put("A", new ArrayList<>(Arrays.asList(0.7, 0.3, 0.4)));
      put("T", new ArrayList<>(Arrays.asList(0.1, 0.2, 0.2)));
      put("C", new ArrayList<>(Arrays.asList(0.1, 0.4, 0.2)));
      put("G", new ArrayList<>(Arrays.asList(0.1, 0.1, 0.2)));
    }};
    double[][] transition_p = {{0.2, .4, .4}, {0.1, 0.6, 0.3},{0.8, 0.1, 0.1}};
    double[] start_p = {.3, .3, .4};

    double[][] alpha = forward(seq, emission_p, transition_p, start_p);
    double[][] beta = backward(seq, emission_p, transition_p, start_p);


    print2D(alpha);
    print2D(beta);

    ReturnBM rbm = baumWelch(seq, emission_p, transition_p, start_p);

    print2D(rbm.tp);
  }

  public static ReturnBM iterBaumWelch(ArrayList<ArrayList<ArrayList<String>>> sentences, int iters, int subsetSize, int clustersNumber){

    //INITALIZATION OF MATRICES

    double[] startProbs = random1D(clustersNumber);
    double sumStartProbs = 0;
    for(int i = 0; i <clustersNumber ; i++) {
      sumStartProbs+=startProbs[i];
    }
    for(int i = 0; i <clustersNumber ; i++) {
      double numerator = startProbs[i];
      startProbs[i] = numerator/sumStartProbs;
    }

    double[][] transitionProbs = random2D(clustersNumber, clustersNumber);
    transitionProbs = tpNormalization(transitionProbs);

    //Deep Copy of sentences that is shuffled
    Collections.shuffle(sentences);
//    int subsetSize = 100;
    ArrayList<ArrayList<ArrayList<String>>> croppedSentences =  new ArrayList<>();
    for (int i = 0; i <subsetSize; i++) {
      croppedSentences.add(sentences.get(i));
    }

    HashSet<String> vocabSentsSet = new HashSet<>();
    for (ArrayList<ArrayList<String>> sent: sentences) {
      for (ArrayList<String> tokenData : sent ) {
        vocabSentsSet.add(tokenData.get(2));
      }
    }
    ArrayList<String> vocabSents = new ArrayList<>(vocabSentsSet);

    int T = vocabSents.size();
    int Z = subsetSize;
    int S = clustersNumber;

    double[][][] M =  new  double[T][Z][S];

    for (int z = 0; z < Z; z++) {
      ArrayList<ArrayList<String>> sent = croppedSentences.get(z);
      for (ArrayList<String> tokenData : sent) {
        int t = vocabSents.indexOf(tokenData.get(2));

        double[] stateCounts = random1D(S);
        double sumStateCounts = 0;
        for(int i = 0; i <S; i++) {
          sumStateCounts+=stateCounts[i];
        }

        for(int i = 0; i <S; i++) {
          M[t][z][i]+=stateCounts[i]/sumStateCounts;
        }
      }
    }

    // BAUM WELCH ITERATIONS
    double[][] tpFinal = new double[S][S];
    HashMap<String, ArrayList<Double>> epFinal = new HashMap<>();

    for (int iter = 0; iter<iters; iter++) {
      //
      System.out.println("Iteration: "+ (iter+1) );

      //Emission probs
      HashMap<String, ArrayList<Double>> emissionProbs = new HashMap<>();

      double[] denom = new double[S];
      for (int t = 0; t < T; t++) {
        for (int z =0; z < Z; z++) {
          for (int s = 0; s <S; s++) {
            denom[s]+=M[t][z][s];
          }
        }
      }

      for (int t = 0; t < T; t++) {
        String tokenLowercase = vocabSents.get(t);
        double[] numerator = new double[S];
        for (int z =0; z < Z; z++) {
          for (int s = 0; s <S; s++) {
            numerator[s]+=M[t][z][s];
          }
        }
        double[] emissionsPrimitive = divide1D(numerator, denom);
        ArrayList<Double> emissions = DoubleStream.of(emissionsPrimitive).boxed().collect(Collectors.toCollection(ArrayList::new));

        emissionProbs.put(tokenLowercase, emissions);
      }

      //Baum Welch
      double[][] transitionProbsTotal = new double[S][S];
      ArrayList<Integer> randomSenteneOrder = IntStream.range(0,Z).boxed().collect(Collectors.toCollection(ArrayList::new));
      Collections.shuffle(randomSenteneOrder);

      for (Integer z : randomSenteneOrder ) {
        ArrayList<ArrayList<String>> sentAllData= croppedSentences.get(z);
        ArrayList<String> sent  = new ArrayList<>();
        for (ArrayList<String> tokenData: sentAllData) {
          sent.add(tokenData.get(2));
        }
        ReturnBM rBM = baumWelch(sent, emissionProbs, transitionProbs, startProbs);

        for (String word : sent) {
          int t = vocabSents.indexOf(word);
          for (int s = 0; s < S; s++) {
            M[t][z][s] = 0;
          }
        }

        for (String word : sent) {
          int t = vocabSents.indexOf(word);
          ArrayList<Double> epWord = rBM.ep.get(word);
          for (int s = 0; s < S; s++) {
            M[t][z][s] += epWord.get(s);
          }
        }
        transitionProbsTotal = add2D2D(transitionProbsTotal,  multiply2Dscalar(rBM.tp, sent.size()));

      }

      transitionProbs = tpNormalization(transitionProbsTotal);


      if(iter%5==0){
        print2D(transitionProbs);
      }

      tpFinal = transitionProbs;
      epFinal = emissionProbs;
    }
//    print2D(transitionProbs);

    ReturnBM res = new ReturnBM(tpFinal, epFinal);
    return res;
  }
  public static void main(String[] args) {
    //simpleTest();


    ArrayList<ArrayList<ArrayList<String>>> sentences = DataLoader.populateSentences(DataLoader.PTB_PATH);
    System.out.println(sentences.size() + " sentences were loaded.");
    int iters = 30;
    int subsetSize = 250;
    int clustersNumber = 4; //noun, verb, adjective, function words
    ReturnBM rBM = iterBaumWelch(sentences, iters, subsetSize, clustersNumber);
    System.out.println(iters + " iterations over " + subsetSize + " sentences were completed. ");
    System.out.println("Final transition probabilities: ");
    print2D(rBM.tp);

  }

}
