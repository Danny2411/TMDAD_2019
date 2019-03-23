import java.util.ArrayList;
import java.util.List;

public class ChatRoomsController {

	private List<ChatRoom> chatRooms = new ArrayList<ChatRoom>();
	public List<ChatRoom> getChatRooms() {
		return chatRooms;
	}

	public void setChatRooms(List<ChatRoom> chatRooms) {
		this.chatRooms = chatRooms;
	}

	private long lastId = 0;
	
	// Create a new room
	public void createRoom(String name, String user) {
		ChatRoom newRoom = new ChatRoom(lastId, name, user);
		lastId += 1;
		chatRooms.add(newRoom);
	}
	
	// Delete an existing room
	public void deleteRoom(Long id) {
		int idx = -1;
		for(int i = 0; i < chatRooms.size(); i++) {
			if (chatRooms.get(i).getId() == id) {
				idx = i;
				break;
			}
		}
		if (idx >= 0) {
			chatRooms.remove(idx);
		}
	}
	
	// List available rooms
	public void availableRooms(String user) {
		for (ChatRoom c : chatRooms) {
			Chat.broadcastMessage("Server", c.toString());
		}
	}
	
	// Join an existing room
	public void joinRoom(Long id, String user) {
		int idx = -1;
		for(int i = 0; i < chatRooms.size(); i++) {
			if (chatRooms.get(i).getId() == id) {
				idx = i;
				break;
			}
		}
		if (idx >= 0) {
			for(String u : chatRooms.get(idx).getUsers()) {
				if(u == user) {
					return;
				}
			}
			chatRooms.get(idx).userJoins(user);
		}
	}
	
	// Check if a user is on a room
	public ChatRoom isUserOnRoom(String user) {
		System.out.println("Checking : " + user);
		for (ChatRoom c : chatRooms) {
			for(String u : c.getUsers()) {
				if(u.equals(user)){
					return c;
				}
			}
		}
		return null;
	}
	
	
}
