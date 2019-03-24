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
        ChatRoom cr = chat.isUserOnRoom(sender);
        chat = cmd.parseMessage(chat, message, sender);
    	if(cr != null) {
    		System.out.println("Sending message to " + cr.getId() + " which has " + cr.getUsers().size() + " users");
    		chat = Chat.sendMessageToChannel(sender = Chat.userUsernameMap.get(user), msg = message, cr.getId(), chat);
    	} else {
    		chat = Chat.broadcastMessage(sender = Chat.userUsernameMap.get(user), msg = message, chat);
    	}
    }
    

}
