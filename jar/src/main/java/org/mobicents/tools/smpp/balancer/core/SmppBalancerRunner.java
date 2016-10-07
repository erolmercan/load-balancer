/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.tools.smpp.balancer.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.mobicents.tools.sip.balancer.BalancerRunner;
import org.mobicents.tools.smpp.multiplexer.MBalancerDispatcher;
import org.mobicents.tools.smpp.multiplexer.MServer;

import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.ssl.SslConfiguration;

/**
 * @author Konstantin Nosach (kostyantyn.nosach@telestax.com)
 */

public class SmppBalancerRunner {
	
	private MBalancerDispatcher balancerDispatcher;
	private ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
	private ScheduledExecutorService monitorExecutor  = Executors.newScheduledThreadPool(4);
	private MServer smppLbServer;
	private BalancerRunner balancerRunner;

	/**
	 * Start load balancer
	 * @param balancerRunner
	 */
	public SmppBalancerRunner(BalancerRunner balancerRunner)
	{
		this.balancerRunner = balancerRunner;
	}
	public void start()
	{
        SmppServerConfiguration regularConfiguration = new SmppServerConfiguration();
        regularConfiguration.setName("SMPP Load Balancer");
        regularConfiguration.setHost(balancerRunner.balancerContext.lbConfig.getSmppConfiguration().getSmppHost());
        regularConfiguration.setPort(balancerRunner.balancerContext.lbConfig.getSmppConfiguration().getSmppPort());
       	regularConfiguration.setMaxConnectionSize(balancerRunner.balancerContext.lbConfig.getSmppConfiguration().getMaxConnectionSize());
       	regularConfiguration.setNonBlockingSocketsEnabled(balancerRunner.balancerContext.lbConfig.getSmppConfiguration().isNonBlockingSocketsEnabled());
      	regularConfiguration.setDefaultSessionCountersEnabled(balancerRunner.balancerContext.lbConfig.getSmppConfiguration().isDefaultSessionCountersEnabled());
        regularConfiguration.setUseSsl(false);                
        
        SmppServerConfiguration securedConfiguration = null;
        Integer smppSslPort = balancerRunner.balancerContext.lbConfig.getSmppConfiguration().getSmppSslPort();
        if(smppSslPort!=null)
        {
        	securedConfiguration = new SmppServerConfiguration();
        	securedConfiguration.setHost(balancerRunner.balancerContext.lbConfig.getSmppConfiguration().getSmppHost());
	        securedConfiguration.setPort(smppSslPort);
	        securedConfiguration.setMaxConnectionSize(balancerRunner.balancerContext.lbConfig.getSmppConfiguration().getMaxConnectionSize());
	        securedConfiguration.setNonBlockingSocketsEnabled(balancerRunner.balancerContext.lbConfig.getSmppConfiguration().isNonBlockingSocketsEnabled());
	        securedConfiguration.setDefaultSessionCountersEnabled(balancerRunner.balancerContext.lbConfig.getSmppConfiguration().isDefaultSessionCountersEnabled());
	        securedConfiguration.setUseSsl(true);
            SslConfiguration sslConfig = new SslConfiguration();
            sslConfig.setKeyStorePath(balancerRunner.balancerContext.lbConfig.getSslConfiguration().getKeyStore());
	        sslConfig.setKeyStorePassword(balancerRunner.balancerContext.lbConfig.getSslConfiguration().getKeyStorePassword());
	        sslConfig.setTrustStorePath(balancerRunner.balancerContext.lbConfig.getSslConfiguration().getTrustStore());
	        sslConfig.setTrustStorePassword(balancerRunner.balancerContext.lbConfig.getSslConfiguration().getTrustStorePassword());
	        String sProtocols = balancerRunner.balancerContext.lbConfig.getSslConfiguration().getTlsClientProtocols();
	        String sCipherSuites = balancerRunner.balancerContext.lbConfig.getSslConfiguration().getEnabledCipherSuites();
	        if(sProtocols!=null)
	        {
	        	String [] protocols = sProtocols.split(",");
	        	sslConfig.setIncludeProtocols(protocols);
	        }
	        if(sCipherSuites!=null)
	        {
	        	String [] cipherSuites = sCipherSuites.split(",");
	        	sslConfig.setIncludeCipherSuites(cipherSuites);
	        }
	        securedConfiguration.setSslConfiguration(sslConfig);        
        } 
        
        balancerDispatcher = new MBalancerDispatcher(balancerRunner,monitorExecutor);
		smppLbServer = new MServer(regularConfiguration, securedConfiguration, executor, balancerRunner, balancerDispatcher, monitorExecutor);
        smppLbServer.start();
	}
	public void stop()
	{
		smppLbServer.stop();
        executor.shutdown();
        monitorExecutor.shutdown();
	}

	public MBalancerDispatcher getBalancerDispatcher() 
	{
		return balancerDispatcher;
	}
	//Statistic
	/**
     * @return the smppRequestsToServer
     */
	public long getNumberOfSmppRequestsToServer() 
	{
		return balancerRunner.balancerContext.smppRequestsToServer.get();
	}
	/**
     * @return the smppRequestsToClient
     */
	public long getNumberOfSmppRequestsToClient()
	{
		return balancerRunner.balancerContext.smppRequestsToClient.get();
	}
	
	/**
     * @return the smppBytesToServer
     */
	public long getNumberOfSmppBytesToServer()
	{
		return balancerRunner.balancerContext.smppBytesToServer.get();
	}
	
	/**
     * @return the smppBytesToClient
     */
	public long getNumberOfSmppBytesToClient()
	{
		return balancerRunner.balancerContext.smppBytesToClient.get();
	}
	
	/**
     * @return the smppRequestsProcessedById
     */
	public long getSmppRequestsProcessedById(Integer id) 
	{
	        AtomicLong smppRequestsProcessed = balancerRunner.balancerContext.smppRequestsProcessedById.get(id);
	        if(smppRequestsProcessed != null) {
	            return smppRequestsProcessed.get();
	        }
	        return 0;
	}
	
	/**
     * @return the smppResponsesProcessedById
     */
	public long getSmppResponsesProcessedById(Integer id) 
	{
	        AtomicLong smppResponsesProcessed = balancerRunner.balancerContext.smppResponsesProcessedById.get(id);
	        if(smppResponsesProcessed != null) {
	            return smppResponsesProcessed.get();
	        }
	        return 0;
	}
	
	/**
     * @return the NumberOfActiveSmppConnections
     */
	public int getNumberOfActiveSmppConnections()
	{
		//int userSpaces = balancerDispatcher.getUserSpaces().size();
		int customers = 0;
		for(String key : balancerDispatcher.getUserSpaces().keySet())
			customers +=balancerDispatcher.getUserSpaces().get(key).getCustomers().size();
		if(customers==0)
			return customers;
			else
				return customers + 1;
	}

}
