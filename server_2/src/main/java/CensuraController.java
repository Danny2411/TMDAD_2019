import java.util.ArrayList;
import java.util.List;

public class CensuraController {

	List<String> censoredStrings;
	
	public CensuraController() {
		censoredStrings = new ArrayList<String>();
		censoredStrings.add("berenjena");
	}
	
	public CensuraController(ArrayList<String> c) {
		censoredStrings = c;
	}
	
	public Pair<String, List<String>> censorMessage(String msg){
		List<String> censoredWords = new ArrayList<String>();
		String new_msg = msg;
		for(String c : censoredStrings) {
			if(new_msg.contains(c)) {
				new_msg = new_msg.replaceAll(c, "****");
				censoredWords.add(c);
			}
		}
		System.out.println(new_msg);
		return new Pair<String, List<String>>(new_msg, censoredWords);
	}
	
	
}
