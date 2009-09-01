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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.util.Hashtable;

import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.ObjectUpdate;
import com.vmware.vim25.PropertyChange;
import com.vmware.vim25.PropertyChangeOp;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertyFilterUpdate;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.UpdateSet;
import com.vmware.vim25.mo.util.MorUtil;
import com.vmware.vim25.mo.util.PropertyCollectorUtil;
import com.vmware.vim25.ws.AbstractServerConnection;
import com.vmware.vim25.ws.IPropertyCollector;

/**
 * This class is intended to provide a wrapper around a managed object class.
 * The abstraction will hide the web service details and make the managed
 * objects OO style in the client side programming.
 * Every managed object class can inherit from this and take advantage of this
 * abstraction.
 * @author Steve JIN (sjin@vmware.com)
 */
abstract public class ManagedObject
{
	private String moPackageName = null;

	/** holds the ServerConnection instance */
	private AbstractServerConnection serverConnection = null;
	/** holds the ExtensionManager managed object reference */
	private ManagedObjectReference mor =  null;

	protected ManagedObject()
	{
	   moPackageName = this.getClass().getPackage().getName();
	}

	/**
	 * Constructor that reuse exiting web service connection
	 * Use this contructor when you can re-use existing web service connection.
	 * @param serverConnection
	 * @param mor
	 */
	public ManagedObject(AbstractServerConnection serverConnection, ManagedObjectReference mor)
	{
	   this();
		this.serverConnection = serverConnection;
		this.mor = mor;
	}

	/**
	 * Set the ManagedObjectReference object pointing to the managed object
	 */
	protected void setMOR(ManagedObjectReference mor)
	{
		this.mor = mor;
	}

	/**
	 * get the ManagedObjectReference object pointing to the managed object
	 * @return
	 */
	public ManagedObjectReference getMOR()
	{
		return this.mor;
	}

	public AbstractServerConnection getServerConnection()
	{
		return serverConnection;
	}

	/**
	 * Set up the ServerConnection, only when it hasn't been set yet.
	 * @param sc
	 */
	protected void setServerConnection(AbstractServerConnection sc)
	{
		if(this.serverConnection==null)
		{
			this.serverConnection = sc;
		}
	}

   /**
    * Get the name of the package this ManagedObject is located in.
    *
    * @return Package name
    */
	public String getMoPackageName() {
	   return moPackageName;
	}

   /**
    * Set the name of the package this ManagedObject is located in.
    * <p />
    * This value is initialized by the Constructor, and should not be modified
    * in most cases.
    *
    * @param packageName
    *           package name
    */
	public void setMoPackageName(String packageName) {
	   moPackageName = packageName;
	}

	/**
    * @param propertyName The property name of current managed object
    * @return it will return either an array of related data objects, or an data object itself.
    * ManagedObjectReference objects are data objects!!!
    * @throws RemoteException
    * @throws RuntimeFault
    * @throws InvalidProperty
    * @
    */
   protected Object getCurrentProperty(String propertyName)
   {
      ObjectContent objContent = retrieveObjectProperties(new String[] { propertyName });

      Object propertyValue = null;

      if (objContent != null)
      {
         DynamicProperty[] dynaProps = objContent.getPropSet();

         if ((dynaProps != null) && (dynaProps[0]!= null))
         {
            propertyValue = PropertyCollectorUtil.convertProperty(dynaProps[0].getVal());
         }
      }
      return propertyValue;
   }

   protected ObjectContent retrieveObjectProperties(String[] properties)
   {
      ObjectSpec oSpec = PropertyCollectorUtil.creatObjectSpec(
            getMOR(), Boolean.FALSE, null);

      PropertySpec pSpec = PropertyCollectorUtil.createPropertySpec(
            getMOR().getType(),
            properties == null || properties.length == 0, //if true, all props of this obj are to be read regardless of propName
            properties);

      PropertyFilterSpec pfSpec = new PropertyFilterSpec();
      pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
      pfSpec.setPropSet(new PropertySpec[] { pSpec });

      IPropertyCollector pc = getPropertyCollector();

      ObjectContent[] objs;
      try
      {
         objs = pc.retrieveProperties(new PropertyFilterSpec[] { pfSpec });
      } catch(Exception e)
      {
         throw new RuntimeException(e);
      }

      if (objs == null || objs[0]==null)
         return null;
      else
         return objs[0];
   }

	public Object getPropertyByPath(String propPath)
	{
		return getCurrentProperty(propPath);
	}

	/**
    * Get multiple properties by their paths
    * @param propPaths an array of strings for property path
    * @return a Hashtable holding with the property path as key, and the value.
    * @throws InvalidProperty
    * @throws RuntimeFault
    * @throws RemoteException
    */
   public Hashtable getPropertiesByPaths(String[] propPaths)
      throws InvalidProperty, RuntimeFault, RemoteException
   {
      Hashtable[] pht = PropertyCollectorUtil.retrieveProperties(
            new ManagedObject[] { this }, getMOR().getType(), propPaths);
      if(pht.length!=0)
         return pht[0];
      else
         return null;
   }

