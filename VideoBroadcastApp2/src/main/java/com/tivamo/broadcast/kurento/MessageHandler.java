package com.tivamo.broadcast.kurento;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class MessageHandler {
	
	private static final Logger log = LoggerFactory
			.getLogger(MessageHandler.class);
	private static final Gson gson = new GsonBuilder().create();

	
	private Map<String , BroadcastRoom> activeRooms = Maps.newHashMap();
	
	
	private KurentoClient kurento;

	private static MessageHandler _instance = null;
	
	static {
		MessageHandler.getInstance();
	}
	
	public static MessageHandler getInstance() {
		if(_instance == null){
			_instance = new MessageHandler();
		}
		
		return _instance;
	}
	
	private MessageHandler() {
		kurento = KurentoClient.create ("ws://192.168.0.114:8888/kurento");
	}
	
	public void handleTextMessage(Session session, String message)
			throws Exception {
		
		TextMessage msg = new TextMessage(message);
		JsonObject jsonMessage = gson.fromJson(msg.getPayload(),
				JsonObject.class);
		log.debug("Incoming message from session '{}': {}", session.getRemote().toString() ,
				jsonMessage);
		
		
		String roomName = jsonMessage.getAsJsonPrimitive("room").getAsString();
		if(roomName == null || roomName ==""){
			
			log.error("****check room name in incoming message got null.");
		}
		
		if(!activeRooms.containsKey(roomName)){
			//create a new pipeline.
			//create a room object
			MediaPipeline roomPipeline = kurento.createMediaPipeline();
			log.debug("Creating a new room object name=="+roomName);
			BroadcastRoom roomObject = new BroadcastRoom(roomName , roomPipeline);
			activeRooms.put(roomName, roomObject);
			
		}
		
		switch (jsonMessage.get("id").getAsString()) {
		case "master":
				master(session, jsonMessage);
			
			break;
//		case "play":
//			PlayHandler player = new PlayHandler(kurento);
//			player.handleTextMessage(session, message);
//			break;
		case "viewer":
				viewer(session, jsonMessage);
			break;
		case "stop":
			//stop(session ,roomName);
			break;
		default:
			break;
		}
	}

	private synchronized void master(Session session,
			JsonObject jsonMessage) throws IOException {
		
		String roomName = jsonMessage.getAsJsonPrimitive("room").getAsString();
		BroadcastRoom roomObject = activeRooms.get(roomName);
		
		roomObject.addBroadcaster(session, jsonMessage);
	}

	private synchronized void viewer(Session session,
			JsonObject jsonMessage) throws IOException {
		String roomName = jsonMessage.getAsJsonPrimitive("room").getAsString();
		BroadcastRoom roomObject = activeRooms.get(roomName);
		
		roomObject.addViewer(session, jsonMessage);
	}

	private synchronized void stop(WebSocketSession session , String roomName ) throws IOException {
		
		if(roomName != null){
			activeRooms.get(roomName).stopClient(session);
		}else{
			//try in all rooms
			for (BroadcastRoom room : activeRooms.values()){
				room.stopClient(session);
			}
		}
		
		
	}

	public void afterConnectionClosed(WebSocketSession session,
			CloseStatus status) throws Exception {
		stop(session , null);
	}
	
}
