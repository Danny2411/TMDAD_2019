import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class ChatWebSocketHandler {

    private String sender, msg;
    private CommandController cmd = new CommandController();
    private ChatRoomsController chat = new ChatRoomsController();

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String username = "User" + Chat.nextUserNumber++;
        Chat.userUsernameMap.put(user, username);
        chat = Chat.broadcastMessage(sender = "Server", msg = (username + " joined the chat"), chat);
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = Chat.userUsernameMap.get(user);
        Chat.userUsernameMap.remove(user);
        chat = Chat.broadcastMessage(sender = "Server", msg = (username + " left the chat"), chat);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        String sender = Chat.userUsernameMap.get(user);
        Pair<ChatRoomsController, String> res = cmd.parseMessage(chat, message, sender);
        chat = res.getFirst();
    
        if(res.getSecond().equals("YAENSALA")) {
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
        else {
        	chat = Chat.serverSaysToUser("Server", "Comando desconocido.", chat, sender);
        }
    }
    

}
