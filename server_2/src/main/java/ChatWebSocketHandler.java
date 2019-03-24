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
        else if(res.getSecond().contains("SENDMSG")) {
        	String dest = res.getSecond().split("!")[1];
        	message = message.split(" ")[2];
        	chat = Chat.userSaysToUser(sender, message, chat, dest);
        } else if (res.getSecond().contains("CHATROOMS")){
        	String crs = res.getSecond().split("!")[1];
        	String[] salas = crs.split(";");
        	for(String s : salas) {
        		chat = Chat.serverSaysToUser("Server", s, chat, sender);
        	}
        } else if(res.getSecond().equals("LEAVINGROOM")) { 
        	ChatRoom cr = chat.isUserOnRoom(sender);
        	if(cr != null) {
				chat.leaveRoom(sender);
        		chat = Chat.serverSaysToUser("Server", "Abandonando sala " + cr.getName() + ".", chat, sender);
        	} else {
        		chat = Chat.serverSaysToUser("Server", "Solo se puede abandonar una sala si est�s dentro de ella.", chat, sender);
        	}
        } else {
	        ChatRoom cr = chat.isUserOnRoom(sender);
	    	if(cr != null) {
	    		System.out.println("Sending message to " + cr.getId() + " which has " + cr.getUsers().size() + " users");
	    		chat = Chat.sendMessageToChannel(sender = Chat.userUsernameMap.get(user), msg = message, cr.getId(), chat);
	    	} else {
	    		// Ahora mismo, cualquier mensaje fuera de salas y sin ser CMD se hace broadcast
	    		chat = Chat.broadcastMessage(sender = Chat.userUsernameMap.get(user), msg = message, chat);
	    	}
        }
    }
    

}
