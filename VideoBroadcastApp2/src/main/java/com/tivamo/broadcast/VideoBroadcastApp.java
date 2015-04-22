package com.tivamo.broadcast;

import javax.servlet.ServletRegistration;


import com.tivamo.broadcast.resource.HelloWorldResource;
import com.tivamo.broadcast.websocket.MyWebsocketServlet;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class VideoBroadcastApp extends Application<VideoBroadcastAppConfiguration>{

	@Override
	public void run(VideoBroadcastAppConfiguration conf, Environment enviornment)
			throws Exception {
		
		final HelloWorldResource resource = new HelloWorldResource();
		enviornment.jersey().register(resource);
		
//		AtmosphereServlet servlet = new AtmosphereServlet();
//		servlet.framework().addInitParameter("com.sun.jersey.config.property.packages", "com.tivamo.broadcast.websocket");
//		servlet.framework().addInitParameter(ApplicationConfig.WEBSOCKET_CONTENT_TYPE, "application/json");
//		servlet.framework().addInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT, "true");
//		
//		ServletRegistration.Dynamic servletHolder = enviornment.servlets().addServlet("Chat", servlet);
//		servletHolder.addMapping("/call/*");
		
		
		final ServletRegistration.Dynamic websocket = enviornment.servlets().addServlet(
	            "websocket",
	            new MyWebsocketServlet()
	    );
	    websocket.setAsyncSupported(true);
	    websocket.addMapping("/call/*");
		
	}
	
	 public static void main(String[] args) throws Exception {
	        new VideoBroadcastApp().run(args);
	    }
	
}
