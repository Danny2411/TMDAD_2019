import java.sql.SQLException;
import java.util.List;

public class CommandController {
	// Database controller
	public DatabaseController db;
	// Censor controller
	public CensuraController cs;
	// Command controller itself
	public CommandController() {
		db = new DatabaseController();
		cs = new CensuraController();
		try {
			db.connectToDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	// Parse received messages
	public Pair<ChatRoomsController, String> parseMessage(ChatRoomsController chat, String msg, String sender) {
		String[] parts = msg.split(" ");
		String ok = "";
		ChatRoom cr = null;
		switch(parts[0]) {
			case "!CREATEROOM" :
				boolean possible = chat.createRoom(parts[1], sender);
				if(!possible) {
					ok = "YAENSALA";
				} else {
					ok = "CREATED";
					
					// DATABASE
					cr = chat.isUserOnRoom(sender);
					db.insertCRToDatabase(cr);
					db.insertUserToDatabase(cr, sender);
				}
				break;
			case "!INVITE" :
				cr = chat.isUserOnRoom(sender);
				if(parts.length < 2 || parts.length > 2) {
					ok = "NODSTTOINVITE";
				} else {
					ok = "INVITE" + "!" + parts[1] + "!" + cr.getId();
					
					// DATABASE
					cr = chat.isUserOnRoom(sender);
					db.insertMsgToDatabase(sender, cr, "Se ha invitado a " + parts[1] + " a la sala.");
				}
				break;
			case "!JOINROOM" :
				
				// Check if user has been invited to the room	
				// ONLY IF ITS PUBLIC
				List<ChatRoom> crs2 = chat.getChatRooms();
				ChatRoom c2 = null;
				for(ChatRoom it : crs2) {
					if(it.getId() == Long.parseLong(parts[1])) {
						c2 = it;
					}
				}
				if(c2 == null) {
					ok = "NOJOIN";
				} else {
					boolean success = false;
					List<String> messages = db.getMessagesFromRoom(c2);
					for(String m : messages) {
						m = m.split("!")[1];
						if(m.equals("Se ha invitado a " + sender + " a la sala.")) {
							success = true;
						}
					}
					if(success == true || (c2.getPriv() == true && (c2.getAllowed().equals(sender) || c2.getCreator().equals(sender)))) {
						success = chat.joinRoom(Long.parseLong(parts[1]), sender);
						if(!success) {
							ok = "NOJOIN";
						} else {
							ok = "JOINED";
							// DATABASE
							cr = chat.isUserOnRoom(sender);
							List<String> mensajes = db.getMessagesFromRoom(cr);
							for(String mensaje : mensajes) {
								// CENSOR
								Pair<String, List<String>> c_res = cs.censorMessage(mensaje);
								if(c_res.getSecond().size() > 0) {
									db.saveCensor(mensaje, c_res.getSecond(), sender, cr);
								}					
								ok += ";" + c_res.getFirst();
							}					
							db.insertUserToDatabase(cr, sender);
						} 
					} else {
						ok = "NOJOINPERMISOSs";
					}
				}
				break;
			case "!LEAVEROOM" :
				ok = "LEAVINGROOM";
				
				// DATABASE
				cr = chat.isUserOnRoom(sender);
				db.removeUserFromRoom(cr, sender);
				if(cr.getUsers().size() <= 1) {
					db.deleteRoom(cr);
				}
				break;
			case "!AVAILABLEROOMS":
				List<ChatRoom> crs = chat.availableRooms(sender);
				ok += "CHATROOMS" + "!";
				if (crs.size() == 0) {
					ok += "No hay salas disponibles.";
				} else {
					ok += "A continuación se mostrarán las salas públicas disponibles." + ";";
					for(ChatRoom c : crs) {
						if(c.getPriv() == false) {
							ok += "SALA " + c.getId() + ": " + c.getName() + ";";
						}
					}
				}
				break;
			case "!SEND":
				// Check if user is on a private room with the other
				try {
					String dest = parts[1];
					String msg2 = "";
					// Join messages from spaces
					if(parts.length > 2) {
						String m = "";
						for(int i = 2; i < parts.length; i++) {
							m += parts[i] + " ";
						}
						msg2 = m;
					}
					ok = chat.createPrivateRoom("PRIVATE ROOM " + sender + " - " +  parts[1], sender, dest);
					if(ok.contains("!")) {
						// CENSOR
						Pair<String, List<String>> c_res = cs.censorMessage(msg2);
						if(c_res.getSecond().size() > 0) {
							db.saveCensor(msg2, c_res.getSecond(), sender, chat.isUserOnRoom(sender));
						}
						
						ok += c_res.getFirst();
					
						// DATABASE
						cr = chat.isUserOnRoom(sender);
						db.insertCRToDatabase(cr);
						db.insertMsgToDatabase(sender, cr, msg2);
					}
				} catch (Exception e) {
					ok = "BADARG";
				}
				break;
			case "!CHATW":
				// Check if user is on a private room with the other
				String d2 = parts[1];
				String res = chat.createPrivateRoom("PRIVATE ROOM " + sender + " - " +  parts[1], sender, d2);
				
				ok = "JOINED";
				
				// DATABASE
				cr = chat.isUserOnRoom(sender);
				List<String> mensajes = db.getMessagesFromRoom(cr);
				for(String mensaje : mensajes) {
					ok += ";" + mensaje;
				}		
				db.insertCRToDatabase(cr);
				break;
			case "!SENDR":
				
				String msg2 = "";
				// Join messages from spaces
				if(parts.length > 1) {
					String m = "";
					for(int i = 1; i < parts.length; i++) {
						m += parts[i] + " ";
					}
					msg2 = m;
				}
				
				// CENSOR
				Pair<String, List<String>> c_res = cs.censorMessage(msg2);
				if(c_res.getSecond().size() > 0) {
					db.saveCensor(msg2, c_res.getSecond(), sender, chat.isUserOnRoom(sender));
				}
				
				ok = "SENDMSGTOROOM" + "!" + c_res.getFirst();
				
				// DATABASE
				cr = chat.isUserOnRoom(sender);
				if(cr != null) {
					db.insertMsgToDatabase(sender, cr, msg2);
				}
				break;	
			case "!CLEAR":
				ok = "CLEAR";
				break;
			case "!CHANGENAME":
				if(chat.getRoot() != null && chat.getRoot().equals(sender)) {
					ok = "ROOTNOCHANGE";
				} else {
					ok = "NAME" + "!" + parts[1];
					
					// DATABASE
					db.updateName(sender, parts[1]);
				}
				break;
			case "!SUPERUSER":
				try {
					String pass = parts[1];
					ok = chat.superUser(pass);
					if(ok.equals("OKROOT")) {
						chat.goRoot(sender);
						
						// DATABASE
						db.setRoot(sender);
					} 
				} catch (Exception e) {
					ok = "BADARG";
				}
				break;
			case "!BROADCAST":
				if(sender.equals(chat.getRoot())) {
					ok = "BROADCAST" + "!" + parts[1];
				} else {
					ok = "BCASTNOROOT";
				}
				break;
			case "!DELETEROOM":
				cr = chat.isUserOnRoom(sender);
				if(parts.length > 1) {
					ok = chat.deleteRoom(Long.parseLong(parts[1]), sender);
				} else {
					ok = chat.deleteRoom(cr.getId(), sender);
				}
				
				// DATABASE
				db.deleteRoom(cr);
				break;
			case "!UPDATEUSERS":
				ok = "UPDATEUSERS";
				break;
			case "!HELP":
				ok = "HELP";
				break;
		}
		return new Pair<ChatRoomsController, String>(chat,ok);
	}
	
	
	public String listCommands() {
		
		String list = "";
		list += "!HELP to show available commands\n";
		list += "!CREATEROOM <name> to create a new room\n";
		list += "!INVITE <user> to invite a user to the current room\n";
		list += "!JOINROOM <id> to join a new room\n";
		list += "!LEAVEROOM to leave the current room\n";
		list += "!DELETEROOM <id> to delete a room you created\n";
		list += "!AVAILABLEROOMS to list all public available rooms\n";
		list += "!SEND <user> <msg> to send a message to a user\n";
		list += "!CHATW <user> to start a chat wih a user without sending a message\n";
		list += "!SENDR <msg> to send a message in a room\n";
		list += "!CLEAR to clear messages in your screen\n";
		list += "!CHANGENAME <name> to change your name\n";
		list += "!SUPERUSER <password> to become the administrator\n";
		list += "!BROADCAST <msg> to send a message to all users\n";
		list += "!UPDATEUSERS to check who is online\n";
		
		return list;
	}
	
	
}
