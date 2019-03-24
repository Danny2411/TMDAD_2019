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
			case "!DELETEROOM" :
				/*
				System.out.println("Delete room: " + parts[1]);
				boolean success = chat.deleteRoom(Long.parseLong(parts[1]));
				if(!success) {
					ok = "NOPOSIBLEBORRAR";
				} else {
					ok = "DELETED";
				}
				*/
				break;
			case "!INVITETOROOM" :
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
					ok += "A continuación se mostrarán las salas disponibles." + ";";
					for(ChatRoom c : crs) {
						ok += "SALA " + c.getId() + ": " + c.getName() + ";";
					}
				}
				break;
			case "!SEND":
				ok = "SENDMSG" + "!" + parts[1];
				break;
			case "!SENDR":
				ok = "SENDMSGTOROOM" + "!" + parts[1];
				break;
			case "!CLEAR":
				ok = "CLEAR";
				break;
		}
		return new Pair<ChatRoomsController, String>(chat,ok);
	}
	
	
}