   /**
    * Handle Updates for a single object. waits till expected values of
    * properties to check are reached Destroys the ObjectFilter when done.
    *
    * @param filterProps
    *           Properties list to filter
    * @param endWaitProps
    *           Properties list to check for expected values these be properties
    *           of a property in the filter properties list
    * @param expectedVals
    *           values for properties to end the wait
    * @return true indicating expected values were met, and false otherwise
    * @throws RemoteException
    * @throws RuntimeFault
    * @throws InvalidProperty
    */
   protected Object[] waitForValues(String[] filterProps, String[] endWaitProps, Object[][] expectedVals) throws InvalidProperty, RuntimeFault, RemoteException
   {
      String version = "";
      Object[] endVals = new Object[endWaitProps.length];
      Object[] filterVals = new Object[filterProps.length];

      ObjectSpec oSpec = PropertyCollectorUtil.creatObjectSpec(
            getMOR(), Boolean.FALSE, null);

      PropertySpec pSpec = PropertyCollectorUtil.createPropertySpec(
            getMOR().getType(),
            filterProps == null || filterProps.length == 0, //if true, all props of this obj are to be read regardless of propName
            filterProps);

      PropertyFilterSpec spec = new PropertyFilterSpec();
      spec.setObjectSet(new ObjectSpec[] { oSpec });
      spec.setPropSet(new PropertySpec[] { pSpec });

      IPropertyCollector pc = getPropertyCollector();
      IPropertyCollector.Filter pf = pc.createFilter(spec, true);

      boolean reached = false;

      while (!reached)
      {
         UpdateSet updateset = pc.waitForUpdates(version);
         if(updateset==null)
         {
            continue;
         }
          version = updateset.getVersion();
          PropertyFilterUpdate[] filtupary = updateset.getFilterSet();
          if (filtupary == null)
          {
            continue;
          }

          // Make this code more general purpose when PropCol changes later.
          for (int i = 0; i < filtupary.length; i++)
          {
            PropertyFilterUpdate filtup = filtupary[i];
            if(filtup==null)
            {
               continue;
            }
            ObjectUpdate[] objupary = filtup.getObjectSet();
            for (int j = 0; objupary!=null && j < objupary.length; j++)
            {
               ObjectUpdate objup = objupary[j];
               if(objup==null)
               {
                  continue;
               }
               PropertyChange[] propchgary = objup.getChangeSet();
                  for (int k = 0; propchgary!=null && k < propchgary.length; k++)
                  {
                  PropertyChange propchg = propchgary[k];
                       updateValues(endWaitProps, endVals, propchg);
                       updateValues(filterProps, filterVals, propchg);
                  }
            }
          }

          // Check if the expected values have been reached and exit the loop if done.
          // Also exit the WaitForUpdates loop if this is the case.
          for (int chgi = 0; chgi < endVals.length && !reached; chgi++)
          {
            for (int vali = 0; vali < expectedVals[chgi].length && !reached; vali++)
            {
               Object expctdval = expectedVals[chgi][vali];
               reached = expctdval.equals(endVals[chgi]) || reached;
            }
          }
      }

      pf.destroyPropertyFilter();

      return filterVals;
   }

   private void updateValues(String[] props, Object[] vals, PropertyChange propchg)
   {
      for (int i = 0; i < props.length; i++)
      {
         if (propchg.getName().lastIndexOf(props[i]) >= 0)
         {
            if (propchg.getOp() == PropertyChangeOp.remove)
            {
               vals[i] = "";
            }
            else
            {
               vals[i] = propchg.getVal();
            }
         }
      }
   }

	protected ManagedObject[] getManagedObjects(String propName, boolean mixedType)
	{
		Object object = getCurrentProperty(propName);
		ManagedObjectReference[] mors = null;
		if(object instanceof ManagedObjectReference[])
		{
			mors = (ManagedObjectReference[]) object;
		}

		if(mors==null || mors.length==0)
		{
			return new ManagedObject[] {};
		}

		Object mos = new ManagedObject[mors.length];;

		try
		{
			Class moClass = null;

			if(mixedType==false)
			{
				moClass = Class.forName(moPackageName + "." + mors[0].getType());
			    mos = Array.newInstance(moClass, mors.length);
			}

			for(int i=0; i<mors.length; i++)
			{
				if(mixedType == true )
				{
					moClass = Class.forName(moPackageName + "." + mors[i].getType());
				}
				Constructor constructor = moClass.getConstructor(
						new Class[] {ServerConnection.class, ManagedObjectReference.class});

				Array.set(mos, i,
					constructor.newInstance(new Object[] { getServerConnection(), mors[i]}) );
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}

		return (ManagedObject[]) mos;
	}

	protected ManagedObject[] getManagedObjects(String propName)
	{
		return getManagedObjects(propName, false);
	}

	protected ManagedObject getManagedObject(String propName)
	{
		ManagedObjectReference mor = (ManagedObjectReference) getCurrentProperty(propName);
		return MorUtil.createExactManagedObject(getServerConnection(), mor, moPackageName);
	}

	@Override
   public String toString()
	{
		return mor.getType() + ":" + mor.get_value()
			+ " @ " + getServerConnection().getUrl();
	}

	protected ManagedObjectReference[] convertMors(ManagedObject[] mos) {
		ManagedObjectReference[] mors = null;
		if(mos!=null)
		{
			mors = MorUtil.createMORs(mos);
		}
		return mors;
	}

   /**
    * Override equals to compare managed objects by their managed object
    * references. If the object is not a ManagedObject, this will return
    * <code>false</code>.
    */
	@Override
   public boolean equals(Object mo) {
	   if (mo != null && mo instanceof ManagedObject) {
	      return this.toString().equals(mo.toString());
	   } else {
	      return false;
	   }
	}

   /**
    * Get the PropertyCollector object that should be used for this
    * ManagedObject.
    *
    * @return IPropertyCollector
    */
	public abstract IPropertyCollector getPropertyCollector();
}