import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChatRoomsManager {

	private List<ChatRoom> chatRooms = new ArrayList<ChatRoom>();	
	
	private long lastId = 0;
	public long getLastId() {
		return lastId;
	}

	public void setLastId(long lastId) {
		this.lastId = lastId;
	}

	public List<ChatRoom> getChatRooms() {
		return chatRooms;
	}

	public void setChatRooms(List<ChatRoom> chatRooms) {
		this.chatRooms = chatRooms;
	}
	
	
	private String currentRoot = null;
	
	// Create a new room
	public boolean createRoom(String name, String user) {
		if(isUserOnRoom(user) != null) {
			return false;
		}
		ChatRoom newRoom = new ChatRoom(lastId, name, user);
		newRoom.setCreator(user);
		lastId += 1;
		chatRooms.add(newRoom);
		return true;
	}
	
	// Create a new private room for a 2-chat
	public String createPrivateRoom(String name, String user1, String user2) {
		ChatRoom c = isUserOnRoom(user1);
		if(c != null && (c.getPriv() == false || (c.getPriv() == true && c.getAllowed().equals(user2)) ) ) {
			return "YAENSALA";
		} else if ((c != null && c.getPriv() == true && c.getUsers().contains(user1) && c.getAllowed().equals(user2)) ||
				(c != null && c.getPriv() == true && c.getUsers().contains(user2) && c.getAllowed().equals(user1))) {
			// ya está donde debe estar
			lastId += 1;
			return "SENDMSGTOROOM" + "!";
		} else if (c == null) {
			// Check if the other user already created 
			for(ChatRoom cr : chatRooms) {
				if(cr.getPriv() == true && cr.getAllowed().equals(user1) && cr.getUsers().contains(user2)) {
					joinRoom(cr.getId(), user1);
					return "CLEAR::SENDMSGTOROOM" + "!";
				}
			}
			// If the other didnt created it, check if you are already in
			ChatRoom newRoom = new ChatRoom(lastId, name, user1, true, user2);
			newRoom.setCreator(user1);
			lastId += 1;
			chatRooms.add(newRoom);
			return "SENDMSGTOROOM" + "!";
		} else {
			return "";
		}
	}

	
	// Delete an existing room
		public String deleteRoom(Long id, String user) {
			int idx = -1;
			for(int i = 0; i < chatRooms.size(); i++) {
				if (chatRooms.get(i).getId() == id) {
					idx = i;
					break;
				}
			}
			if (idx >= 0) {
				if(chatRooms.get(idx).getCreator().equals(user)) {
					String left = "KICK::";
					List<String> aux_u = new ArrayList<String>(chatRooms.get(idx).getUsers());
					Iterator<String> iter = aux_u.iterator();
					while (iter.hasNext()) {
					    String u = iter.next();
					    chatRooms.get(idx).userLeaves(u);
						left += "::" + u;
					}
					return left;
				} else {
					return "DELNOCREATOR";
				}
				
			}
			return "DELNOROOM";
		}
	
	// Delete an existing room because it has no users
	public boolean deleteRoomLeft(Long id) {
		int idx = -1;
		
		for(int i = 0; i < chatRooms.size(); i++) {
			if (chatRooms.get(i).getId() == id) {
				idx = i;
				break;
			}
		}
		if (idx >= 0) {
			chatRooms.remove(idx);
			return true;
		}
		return false;
	}
	
	// List available rooms
	public List<ChatRoom> availableRooms(String user) {
		return this.chatRooms;
	}
	
	// Join an existing room
	public boolean joinRoom(Long id, String user) {
		if(isUserOnRoom(user) != null) {
			return false;
		}
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
					return false;
				}
			}
			chatRooms.get(idx).userJoins(user);
			return true;
		}
		return false;
	}
	
	// Check if a user is on a room
	public ChatRoom isUserOnRoom(String user) {
		for (ChatRoom c : chatRooms) {
			for(String u : c.getUsers()) {
				if(u.equals(user)){
					return c;
				}
			}
		}
		return null;
	}
	
	// Leave a room
	public boolean leaveRoom(String user) {
		for(ChatRoom c : chatRooms) {
			boolean onRoom = false;
			for(String u : c.getUsers()) {
				if (u.equals(user)){
					onRoom = true;
				}
			}
			if(onRoom) {
				c.userLeaves(user);
				if(c.getUsers().size() <= 0) {
					deleteRoomLeft(c.getId());
				}
				return true;
			}
		}
		return false;
	}
	
	// Superuser
	public String superUser(String password) {
		// Check if password is OK
		if(password.equals("TOOR")) {
			return "OKROOT";
		}
		return "BADROOT";
	}
	
	public void goRoot(String user) {
		this.currentRoot = user;
	}
	
	public String getRoot() {
		return this.currentRoot;
	}
	
	
	
}
