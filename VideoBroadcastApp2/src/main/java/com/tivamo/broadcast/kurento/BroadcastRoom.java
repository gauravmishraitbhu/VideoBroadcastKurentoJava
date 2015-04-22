package com.tivamo.broadcast.kurento;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.kurento.client.MediaPipeline;
import org.kurento.client.RtpEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

public class BroadcastRoom {

	private static final Logger log = LoggerFactory
			.getLogger(BroadcastRoom.class);

	/**
	 *  name of stream ie myStream.
	 */
	private String roomName;

	/**
	 * pipeline to which this broadcast room belongs.
	 */
	private  MediaPipeline pipeline;

	/**
	 * broadcaster
	 */
	private UserSession masterUserSession;

	/**
	 * viewers conected to current room.
	 */
	private Map<String,UserSession> viewers = Maps.newHashMap();


	public BroadcastRoom(String roomName , MediaPipeline pipeline){
		this.roomName = roomName;
		this.pipeline = pipeline;
	}


	public void addViewer(Session session,JsonObject jsonMessage) throws IOException{
		try{

			if(masterUserSession!=null){
				//create a user session.
				UserSession viewer = new UserSession(session);
				viewers.put(session.getRemote().toString(), viewer);

				//get sdp offer from client
				String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer")
						.getAsString();

				//create a webrtc endpoint for this viewer
				WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.Builder(pipeline)
				.build();
				viewer.setWebRtcEndpoint(nextWebRtc);

				//connect the broadcaster endpoint to this viewer's webrtc endpoint
				masterUserSession.getWebRtcEndpoint().connect(nextWebRtc);


				//get sdp answer from viewers webrtc endpoint
				String sdpAnswer = nextWebRtc.processOffer(sdpOffer);

				JsonObject response = new JsonObject();
				response.addProperty("id", "viewerResponse");
				response.addProperty("response", "accepted");
				response.addProperty("sdpAnswer", sdpAnswer);
				viewer.sendMessage(response);
			}else{
				JsonObject response = new JsonObject();
				response.addProperty("id", "viewerResponse");
				response.addProperty("response", "rejected");
				response.addProperty("message", "broadcaster is not live yet");
				session.getRemote().sendString(response.toString());
			}
		}catch(IOException e){
			log.error(e.getMessage(), e);
			viewers.remove(session.getRemote().toString());
			JsonObject response = new JsonObject();
			response.addProperty("id", "viewerResponse");
			response.addProperty("response", "rejected");
			response.addProperty("message", e.getMessage());
			session.getRemote().sendString(response.toString());
		}
	}

	public void addBroadcaster(Session session,JsonObject jsonMessage) throws IOException {

		try{
			if (masterUserSession == null) {

				//create new session obejct
				masterUserSession = new UserSession(session);


				masterUserSession.setWebRtcEndpoint(new WebRtcEndpoint.Builder(
						pipeline).build());

				//create endpoint for broadcaster
				WebRtcEndpoint masterWebRtc = masterUserSession.getWebRtcEndpoint();

				//retrieve sdp offer recieved from client
				String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer")
						.getAsString();

				//create a reply sdp 
				String sdpAnswer = masterWebRtc.processOffer(sdpOffer);
				JsonObject response = new JsonObject();
				response.addProperty("id", "masterResponse");
				response.addProperty("response", "accepted");
				response.addProperty("sdpAnswer", sdpAnswer);
				masterUserSession.sendMessage(response);
				RtpEndpoint end = new RtpEndpoint.Builder(pipeline).build();

			} else {
				JsonObject response = new JsonObject();
				response.addProperty("id", "masterResponse");
				response.addProperty("response", "rejected");
				response.addProperty("message",
						"Another user is currently acting as sender. Try again later ...");
				session.getRemote().sendString(response.toString());
			}
		}catch (IOException e){
			JsonObject response = new JsonObject();
			response.addProperty("id", "masterResponse");
			response.addProperty("response", "rejected");
			response.addProperty("message", e.getMessage());
			session.getRemote().sendString(response.toString());
			masterUserSession = null;

		}
	}

	public void stopClient(WebSocketSession session) {

		//if master is stopping then remove all viewers and inform them.
		if( masterUserSession!=null 
				&& masterUserSession.getSession().getRemote().toString().equalsIgnoreCase(session.getId())){

			log.info("Releasing media pipeline");
			masterUserSession.getWebRtcEndpoint().release();
			masterUserSession = null;
			for (UserSession viewer : viewers.values() ){

				JsonObject response = new JsonObject();
				response.addProperty("id", "stopCommunication");
				try {
					viewer.sendMessage(response);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				viewer.getWebRtcEndpoint().release();
			}
			viewers = Maps.newHashMap();
		}else{
			if(viewers.containsKey(session.getId())){
				viewers.remove(session.getId());
			}
		}
	}
}
