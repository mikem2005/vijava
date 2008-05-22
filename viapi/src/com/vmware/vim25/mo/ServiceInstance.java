/*================================================================================
Copyright (c) 2008 VMware, Inc. All Rights Reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, 
this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, 
this list of conditions and the following disclaimer in the documentation 
and/or other materials provided with the distribution.

* Neither the name of VMware, Inc. nor the names of its contributors may be used
to endorse or promote products derived from this software without specific prior 
written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.
================================================================================*/

package com.vmware.vim25.mo;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.util.*;

/**
 * The managed object class corresponding to the one defined in VI SDK API reference.
 * @author Steve JIN (sjin@vmware.com)
 */

public class ServiceInstance extends ManagedObject 
{
	// TODO * add singleton control of each manager attached with this ServiceInstance
	// TODO* one way to verify the property names before it's passed to server!!!
	final static String[] propNames = {"capability", "content", "serverClock" };

	private ServiceContent serviceContent = null;

	final static ManagedObjectReference SERVICE_INSTANCE_MOR;
	
	static 
	{
		SERVICE_INSTANCE_MOR = new ManagedObjectReference();
		SERVICE_INSTANCE_MOR.set_value("ServiceInstance");
		SERVICE_INSTANCE_MOR.setType("ServiceInstance");
	}

	public ServiceInstance(URL url, String username, String password) 
	{
		this(url, username, password, false);
	}

	public ServiceInstance(URL url, String username, String password, boolean ignoreCert)
	{
		if(url == null || username==null)
		{
			throw new NullPointerException("None of url, username can be null.");
		}
		
		if(ignoreCert==true)
		{
			this.ignoreCertificate();
		}
		
		setMOR(SERVICE_INSTANCE_MOR);
		
		VimServiceLocator serviceLocator = new VimServiceLocator();
		serviceLocator.setMaintainSession(true);

		try
		{
			VimPortType vimService = serviceLocator.getVimPort(url);
			((org.apache.axis.client.Stub)vimService).setTimeout(1200000); //optional

			((VimBindingStub) vimService).setMaintainSession(true);
		
			serviceContent = vimService.retrieveServiceContent(SERVICE_INSTANCE_MOR);
			setServerConnection(new ServerConnection(url, vimService, this));
			UserSession userSession = getSessionManager().login(username, password, null);
			getServerConnection().setUserSession(userSession);
		}
		catch (Exception e)
		{
			System.err.println("Exception: " + e.getMessage() );
		}
	}
	
	// sessionStr format: "vmware_soap_session=\"B3240D15-34DF-4BB8-B902-A844FDF42E85\""
	public ServiceInstance(URL url, String sessionStr, boolean ignoreCert)
	{
		if(url == null || sessionStr ==null)
		{
			throw new NullPointerException("None of url, session string can be null.");
		}
		
		if(ignoreCert==true)
		{
			this.ignoreCertificate();
		}

		setMOR(SERVICE_INSTANCE_MOR);
		
		VimServiceLocator serviceLocator = new VimServiceLocator();
		serviceLocator.setMaintainSession(true);
		
		try
		{
			VimPortType vimService = serviceLocator.getVimPort(url);
			((org.apache.axis.client.Stub)vimService).setTimeout(1200000); //optional
			((VimBindingStub) vimService).setMaintainSession(true);
		
			VimBindingStub vimStub = (VimBindingStub) vimService;
			vimStub._setProperty(org.apache.axis.transport.http.HTTPConstants.HEADER_COOKIE, sessionStr	);
			
			serviceContent = vimService.retrieveServiceContent(SERVICE_INSTANCE_MOR);
			setServerConnection(new ServerConnection(url, vimService, this));
			UserSession userSession = (UserSession) getSessionManager().getCurrentProperty("currentSession");
			getServerConnection().setUserSession(userSession);
		}
		catch (Exception e)
		{
			System.err.println("Exception: " + e.getMessage() );
		}
	}
	
	private void ignoreCertificate() 
	{
		System.setProperty("org.apache.axis.components.net.SecureSocketFactory",
			"org.apache.axis.components.net.SunFakeTrustSocketFactory");
	}
	
	
	public ServiceInstance(ServerConnection sc) 
	{
		super(sc, SERVICE_INSTANCE_MOR);
	}

	public Calendar getServerClock()
	{
		return (Calendar) getCurrentProperty("serverClock");
	}
	
	public Capability getCapability()
	{
		return (Capability) getCurrentProperty("capability");
	}
	
	public Calendar currentTime() throws RuntimeFault, RemoteException 
	{
		return getVimService().currentTime(getMOR());
	}
	

	public Folder getRootFolder() 
	{
		return new Folder(this.getServerConnection(), this.getServiceContent().getRootFolder());
	}
	
