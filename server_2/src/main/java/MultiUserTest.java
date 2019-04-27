import java.util.ArrayList;
import java.util.List;

import java.util.Random;

public class MultiUserTest {
	
	public static int NUM_ACTIONS = 1000;
	static Random rand = new Random();

	public static Pair<String, String> test_1() {

		// Simulate 5 users connecting
		List<String> users = new ArrayList<String>();
		users.add("Daniel");
		users.add("Irene");
		users.add("Diego");
		users.add("Laura");
		users.add("Adriana");
		
		// Possible actions
		List<String> actions = new ArrayList<String>();
		actions.add("!CREATEROOM");
		actions.add("!INVITE");
		actions.add("!SENDR");
		actions.add("!JOINROOM");
		actions.add("!LEAVEROOM");
		
		// Room ids = 5
		
		// Possible messages
		List<String> messages = new ArrayList<String>();
		messages.add("hola que tal");
		messages.add("pues bien");
		messages.add("toma berenjena");
		
		// Random message
		String u = users.get(rand.nextInt(5));
		String c = actions.get(rand.nextInt(5));
		String m = "";
		switch (c){
			case "!CREATEROOM":
				m = (c + " " + rand.nextInt(5));
				break;
			case "!INVITE":
				m = (c + " " + users.get(rand.nextInt(5)));				
				break;
			case "!SENDR":
				m = (c + " " + messages.get(rand.nextInt(3)));
				break;
			case "!JOINROOM":
				m = (c + " " + rand.nextInt(5));
				break;
			case "!LEAVEROOM":
				m = c;
				break;
		}			
		
		return new Pair<String, String>(u, m);
		
	}
	
}
