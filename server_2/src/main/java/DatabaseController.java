import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseController {

	private static java.sql.Connection con;
	
	public static java.sql.Connection getCon() {
		return con;
	}

	public static void setCon(java.sql.Connection con) {
		DatabaseController.con = con;
	}

	public void connectToDatabase() throws SQLException {
		con = DriverManager.getConnection("jdbc:mysql://localhost/tmdad_schema?" +
                "user=root&password=2411&serverTimezone=UTC");
	}
	
	// Sabe a MSG to DDBB
	public int insertMsgToDatabase(String sender, ChatRoom cr , String m) {
		try {
			Statement stmt = con.createStatement();  
			
			java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
			return stmt.executeUpdate("INSERT INTO mensajes (src_usr, dst_sala, type, text, timestamp) VALUES ('" 
					 	+ sender + "', '" + cr.getId() + "', 'text', '" + m + "', '" + date + "')"); 
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// Sabe a MSG to DDBB
	public int insertCRToDatabase(ChatRoom cr) {
		try {
			Statement stmt = con.createStatement();  
			
			return stmt.executeUpdate("INSERT INTO salas (id_sala, nombre_sala, creator, private, allowed) VALUES (" 
					 	+ cr.getId() + ", '" + cr.getName() + "', '" + cr.getCreator() + "', " + cr.getPriv() + ", '" + cr.getAllowed() + "')"); 
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
