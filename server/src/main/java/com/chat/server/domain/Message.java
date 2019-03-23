package com.chat.server.domain;


public class Message {

	private String from;
	private String to;
	private String message;
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "[From=" + from + "] [To=" + to + "] - Body: " + message + "]";
	}
	
	
}