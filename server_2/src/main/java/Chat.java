import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Chat {

    // this map is shared between sessions and threads, so it needs to be thread-safe (http://stackoverflow.com/a/2688817)
    static Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
    static Map<Session, String> notifications = new ConcurrentHashMap<>();
    static int nextUserNumber = 1; //Assign to username for next connecting user
    
    public static void main(String[] args) {
        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        staticFiles.expireTime(600);
        webSocket("/chat", ChatWebSocketHandler.class);
        init();
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static ChatRoomsManager broadcastMessage(String sender, String message, ChatRoomsManager chat) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
        	String channel = "";
        	ChatRoom c = chat.isUserOnRoom(userUsernameMap.get(session));
        	if(c != null) {
        		if(c.getPriv() == false) {
        			channel = c.getName() + " - ID: " + c.getId();
            	} else {
            		channel = c.getName();
            	}
        	} else {
        		channel = "No channel";
        	}
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("userMessage", createHtmlMessageFromSender(sender, message))
                    .put("userlist", userUsernameMap.values())
                    .put("currentchannel", channel)
                    .put("isfile", "no")
                    .put("yourname", userUsernameMap.get(session))
                    .put("notificationlist", notifications.get(session))
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        return chat;
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article(
            b(sender + " says:"),
            span(attrs(".timestamp"), new SimpleDateFormat("HH:mm:ss").format(new Date())),
            p(message)
        ).render();
    }
    
    //Sends a message from one user to all users, along with a list of current usernames
    public static ChatRoomsManager sendMessageToChannel(String sender, String message, Long room, ChatRoomsManager chat) {
    	
    	ChatRoom c = null;
    	List<ChatRoom> cr = chat.getChatRooms();
        for(int i = 0; i < cr.size(); i++ ) {
        	if(cr.get(i).getId() == room) {
        		c = cr.get(i);
        		break;
        	}
        }
        List<String> users = c.getUsers();
        final ChatRoom current_c = c;
        String name = "";
        if(current_c.getPriv() == false) {
        	name = current_c.getName() + " - ID: " + current_c.getId();
        } else {
        	name = current_c.getName();
        }
        
        final String final_name = name;
    	userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
    		String u = userUsernameMap.get(session);
    		if (users.contains(u)) {
    			 try {
    				
	                session.getRemote().sendString(String.valueOf(new JSONObject()
	                    .put("userMessage", createHtmlMessageFromSender(sender, message))
	                    .put("userlist", userUsernameMap.values())
	                    .put("currentchannel", final_name)
	                    .put("isfile", "no")
	                    .put("yourname", userUsernameMap.get(session))
	                    .put("notificationlist", notifications.get(session))
	                ));
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
    		}

        });
    	
    	return chat;
      
    }
    
  //Sends a message from one user to all users, along with a list of current usernames
    public static ChatRoomsManager downloadFile(String sender, String message, Long room, ChatRoomsManager chat, String user, byte buf[], String filename) {
    	
    	
    	// System.out.println("[DFile] " + filename);
    	// System.out.println("[DFile] " + new String(buf));
    	
    	
    	ChatRoom c = null;
    	List<ChatRoom> cr = chat.getChatRooms();
        for(int i = 0; i < cr.size(); i++ ) {
        	for(String uir : cr.get(i).getUsers()) {
        		if(uir.equals(user)) {
        			c = cr.get(i);
            		break;
        		}
        	}
        }
      
        String currentchannel = "";
        if (c != null) {
        	if(c.getPriv() == false) {
        		currentchannel = c.getName() + " - ID: " + c.getId();
        	} else {
        		currentchannel = c.getName();
        	}
        } else {
        	currentchannel = "No channel";
        }
        final String ch = currentchannel;
        final String wildcard = "yes";
    	userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
    		String u = userUsernameMap.get(session);
    		if (user.equals(u)) {
    			 try {
	                session.getRemote().sendString(String.valueOf(new JSONObject()
	                	.put("userMessage", createHtmlMessageFromSender(sender, message))
	                	.put("file", buf)
	                	.put("route", filename)
	                	.put("isfile", "yes")
	                    .put("userlist", userUsernameMap.values())
	                    .put("currentchannel", ch)
	                    .put("yourname", userUsernameMap.get(session))
	                ));
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
    		}

        });
    	
    	return chat;
      
    }
    
  //Sends a message from one user to all users, along with a list of current usernames
    public static ChatRoomsManager serverSaysToUser(String sender, String msg, ChatRoomsManager chat, String user) {
    	
    	ChatRoom c = null;
    	List<ChatRoom> cr = chat.getChatRooms();
        for(int i = 0; i < cr.size(); i++ ) {
        	for(String uir : cr.get(i).getUsers()) {
        		if(uir.equals(user)) {
        			c = cr.get(i);
            		break;
        		}
        	}
        }
      
        String currentchannel = "";
        if (c != null) {
        	if(c.getPriv() == false) {
        		currentchannel = c.getName() + " - ID: " + c.getId();
        	} else {
        		currentchannel = c.getName();
        	}
        } else {
        	currentchannel = "No channel";
        }
        final String ch = currentchannel;
        
    	userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
    		String u = userUsernameMap.get(session);
    		if (user.equals(u)) {
    			 try {
	                session.getRemote().sendString(String.valueOf(new JSONObject()
	                    .put("userMessage", createHtmlMessageFromSender(sender, msg))
	                    .put("userlist", userUsernameMap.values())
	                    .put("currentchannel", ch)
	                    .put("isfile", "no")
	                    .put("yourname", userUsernameMap.get(session))
	                    .put("notificationlist", notifications.get(session))
	                ));
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
    		}

        });
    	
    	return chat;
      
    }
    
  //Sends a message from one user to all users, along with a list of current usernames
    public static ChatRoomsManager userSaysToUser(String sender, String msg, ChatRoomsManager chat, String user) {
    	
    	ChatRoom c = null;
    	List<ChatRoom> cr = chat.getChatRooms();
        for(int i = 0; i < cr.size(); i++ ) {
        	for(String uir : cr.get(i).getUsers()) {
        		if(uir.equals(user)) {
        			c = cr.get(i);
            		break;
        		}
        	}
        }
      
        String currentchannel = "";
        if (c != null) {
        	if(c.getPriv() == false) {
        		currentchannel = c.getName() + " - ID: " + c.getId();
        	} else {
        		currentchannel = c.getName();
        	}
        		// Como el receptor est� en una sala, no le llegar� el mensaje de primeras
        	serverSaysToUser("Server", "No se pudo entregar el mensaje a " + user , chat, sender);
        } else {
        	currentchannel = "No channel";
        	String ch = currentchannel;
        	userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
        		String u = userUsernameMap.get(session);
        		if (user.equals(u)) {
        			 try {
    	                session.getRemote().sendString(String.valueOf(new JSONObject()
    	                    .put("userMessage", createHtmlMessageFromSender(sender, msg))
    	                    .put("userlist", userUsernameMap.values())
    	                    .put("currentchannel", ch)
    	                    .put("isfile", "no")
    	                    .put("yourname", userUsernameMap.get(session))
    	                    .put("notificationlist", notifications.get(session))
    	                ));
    	            } catch (Exception e) {
    	                e.printStackTrace();
    	            }
        		}

            });
        	
        }
 
        
    	
    	return chat;
      
    }
    
  //Sends a message from one user to all users, along with a list of current usernames
    public static ChatRoomsManager recoverMessages(String sender, String msg, ChatRoomsManager chat, String user, ChatRoom cr) {
    	
    	String tmp = "";
    	if(cr.getPriv() == false) {
    		tmp = cr.getName() + " - ID: " + cr.getId();
    	} else {
    		tmp = cr.getName();
    	}
    	String ch = tmp;
    	userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
    		String u = userUsernameMap.get(session);
    		if (user.equals(u)) {
    			 try {
	                session.getRemote().sendString(String.valueOf(new JSONObject()
	                    .put("userMessage", createHtmlMessageFromSender(sender, msg))
	                    .put("userlist", userUsernameMap.values())
	                    .put("currentchannel", ch)
	                    .put("isfile", "no")
	                    .put("yourname", userUsernameMap.get(session))
	                    .put("notificationlist", notifications.get(session))
	                ));
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
    		}

        });
        	
        
 
        
    	
    	return chat;
      
    }
    
  //Sends a message from one user to all users, along with a list of current usernames
    public static ChatRoomsManager notify(String user, String message, ChatRoomsManager chat) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
       
        	if(userUsernameMap.get(session).equals(user)) {
        		try {
                    session.getRemote().sendString(String.valueOf(new JSONObject()
                        .put("notificationlist", notifications.get(session))
                        .put("isfile", "no")
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        	}
 
        });
        
        return chat;
    }

}
