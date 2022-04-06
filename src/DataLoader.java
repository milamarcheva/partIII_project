import java.util.*;
import java.io.*;
//import  java.util.Arrays.*;
//import java.util.stream.Collectors;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;

public class DataLoader {

  private static final String TAB_DELIMETER = "\t";
  protected static final String PTB_PATH = System.getProperty("user.dir") + "\\resources\\ptb-dep\\";


  private static ArrayList<ArrayList<ArrayList<String>>> sentences = new ArrayList<>();

  private DataLoader(ArrayList<ArrayList<ArrayList<String>>> sentences) {
    this.sentences = sentences;
  }

  public void setSentences(ArrayList<ArrayList<ArrayList<String>>> sentences) {
    this.sentences = sentences;
    }

  public ArrayList<ArrayList<ArrayList<String>>> getSentences(){
    return this.sentences;
  }


  protected static ArrayList<ArrayList<ArrayList<String>>> populateSentences(String ptbPath){
    ArrayList<ArrayList<ArrayList<String>>> sentences = new ArrayList<>();

    String[] filenames;
    File f = new File(ptbPath);
    filenames = f.list();

    for(String filename : filenames){
      ArrayList<ArrayList<String>> sent = new ArrayList<>();
      String filepath = ptbPath + filename;
      try (BufferedReader br =
          new BufferedReader(new FileReader(filepath))) {
        String line;
        while ((line = br.readLine()) != null) {
            if(line.equals("")){
              sentences.add(sent);
              sent = new ArrayList<>();
            }
            else{
              String[] allValues = line.split(TAB_DELIMETER);
              //token_data: index, og_token, lowercased_token, Universal pos tag, PennTB pos tag
              ArrayList<String> token_data = new ArrayList<>(Arrays.asList(allValues[0], allValues[1], allValues[1].toLowerCase(), allValues[3], allValues[4]));
              sent.add(token_data);
            }
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

//    assert sentences.size() == 49208;
    return sentences;
  }

  public static ArrayList<String> getLines(String filepath) {
    ArrayList<String> lines = new ArrayList<>();
    try (BufferedReader br =
        new BufferedReader(new FileReader(filepath))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split(TAB_DELIMETER);
        lines.add(line);
      }
    }
    catch(java.io.FileNotFoundException fnfe){
      System.out.println("\n File not found exception for filename: " +filepath);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
    }

  public static ArrayList<ArrayList<ArrayList<String>>> getSentencesOld(ArrayList<String> lines){
    ArrayList<ArrayList<ArrayList<String>>> sentences = new ArrayList<>();

    ArrayList<ArrayList<String>> sent = new ArrayList<>();
    for(String line:lines){

      if(line.equals("")){
        sentences.add(sent);
        sent = new ArrayList<>();
      }
      else{
        String[] allValues = line.split(TAB_DELIMETER);
        ArrayList<String> token_data = new ArrayList<>(Arrays.asList(allValues[0], allValues[1], allValues[3], allValues[4]));
        sent.add(token_data);
      }

    }
    return sentences;
  }

  public static void main(String[] args) {

    ArrayList<ArrayList<ArrayList<String>>> sentences = populateSentences(PTB_PATH);
    DataLoader dl = new DataLoader(sentences);
//    System.out.println(dl.getSentences().get(3).get(0).get(2));
  }
}
