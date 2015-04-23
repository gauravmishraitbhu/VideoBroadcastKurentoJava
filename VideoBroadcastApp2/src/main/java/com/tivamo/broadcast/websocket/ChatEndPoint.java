package com.tivamo.broadcast.websocket;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tivamo.broadcast.kurento.MessageHandler;

@WebSocket
public class ChatEndPoint {

	private static final Logger log = LoggerFactory
			.getLogger(ChatEndPoint.class);


	@OnWebSocketMessage
	public void onMessage(Session session, String s)  {
		//        session.getRemote().sendString("Returned; "+s);

		MessageHandler handler = MessageHandler.getInstance();
		try{
			handler.handleTextMessage(session, s);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		
	}

	@OnWebSocketClose
	public void onDisconnect(Session session , int status , String reason){
		MessageHandler handler = MessageHandler.getInstance();

		try {
			handler.afterConnectionClosed(session, status);
		}catch(Exception e){
			log.error(e.getMessage());
		}
	}

}
