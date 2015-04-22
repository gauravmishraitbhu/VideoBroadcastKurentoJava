package com.tivamo.broadcast;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;


public class VideoBroadcastAppConfiguration extends Configuration{
	
	//dummy placeholder
	@NotEmpty
    private String template = "hello";

	@JsonProperty
	public String getTemplate() {
		return template;
	}
	
	@JsonProperty
	public void setTemplate(String template) {
		this.template = template;
	}
	
}
