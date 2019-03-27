import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
	
	private long id;
	private String name;
	private List <String> users;
	private List <String> messages;
	private boolean priv = false;
	private String allowed;
	private String creator;
	
	// Constructor
	public ChatRoom(long id, String name, String user) {
		this.id = id;
		this.name = name;
		this.users = new ArrayList<String>();
		this.messages = new ArrayList<String>();
		this.users.add(user);
		this.priv = false;
		this.allowed = null;
	}
	
	public ChatRoom(long id, String name, String user, boolean priv, String allowed) {
		this.id = id;
		this.name = name;
		this.users = new ArrayList<String>();
		this.messages = new ArrayList<String>();
		this.users.add(user);
		this.priv = priv;
		this.allowed = allowed;
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
	public boolean getPriv() {
		return this.priv;
	}
	public void setPriv(boolean priv) {
		this.priv = priv;
	}
	public String getAllowed() {
		return this.allowed;
	}
	public void setAllowed(String allowed) {
		this.allowed = allowed;
	}
	public String getCreator() {
		return this.creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
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
