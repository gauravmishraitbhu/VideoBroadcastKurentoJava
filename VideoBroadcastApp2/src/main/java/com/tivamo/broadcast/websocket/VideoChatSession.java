package com.tivamo.broadcast.websocket;

import org.eclipse.jetty.websocket.api.Session;

/** wrapper of Session Object.
 * @author gaurav
 *
 */
public class VideoChatSession {
	
	/**
	 * on the entry point of our app this object will be wrapped in 
	 * VideoChatSession.
	 */
	private Session session;
	
	
	/**
	 * Unique Identifier for every session object.
	 */
	private String id;
}
