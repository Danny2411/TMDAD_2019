import java.util.ArrayList;
import java.util.List;

// Class to check how to censor words
public class CensuraController {

	// List to keep strings that must be cesnored
	List<String> censoredStrings;
	
	public CensuraController() {
		censoredStrings = new ArrayList<String>();
		censoredStrings.add("berenjena");
	}
	
	public CensuraController(ArrayList<String> c) {
		censoredStrings = c;
	}
	
	// Check if a message has anything to be censored.
	// Replace censored words with **** and return which words were censored
	public Pair<String, List<String>> censorMessage(String msg){
		List<String> censoredWords = new ArrayList<String>();
		String new_msg = msg;
		for(String c : censoredStrings) {
			if(new_msg.contains(c)) {
				new_msg = new_msg.replaceAll(c, "****");
				censoredWords.add(c);
			}
		}
		return new Pair<String, List<String>>(new_msg, censoredWords);
	}
	
	
}
