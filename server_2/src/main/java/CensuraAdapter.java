import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Class to check how to censor words
public class CensuraAdapter {

	// Connection to database
	private static java.sql.Connection con;
	
	public java.sql.Connection getCon() {
		return con;
	}

	public void setCon(java.sql.Connection con) {
		CensuraAdapter.con = con;
	}
	
	// List to keep strings that must be cesnored
	List<String> censoredStrings;
	
	// Connects to database
	public void connectToDatabase() throws SQLException {
		con = DriverManager.getConnection("jdbc:mysql://localhost/tmdad_schema?" +
                "user=root&password=2411&serverTimezone=UTC");
	}
	
	
	public List<String> getCensoredWordsFromDB(){
		try {
			List<String> censurados = new ArrayList<String>();
		    Statement stmt = con.createStatement();
		    // Get messages
		    String sql = "SELECT censura, active FROM censuras c WHERE c.active = " + true;
		    ResultSet rs = stmt.executeQuery(sql);
		    // Iterate through messages
		    while(rs.next()){
		         // Get relevant data
		         String msg = rs.getString("censura");
		         // Add to return
		         censurados.add(msg);
		      }
		      rs.close();
		      return censurados;
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	
	public CensuraAdapter() {
		censoredStrings = new ArrayList<String>();
		try {
			connectToDatabase();
			censoredStrings = getCensoredWordsFromDB();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		censoredStrings.add("berenjena");
	}
	
	public CensuraAdapter(ArrayList<String> c) {
		censoredStrings = c;
	}
	
	// Check if a message has anything to be censored.
	// Replace censored words with **** and return which words were censored
	public Pair<String, List<String>> censorMessage(String msg){
		// Check every message if censored words have changed
		censoredStrings = getCensoredWordsFromDB();
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
