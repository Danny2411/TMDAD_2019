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
		Pair<String, List<String>> result = null;
		List<String> censoredWords = new ArrayList<String>();
		for(String c : censoredStrings) {
			if(msg.contains(c)) {
				msg = msg.replaceAll(c, "****");
				censoredWords.add(c);
			}
		}
		return new Pair<String, List<String>>(msg, censoredWords);
	}
	
	
}
