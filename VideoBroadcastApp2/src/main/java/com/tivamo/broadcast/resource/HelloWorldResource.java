package com.tivamo.broadcast.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.tivamo.broadcast.data.TestData;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
	
	
	@GET
    @Timed
    public TestData sayHello() {
        return new TestData("asa");
    }	
}
