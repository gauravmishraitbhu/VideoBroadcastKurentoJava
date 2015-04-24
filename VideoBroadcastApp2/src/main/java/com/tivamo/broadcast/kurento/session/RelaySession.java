package com.tivamo.broadcast.kurento.session;

import org.eclipse.jetty.websocket.api.Session;
import org.kurento.client.RtpEndpoint;

/** this session will wrap RtpEndpoint which will act source for Pipeline create on edge server.
 * other end of this session will be a RtpEndpont (sink) of some other pipeline residing on diff KMS.
 * @author gaurav.
 *
 */
public class RelaySession extends UserSession{

	private RtpEndpoint rtpEndpoint;
	
	public RelaySession(Session session) {
		super(session);
	}
	
	public RtpEndpoint getRtpEndpoint() {
		return rtpEndpoint;
	}

	public void setRtpEndpoint(RtpEndpoint rtpEndpoint) {
		this.rtpEndpoint = rtpEndpoint;
	}

	

}
