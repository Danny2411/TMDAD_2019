import java.util.List;

public class CommandController {

	public Pair<ChatRoomsController, String> parseMessage(ChatRoomsController chat, String msg, String sender) {
		String[] parts = msg.split(" ");
		String ok = "";
		switch(parts[0]) {
			case "!CREATEROOM" :
				System.out.println("Create room: " + parts[1]);
				boolean possible = chat.createRoom(parts[1], sender);
				if(!possible) {
					ok = "YAENSALA";
				} else {
					ok = "CREATED";
				}
				break;
			case "!JOINROOM" :
				System.out.println(sender + " joining room: " + parts[1]);
				boolean success = chat.joinRoom(Long.parseLong(parts[1]), sender);
				if(!success) {
					ok = "NOJOIN";
				} else {
					ok = "JOINED";
				}
				break;
			case "!LEAVEROOM" :
				System.out.println(sender + " leaving room");
				ok = "LEAVINGROOM";
				break;
			case "!AVAILABLEROOMS":
				System.out.println("Listing available rooms");
				List<ChatRoom> crs = chat.availableRooms(sender);
				ok += "CHATROOMS" + "!";
				if (crs.size() == 0) {
					ok += "No hay salas disponibles.";
				} else {
					ok += "A continuación se mostrarán las salas públicas disponibles." + ";";
					for(ChatRoom c : crs) {
						if(c.getPriv() == false) {
							ok += "SALA " + c.getId() + ": " + c.getName() + ";";
						}
					}
				}
				break;
			case "!SEND":
				// Check if user is on a private room with the other
				try {
					String dest = parts[1];
					String msg2 = parts[2];
					ok = chat.createPrivateRoom("PRIVATE ROOM " + sender + " - " +  parts[1], sender, dest);
					if(ok.contains("!")) {
						ok += msg2;
					}
					System.out.println("Trying to create room with " + parts[1]);
				} catch (Exception e) {
					ok = "BADARG";
				}
				break;
			case "!CHATW":
				// Check if user is on a private room with the other
				String d2 = parts[1];
				chat.createPrivateRoom("PRIVATE ROOM " + sender + " - " +  parts[1], sender, d2);
				ok = "JOINED";
				System.out.println("Trying to create room with " + parts[1]);
				break;
			case "!SENDR":
				ok = "SENDMSGTOROOM" + "!" + parts[1];
				break;	
			case "!CLEAR":
				ok = "CLEAR";
				break;
			case "!CHANGENAME":
				if(chat.getRoot() != null && chat.getRoot().equals(sender)) {
					ok = "ROOTNOCHANGE";
				} else {
					ok = "NAME" + "!" + parts[1];
				}
				break;
			case "!SUPERUSER":
				try {
					String pass = parts[1];
					ok = chat.superUser(pass);
					if(ok.equals("OKROOT")) {
						chat.goRoot(sender);
					} 
				} catch (Exception e) {
					ok = "BADARG";
				}
				break;
			case "!BROADCAST":
				if(sender.equals(chat.getRoot())) {
					ok = "BROADCAST" + "!" + parts[1];
				} else {
					ok = "BCASTNOROOT";
				}
				break;
			case "!DELETEROOM":
				ok = chat.deleteRoom(Long.parseLong(parts[1]), sender);
				break;
			case "!HELP":
				ok = "HELP";
				break;
		}
		return new Pair<ChatRoomsController, String>(chat,ok);
	}
	
	
	public String listCommands() {
		
		String list = "";
		list += "!HELP to show available commands\n";
		list += "!CREATEROOM <name> to create a new room\n";
		list += "!JOINROOM <id> to join a new room\n";
		list += "!LEAVEROOM to leave the current room\n";
		list += "!DELETEROOM <id> to delete a room you created\n";
		list += "!AVAILABLEROOMS to list all public available rooms\n";
		list += "!SEND <user> <msg> to send a message to a user\n";
		list += "!CHATW <user> to start a chat wih a user without sending a message\n";
		list += "!SENDR <msg> to send a message in a room\n";
		list += "!CLEAR to clear messages in your screen\n";
		list += "!CHANGENAME <name> to change your name\n";
		list += "!SUPERUSER <password> to become the administrator\n";
		list += "!BROADCAST <msg> to send a message to all users\n";
		
		return list;
	}
	
	
}
