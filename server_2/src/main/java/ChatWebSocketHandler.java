import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@WebSocket(maxBinaryMessageSize = 1048576)
public class ChatWebSocketHandler {

    private String sender, msg;
    private CommandController cmd = new CommandController();
    private ChatRoomsManager chat = new ChatRoomsManager();
    private FileHandler fh = new FileHandler();

    
    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
    	String req = user.getUpgradeRequest().getRequestURI().toString();
    	String username = "";
    	boolean informaCambioNombre = false;
    	if(req.contains("?u=")) {
    		req = req.substring(req.indexOf('=') + 1);
    		if(Chat.userUsernameMap.containsValue(req)){
    			username = "User" + Chat.nextUserNumber++;
    			informaCambioNombre = true;
    		} else {
    			// Deber�a entrar siempre aqu� por como funciona JS
    			username = req;
    		}
    	} else {
    		username = "User" + Chat.nextUserNumber++;
    	} 	
    
        Chat.userUsernameMap.put(user, username);
        Chat.notifications.put(user, "");
        chat = Chat.serverSaysToUser("Server", "Bienvenid@, " + username, chat, username);
        if(informaCambioNombre) {
            chat = Chat.serverSaysToUser("Server", "Se te ha asignado el nombre " + username + " por duplicidad.", chat, username);
        }
        chat.setLastId(cmd.db.getLastIdx());
        // Insert user to Database
        cmd.db.insertUserToDatabase(null, username);
        // Get unread messages
        List<String> pending = cmd.db.checkRoomsWithMessages(username);
        for(String s : pending) {
        	chat = Chat.serverSaysToUser("Server", "Tienes mensajes disponibles en la sala de ID = " + s + ".", chat, username);
        }
        
        // Simulation...
        /*
        for(int i = 0; i < 1000; i++) {
        	Pair<String, String> r = MultiUserTest.test_1();
        	simulateOnMessage(r.getFirst(), r.getSecond());
        }
        */
        
