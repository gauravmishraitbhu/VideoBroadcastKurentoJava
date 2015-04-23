package com.tivamo.broadcast.kurento.stream;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.kurento.client.MediaPipeline;
import org.kurento.client.RtpEndpoint;
import org.kurento.client.WebRtcEndpoint;

import com.google.gson.JsonObject;
import com.tivamo.broadcast.kurento.session.RelaySession;
import com.tivamo.broadcast.kurento.session.UserSession;

/**
 *  This intance will be created on edge server (KMS). masterUserSession in this class will be RtpEndpoint.
 *  Rest of viewers will be connected to RtpEndpoint.
 * @author gaurav.
 *
 */
public class RelayStream extends MediaStream{

	public RelayStream(String roomName, MediaPipeline pipeline) {
		super(roomName, pipeline);
	}

	public RtpEndpoint getRtpSourceEndPoint() {
		RelaySession relaySession = (RelaySession) masterUserSession;
		return relaySession.getRtpEndpoint();
	}

	@Override
	public void addMaster(Session session,JsonObject jsonMessage) throws IOException{
		masterUserSession = new RelaySession(session);

		RelaySession relaySession = (RelaySession) masterUserSession;

		relaySession.setRtpEndpoint(new RtpEndpoint.Builder(pipeline).build());

	}

	@Override
	public void addViewer(Session session,JsonObject jsonMessage) throws IOException{

		if(masterUserSession != null){
			
			UserSession viewer = new UserSession(session);
			viewers.put(session.toString(), viewer);
			RelaySession relaySession = (RelaySession) masterUserSession;
			//get sdp offer from client
			String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer")
					.getAsString();

			//create a webrtc endpoint for this viewer
			WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.Builder(pipeline)
			.build();
			viewer.setWebRtcEndpoint(nextWebRtc);

			//connect the broadcaster endpoint to this viewer's webrtc endpoint
			relaySession.getRtpEndpoint().connect(nextWebRtc);


			//get sdp answer from viewers webrtc endpoint
			String sdpAnswer = nextWebRtc.processOffer(sdpOffer);

			JsonObject response = new JsonObject();
			response.addProperty("id", "viewerResponse");
			response.addProperty("response", "accepted");
			response.addProperty("sdpAnswer", sdpAnswer);
			viewer.sendMessage(response);
		}
		else{
			JsonObject response = new JsonObject();
			response.addProperty("id", "viewerResponse");
			response.addProperty("response", "rejected");
			response.addProperty("message", "relay is not created");
			session.getRemote().sendString(response.toString());
		}


	}
}
