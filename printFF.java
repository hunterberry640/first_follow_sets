/* 
Author: Hunter Berry
Date: March 6th, 2021
Desc: Program that computes first and follow sets of a grammar read from a textfile.
      command line use: $ java printFF grammarFile.txt
*/
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;


public class printFF {
  static ArrayList<String> nonterminals = new ArrayList<>();
  static ArrayList<String> terminals = new ArrayList<>();
  static ArrayList<String> ignore = new ArrayList<>();
  static ArrayList<ArrayList<String>> grammar = new ArrayList<>();
  static Map<String,Set<String>> followSets = new HashMap<>();
  public static void main(String[] args) 
  {
    ignore.add("::=");
    ignore.add("lambda");

    String fileName = args[0];

    Scanner lineScanner = null;
    
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

      //don't insert nonterminals in list more than once
      if (!nonterminals.contains(token)) 
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


    for (ArrayList<String> rule : grammar) 
    {
      for (String string : rule) 
      {
        System.out.print(string + " ");
      }
      System.out.println();
    }


    System.out.println("\nNon-terminal symbols: " + nonterminals.toString());
    System.out.println("Terminal symbols: " + terminals.toString());
   

    for (String nonterm : nonterminals) 
    {
      Set<String> set = new HashSet<>();
      followSets.put(nonterm, set); //initialize with empty sets
    }


    System.out.println("\nFirst Sets:\n===");
    for (String nonterm : nonterminals) 
    {
      Set<String> set = new HashSet<>();
      set = findFirstSet(nonterm, set, null);
      System.out.println(nonterm + ": " + set.toString());
    }
    
    System.out.println("\nFollow Sets:\n===");
    for (String nonterm : nonterminals) 
    {
      Set<String> set = new HashSet<>();
      set = findFollowSet(nonterm, set, null);
      followSets.get(nonterm).addAll(set);
      System.out.println(nonterm + ": " + followSets.get(nonterm).toString());
    }
  }

  public static Set<String> findFollowSet(String nonterminal, Set<String> set, String prev)
  {
    boolean hasLambda = false;
    int ruleNum = 0;
    String startSymbol = "S";
    for (ArrayList<String> rule : grammar) 
    {
      if(ruleNum ==0)
      {
        startSymbol = rule.get(0);
      }
      ruleNum++;


      // -1 because we will be looking ahead and don't care about last elem
      for (int i = 2; i < rule.size()-1; i++)
      { 
        
      //For the start symbol S, place $ in Follow(S).
        if(nonterminal.equals(startSymbol))
        {
          set.add("$");
        }
        //if A->B
        //follow(B) = Follow(A)
        if(rule.size() ==3 && nonterminals.contains(rule.get(rule.size()-1)))
        {
                Set<String> temp = new HashSet<>();
                temp = findFollowSet(rule.get(0), set, nonterminal);
                temp = followSets.get(rule.get(0));
                followSets.put(nonterminal, temp);
        }

        if(rule.size() <=3 || !hasNonTerm(rule, nonterminal) )
        { //if RHS of rule !contain nonterminal skip 
          break;
        }

        if(rule.get(i).equals(nonterminal))
        {
          if(terminals.contains(rule.get(i+1)))
          {
            set.add(rule.get(i+1));
            break;
          }else
          {
           // If A → aBb, then First(b) ‐ {e} ⊆ Follow(B)
            Set<String> temp = new HashSet<>();
            temp = findFirstSet(rule.get(i+1), temp, rule.get(i+1)); //temp = first(b)
            
            /*if (A → aBb and ε in First(b)),
              then Follow(A) ⊆ Follow(B) */
             // If ∈ ∈ First(β), then Follow(B) = { First(β) – ∈ } ∪ Follow(A)
            if(temp.contains("lambda"))
            {// If ∈ ∈ First(β)
              int count = 2;
              hasLambda = false;
              while(count+i <= rule.size()-1 && temp.contains("lambda"))
              { 
                
                temp = findFirstSet(rule.get(i+count), temp, rule.get(i+count));
                
                set.addAll(temp);
                
                count++;
                
              }
              //then Follow(B) = { First(β) – ∈ } ∪ Follow(A)
              if(temp.contains("lambda")&& !rule.get(0).equals(nonterminal) && prev != rule.get(0))
              {
                hasLambda = true;
                temp = findFollowSet(rule.get(0), set, nonterminal);
                temp = followSets.get(rule.get(0));
                followSets.put(nonterminal, temp);
                // temp.addAll(findFollowSet(rule.get(0), temp, nonterminal));
              }
              
              
            }
            temp.remove("lambda");
            if(hasLambda){
              break;
            }else{
              temp.remove("$");
            }
            set.addAll(temp);
            
          }
          
          
        }
        /*if A → αB: Follow(B) = Follow(A)*/
        //B: nonterm
        if(rule.get(rule.size()-1).equals(nonterminal) && !rule.get(0).equals(nonterminal) && prev != rule.get(0))
        { // if nonterm not follow by anything
          Set<String> temp = new HashSet<>();
          
          temp = findFollowSet(rule.get(0), set, nonterminal);
          set.addAll(temp);
          
          temp = followSets.get(rule.get(0));
          followSets.put(nonterminal, temp);
          break;
        }
      }
    }

    

    // For any production rule A → αB: Follow(B) = Follow(A)

    // For any production rule A → αBβ,
    //  If ∈ ∉ First(β), then Follow(B) = First(β)
    //  If ∈ ∈ First(β), then Follow(B) = { First(β) – ∈ } ∪ Follow(A)
    return set;

  }

  public static boolean hasNonTerm(ArrayList<String> rule, String  nonterminal)
  {
    for (int i = 2; i < rule.size(); i++)
    {
      if(nonterminal.equals(rule.get(i)))
      {
        return true;
      }
    }
    return false;
  }

  public static boolean containslambda(String nonterminal)
  {
    for (ArrayList<String> rule : grammar)
    {
      if(rule.get(0).equals(nonterminal))
      {
        if (rule.contains("lambda"))
        {
          return true;
        }
      }
    }
    return false;
  }


  public static Set<String> findFirstSet(String nonterminal, Set<String> set, String prev)
  {
    for (ArrayList<String> rule : grammar) 
    {
      if(rule.get(0).equals(nonterminal))
      { // only analyze rules of current nonterminal
        for (int i = 2; i < rule.size(); i++) 
        {
            if(prev != null && prev.equals(rule.get(i))){
              break;
            }
            if(terminals.contains(rule.get(i)))
            { // if A ::= t
              set.add(rule.get(i));
              set.remove("lambda");
              break;
            }
            else if(nonterminal.equals(rule.get(i)))
            { // if A ::= Aa
              if(containslambda(rule.get(i)))
              { // if rule contains lambda go to next sym
                continue;
              }
              else
              { // if not break out of loop because of infinite loop go to next rule
                break;
              }
            }
            else if(containslambda(rule.get(i)))
            { // if A ::= B where lambda in B
              set.addAll(findFirstSet(rule.get(i), set, nonterminal));
            }
            else
            {// A ::= B where lambda not in B
              set.remove("lambda");
              set.addAll(findFirstSet(rule.get(i), set, nonterminal));
              break;
            }
        }
      }
      if(containslambda(nonterminal))
      { // if A ::= lambda
        set.add("lambda");
      }
    }
    return set;
  }
}