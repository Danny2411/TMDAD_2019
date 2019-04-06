import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
	
	// Save a MSG to DDBB
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
	
	// Save a CR to DDBB
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
	
	// Save a User to DDBB
	public int insertUserToDatabase(ChatRoom cr, String user) {
		try {
			Statement stmt = con.createStatement();  
			
			return stmt.executeUpdate("INSERT INTO usuarios (nombre_usuario, current_room_id) VALUES ('" 
					 	+ user + "', " + cr.getId() + ")"); 
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// Get messages from room
	public List<String> getMessagesFromRoom(ChatRoom cr) {
		try {
			
			List<String> mensajes = new ArrayList<String>();
		    Statement stmt = con.createStatement();
	
		    String sql = "SELECT src_usr, text, timestamp FROM mensajes m WHERE m.dst_sala = " + cr.getId() + " and m.type = 'text'";
		    ResultSet rs = stmt.executeQuery(sql);
		    
		    while(rs.next()){
		         
		         String user  = rs.getString("src_usr");
		         String msg = rs.getString("text");
		         Date date = rs.getDate("timestamp");
	
		         //Display values
		         System.out.println("User: " + user);
		         System.out.println("Msg: " + msg);
		         System.out.println("Date: " + date);
		         
		         mensajes.add(user + "!" + msg + "!" + date);
		      }
		      rs.close();
		      return mensajes;
		
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
	// Remove user from Room
	public void removeUserFromRoom(String user) {
		Statement stmt;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE FROM usuarios WHERE nombre_usuario = '" + user + "'");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	// Remove data from Room
	public void deleteRoom(ChatRoom cr) {
		Statement stmt;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE FROM mensajes WHERE dst_sala = " + cr.getId());
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE FROM salas WHERE id_sala = " + cr.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
