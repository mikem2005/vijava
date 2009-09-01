/*================================================================================
Copyright (c) 2009 VMware, Inc. All Rights Reserved.

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

import com.vmware.vim25.*;
import com.vmware.vim25.ws.IPropertyCollector;

public class VimManagedObject extends ManagedObject {

   protected VimManagedObject()
   {
      super();
   }

   /**
    * Constructor that reuse exiting web service connection
    * Use this contructor when you can re-use existing web service connection.
    * @param serverConnection
    * @param mor
    */
   public VimManagedObject(ServerConnection serverConnection, ManagedObjectReference mor)
   {
      super(serverConnection, mor);
   }

   /**
    * Get the web service
    * @return
    */
   protected VimPortType getVimService()
   {
      return getServerConnection().getVimService();
   }

   protected Datastore[] getDatastores(String propName)
   {
      Object[] objs = getManagedObjects(propName);
      if(objs.length == 0)
      {
         return new Datastore[] {};
      }
      return (Datastore[]) objs;
   }

   protected Network[] getNetworks(String propName)
   {
      Object[] objs = getManagedObjects(propName, true);
      if(objs.length == 0)
      {
         return new Network[] {};
      }
      Network[] nets = new Network[objs.length];
      for(int i=0; i< objs.length; i++)
      {
         nets[i] = (Network) objs[i];
      }
      return nets;
   }

   protected VirtualMachine[] getVms(String propName)
   {
      ManagedObject[] objs = getManagedObjects(propName);
      if(objs.length == 0)
      {
         return new VirtualMachine[] {};
      }
      return (VirtualMachine[]) objs;
   }

   protected PropertyFilter[] getFilter(String propName)
   {
      Object[] objs = getManagedObjects(propName);
      if(objs.length == 0)
      {
         return new PropertyFilter[] {};
      }
      return (PropertyFilter[]) objs;
   }

   protected ResourcePool[] getResourcePools(String propName)
   {
      Object[] objs = getManagedObjects(propName, true);
      ResourcePool[] rps = new ResourcePool[objs.length];
      for(int i=0; i < rps.length; i++)
      {
        rps[i] = (ResourcePool) objs[i];
      }
      return rps;
   }

   protected Task[] getTasks(String propName)
   {
      Object[] objs = getManagedObjects(propName);
      if(objs.length == 0)
      {
         return new Task[] {};
      }
      return (Task[]) objs;
   }

   protected ScheduledTask[] getScheduledTasks(String propName)
   {
      Object[] objs = getManagedObjects(propName);
      if(objs.length == 0)
      {
         return new ScheduledTask[] {};
      }
      return (ScheduledTask[]) objs;
   }

   protected View[] getViews(String propName)
   {
      Object[] objs = getManagedObjects(propName);
      if(objs.length == 0)
      {
         return new View[] {};
      }
      return (View[]) objs;
   }

   protected HostSystem[] getHosts(String propName)
   {
      Object[] objs = getManagedObjects(propName);
      if(objs.length == 0)
      {
         return new HostSystem[] {};
      }
      return (HostSystem[]) objs;
   }

   @Override
   public ServerConnection getServerConnection() {
      return (ServerConnection) super.getServerConnection();
   }

   @Override
   public IPropertyCollector getPropertyCollector() {
      return getServerConnection().getServiceInstance().getPropertyCollector();
   }
}