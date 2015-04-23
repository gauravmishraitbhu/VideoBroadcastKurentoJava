package com.tivamo.broadcast.kurento;

import java.util.List;

import com.tivamo.broadcast.kurento.stream.MediaStream;
import com.tivamo.broadcast.kurento.stream.RelayStream;

import jersey.repackaged.com.google.common.collect.Lists;

/** Manager class to handle the activities like loadbalancing / creating relay etc.
 * @author gaurav.
 *
 */
public class KurentoManager {

	private static KurentoManager _instance = null;

	private List<KurentoClientInstance> kurentoClients = Lists.newArrayList();

	static {
		KurentoManager.getInstance();
	}

	private KurentoManager () {
		kurentoClients.add(new KurentoClientInstance("ws://192.168.0.105:8888/kurento"));
		kurentoClients.add(new KurentoClientInstance("ws://192.168.0.105:8888/kurento"));
	}

	public static KurentoManager getInstance() {
		if( _instance == null ){
			_instance = new KurentoManager();
		}

		return _instance;
	}

	public KurentoClientInstance getInstanceForPublish () {
		return kurentoClients.get(0);
	}

	public KurentoClientInstance getInstanceForConsume(){
		return kurentoClients.get(1);
	}


	/**
	 *  Utility function to create a relay between origin server and edge server.
	 */
	public void createRelay(String streamName) {
		try{
			//get the server(KMS)  on which stream is being published
			KurentoClientInstance origin = getOrigin(streamName);

			//get a edge server.
			KurentoClientInstance edge = getInstanceForConsume();
			
			//create a relay stream on edge
			RelayStream edgeStream = edge.createRelayStream(streamName);

			//get the origin stream object 
			MediaStream originStream = origin.getMediaStream(streamName);

			//sdp negotiations
			String offerSdp = originStream.getRtpSink().generateOffer();
			String edgeOfferSdp = edgeStream.getRtpSourceEndPoint().generateOffer();


			String result = edgeStream.getRtpSourceEndPoint().processAnswer(offerSdp);
			originStream.getRtpSink().processAnswer(result);
			
			
		}catch(Exception e){

		}

	}

	public KurentoClientInstance getOrigin (String streamName) {
		for (KurentoClientInstance instance : kurentoClients ){
			if(instance.isOriginForStream(streamName)){
				return instance;
			}
		}

		return null;
	}
}
