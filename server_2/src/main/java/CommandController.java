import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CommandController {
	// Database controller
	public DatabaseAdapter db;
	// Censor controller
	public CensuraAdapter cs;
	// RabbitMQ Adapter
    private RabbitMQAdapter rabbitMQ = new RabbitMQAdapter();
    // Censor host
    private String censorHost;
	
	// Command controller itself
	public CommandController() {
		db = new DatabaseAdapter();
		cs = new CensuraAdapter();
		try {
			db.connectToDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try (InputStream input = new FileInputStream("config/config.properties")) {
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);   
            censorHost = prop.getProperty("censor.host");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	// Parse received messages
	public Pair<ChatRoomsManager, String> parseMessage(ChatRoomsManager chat, String msg, String sender) {
		// Guarantee 500 max length
		if(msg.length() > 499)
			msg = msg.substring(0, 499);
		// Rest of the functionalities
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
					
					// RabbitMQ
					rabbitMQ.createQueue(Long.toString(cr.getId()));
				}
				break;
			case "!INVITE" :
				cr = chat.isUserOnRoom(sender);
				if(cr != null && cr.getPriv() == true) {
					ok = "NOINVINPRIV";
				} else {
					if(parts.length < 2 || parts.length > 2) {
						ok = "NODSTTOINVITE";
					} else {
						if(cr == null || !cr.getCreator().equals(sender)) {
							ok = "BADINVITE";
						} else {
							ok = "INVITE" + "!" + parts[1] + "!" + cr.getId();
							
							// DATABASE
							cr = chat.isUserOnRoom(sender);
							db.insertMsgToDatabase(sender, cr, "Se ha invitado a " + parts[1] + " a la sala.");
						}
					}
				}
				break;
			case "!KICK" :
				cr = chat.isUserOnRoom(sender);
				if(parts.length < 2 || parts.length > 2) {
					ok = "NODSTTOKICK";
				} else {
					if(cr == null || !cr.getCreator().equals(sender)) {
						ok = "BADKICK";
					} else {
						ok = "KICKONPURPOSE" + "!" + parts[1] + "!" + cr.getId();
						chat.leaveRoom(parts[1]);
						
						// DATABASE
						cr = chat.isUserOnRoom(sender);
						db.removeUserFromRoom(cr, parts[1]);
						db.removeMsgToDatabase(cr, "Se ha invitado a " + parts[1] + " a la sala.");
					}
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
								/*
								Pair<String, List<String>> c_res = cs.censorMessage(mensaje);
								if(c_res.getSecond().size() > 0) {
									db.saveCensor(mensaje, c_res.getSecond(), sender, cr);
								}					
								ok += ";" + c_res.getFirst();
								*/
								
								// DISTRIBUTED CENSOR
								
								try {
									Socket socket = new Socket(censorHost, 4568);
									PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
									BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
									out.println(mensaje);
									String fromServer;
							
									if ((fromServer = in.readLine()) != null) {
									    System.out.println("Server: " + fromServer);
									}
									
									socket.close();
									
									// Check censored words
									String original_msg = mensaje;
									mensaje = fromServer.split("!!")[0];
									String cw = fromServer.split("!!")[1].replace("[", "").replace("]", "");
									if(cw != null && !cw.equals("")) {
										String[] censoredWords = cw.split(",");
										List<String> ls = new ArrayList<String>();
										for(String s_ : censoredWords)
											ls.add(s_);
										db.saveCensor(original_msg, ls, sender, chat.isUserOnRoom(sender));
									}
									
									ok += ";" + mensaje;
									
								} catch (Exception e) {
									e.printStackTrace();
								}
								
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
				if(cr != null) {
					db.removeUserFromRoom(cr, sender);
					if(cr.getUsers().size() <= 1) {
						// No borrar si se van todos: podría ser caída del sistema
						// db.deleteRoom(cr);
					}
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
						/*
						Pair<String, List<String>> c_res = cs.censorMessage(msg2);
						if(c_res.getSecond().size() > 0) {
							db.saveCensor(msg2, c_res.getSecond(), sender, chat.isUserOnRoom(sender));
						}
						
						ok += c_res.getFirst();
						*/
						
						// DISTRIBUTED CENSOR
						
						try {
							Socket socket = new Socket(censorHost, 4568);
							PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
							BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							out.println(msg2);
							String fromServer;
					
							if ((fromServer = in.readLine()) != null) {
								// System.out.println("Server: " + fromServer);
							}
							socket.close();
							
							// Check censored words
							String original_msg = msg2;
							msg2 = fromServer.split("!!")[0];
							String cw = fromServer.split("!!")[1].replace("[", "").replace("]", "");
							if(cw != null && !cw.equals("")) {
								String[] censoredWords = cw.split(",");
								List<String> ls = new ArrayList<String>();
								for(String s_ : censoredWords)
									ls.add(s_);
								db.saveCensor(original_msg, ls, sender, chat.isUserOnRoom(sender));
							}
							
							ok += msg2;
							
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					
						// DATABASE
						cr = chat.isUserOnRoom(sender);
						db.insertCRToDatabase(cr);
						db.insertMsgToDatabase(sender, cr, msg2);
									
						// RabbitMQ
						if(cr != null) {
							rabbitMQ.createQueue(Long.toString(cr.getId()));
							rabbitMQ.publishMessage(Long.toString(cr.getId()), msg2);
						}
					}
				} catch (Exception e) {
					ok = "BADARG";
				}
				break;
			case "!CHATW":
				// Check if user is on a private room with the other
				String d2 = parts[1];
				chat.createPrivateRoom("PRIVATE ROOM " + sender + " - " +  parts[1], sender, d2);
				
				ok = "JOINED";
				
				// RabbitMQ
				cr = chat.isUserOnRoom(sender);
				rabbitMQ.createQueue(Long.toString(cr.getId()));
				String rbb = "Mensajes en [RabbitMQ]:";
				while(true) {
					rbb = rabbitMQ.consumeMessage(Long.toString(cr.getId()));
					if(rbb == null)
						break;
					System.out.println("[RabbitMQ] " + rbb);
				}
				
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
				/*
				cr = chat.isUserOnRoom(sender);
				if(cr != null) {
					Pair<String, List<String>> c_res = cs.censorMessage(msg2);
					if(c_res.getSecond().size() > 0) {
						db.saveCensor(msg2, c_res.getSecond(), sender, chat.isUserOnRoom(sender));
					}
					
					ok = "SENDMSGTOROOM" + "!" + c_res.getFirst();
				}
				*/
				
				// DISTRIBUTED CENSOR
				
				try {
					Socket socket = new Socket(censorHost, 4568);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out.println(msg2);
					String fromServer;
			
					if ((fromServer = in.readLine()) != null) { 
					   // System.out.println("Server: " + fromServer);
					}
					
					socket.close();
					
					// Check censored words
					String original_msg = msg2;
					msg2 = fromServer.split("!!")[0];
					String cw = fromServer.split("!!")[1].replace("[", "").replace("]", "");
					if(cw != null && !cw.equals("")) {
						String[] censoredWords = cw.split(",");
						List<String> ls = new ArrayList<String>();
						for(String s_ : censoredWords)
							ls.add(s_);
						db.saveCensor(original_msg, ls, sender, chat.isUserOnRoom(sender));
					}
					
					ok = "SENDMSGTOROOM" + "!" + msg2;
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				ok = "SENDMSGTOROOM" + "!" + msg2;
				
				// DATABASE
				cr = chat.isUserOnRoom(sender);
				if(cr != null) {
					db.insertMsgToDatabase(sender, cr, msg2);
				}
				
				// RabbitMQ
				if(cr != null && cr.getPriv() == true) {
					rabbitMQ.createQueue(Long.toString(cr.getId()));
					rabbitMQ.publishMessage(Long.toString(cr.getId()), msg2);
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
				
				// RabbitMQ
				rabbitMQ.removeQueue(Long.toString(cr.getId()));
				
				break;
			case "!GETFILE":
				cr = chat.isUserOnRoom(sender);
				if(parts.length > 1 && parts.length < 3) {
					String filename = parts[1];
					boolean exists = db.findFileInDatabase(filename);
					if(exists) {
						ok = "DOWNLOAD" + "!" + filename;
					} else {
						ok = "BADARG";
					}
				} else {
					ok = "BADARG";
				}
				break;
			case "!UPDATEUSERS":
				ok = "UPDATEUSERS";
				break;
			case "!HELP":
				ok = "HELP";
				break;
		}
		return new Pair<ChatRoomsManager, String>(chat,ok);
	}
	
	
	public String listCommands() {
		
		String list = "";
		list += "!HELP to show available commands\n";
		list += "!CREATEROOM <name> to create a new room\n";
		list += "!INVITE <user> to invite a user to the current room\n";
		list += "!KICK <user> to kick a user from the current room\n";
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
