package com.tivamo.broadcast.websocket;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.tivamo.broadcast.kurento.MessageHandler;

@WebSocket
public class ChatEndPoint {

	@OnWebSocketMessage
    public void onMessage(Session session, String s) throws Exception {
//        session.getRemote().sendString("Returned; "+s);
		
		MessageHandler handler = MessageHandler.getInstance();
		handler.handleTextMessage(session, s);
    }
}
