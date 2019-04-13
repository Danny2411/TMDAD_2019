import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// This class is used to controll the access to database
public class DatabaseAdapter {

	// Connection to database
	private static java.sql.Connection con;
	
	public static java.sql.Connection getCon() {
		return con;
	}

	public static void setCon(java.sql.Connection con) {
		DatabaseAdapter.con = con;
	}
	
	// Connects to database
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
	
	// Remove a MSG to DDBB
	public int removeMsgToDatabase(ChatRoom cr , String m) {
		try {
			Statement stmt = con.createStatement();  
			// Remoce invitation message
			stmt = con.createStatement();
			return stmt.executeUpdate("DELETE FROM mensajes WHERE text = '" + m + "' and dst_sala = " + cr.getId());	
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// Save a CR to DDBB
	public int insertCRToDatabase(ChatRoom cr) {
		try {
			 Statement stmt_2 = con.createStatement();
			 // First check if CR already exists
			 String sql_2 = "SELECT DISTINCT id_sala FROM salas s WHERE s.id_sala = " + cr.getId();
			 ResultSet rs_2 = stmt_2.executeQuery(sql_2);
	         // if it does not
	         if (!rs_2.next()) {
				Statement stmt = con.createStatement();  
				// Create the new room
				return stmt.executeUpdate("INSERT INTO salas (id_sala, nombre_sala, creator, private, allowed) VALUES (" 
						 	+ cr.getId() + ", '" + cr.getName() + "', '" + cr.getCreator() + "', " + cr.getPriv() + ", '" + cr.getAllowed() + "')"); 
	         }
	         return -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// Save a User to DDBB
	public int insertUserToDatabase(ChatRoom cr, String user) {
		try {
			Statement stmt = con.createStatement();  
			if (cr != null) {
				// If the user is already on system, modify
				return stmt.executeUpdate("UPDATE usuarios SET current_room_id = " + cr.getId() + " WHERE nombre_usuario = '" + user + "'");
			} else {
				// If not, create it in the database
				return stmt.executeUpdate("INSERT INTO usuarios (nombre_usuario, current_room_id, is_root) VALUES ('" 
					 	+ user + "', " + null + ", " + false + ")");
			}
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
		    // Get messages
		    String sql = "SELECT src_usr, text, timestamp FROM mensajes m WHERE m.dst_sala = " + cr.getId() + " and m.type = 'text'";
		    ResultSet rs = stmt.executeQuery(sql);
		    // Iterate through messages
		    while(rs.next()){
		         // Get relevant data
		         String user  = rs.getString("src_usr");
		         String msg = rs.getString("text");
		         Date date = rs.getDate("timestamp");
		         // Add to return
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
	public void removeUserFromRoom(ChatRoom cr, String user) {
		Statement stmt;
		try {
			// If a user leaves a room, it means that she-he was already in system
			stmt = con.createStatement();
			stmt.executeUpdate("UPDATE usuarios SET current_room_id = " + null + " WHERE current_room_id = " + cr.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Remove data from Room
	public void deleteRoom(ChatRoom cr) {
		Statement stmt;
		try {
			// Delete messages from room
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE FROM mensajes WHERE dst_sala = " + cr.getId());
			// Delete room itself
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE FROM salas WHERE id_sala = " + cr.getId());
			// Change all users current room
			stmt = con.createStatement();
			stmt.executeUpdate("UPDATE usuarios SET current_room_id = " + null + " WHERE current_room_id = " + cr.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Get first available room ID
	public long getLastIdx() {
		try {
			// Check which is the max ID used
		    Statement stmt = con.createStatement();
		    String sql = "SELECT MAX(id_sala) as maxid FROM salas";
		    ResultSet rs = stmt.executeQuery(sql);
		    int max = -1;
		    // Check if its at least one or not
		    if(rs.next()){ 
		         max = rs.getInt("maxid");
		    }
		    rs.close();
		    return max + 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// Update a name
	public void updateName(String original_name, String new_name) {
		try {
			// Update user name
			Statement stmt = con.createStatement();
		    String sql = "UPDATE usuarios SET nombre_usuario = '" + new_name + "' WHERE nombre_usuario = '" + original_name + "'";
		    stmt.executeUpdate(sql);
		    // Update room
		    stmt = con.createStatement();
		    sql = "UPDATE salas SET creator = '" + new_name + "' WHERE creator = '" + original_name + "'";
		    stmt.executeUpdate(sql);
		    // Update messages
		    stmt = con.createStatement();
		    sql = "UPDATE mensajes SET src_usr = '" + new_name + "' WHERE src_usr = '" + original_name + "'";
		    stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Set root also in database
	public void setRoot(String user) {
		try {
			// Update usuario
			Statement stmt = con.createStatement();
		    String sql = "UPDATE usuarios SET is_root = " + true + " WHERE nombre_usuario = '" + user + "'";
		    stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// User disconnects
	public void disconnectUser(String user) {
		try {
			// Delete usuario
			Statement stmt = con.createStatement();
			stmt.executeUpdate("DELETE FROM usuarios WHERE nombre_usuario = '" + user + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Save censored words to DDBB
	public int saveCensor(String msg, List<String> words, String sender, ChatRoom cr) {
		try {
			Statement stmt = con.createStatement();  
			// Save censored words as a string like "aaa,bbb,ccc"
			String w = "";
			for(String s : words) {
				w += s + ",";
			}
			w = w.substring(0, w.length() - 1);
			// Also save original message and the rest of data as a normal message
			Long id = null;
			if (cr != null){
				id = cr.getId();
			}
			java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
			return stmt.executeUpdate("INSERT INTO mensajes_censurados (src_usr, dst_sala, text, censored_words, timestamp) VALUES ('" 
					 	+ sender + "', " + id + ", '" + msg + "', '" + w + "', '" + date + "')");
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		
	}
	
	// Check pending messages when connecting
	public List<String> checkRoomsWithMessages(String username){
		List<String> mensajes = new ArrayList<String>();
		try {
			Statement stmt = con.createStatement();
			// Check private rooms of a user
		    String sql = "SELECT DISTINCT id_sala FROM salas s WHERE s.allowed = '" + username + "' and s.private = " + true;
		    ResultSet rs = stmt.executeQuery(sql);
		    // For each room, check if there are messages
		    while(rs.next()){	 
		         int id  = rs.getInt("id_sala"); 
		         Statement stmt_2 = con.createStatement();
				 // Get messages
				 String sql_2 = "SELECT DISTINCT dst_sala FROM mensajes m WHERE m.dst_sala = " + id;
				 ResultSet rs_2 = stmt_2.executeQuery(sql_2);
		         if(rs_2.next()) {
		        	 // Add to returned messages
			         mensajes.add(Integer.toString(id));
		         }
		      }
		      rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return mensajes;
	}
}
