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
import com.tivamo.broadcast.kurento.stream.MediaStream;
import com.tivamo.broadcast.kurento.stream.RelayStream;

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
	 *  Maps streamName to MediaStream Object.
	 */
	private Map<String , MediaStream> activeStreams = Maps.newHashMap();
	
	
	public KurentoClientInstance(String url) {
		kurento = KurentoClient.create (url);
	}
	
	/** To be called when a user wants to start publishing a stream.
	 * @param session
	 * @param jsonMessage
	 * @throws IOException
	 */
	public void addBroadcaster (Session session,
			JsonObject jsonMessage) throws IOException {
		
		String streamName = jsonMessage.getAsJsonPrimitive("room").getAsString();
		
		if(!activeStreams.containsKey(streamName)){
			
			MediaPipeline pipeline = kurento.createMediaPipeline();
			log.debug("Creating a new room object name=="+streamName);
			MediaStream stream = new MediaStream(streamName, pipeline);
			
			activeStreams.put(streamName, stream);
		}
		
		MediaStream roomObject = activeStreams.get(streamName);

		roomObject.addMaster(session, jsonMessage);
		
		
		//FIXME this should be triggred when a viewer connects to a server
		//on which stream is not being published.
		KurentoManager.getInstance().createRelay(streamName);
	}
	
	
	/** to be called when a user wants to consume stream.
	 * @param session
	 * @param jsonMessage
	 * @throws IOException
	 */
	public void addViewer (Session session ,JsonObject jsonMessage) throws IOException {
		
		String roomName = jsonMessage.getAsJsonPrimitive("room").getAsString();
		MediaStream roomObject = activeStreams.get(roomName);

		roomObject.addViewer(session, jsonMessage);
	}
	
	/** Either clicked on stop or browser refresh. 
	 * disconnect and cleanup the current client and if session is 
	 * broadcaster then disconect all viewers
	 * @param session
	 * @param roomName
	 */
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
	
	/** Returns true if stream is being published on current KMS.
	 * @param streamName 
	 * @return
	 */
	public boolean isOriginForStream(String streamName) {
		for (MediaStream stream : activeStreams.values()){
			if(!stream.isRelayStream()){
				if(stream.getStreamName().equalsIgnoreCase(streamName)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	/** Creates a Stream of type RelayStream and add to active stream list.
	 * this will be used on KurentoClients which are serving as edge servers.
	 * @param streamName 
	 * @return
	 * @throws Exception 
	 */
	public RelayStream createRelayStream (String streamName) throws Exception {
		
		if(isOriginForStream(streamName)){
			
			throw new Exception("Same server cannot contain MediaStream as well as relay stream");
		}
		
		MediaPipeline pipeline = kurento.createMediaPipeline();
				
		RelayStream stream = new RelayStream(streamName, pipeline);
		try {
			stream.addMaster(null, null);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		activeStreams.put(streamName, stream);
		
		return stream;
	}
	
	/** Returns the media stream instance for given streamName.
	 * this is applicable on on origin server.
	 * @param streamName -- name of stream.
	 * @return
	 */
	public MediaStream getMediaStream (String streamName){
		
		
		//if this client is not origin for given stream then we wont find MediaStream object
		//on this client.
		if(!isOriginForStream(streamName)){
			 return null;
		}
		
		for (String key : activeStreams.keySet()){
			MediaStream stream = activeStreams.get(key);
			
			if(!stream.isRelayStream() && key.equalsIgnoreCase(streamName)){
				return stream;
			}
		}
		
		return null;
	}
	
}
