import java.util.ArrayList;
import java.util.List;

public class ChatRoomsController {

	private List<ChatRoom> chatRooms = new ArrayList<ChatRoom>();
	private long lastId = 0;
	
	public void createRoom(String name, String user) {
		ChatRoom newRoom = new ChatRoom(lastId, name, user);
		lastId += 1;
		chatRooms.add(newRoom);
	}
	
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
	
	public void availableRooms(String user) {
		for (ChatRoom c : chatRooms) {
			Chat.broadcastMessage("Server", c.toString());
		}
	}
	
}
