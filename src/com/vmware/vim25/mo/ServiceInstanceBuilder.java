package com.vmware.vim25.mo;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * This class builds ServiceInstances using the builder pattern
 * @author Michael Matczynski (mjm@alum.mit.edu)
 */
public class ServiceInstanceBuilder
{
	private URL url;
	
	// either username/password or sessionStr may be simultaneously
	private String username;
	private String password;
	
	private String sessionStr;
	
	private Boolean ignoreCert;
	private String namespace;
	
	private Integer connectTimeoutMillis;
	private Integer readTimeoutMillis;
	
	public ServiceInstanceBuilder(URL url, String username, String password)
	{
		this.url = url;
		this.username = username;
		this.password = password;
	}
	
	public ServiceInstanceBuilder(URL url, String sessionStr)
	{
		this.url = url;
		this.sessionStr = sessionStr;
	}
	
	public ServiceInstanceBuilder ignoreCert(boolean ignoreCert)
	{
		this.ignoreCert = ignoreCert;
		return this;
	}
	
	public ServiceInstanceBuilder namespace(String namespace)
	{
		this.namespace = namespace;
		return this;
	}
	
	public ServiceInstanceBuilder readTimeout(int timeoutMilliSec)
	{
		this.readTimeoutMillis = timeoutMilliSec;
		return this;
	}
	
	public ServiceInstanceBuilder connectTimeout(int timeoutMilliSec)
	{
		this.connectTimeoutMillis = timeoutMilliSec;
		return this;
	}
	
	public ServiceInstance build() throws RemoteException, MalformedURLException
	{
		// apply defaults
		if (ignoreCert == null) {
			ignoreCert = false;
		}
		if (namespace == null) {
			namespace = ServiceInstance.VIM25_NAMESPACE;
		}
		
		// create ServiceInstance using either session String or username/password
		if (sessionStr != null) {
			return new ServiceInstance(url, sessionStr, ignoreCert, namespace, connectTimeoutMillis, readTimeoutMillis);			
		} else {
			return new ServiceInstance(url, username, password, ignoreCert, namespace, connectTimeoutMillis, readTimeoutMillis);
		}
	}
}
