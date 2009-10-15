package org.mobicents.tools.sip.balancer;

import java.util.Properties;

import javax.sip.message.Request;
import javax.sip.message.Response;

public abstract class DefaultBalancerAlgorithm implements BalancerAlgorithm {
	protected Properties properties;

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public BalancerContext getBalancerContext() {
		return SIPBalancerForwarder.balancerContext;
	}

	public Properties getProperties() {
		return properties;
	}
	
	public void processResponse(Response response) {
		
	}
	
	public void jvmRouteSwitchover(String fromJvmRoute, String toJvmRoute) {
		
	}

	public abstract void nodeAdded(SIPNode node);
	public abstract void nodeRemoved(SIPNode node);
	public abstract SIPNode processRequest(Request request);
	public abstract void init();
	public abstract void stop();

}