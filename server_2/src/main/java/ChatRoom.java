import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
	
	private long id;
	private String name;
	private List <String> users;
	private List <String> messages;
	
	// Constructor
	public ChatRoom(long id, String name, String user) {
		this.id = id;
		this.name = name;
		this.users = new ArrayList<String>();
		this.messages = new ArrayList<String>();
		this.users.add(user);
	}
	
	// Getters and Setters
	public List<String> getUsers() {
		return users;
	}
	public void setUsers(List<String> users) {
		this.users = users;
	}
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	// Modify room
	public void appendMessage(String msg) {
		this.messages.add(msg);
	}
	
	public void userJoins(String id) {
		this.users.add(id);
	}
	
	public boolean userLeaves(String id) {
		return this.users.remove(id);
	}
	
	@Override
	public String toString() {
		return Long.toString(id);
	}
}