	public HostVMotionCompatibility[] queryVMotionCompatibility(VirtualMachine vm, HostSystem[] hosts, String[] compatibility) throws RuntimeFault, RemoteException
	{
		if(vm==null || hosts==null)
		{
			throw new IllegalArgumentException("Neither vm or hosts can be null.");
		}
		return getVimService().queryVMotionCompatibility(getMOR(), vm.getMOR(), MorUtil.createMORs(hosts), compatibility);
	}
	
	public ProductComponentInfo[] retrieveProductComponents() throws RuntimeFault, RemoteException 
	{
		return getVimService().retrieveProductComponents(getMOR());
	}
	
	private ServiceContent retrieveServiceContent() throws RuntimeFault, RemoteException 
	{
		return getVimService().retrieveServiceContent(getMOR());
	}
	
	public Event[] validateMigration(VirtualMachine[] vms, VirtualMachinePowerState state, String[] testType
			, ResourcePool pool, HostSystem host) throws InvalidState, RuntimeFault, RemoteException 
	{
		if(vms==null)
		{
			throw new IllegalArgumentException("vms must not be null.");
		}
		
		return getVimService().validateMigration(getMOR(), MorUtil.createMORs(vms), state, testType, 
				pool==null? null: pool.getMOR(), host==null? null : host.getMOR());
	}
	
	public ServiceContent getServiceContent()
	{
		if(serviceContent == null)
		{
			try
			{
				serviceContent = retrieveServiceContent();
			} catch(Exception e)
			{
				System.out.println("Exceptoin: " + e);
			}
		}
		return serviceContent;
	}
	
	public AboutInfo getAboutInfo() 
	{
		return getServiceContent().getAbout();
	}
	
	public AlarmManager getAlarmManager()
	{
		return (AlarmManager) createMO(getServiceContent().getAlarmManager());
	}
	
	public AuthorizationManager getAuthorizationManager()
	{
		return (AuthorizationManager) createMO(getServiceContent().getAuthorizationManager());
	}

	public CustomFieldsManager getCustomFieldsManager()
	{
		return (CustomFieldsManager) createMO(getServiceContent().getCustomFieldsManager());
	}
	
	public CustomizationSpecManager getCustomizationSpecManager()
	{
		return (CustomizationSpecManager) createMO(getServiceContent().getCustomizationSpecManager());
	}
	
	public EventManager getEventManager()
	{
		return (EventManager) createMO(getServiceContent().getEventManager());
	}
	
	public DiagnosticManager getDiagnosticManager()
	{
		return (DiagnosticManager) createMO(getServiceContent().getDiagnosticManager());
	}
	
	public ExtensionManager getExtensionManager()
	{
		return (ExtensionManager) createMO(getServiceContent().getExtensionManager());
	}
	
	public FileManager getFileManager()
	{
		return (FileManager) createMO(getServiceContent().getFileManager());
	}
	
	public HostLocalAccountManager getAccountManager()
	{
		return (HostLocalAccountManager) createMO(getServiceContent().getAccountManager());
	}
	
	public LicenseManager getLicenseManager()
	{
		return (LicenseManager) createMO(getServiceContent().getLicenseManager());
	}

	public PerformanceManager getPerformanceManager()
	{
		return (PerformanceManager) createMO(getServiceContent().getPerfManager());
	}

	public PropertyCollector getPropertyCollector()
	{
		return (PropertyCollector) createMO(getServiceContent().getPropertyCollector());
	}

	public ScheduledTaskManager getScheduledTaskManager()
	{
		return (ScheduledTaskManager) createMO(getServiceContent().getScheduledTaskManager());
	}

	public SearchIndex getSearchIndex()
	{
		return (SearchIndex) createMO(getServiceContent().getSearchIndex());
	}

	public SessionManager getSessionManager()
	{
		return (SessionManager) createMO(getServiceContent().getSessionManager());
	}

	public TaskManager getTaskManager()
	{
		return (TaskManager) createMO(getServiceContent().getTaskManager());
	}
	
	public UserDirectory getUserDirectory()
	{
		return (UserDirectory) createMO(getServiceContent().getUserDirectory());
	}

	public ViewManager getViewManager()
	{
		return (ViewManager) createMO(getServiceContent().getViewManager());
	}
	
	public VirtualDiskManager getVirtualDiskManager()
	{
		return (VirtualDiskManager) createMO(getServiceContent().getVirtualDiskManager());
	}
	
	public OptionManager getOptionManager()
	{
		return (OptionManager) createMO(getServiceContent().getSetting());
	}
	
	private ManagedObject createMO(ManagedObjectReference mor)
	{
		return MorUtil.createExactManagedObject(getServerConnection(), mor);
	}
	
	// TODO vim.VirtualizationManager is defined in servicecontent but no documentation there. Filed a bug already
	
}
