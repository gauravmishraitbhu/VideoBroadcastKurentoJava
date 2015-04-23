package com.tivamo.broadcast.kurento;

public class KurentoManager {

	private static KurentoManager _instance = null;
	
	private KurentoClientInstance kurento1;
	
	static {
		KurentoManager.getInstance();
	}
	
	private KurentoManager () {
		kurento1 = new KurentoClientInstance("ws://192.168.0.114:8888/kurento");
	}
	
	public static KurentoManager getInstance() {
		if( _instance == null ){
			_instance = new KurentoManager();
		}
		
		return _instance;
	}
	
	public KurentoClientInstance getInstanceForPublish () {
		return kurento1;
	}
	
	public KurentoClientInstance getInstanceForConsume (){
		return kurento1;
	}
}
