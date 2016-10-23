import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class InvertedIndex {
	private static LinkedList<Integer> postingsLinkedList ; 
	private static HashMap<String,LinkedList> postingsMap = new HashMap<String,LinkedList>();
	private static int comp = 0 ; 
	static String finalResult = "";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			if(args.length<2){
				System.out.println("Please check input arguments. Format: indexPath outputFile inputFile");
				return ; 
			}
			String path = args[0];
		     //"C:\\Users\\sdua\\Downloads\\index\\index" ;
			IndexReader reader = DirectoryReader.open
					(FSDirectory.open(FileSystems.getDefault().getPath(path)));
			 	Collection<String> coll = MultiFields.getIndexedFields(reader);	
			
			int count =  0; 
		    for(String c:coll){
				if(c.startsWith("text_")){
					count++ ; 
					//System.out.println(c);
					Terms terms = MultiFields.getTerms(reader, c); 
					TermsEnum termsEnum = terms.iterator(); 
					PostingsEnum postingsEnum  ; 
					 
					while(termsEnum.next()!=null){
						BytesRef bytes = termsEnum.term(); 
						String termString = bytes.utf8ToString(); 
						//System.out.println(termString + "  ");
						PostingsEnum postEnum = termsEnum.postings(null);
						postingsLinkedList = new LinkedList<Integer>(); 
						while(postEnum.nextDoc()!= postEnum.NO_MORE_DOCS){
							//System.out.print(postEnum.docID() + " , ");
							postingsLinkedList.add(postEnum.docID()); 
						}
						//if(postingsLinkedList.size()==10)
						//System.out.println(termString + " " + postingsLinkedList.size() +" \n" + postingsLinkedList.toString());
						//System.out.println("\n=======================================\n");
						LinkedList copyList = new LinkedList<Integer>(); 
						
						if(postingsMap.containsKey(termString)){
							count++ ; 
							//System.out.println(postingsLinkedList);
							//System.out.println("Found already  " + termString + " " + postingsMap.get(termString).size() + " to insert " + postingsLinkedList.size());
							copyList = postingsMap.get(termString);
							postingsLinkedList.addAll(copyList); 
							//Collections.sort(postingsLinkedList);
							 
						/*	System.out.println(postingsLinkedList);
							System.out.println("\n===============================\n");*/
						
						}
						else {
							postingsMap.put(termString, postingsLinkedList);
						}
					}
					
					
					 
					}
				
				
				 
				
			}
		    String input = args[2] ; 
		    ArrayList<String> inputLines = new ArrayList<String>(); 
			try (BufferedReader br = new BufferedReader(new FileReader(input))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			       // process the line.
			    	//System.out.println(line.toString());
			      try{
			    	     inputLines.add(line.toString()); 
			      }
			      catch(Exception e){
			    	    System.out.println("Unable to process - " + line.toString());
			      }
			    	
			    }
			    for(String nextLine : inputLines){
			    	String[] terms = nextLine.split(" "); 
			    	for(String t : terms ){
			    		finalResult += "GetPostings\n";
			    		finalResult += t + "\n" ; 
			    		finalResult += "Postings list: " + getPostings(t) + "\n" ; ; 
			    		
			    	}
			    	
			    	finalResult += "TaatAnd" + "\n" + nextLine + "\n";  
			    	finalResult += taatAnd(nextLine); 
			    	
			    	finalResult += "TaatOr" + "\n" + nextLine + "\n"; 
			    	finalResult += taatOr(nextLine); 
			    	
			    	finalResult += "DaatAnd" + "\n" + nextLine + "\n";  
			    	finalResult += daatAnd(nextLine); 
			    	
			    	finalResult += "DaatOr" + "\n" + nextLine + "\n"; 
			    	finalResult += daatOr(nextLine); 
			    	
			    	 
			    }
			    
			   // System.out.println(finalResult);
			    String outputFile = args[1];
			    try{
			    	BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
				    writer.write(finalResult);
				    writer.close();
			    } catch(Exception e){
			    	System.out.println("Unable to write to file.");
			    }
			    
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("Unable to read file. ");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getPostings(String term){
		String postings = ""; 
		//System.out.println(term);
		
		LinkedList list = postingsMap.get(term); 
		int size = list.size(); 
		for(int i=0 ; i < size ; i++){
			postings += list.get(i) + " "; 
		}
		//System.out.println(postings);
		return postings ; 
	}
	
	public static String taatAnd(String terms){
		String[] t = terms.split(" "); 
		int numberOfTerms = t.length; 
		LinkedList listOne , listTwo ; 
		LinkedList result = new LinkedList() ; 
		String returnString = ""; 
		String resultString = "";
		 
		
		int comparisons = comp ; 
		String term1 = t[0] ; String term2 = t[1]; 
		//System.out.println("Term 1 " + term1 + "  Term 2 " + term2);
		if(numberOfTerms>1){
			result = intersection(postingsMap.get(term1), postingsMap.get(term2)); 
			comparisons = comp ; 
		}
			
		//System.out.println("First Result " + result + " Empty - " + result.isEmpty());
		
		
		if(numberOfTerms>2){
			
			for(int i = 2 ; i < numberOfTerms ; i++){
				if(!result.isEmpty()){
					result = intersection(result, postingsMap.get(t[i]));
					comparisons += comp ;
				}
					
			}
		}
		if(result.isEmpty())
			resultString = " empty" ;
		else{
			
			for(int i = 0 ; i < result.size() ; i++){
				resultString += " " + result.get(i) ;  
			}
		}
		
		
		returnString += "Results:" + resultString +  "\n" 
		+ "Number of documents in results: " + result.size() + "\n" + "Number of comparisons: " + comp + "\n";
		 
		 
		return returnString ; 
	}
	
	public static String taatOr(String terms){
		String returnString = ""; 
		String resultString = "";
		String[] t = terms.split(" "); 
		int comparisons = comp ;
		int numberOfTerms = t.length; 
		LinkedList listOne , listTwo ; 
		LinkedList result = new LinkedList() ; 
		if(numberOfTerms>1){
			String term1 = t[0] ; String term2 = t[1]; 
			result = union(postingsMap.get(term1), postingsMap.get(term2)); 
			//System.out.println("First Union Result " + result);
			comparisons = comp ;
		}
		
		if(numberOfTerms>2){
			for(int i = 2 ; i < numberOfTerms ; i++){
				if(!result.isEmpty()){
					
					result = union(result, postingsMap.get(t[i]));
					comparisons += comp ;
					//System.out.println("Result " + i + " " + result);
				}
					
			}
		}
		if(result.isEmpty())
			resultString = " empty" ;
		else{
		for(int i = 0 ; i < result.size() ; i++){
			resultString += " " + result.get(i) ;  
		}
		}
		returnString += "Results:" + resultString +  "\n" 
		+ "Number of documents in results: " + result.size() + "\n" + "Number of comparisons: " + comp + "\n";
		 
		 
		return returnString ; 
	}
	
	public static String daatAnd(String terms){
		 
		 
		String lists[] = terms.split(" "); 
		int numberOfLists = lists.length; 
		int listPointers[] = new int[numberOfLists] ; 
		int maxSize = 0 ; 
		ArrayList<LinkedList> listOfLink = new ArrayList<LinkedList>(); 
		LinkedList<Integer> result = new LinkedList<Integer>(); 
		int doc = 0 ; 
		for(String s : lists){
			try {
				LinkedList<Integer> temp = postingsMap.get(s);
				//System.out.println(temp + " " + temp.size());
				if(temp.size()>maxSize) maxSize = temp.size(); 
				listOfLink.add(temp); 
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			
		}
		
	 
		
		int comparisons = 0 ; 
		int min = 80000; int lastMin = 0 ; 
		boolean breakOut = false ; 
		for(int i=0 ; i < maxSize ; i++ ){
				min = 80000;  
				//System.out.println("\n================\n");
				for(int j=0; j < numberOfLists ; j++){
					//System.out.println(" i j " + i + " " + j );
					if(listPointers[j]<listOfLink.get(j).size()){
						comparisons++; 
						//System.out.println("Pointers - " + listPointers[0] + " " + listPointers[1] + " " +listPointers[2]);
						doc  = (int) listOfLink.get(j).get(listPointers[j]) ;
						//System.out.println(" Doc - " + doc);
						if(doc<min){
							min = doc ; 
							
							//result.add(docId); 	
						}
						if(doc == min){
							min = doc ; 
							//matchCount++  ; 
							//listPointers[j] = listPointers[j] + 1 ;
						}
						 	
					}
					
					else{
						breakOut = true ; 
						break ; 
					}
					
					//System.out.println("Pointers - " + listPointers[0] + " " + listPointers[1] + " " +listPointers[2]);
					
				}
				if(breakOut) break ; 
				
				//result.add(min); 
				boolean add = false  ;
				int matchCount = 0 ; 
				for(int j=0; j < numberOfLists ; j++){
					 
					if(listPointers[j]<listOfLink.get(j).size()){
						int m = (int) listOfLink.get(j).get(listPointers[j]); 
						if(m==min){
							listPointers[j] = listPointers[j] + 1; 
							add = true ; 
							matchCount++ ; 
						}
						
					}
					 
				}
				//System.out.println("Min | Match Count - " + min + " " + matchCount);
				if(add && matchCount == numberOfLists) {
					if(lastMin!=min)
						result.add(min);
				}
				
				lastMin = min ; 
			}
		
		
		String returnString = ""; 
		String resultString = "";
		if(result.isEmpty())
			resultString = " empty" ;
		else{
		for(int i = 0 ; i < result.size() ; i++){
			resultString += " " + result.get(i) ;  
		}
		}
		returnString += "Results:" + resultString +  "\n" 
		+ "Number of documents in results: " + result.size() + "\n" + "Number of comparisons: " + comparisons + "\n";
		 
		
		
		 
		return returnString ; 
	}
	
	
	public static String daatOr(String terms){
		 
		int matchCount = 0 ; 
		String lists[] = terms.split(" "); 
		int numberOfLists = lists.length; 
		int listPointers[] = new int[numberOfLists] ; 
		int maxSize = 0 ; 
		ArrayList<LinkedList> listOfLink = new ArrayList<LinkedList>(); 
		LinkedList<Integer> result = new LinkedList<Integer>(); 
		int doc = 0 ; 
		for(String s : lists){
			LinkedList<Integer> temp = postingsMap.get(s);
			//System.out.println(temp + " " + temp.size());
			if(temp.size()>maxSize) maxSize = temp.size(); 
			listOfLink.add(temp); 
			
		}
 
		
		int comparisons = 0 ; int innerLoop = 0 ; 
		  int min = 80000; int lastMin = 0 ; 
			for(int i=0 ;  ; i++ ){
				min = 80000; matchCount = 0 ; innerLoop = 0 ; 
				//System.out.println("\n==================\n");
				for(int j=0; j < numberOfLists ; j++){
					//System.out.println(" i | j | List Poiter   " + i + " | " + j + " | " + listPointers[j] );
					if(listPointers[j]<listOfLink.get(j).size()){
						comparisons++; innerLoop++; 
						//System.out.println("Pointers - " + listPointers[0] + " " + listPointers[1] + " " +listPointers[2]);
						doc  = (int) listOfLink.get(j).get(listPointers[j]) ;
						//System.out.println(" Doc - " + doc);
						if(doc<min){
							min = doc ; 
							
							//result.add(docId); 	
						}
						if(doc == min){
							min = doc ; 
							//matchCount++  ; 
							//listPointers[j] = listPointers[j] + 1 ;
						}
						 	
					}
					
					//System.out.println("Pointers - " + listPointers[0] + " " + listPointers[1] + " " +listPointers[2]);
					
				}
				if(innerLoop==0) break; 
				
				//result.add(min); 
				boolean add = false  ;
				
				for(int j=0; j < numberOfLists ; j++){
					 
					if(listPointers[j]<listOfLink.get(j).size()){
						int m = (int) listOfLink.get(j).get(listPointers[j]); 
						if(m==min){
							listPointers[j] = listPointers[j] + 1; 
							add = true ; 
						}
						
					}
					 
				}
				if(add) {
					if(lastMin!=min)
						result.add(min);
				}
				
				lastMin = min ; 
			}
		
		
			String returnString = ""; 
			String resultString = "";
			if(result.isEmpty())
				resultString = " empty" ;
			
			else{
			for(int i = 0 ; i < result.size() ; i++){
				resultString += " " + result.get(i) ;  
			}
			}
			returnString += "Results:" + resultString +  "\n" 
			+ "Number of documents in results: " + result.size() + "\n" + "Number of comparisons: " + comparisons + "\n";
			 
			
			
			 
			return returnString ; 
	}
	
	
	
	public static LinkedList union(LinkedList l1 , LinkedList l2){
		 LinkedList<Integer> shortList = l1 ; 
		 LinkedList<Integer> longList = l2 ; 
		 LinkedList<Integer> result = new LinkedList<Integer>(); 
		 comp = 0 ; 
		 if(l1.size()>l2.size()){
			 shortList = l2 ; 
			 longList = l1 ; 
		 }
		 
		 //System.out.println("Short List " + shortList);
		 //System.out.println("Long list " + longList);
		 
		 boolean addRemaining = false ; 
		 int maxLength = longList.size(); 
		 int valueShort =0 , valueLong =0 , pointerShort = 0, pointerLong = 0 ; 
		 for(int i = 0 ; i < maxLength ; i++){
			
			 valueShort = (int)shortList.get(pointerShort); 
			 valueLong = (int) longList.get(pointerLong); 
			 comp++ ;
			 //System.out.println("Compare - " + valueShort + " | " +  valueLong);
			 if(valueShort == valueLong){
				 result.add(valueShort); 
				 pointerShort++; 
				 pointerLong++; 
				 //System.out.println(result);
				
			 }
			 
			 else if(valueShort > valueLong){
				 result.add(valueLong);
				 pointerLong++; 
				 //System.out.println(result);
			 }
			 
			 else {
				 result.add(valueShort);
				 pointerShort++; 
				 //System.out.println(result);
			 }
			 //System.out.println(pointerShort + " | " + pointerLong );
			 if(pointerShort >= shortList.size()) {
				 addRemaining = true ; 
				 break ; 
			 }
		 }
		 
		 //if(addRemaining){
			 //System.out.println("Pointer long " + pointerLong + " Pointer Short " + pointerShort);
			 
			 for(int i = pointerShort ; i < shortList.size() ; i++){
				 result.add(shortList.get(i)); 
			 }
			 for(int i = pointerLong ; i < maxLength ; i++){
				 result.add(longList.get(i)); 
			 }
			 
			 Collections.sort(result);
			 //result.addAll(pointerLong, longList); 
		 //}
		 return result ; 
	}
	
	
	public static LinkedList intersection(LinkedList l1 , LinkedList l2){
		 //System.out.println("\n=================\n");
		 //System.out.println("Input L1 " + l1);
		 //System.out.println("Input l2 " + l2);
		 LinkedList<Integer> shortList = l1 ; 
		 LinkedList<Integer> longList = l2 ; 
		 LinkedList<Integer> result = new LinkedList<Integer>(); 
		 
		 if(l1.size()>l2.size()){
			 shortList = l2 ; 
			 longList = l1 ; 
		 }
		 //System.out.println("Short List " + shortList);
		 //System.out.println("Long List " + longList);
		 comp = 0 ; 
		 int maxLength = longList.size(); 
		 int valueShort =0 , valueLong = 0 , pointerShort = 0, pointerLong = 0 ; 
		 for(int i = 0 ; i < maxLength ; i++){
			 valueShort = (int)shortList.get(pointerShort); 
			 valueLong = (int) longList.get(pointerLong); 
			 comp++ ; 
			 //System.out.println("Compare - " + valueShort + " | " +  valueLong);
			
			 if(valueShort == valueLong){
				 result.add(valueShort); 
				 pointerShort++; 
				 pointerLong++; 
				
			 }
			 
			 else if(valueShort > valueLong){
				 
				 pointerLong++; 
				  
			 }
			 
			 else {
				 
				 pointerShort++; 
			 }
			 if(pointerShort >= shortList.size()) {
				 
				 break ; 
			 }
		 }
		 
		 
		 return result ; 
	}
	

	 
	
}
