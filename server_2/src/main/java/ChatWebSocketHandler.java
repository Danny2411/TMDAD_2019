import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;  

@WebSocket
public class ChatWebSocketHandler {

    private String sender, msg;
    private CommandController cmd = new CommandController();
    private ChatRoomsController chat = new ChatRoomsController();
    
    private final static String ENV_AMQPURL_NAME  = "amqp://iiilqiyz:IB5oVZP1FEUICOlk9jpf7LsDrFynH-wQ@raven.rmq.cloudamqp.com/iiilqiyz";
    private final static String TEST_QUEUE = "HOLA";

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
    	String req = user.getUpgradeRequest().getRequestURI().toString();
    	String username = "";
    	boolean informaCambioNombre = false;
    	if(req.contains("?u=")) {
    		req = req.substring(req.indexOf('=') + 1);
    		if(Chat.userUsernameMap.containsValue(req)){
    			username = "User" + Chat.nextUserNumber++;
    			informaCambioNombre = true;
    		} else {
    			// Debería entrar siempre aquí por como funciona JS
    			username = req;
    		}
    	} else {
    		username = "User" + Chat.nextUserNumber++;
    	} 	
    
        Chat.userUsernameMap.put(user, username);
        Chat.notifications.put(user, "");
        chat = Chat.serverSaysToUser("Server", "Bienvenid@, " + username, chat, username);
        if(informaCambioNombre) {
            chat = Chat.serverSaysToUser("Server", "Se te ha asignado el nombre " + username + " por duplicidad.", chat, username);
        }
        chat.setLastId(cmd.db.getLastIdx());
        // Insert user to Database
        cmd.db.insertUserToDatabase(null, username);
        // Get unread messages
        List<String> pending = cmd.db.checkRoomsWithMessages(username);
        for(String s : pending) {
        	chat = Chat.serverSaysToUser("Server", "Tienes mensajes disponibles en la sala de ID = " + s + ".", chat, username);
        }
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = Chat.userUsernameMap.get(user);
        Chat.userUsernameMap.remove(user);
        Chat.notifications.remove(user);
        // Leave chatroom
        ChatRoom cr = chat.isUserOnRoom(username);
    	if(cr != null) {
			chat.leaveRoom(username);
    		if(cr.getUsers().size() > 0) {
    			for(String u : cr.getUsers()) {
    				chat = Chat.serverSaysToUser("Server", username + " ha abandonado la sala.", chat, u);
    			}
    		}
    	}
    	cmd.db.disconnectUser(username);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws IOException, TimeoutException, SQLException {
        String sender = Chat.userUsernameMap.get(user);
 
        Pair<ChatRoomsController, String> res = cmd.parseMessage(chat, message, sender);
        chat = res.getFirst();
    
        if(res.getSecond().equals("HELP")) {
        	String cmds = cmd.listCommands();
        	chat = Chat.serverSaysToUser("Server", cmds, chat, sender);
        }
        else if(res.getSecond().equals("YAENSALA")) {
        	chat = Chat.serverSaysToUser("Server", "No se puede crear una sala estando en otra.", chat, sender);
        }
        else if(res.getSecond().contains("CREATED")) {
        	chat = Chat.serverSaysToUser("Server", "Sala creada con éxito.", chat, sender);
        } 
        else if(res.getSecond().contains("NOJOIN")) {
        	chat = Chat.serverSaysToUser("Server", "No es posible unirse a esa sala.", chat, sender);
        } 
        else if(res.getSecond().contains("JOINED")) {
        	chat = Chat.serverSaysToUser("Server", "Te has unido a la sala " + chat.isUserOnRoom(sender).getName(), chat, sender);
        	ChatRoom cr = chat.isUserOnRoom(sender);
        	for(String u : cr.getUsers()) {
        		if(!u.equals(sender)) {
        			chat = Chat.serverSaysToUser("Server", sender + " se ha unido a la sala." , chat, u);
        		}
        	}
        	if(res.getSecond().contains(";")) {
        		String[] pendingMsg = res.getSecond().split(";");
        		for (int i = 1; i < pendingMsg.length; i++) {
        			String[] m = pendingMsg[i].split("!");
        			String orig_sender = m[0];
        			String orig_msg = m[1];
                	chat = Chat.recoverMessages(orig_sender, orig_msg, chat, sender, cr);
        		}
        	}
        }  
        else if(res.getSecond().contains("SENDMSGTOROOM")) {
        	 ChatRoom cr = chat.isUserOnRoom(sender);
        	 String m = res.getSecond();
        	 if(res.getSecond().contains("CLEAR::")) {
        		 chat = Chat.serverSaysToUser("Server", "CLEAR CHATS", chat, sender);
        		 m = m.replaceAll("CLEAR::", "");
        	 }
        	 if(cr != null) {
        		 chat = Chat.sendMessageToChannel(sender, m.split("!")[1], cr.getId(), chat);
        	 } else {
        		 chat = Chat.serverSaysToUser("Server", "Primero debes entrar en una sala.", chat, sender);
        	 }
        } 
        else if(res.getSecond().contains("SENDMSG")) {
        	String dest = res.getSecond().split("!")[1];
        	message = message.split(" ")[2];
        	chat = Chat.userSaysToUser(sender, message, chat, dest);
        }
        else if (res.getSecond().contains("CHATROOMS")){
        	String crs = res.getSecond().split("!")[1];
        	String[] salas = crs.split(";");
        	for(String s : salas) {
        		chat = Chat.serverSaysToUser("Server", s, chat, sender);
        	}
        } 
        else if(res.getSecond().equals("LEAVINGROOM")) { 
        	ChatRoom cr = chat.isUserOnRoom(sender);
        	if(cr != null) {
				chat.leaveRoom(sender);
        		chat = Chat.serverSaysToUser("Server", "Abandonando sala " + cr.getName() + ".", chat, sender);
        		if(cr.getUsers().size() > 0) {
        			for(String u : cr.getUsers()) {
        				chat = Chat.serverSaysToUser("Server", sender + " ha abandonado la sala.", chat, u);
        			}
        		}
        	} else {
        		chat = Chat.serverSaysToUser("Server", "Solo se puede abandonar una sala si estás dentro de ella.", chat, sender);
        	}
        } 
        else if(res.getSecond().equals("BADARG")) {
        	chat = Chat.serverSaysToUser("Server", "El mensaje no es correcto.", chat, sender);
        }
        else if(res.getSecond().equals("CLEAR")) {
    		chat = Chat.serverSaysToUser("Server", "CLEAR CHATS", chat, sender);
        } 
        else if(res.getSecond().contains("NAME!")) {
        	String newName = res.getSecond().replaceAll("NAME!","");
        	// Change if name is not taken
        	if(Chat.userUsernameMap.values().contains(newName) == false) {
	        	// Change name in user map
	        	Chat.userUsernameMap.put(user, newName);
	        	// Change name in rooms
	        	for(ChatRoom c : chat.getChatRooms()) {
	        		List<String> newU = new ArrayList<String>();
	        		if(c.getUsers().contains(sender)) {
	        			for(String u : c.getUsers()) {
	        				if(u.equals(sender) == false) {
	        					newU.add(u);
	        		        	chat = Chat.serverSaysToUser("Server", sender + " ahora se llama " + newName, chat, u);
	        				}
	        			}
	        			newU.add(newName);
	        			c.setUsers(newU); 	
	        		}
	        	}
	        	chat = Chat.serverSaysToUser("Server", "Cambiado nombre a " + newName, chat, newName);
        	} else {
        		chat = Chat.serverSaysToUser("Server", "El nombre " + newName + " ya está en uso.", chat, sender);
        	}
        }
        else if(res.getSecond().equals("OKROOT")) {
        	chat = Chat.serverSaysToUser("Server", "Ahora eres todopoderoso.", chat, sender);
        }
        else if(res.getSecond().equals("BADROOT")) {
        	chat = Chat.serverSaysToUser("Server", "No tienes permisos. Se informará de esto.", chat, sender);
        }
        else if(res.getSecond().equals("ROOTNOCHANGE")) {
        	chat = Chat.serverSaysToUser("Server", "El todopoderoso no debe cambiar de nombre.", chat, sender);
        }
        else if(res.getSecond().contains("BROADCAST!")) {
        	String bcast_m = res.getSecond().replaceAll("BROADCAST!","");
        	chat = Chat.broadcastMessage("El todopoderoso", bcast_m , chat);
        }
        else if(res.getSecond().equals("BCASTNOROOT")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes hacer broadcast sin privilegios.", chat, sender);
        }
        else if(res.getSecond().equals("DELNOCREATOR")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes borrar la sala de otro.", chat, sender);
        }
        else if(res.getSecond().equals("DELNOROOM")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes borrar una sala inexistente.", chat, sender);
        }
        else if(res.getSecond().contains("KICK")) {
        	String users = res.getSecond().replaceAll("KICK::", "");
        	String[] us = users.split("::");
        	for(String u : us) {
            	chat = Chat.serverSaysToUser("Server", "Se ha eliminado la sala en la que estabas.", chat, u);
        	}
        }
        else if(res.getSecond().equals("UPDATEUSERS")) {
        	chat = Chat.serverSaysToUser("Server", "Actualizada lista de usuarios.", chat, sender);
        }
        else {
        	chat = Chat.serverSaysToUser("Server", "Comando desconocido.", chat, sender);
        }
    }
    

}
