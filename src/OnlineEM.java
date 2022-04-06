import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import javax.xml.crypto.Data;

public class OnlineEM {


  private static void print2D(double[][] mat)
  {
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
    }
    else if(a[0].length != b[0].length){
      System.out.println("Elementwise multiplication is not possible, because the number of cols differ");
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

  private static double[][] divide2D1D(double[][] a, double[] b){
    double[][] res = new double[a.length][a[0].length];

    if(a[0].length != b.length){ //Columns in a should be the same number as elements in b
      System.out.println("Elementwise multiplication is not possible, because the number of rows differ");
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
      System.out.println("Elementwise multiplication is not possible, because the number of elements differ");
    }

    else{
      for (int i=0; i<a.length; i++){
        res[i] = a[i]/b[i];
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

    System.out.println("Forward probability: " + forwardprob);

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

    System.out.println("Backward probability: " + backwardprob);

    return beta;
  }

  private static ReturnBM baumWelch(
      ArrayList<String> seq,
      HashMap<String, ArrayList<Double>> emission_p,
      double[][] transition_p,
      double[] start_p) {
    System.out.println("Baum Welch");
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

  //TODO
//  public static ReturnBM onlineEM(
//      ArrayList<ArrayList<String>> sequences,
//      HashMap<String, ArrayList<Double>> emission_p,
//      double[][] transition_p,
//      double[] start_p) {
//    System.out.println("Online EM");
//    int number_of_states = start_p.length;
//    int seq_len = seq.size();
//
//    return new ReturnBM(tp, ep);
//    }

  public static void main(String[] args) {
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

    ArrayList<ArrayList<ArrayList<String>>> sentences = DataLoader.populateSentences(DataLoader.PTB_PATH);
//    System.out.println(sentences.get(3).get(3).get(2));

  }

}
