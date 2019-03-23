import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    static int nextUserNumber = 1; //Assign to username for next connecting user
    
    public static void main(String[] args) {
        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        staticFiles.expireTime(600);
        webSocket("/chat", ChatWebSocketHandler.class);
        init();
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("userMessage", createHtmlMessageFromSender(sender, message))
                    .put("userlist", userUsernameMap.values())
                    .put("currentchannel", (new ArrayList<String>().add("No channel")))
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
    public static void sendMessageToChannel(String sender, String message, Long room, ChatRoomsController chat) {
    	System.out.println( userUsernameMap.values());
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
        
    	userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
    		String u = userUsernameMap.get(session);
    		if (users.contains(u)) {
    			 try {
	                session.getRemote().sendString(String.valueOf(new JSONObject()
	                    .put("userMessage", createHtmlMessageFromSender(sender, message))
	                    .put("userlist", u)
	                    .put("currentchannel", current_c.getName())
	                ));
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
    		}

        });
      
    }

}