        // Multi-thread simulation
        // multiThreadSimulation();

    }
    
    public void multiThreadSimulation() {
    	
    	// used this class to check max threads 
    	// Got from: https://github.com/jheusser/core-java-performance-examples/blob/master/src/test/java/com/google/code/java/core/threads/MaxThreadsMain.java
    	class MaxThreadsMain {

    		  public final int BATCH_SIZE = 10000;

    		  public void run() throws InterruptedException {
    		    List<Thread> threads = new ArrayList<Thread>();
    		    try {
    		      for (int i = 0; i <= 100 * 10000; i += BATCH_SIZE) {
    		        long start = System.currentTimeMillis();
    		        addThread(threads, BATCH_SIZE);
    		        long end = System.currentTimeMillis();
    		        Thread.sleep(1000);
    		        long delay = end - start;
    		        System.out.printf("%,d threads: Time to create %,d threads was %.3f seconds %n", threads.size(), BATCH_SIZE, delay / 1e3);
    		      }
    		    } catch (Throwable e) {
    		      System.err.printf("After creating %,d threads, ", threads.size());
    		      e.printStackTrace();
    		    }

    		  }

    		  private void addThread(List<Thread> threads, int num) {
    		    for (int i = 0; i < num; i++) {
    		      Thread t = new Thread(new Runnable() {
    		        @Override
    		        public void run() {
    		          try {
    		            while (!Thread.interrupted()) {
    		              Thread.sleep(1000);
    		            }
    		          } catch (InterruptedException ignored) {
    		            //
    		          }
    		        }
    		      });
    		      t.setDaemon(true);
    		      t.setPriority(Thread.MIN_PRIORITY);
    		      threads.add(t);
    		      t.start();
    		    }
    		  }
    		}
    	
    	
    	// Private class to test
    	class UserThread{
    		
    		private Object s = new Object();
    	    private String name;
    	    private int count = 0;
    	    private int NUM_MENSAJES = 5;
    	    private int NUM_USERS;
    	    
    	    public UserThread(int n) {
    	    	NUM_USERS = n;
    	    }

    	    public void run() {
				ExecutorService es = Executors.newCachedThreadPool();
    	    	for(int i = 0; i < NUM_USERS; i++){
    	            es.execute(new Thread(new Runnable(){
    	                    public void run(){
    	                        synchronized(s){
    	                            count += 1;
    	                            System.err.println("New user #"+count);
    	                        }
    	                        
    	                        for(int j = 0; j < NUM_MENSAJES; j++){
    	                            try {
    	                                Thread.sleep(1000);
    	                            	simulateOnMessage("User" + count, "!SENDR mensaje");
    	                            	
    	                            } catch (Exception e){
    	                                System.err.println(e);
    	                            }
    	                        }
    	                    }
    	                }));
    	            
    	        }
    			es.shutdown();
    			try {
    				boolean finished = es.awaitTermination(5, TimeUnit.MINUTES);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    	    }
    	}
    	
    	try {
    		// # of users
    		int n = 250;
    		int r = 1;
    		int gap = n / r;
    		
    		// Create one room to check something
			for(int i = 0; i < r; i++) {
				simulateOnMessage("Daniel_" + i, "!CREATEROOM sala_thread_" + i);
				for(int j = i * gap; j < (i+1) * gap; j++) {
					simulateOnMessage("Daniel_" + i, "!INVITE User" + j);
				}
			}
			
			long startTime = System.nanoTime(); 
			
			// Simulate join room
			int current_room = 0;
			for(int i = 0; i < n; i++) {
				if(i % gap == 0) {
					current_room++;
				}
				simulateOnMessage("User" + i, "!JOINROOM " + current_room);
			}
			
			for(int i = 0; i < n * 5; i++){
                try {   
                	simulateOnMessage("User" + ThreadLocalRandom.current().nextInt(1, n-1), "!SENDR mensaje");
                } catch (Exception e){
                    System.err.println(e);
                }
            }
			
			System.out.println("All joined.");
			// Create threads...
			//UserThread myRunnable = new UserThread(n);
			//myRunnable.run();
			// Time...
			long estimatedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed time: " + (double)estimatedTime / 1_000_000_000.0 + " sec.");
		} catch (IOException | TimeoutException | SQLException e) {
			e.printStackTrace();
		}   	
    } 
    
    
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = Chat.userUsernameMap.get(user);
        Chat.userUsernameMap.remove(user);
        Chat.notifications.remove(user);
        // Leave chatroom
        ChatRoom cr = chat.isUserOnRoom(username);
    	if(cr != null) {
			chat.leaveRoom(username);
    		if(cr.getUsers().size() > 0) {
    			for(String u : cr.getUsers()) {
    				chat = Chat.serverSaysToUser("Server", username + " ha abandonado la sala.", chat, u);
    			}
    		}
    	}
    	cmd.db.disconnectUser(username);
    }
    
    @OnWebSocketMessage
    public void onMessage(byte buf[], int offset, int length) {
    	// Only gets here when sending a image
    	System.out.println("[FILE] " + buf.toString());
    	fh.insertFile(buf);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws IOException, TimeoutException, SQLException {
        // Only gets here when sending a text message
    	System.out.println("[MESSAGE] " + message);

    	String sender = Chat.userUsernameMap.get(user);
    	
    	// Modify message if file uploaded
    	boolean fileUploaded = false;
    	String modified = "";
    	if(message.contains("!FILE:")) {
    		fileUploaded = true;
    		String filename = message.replaceAll("!FILE:", "");
    		fh.setName(filename);
    		modified = sender + " ha enviado el fichero " + filename + ".\n";
    				modified += "Para recuperarlo, escribe\n";
    				modified += "!GETFILE " + filename;
    	}
 
        Pair<ChatRoomsManager, String> res = cmd.parseMessage(chat, message, sender);
        chat = res.getFirst();
    
        if(res.getSecond().equals("HELP")) {
        	String cmds = cmd.listCommands();
        	chat = Chat.serverSaysToUser("Server", cmds, chat, sender);
        }
        else if(res.getSecond().equals("YAENSALA")) {
        	chat = Chat.serverSaysToUser("Server", "No se puede crear una sala estando en otra.", chat, sender);
        }
        else if(res.getSecond().contains("CREATED")) {
        	chat = Chat.serverSaysToUser("Server", "Sala creada con �xito.", chat, sender);
        } 
        else if(res.getSecond().contains("NOJOIN")) {
        	chat = Chat.serverSaysToUser("Server", "No es posible unirse a esa sala.", chat, sender);
        } 
        else if(res.getSecond().contains("JOINED")) {
        	chat = Chat.serverSaysToUser("Server", "Te has unido a la sala " + chat.isUserOnRoom(sender).getName(), chat, sender);
        	ChatRoom cr = chat.isUserOnRoom(sender);
        	for(String u : cr.getUsers()) {
        		if(!u.equals(sender)) {
        			chat = Chat.serverSaysToUser("Server", sender + " se ha unido a la sala." , chat, u);
        		}
        	}
        	if(res.getSecond().contains(";")) {
        		String[] pendingMsg = res.getSecond().split(";");
        		for (int i = 1; i < pendingMsg.length; i++) {
        			String[] m = pendingMsg[i].split("!");
        			String orig_sender = m[0];
        			String orig_msg = m[1];
                	chat = Chat.recoverMessages(orig_sender, orig_msg, chat, sender, cr);
        		}
        	}
        } 
        else if(res.getSecond().contains("NODSTTOINVITE")) {
        	chat = Chat.serverSaysToUser("Server", "El formato de la invitaci�n no es correcto.", chat, sender);
        }
        else if(res.getSecond().contains("BADINVITE")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes realizar esa invitaci�n.", chat, sender);
        }
        else if(res.getSecond().contains("NOINVINPRIV")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes invitar en una sala privada.", chat, sender);
        }
        else if(res.getSecond().contains("INVITE")) {
        	String dst = res.getSecond().split("!")[1];
        	String id = res.getSecond().split("!")[2];
        	chat = Chat.serverSaysToUser("Server", sender + " te ha invitado a la sala " + id + ".", chat, dst);
        	chat = Chat.serverSaysToUser("Server", "Has invitado a " + dst + " a la sala " + id + ".", chat, sender);
        }
        else if(res.getSecond().contains("NODSTTOKICK")) {
        	chat = Chat.serverSaysToUser("Server", "El formato de la expulsi�n no es correcto.", chat, sender);
        }
        else if(res.getSecond().contains("BADKICK")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes realizar esa expulsi�n.", chat, sender);
        }
        else if(res.getSecond().contains("KICKONPURPOSE")) {
        	String dst = res.getSecond().split("!")[1];
        	String id = res.getSecond().split("!")[2];
        	chat = Chat.serverSaysToUser("Server", sender + " te ha expulsado de la sala " + id + ".", chat, dst);
        	chat = Chat.serverSaysToUser("Server", "Has expulsado a " + dst + " de la sala " + id + ".", chat, sender);
        }
        else if(res.getSecond().contains("SENDMSGTOROOM")) {
        	 ChatRoom cr = chat.isUserOnRoom(sender);
        	 String m = res.getSecond();
        	 if(res.getSecond().contains("CLEAR::")) {
        		 chat = Chat.serverSaysToUser("Server", "CLEAR CHATS", chat, sender);
        		 m = m.replaceAll("CLEAR::", "");
        	 }
        	 if(cr != null) {
        		 chat = Chat.sendMessageToChannel(sender, m.split("!")[1], cr.getId(), chat);
        	 } else {
        		 chat = Chat.serverSaysToUser("Server", "Primero debes entrar en una sala.", chat, sender);
        	 }
        } 
        else if(res.getSecond().contains("SENDMSG")) {
        	String dest = res.getSecond().split("!")[1];
        	message = message.split(" ")[2];
        	chat = Chat.userSaysToUser(sender, message, chat, dest);
        }
        else if (res.getSecond().contains("CHATROOMS")){
        	String crs = res.getSecond().split("!")[1];
        	String[] salas = crs.split(";");
        	for(String s : salas) {
        		chat = Chat.serverSaysToUser("Server", s, chat, sender);
        	}
        } 
        else if(res.getSecond().equals("LEAVINGROOM")) { 
        	ChatRoom cr = chat.isUserOnRoom(sender);
        	if(cr != null) {
				chat.leaveRoom(sender);
        		chat = Chat.serverSaysToUser("Server", "Abandonando sala " + cr.getName() + ".", chat, sender);
        		if(cr.getUsers().size() > 0) {
        			for(String u : cr.getUsers()) {
        				chat = Chat.serverSaysToUser("Server", sender + " ha abandonado la sala.", chat, u);
        			}
        		}
        		// Get unread messages
                List<String> pending = cmd.db.checkRoomsWithMessages(sender);
                for(String s : pending) {
                	chat = Chat.serverSaysToUser("Server", "Tienes mensajes disponibles en la sala de ID = " + s + ".", chat, sender);
                }
        	} else {
        		chat = Chat.serverSaysToUser("Server", "Solo se puede abandonar una sala si est�s dentro de ella.", chat, sender);
        	}
        } 
        else if(res.getSecond().equals("BADARG")) {
        	chat = Chat.serverSaysToUser("Server", "El mensaje no es correcto.", chat, sender);
        }
        else if(res.getSecond().equals("CLEAR")) {
    		chat = Chat.serverSaysToUser("Server", "CLEAR CHATS", chat, sender);
        } 
        else if(res.getSecond().contains("NAME!")) {
        	String newName = res.getSecond().replaceAll("NAME!","");
        	// Change if name is not taken
        	if(Chat.userUsernameMap.values().contains(newName) == false) {
	        	// Change name in user map
	        	Chat.userUsernameMap.put(user, newName);
	        	// Change name in rooms
	        	for(ChatRoom c : chat.getChatRooms()) {
	        		List<String> newU = new ArrayList<String>();
	        		if(c.getUsers().contains(sender)) {
	        			for(String u : c.getUsers()) {
	        				if(u.equals(sender) == false) {
	        					newU.add(u);
	        		        	chat = Chat.serverSaysToUser("Server", sender + " ahora se llama " + newName, chat, u);
	        				}
	        			}
	        			newU.add(newName);
	        			c.setUsers(newU); 	
	        		}
	        	}
	        	chat = Chat.serverSaysToUser("Server", "Cambiado nombre a " + newName, chat, newName);
        	} else {
        		chat = Chat.serverSaysToUser("Server", "El nombre " + newName + " ya est� en uso.", chat, sender);
        	}
        }
        else if(res.getSecond().equals("OKROOT")) {
        	chat = Chat.serverSaysToUser("Server", "Ahora eres todopoderoso.", chat, sender);
        }
        else if(res.getSecond().equals("BADROOT")) {
        	chat = Chat.serverSaysToUser("Server", "No tienes permisos. Se informar� de esto.", chat, sender);
        }
        else if(res.getSecond().equals("ROOTNOCHANGE")) {
        	chat = Chat.serverSaysToUser("Server", "El todopoderoso no debe cambiar de nombre.", chat, sender);
        }
        else if(res.getSecond().contains("BROADCAST!")) {
        	String bcast_m = res.getSecond().replaceAll("BROADCAST!","");
        	chat = Chat.broadcastMessage("El todopoderoso", bcast_m , chat);
        }
        else if(res.getSecond().equals("BCASTNOROOT")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes hacer broadcast sin privilegios.", chat, sender);
        }
        else if(res.getSecond().equals("DELNOCREATOR")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes borrar la sala de otro.", chat, sender);
        }
        else if(res.getSecond().equals("DELNOROOM")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes borrar una sala inexistente.", chat, sender);
        }
        else if(res.getSecond().contains("KICK")) {
        	String users = res.getSecond().replaceAll("KICK::", "");
        	String[] us = users.split("::");
        	for(String u : us) {
            	chat = Chat.serverSaysToUser("Server", "Se ha eliminado la sala en la que estabas.", chat, u);
        	}
        }
        else if(res.getSecond().equals("UPDATEUSERS")) {
        	chat = Chat.serverSaysToUser("Server", "Actualizada lista de usuarios.", chat, sender);
        }
        else if(fileUploaded) {
        	 ChatRoom cr = chat.isUserOnRoom(sender);
	       	 if(cr != null) {
	       		 chat = Chat.sendMessageToChannel(sender, modified, cr.getId(), chat);
	       		 cmd.db.insertMsgToDatabase(sender, cr, modified);
	       	 } else {
	       		 chat = Chat.serverSaysToUser("Server", "Primero debes entrar en una sala.", chat, sender);
	       	 }        
       	}
        else if(res.getSecond().contains("DOWNLOAD")) {
        	ChatRoom cr = chat.isUserOnRoom(sender);
        	if(cr != null) {
        		String filename = res.getSecond().split("!")[1];
            	String url = fh.getFile(filename).getFirst();
            	byte buf[] = fh.getFile(filename).getSecond();
            	chat = Chat.downloadFile("Server", url, cr.getId(), chat, sender, buf, filename);
        	} else {
        		chat = Chat.serverSaysToUser("Server", "Primero debes entrar en una sala.", chat, sender);
        	}
        }
        else {
        	chat = Chat.serverSaysToUser("Server", "Comando desconocido.", chat, sender);
        }
    }
    
    public void simulateOnMessage(String sender, String message) throws IOException, TimeoutException, SQLException {
    	// Modify message if file uploaded
    	boolean fileUploaded = false;
    	String modified = "";
    	if(message.contains("!FILE:")) {
    		fileUploaded = true;
    		String filename = message.replaceAll("!FILE:", "");
    		fh.setName(filename);
    		modified = sender + " ha enviado el fichero " + filename + ".\n";
    				modified += "Para recuperarlo, escribe\n";
    				modified += "!GETFILE " + filename;
    	}
 
        Pair<ChatRoomsManager, String> res = cmd.parseMessage(chat, message, sender);
        chat = res.getFirst();
    
        if(res.getSecond().equals("HELP")) {
        	String cmds = cmd.listCommands();
        	chat = Chat.serverSaysToUser("Server", cmds, chat, sender);
        }
        else if(res.getSecond().equals("YAENSALA")) {
        	chat = Chat.serverSaysToUser("Server", "No se puede crear una sala estando en otra.", chat, sender);
        }
        else if(res.getSecond().contains("CREATED")) {
        	chat = Chat.serverSaysToUser("Server", "Sala creada con �xito.", chat, sender);
        } 
        else if(res.getSecond().contains("NOJOIN")) {
        	chat = Chat.serverSaysToUser("Server", "No es posible unirse a esa sala.", chat, sender);
        } 
        else if(res.getSecond().contains("JOINED")) {
        	chat = Chat.serverSaysToUser("Server", "Te has unido a la sala " + chat.isUserOnRoom(sender).getName(), chat, sender);
        	ChatRoom cr = chat.isUserOnRoom(sender);
        	for(String u : cr.getUsers()) {
        		if(!u.equals(sender)) {
        			chat = Chat.serverSaysToUser("Server", sender + " se ha unido a la sala." , chat, u);
        		}
        	}
        	if(res.getSecond().contains(";")) {
        		String[] pendingMsg = res.getSecond().split(";");
        		for (int i = 1; i < pendingMsg.length; i++) {
        			String[] m = pendingMsg[i].split("!");
        			String orig_sender = m[0];
        			String orig_msg = m[1];
                	chat = Chat.recoverMessages(orig_sender, orig_msg, chat, sender, cr);
        		}
        	}
        } 
        else if(res.getSecond().contains("NODSTTOINVITE")) {
        	chat = Chat.serverSaysToUser("Server", "El formato de la invitaci�n no es correcto.", chat, sender);
        }
        else if(res.getSecond().contains("BADINVITE")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes realizar esa invitaci�n.", chat, sender);
        }
        else if(res.getSecond().contains("NOINVINPRIV")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes invitar en una sala privada.", chat, sender);
        }
        else if(res.getSecond().contains("INVITE")) {
        	String dst = res.getSecond().split("!")[1];
        	String id = res.getSecond().split("!")[2];
        	chat = Chat.serverSaysToUser("Server", sender + " te ha invitado a la sala " + id + ".", chat, dst);
        	chat = Chat.serverSaysToUser("Server", "Has invitado a " + dst + " a la sala " + id + ".", chat, sender);
        }
        else if(res.getSecond().contains("NODSTTOKICK")) {
        	chat = Chat.serverSaysToUser("Server", "El formato de la expulsi�n no es correcto.", chat, sender);
        }
        else if(res.getSecond().contains("BADKICK")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes realizar esa expulsi�n.", chat, sender);
        }
        else if(res.getSecond().contains("KICKONPURPOSE")) {
        	String dst = res.getSecond().split("!")[1];
        	String id = res.getSecond().split("!")[2];
        	chat = Chat.serverSaysToUser("Server", sender + " te ha expulsado de la sala " + id + ".", chat, dst);
        	chat = Chat.serverSaysToUser("Server", "Has expulsado a " + dst + " de la sala " + id + ".", chat, sender);
        }
        else if(res.getSecond().contains("SENDMSGTOROOM")) {
        	 ChatRoom cr = chat.isUserOnRoom(sender);
        	 String m = res.getSecond();
        	 if(res.getSecond().contains("CLEAR::")) {
        		 chat = Chat.serverSaysToUser("Server", "CLEAR CHATS", chat, sender);
        		 m = m.replaceAll("CLEAR::", "");
        	 }
        	 if(cr != null) {
        		 chat = Chat.sendMessageToChannel(sender, m.split("!")[1], cr.getId(), chat);
        	 } else {
        		 chat = Chat.serverSaysToUser("Server", "Primero debes entrar en una sala.", chat, sender);
        	 }
        } 
        else if(res.getSecond().contains("SENDMSG")) {
        	String dest = res.getSecond().split("!")[1];
        	message = message.split(" ")[2];
        	chat = Chat.userSaysToUser(sender, message, chat, dest);
        }
        else if (res.getSecond().contains("CHATROOMS")){
        	String crs = res.getSecond().split("!")[1];
        	String[] salas = crs.split(";");
        	for(String s : salas) {
        		chat = Chat.serverSaysToUser("Server", s, chat, sender);
        	}
        } 
        else if(res.getSecond().equals("LEAVINGROOM")) { 
        	ChatRoom cr = chat.isUserOnRoom(sender);
        	if(cr != null) {
				chat.leaveRoom(sender);
        		chat = Chat.serverSaysToUser("Server", "Abandonando sala " + cr.getName() + ".", chat, sender);
        		if(cr.getUsers().size() > 0) {
        			for(String u : cr.getUsers()) {
        				chat = Chat.serverSaysToUser("Server", sender + " ha abandonado la sala.", chat, u);
        			}
        		}
        		// Get unread messages
                List<String> pending = cmd.db.checkRoomsWithMessages(sender);
                for(String s : pending) {
                	chat = Chat.serverSaysToUser("Server", "Tienes mensajes disponibles en la sala de ID = " + s + ".", chat, sender);
                }
        	} else {
        		chat = Chat.serverSaysToUser("Server", "Solo se puede abandonar una sala si est�s dentro de ella.", chat, sender);
        	}
        } 
        else if(res.getSecond().equals("BADARG")) {
        	chat = Chat.serverSaysToUser("Server", "El mensaje no es correcto.", chat, sender);
        }
        else if(res.getSecond().equals("CLEAR")) {
    		chat = Chat.serverSaysToUser("Server", "CLEAR CHATS", chat, sender);
        } 
        else if(res.getSecond().equals("OKROOT")) {
        	chat = Chat.serverSaysToUser("Server", "Ahora eres todopoderoso.", chat, sender);
        }
        else if(res.getSecond().equals("BADROOT")) {
        	chat = Chat.serverSaysToUser("Server", "No tienes permisos. Se informar� de esto.", chat, sender);
        }
        else if(res.getSecond().equals("ROOTNOCHANGE")) {
        	chat = Chat.serverSaysToUser("Server", "El todopoderoso no debe cambiar de nombre.", chat, sender);
        }
        else if(res.getSecond().contains("BROADCAST!")) {
        	String bcast_m = res.getSecond().replaceAll("BROADCAST!","");
        	chat = Chat.broadcastMessage("El todopoderoso", bcast_m , chat);
        }
        else if(res.getSecond().equals("BCASTNOROOT")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes hacer broadcast sin privilegios.", chat, sender);
        }
        else if(res.getSecond().equals("DELNOCREATOR")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes borrar la sala de otro.", chat, sender);
        }
        else if(res.getSecond().equals("DELNOROOM")) {
        	chat = Chat.serverSaysToUser("Server", "No puedes borrar una sala inexistente.", chat, sender);
        }
        else if(res.getSecond().contains("KICK")) {
        	String users = res.getSecond().replaceAll("KICK::", "");
        	String[] us = users.split("::");
        	for(String u : us) {
            	chat = Chat.serverSaysToUser("Server", "Se ha eliminado la sala en la que estabas.", chat, u);
        	}
        }
        else if(res.getSecond().equals("UPDATEUSERS")) {
        	chat = Chat.serverSaysToUser("Server", "Actualizada lista de usuarios.", chat, sender);
        }
        else if(fileUploaded) {
        	 ChatRoom cr = chat.isUserOnRoom(sender);
	       	 if(cr != null) {
	       		 chat = Chat.sendMessageToChannel(sender, modified, cr.getId(), chat);
	       		 cmd.db.insertMsgToDatabase(sender, cr, modified);
	       	 } else {
	       		 chat = Chat.serverSaysToUser("Server", "Primero debes entrar en una sala.", chat, sender);
	       	 }        
       	}
        else if(res.getSecond().contains("DOWNLOAD")) {
        	ChatRoom cr = chat.isUserOnRoom(sender);
        	if(cr != null) {
        		String filename = res.getSecond().split("!")[1];
            	String url = fh.getFile(filename).getFirst();
            	byte buf[] = fh.getFile(filename).getSecond();
            	chat = Chat.downloadFile("Server", url, cr.getId(), chat, sender, buf, filename);
        	} else {
        		chat = Chat.serverSaysToUser("Server", "Primero debes entrar en una sala.", chat, sender);
        	}
        }
        else {
        	chat = Chat.serverSaysToUser("Server", "Comando desconocido.", chat, sender);
        }
    }


}
