import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class printFF {
  static ArrayList<String> nonterminals = new ArrayList<>();
  static ArrayList<String> terminals = new ArrayList<>();
  static ArrayList<String> ignore = new ArrayList<>();
  static ArrayList<ArrayList<String>> grammar = new ArrayList<>();
  public static void main(String[] args) {

    // ignore.add("#");
    // ignore.add(";");
    ignore.add("::=");
    ignore.add("lambda");

    String fileName = args[0];

    Scanner lineScanner = null;
    // System.out.println("Is semi Colon not uppercase?: " + !Character.isUpperCase(';'));
    try 
    {
      lineScanner = new Scanner(new File(fileName)); // scanner that reads file line by line
    } 
    catch (FileNotFoundException e) 
    {
      System.out.println("file not found " + fileName + "\n " + e);
    }

    String line = lineScanner.nextLine(); // get title line
    

    //read in from file and populate terminals and nonterminals.
    while (lineScanner.hasNext()) 
    {
      
      line = lineScanner.nextLine();
      
      
      StringTokenizer st = new StringTokenizer(line);
      String token = st.nextToken();
      ArrayList<String> currentRule = new ArrayList<>();
      
      if (!nonterminals.contains(token)) //don't insert nonterminals in list more than once
      {
        nonterminals.add(token);
      }
      currentRule.add(token);
      while (st.hasMoreTokens()) 
      {
        //the token is nonterminal if it is not uppercase, lambda or ::=
        token = st.nextToken();
        currentRule.add(token);
        if (!Character.isUpperCase(token.charAt(0)) && !ignore.contains(token) && !terminals.contains(token)) 
        {
          terminals.add(token);
        }
      }
      grammar.add(currentRule);
    }

    // System.out.println("\nNon-terminal symbols: " + nonterminals.toString());
    // System.out.println("Terminal symbols: " + terminals.toString());
    // System.out.println("Grammar Rules: " + grammar.toString());
    System.out.println("==First Sets==");
    for (String nonterm : nonterminals) {
      Set<String> set = new HashSet<>();
      
      set = findFirstSet(nonterm, set);
      System.out.println(nonterm + ": " + set.toString());
    }
    
  }

  public static Set<String> findFirstSet(String nonterminal, Set<String> set){
    Set<String> tempSet = new HashSet<>();
    for (ArrayList<String> rule : grammar) {
      if(rule.get(0).equals(nonterminal)){
        for (int i = 2; i < rule.size(); i++) {
            if(terminals.contains(rule.get(i)) || rule.get(i).equals("lambda") ){ // if production is a terminal add it to first set
              set.add(rule.get(i));
              break;
            }else{  
              if(rule.get(i).equals(nonterminal) && containslambda(nonterminal)){ // if rule calls itself but contains lambda go to next symbol
                continue;
              }
              else if(rule.get(i).equals(nonterminal)){ // if rule calls itself but doesn't contain lambda go to next rule
                break;
              }     
              if(containslambda(rule.get(i))){
                continue;
              }
              tempSet = findFirstSet(rule.get(i), set);       
              if(tempSet.contains("lambda")){
                // System.out.println(tempSet.toString());
                tempSet.remove("lambda");
                set.addAll(tempSet);
              }else{
                break;
              }
              
          }
        }
      }
    }
    return set;
  }

  public static void findFollowSet(){
    
  }

  public static boolean containslambda(String nonterminal){
    for (ArrayList<String> rule : grammar){
      if(rule.get(0).equals(nonterminal)){
        if (rule.contains("lambda")){
          return true;
        }
      }
    }
    return false;
  }
  
}