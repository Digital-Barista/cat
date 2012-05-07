package com.digitalbarista.cat.mail;

public class MailboxConfig {

	public enum MailboxType
	{
		IMAP(143),
		POP3(110);
		
		private int portNum;
		MailboxType(int portNum)
		{
			this.portNum=portNum;
		}
		
		public int getPort(){return portNum;}
	}
	
	private String hostName;
	private int port=-1;
	private String folder;
	private String username;
	private String password;
	private MailboxType type;
	private long pollIntervalMillis=60000;
	
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public MailboxType getType() {
		return type;
	}
	public void setType(MailboxType type) {
		this.type = type;
	}
	public long getPollIntervalMillis() {
		return pollIntervalMillis;
	}
	public void setPollIntervalMillis(long pollIntervalMillis) {
		this.pollIntervalMillis = pollIntervalMillis;
	}
	
}
