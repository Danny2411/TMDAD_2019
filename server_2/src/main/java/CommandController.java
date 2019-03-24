
public class CommandController {

	public ChatRoomsController parseMessage(ChatRoomsController chat, String msg, String sender) {
		String[] parts = msg.split(" ");
		switch(parts[0]) {
			case "!CREATEROOM" :
				System.out.println("Create room: " + parts[1]);
				chat.createRoom(parts[1], sender);
				break;
			case "!DELETEROOM" :
				System.out.println("Delete room: " + parts[1]);
				chat.deleteRoom(Long.parseLong(parts[1]));
				break;
			case "!INVITETOROOM" :
				break;
			case "!JOINROOM" :
				System.out.println(sender + " joining room: " + parts[1]);
				chat.joinRoom(Long.parseLong(parts[1]), sender);
				break;
			case "!LEAVEROOM" :
				System.out.println(sender + " leaving room");
				chat.leaveRoom(sender);
				break;
			case "!AVAILABLEROOMS":
				System.out.println("Listing available rooms");
				chat.availableRooms(sender);
				break;
		}
		return chat;
	}
	
	
}
