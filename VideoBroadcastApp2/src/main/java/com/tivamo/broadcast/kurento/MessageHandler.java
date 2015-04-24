package com.tivamo.broadcast.kurento;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class MessageHandler {

	private static final Logger log = LoggerFactory
			.getLogger(MessageHandler.class);
	private static final Gson gson = new GsonBuilder().create();





	private static MessageHandler _instance = null;


	public static MessageHandler getInstance() {
		if(_instance == null){
			_instance = new MessageHandler();
		}

		return _instance;
	}


	public void handleTextMessage(Session session, String message)
			throws Exception {
		
		TextMessage msg = new TextMessage(message);
		JsonObject jsonMessage = gson.fromJson(msg.getPayload(),
				JsonObject.class);
		
		log.error(session.toString());
		log.debug("Incoming message from session '{}': {}", session.toString() ,
				jsonMessage);

		JsonPrimitive room = jsonMessage.getAsJsonPrimitive("room");
		String roomName;
		if(room == null){
			roomName = null;
		}else{
			roomName = jsonMessage.getAsJsonPrimitive("room").getAsString();
		}

		if(roomName == null || roomName ==""){

			log.error("****check room name in incoming message got null.");
		}


		switch (jsonMessage.get("id").getAsString()) {
		case "master":
			if(roomName != null){
				master(session, jsonMessage);
			}

			break;
			//		case "play":
			//			PlayHandler player = new PlayHandler(kurento);
			//			player.handleTextMessage(session, message);
			//			break;
		case "viewer":
			if(roomName != null){
				viewer(session, jsonMessage);
			}
			break;
		case "stop":
			stop(session ,roomName);
			break;
		default:
			break;
		}
	}

	private synchronized void master(Session session,
			JsonObject jsonMessage) throws IOException {
		
		KurentoClientInstance kurentoInstance = KurentoManager.getInstance().getInstanceForPublish();
		kurentoInstance.addBroadcaster(session, jsonMessage);
	}

	private synchronized void viewer(Session session,
			JsonObject jsonMessage) throws Exception {
		KurentoClientInstance kurentoInstance = KurentoManager.getInstance().getInstanceForConsume();
		kurentoInstance.addViewer(session, jsonMessage);
	}

	private synchronized void stop(Session session , String roomName ) throws IOException {

		KurentoClientInstance kurentoInstance = KurentoManager.getInstance().getInstanceForConsume();
		kurentoInstance.stopSession(session, roomName);

	}

	public void afterConnectionClosed(Session session,
			int status) throws Exception {
		stop(session , null);
	}

}
