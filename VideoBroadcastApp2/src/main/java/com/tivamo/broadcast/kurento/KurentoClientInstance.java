package com.tivamo.broadcast.kurento;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

/** Wrapper around KurentoClient . keeps track of which stream is being published on KMS which this class represent. 
 * @author gaurav.
 *
 */
public class KurentoClientInstance {
	
	private static final Logger log = LoggerFactory
			.getLogger(KurentoClientInstance.class);
	
	/**
	 * handle for a sinfle KMS server.
	 */
	private KurentoClient kurento;
	
	
	/**
	 *  Live chat rooms which are either active or were active.
	 */
	private Map<String , MediaStream> activeStreams = Maps.newHashMap();
	
	
	public KurentoClientInstance(String url) {
		kurento = KurentoClient.create (url);
	}
	
	public void addBroadcaster (Session session,
			JsonObject jsonMessage) throws IOException {
		
		String roomName = jsonMessage.getAsJsonPrimitive("room").getAsString();
		
		if(!activeStreams.containsKey(roomName)){
			
			MediaPipeline pipeline = kurento.createMediaPipeline();
			log.debug("Creating a new room object name=="+roomName);
			MediaStream stream = new MediaStream(roomName, pipeline);
			
			activeStreams.put(roomName, stream);
		}
		
		MediaStream roomObject = activeStreams.get(roomName);

		roomObject.addBroadcaster(session, jsonMessage);
		
	}
	
	
	public void addViewer (Session session ,JsonObject jsonMessage) throws IOException {
		
		String roomName = jsonMessage.getAsJsonPrimitive("room").getAsString();
		MediaStream roomObject = activeStreams.get(roomName);

		roomObject.addViewer(session, jsonMessage);
	}
	
	public void stopSession(Session session , String roomName) {
		if(roomName != null){
			activeStreams.get(roomName).stopClient(session);
		}else{
			//try in all rooms
			for (MediaStream room : activeStreams.values()){
				room.stopClient(session);
			}
		}
	}
	
	
}
