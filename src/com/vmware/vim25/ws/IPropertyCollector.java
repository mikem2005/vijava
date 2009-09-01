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
package com.vmware.vim25.ws;

import java.rmi.RemoteException;

import com.vmware.vim25.InvalidCollectorVersion;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.UpdateSet;

/**
 * Interface for PropertyCollector methods.
 */
public interface IPropertyCollector {

   /**
    * The Filter managed object type defines a filter that controls the
    * properties a collector retrieves and observes for changes. Filters are
    * subordinate objects; they are part of the PropertyCollector and do not
    * have independent lifetimes. A client cannot access a filter without the
    * property collector that was used to create it.
    */
   public interface Filter {

      /**
       * Destroys this filter.
       * <p/>
       * This operation can be called explicitly, or it can take place
       * implicitly when the property collector that created the filter is
       * destroyed.
       *
       * @throws RuntimeFault
       * @throws RemoteException
       */
      public void destroyPropertyFilter() throws RuntimeFault, RemoteException;

      /**
       * @return Flag to indicate if a change to a nested property reports only
       *         the nested change or the entire specified property value.
       */
      public boolean getPartialUpdates();

      /**
       * @return Specifications for this filter.
       */
      public PropertyFilterSpec getSpec();
   }

   /**
    * Cancels an outstanding WaitForUpdates, if any. The outstanding
    * waitForUpdates operation completes by throwing the Canceled fault.
    *
    * @throws RuntimeFault
    * @throws RemoteException
    */
   public void cancelWaitForUpdates() throws RuntimeFault, RemoteException;

   /**
    * Checks for updates on properties specified by the union of all current
    * filters. If no updates are pending, this method returns null.
    * <p/>
    * The return value contains the changes between the previous version and the
    * current values of the properties. The result includes a new version string
    * the client passes in order to receive further updates.
    *
    * @param version
    *           The data version currently known to the client. The value must
    *           be either the special initial version (an empty string), or a
    *           data version string that was returned from a previous call to
    *           checkForUpdates or waitForUpdates.
    * @return Changes since the last updated version. If no updates are pending,
    *         then this method returns null.
    * @throws InvalidCollectorVersion
    * @throws RuntimeFault
    * @throws RemoteException
    */
   public UpdateSet checkForUpdates(String version)
         throws InvalidCollectorVersion, RuntimeFault, RemoteException;

   /**
    * Creates a new filter for the given set of managed objects.
    *
    * @param spec
    *           The specifications for the filter.
    * @param partialUpdates
    *           Flag to specify whether a change to a nested property should
    *           report only the nested change or the entire specified property
    *           value. If the value is true, a change should report only the
    *           nested property. If the value is false, a change should report
    *           the enclosing property named in the filter.
    * @return The new filter
    * @throws InvalidProperty
    * @throws RuntimeFault
    * @throws RemoteException
    */
   public IPropertyCollector.Filter createFilter(
         PropertyFilterSpec spec,
         boolean partialUpdates) throws InvalidProperty, RuntimeFault,
         RemoteException;

   /**
    * The filters that this property collector uses to determine the list of
    * properties to retrieve and observe for changes.
    *
    * @return List of filters if any.
    * @throws RuntimeFault
    * @throws RemoteException
    */
   public IPropertyCollector.Filter[] getFilters()
         throws RuntimeFault, RemoteException;

   /**
    * Retrieves the specified properties of the specified managed objects.
    * <p/>
    * This method is equivalent to creating a filter for the given objects and
    * property specification set, then receiving the property values, and
    * finally destroying the filter. If you want to monitor the contents of an
    * object, you should use either checkForUpdates or waitForUpdates.
    *
    * @param specSet
    *           The specifications for the properties to retrieve.
    * @return The data contents of the specified objects.
    * @throws InvalidProperty
    * @throws RuntimeFault
    * @throws RemoteException
    */
   public ObjectContent[] retrieveProperties(PropertyFilterSpec[] specSet)
         throws InvalidProperty, RuntimeFault, RemoteException;

   /**
    * Waits for updates on properties specified by the union of all current
    * filters. Unlike checkForUpdates, this method waits until updates are
    * available before it completes.
    * <p/>
    * The return value contains the changes between the previous version and the
    * current values of the properties. The result includes a new version string
    * the client passes in order to receive further updates.
    *
    * @param version
    *           The data version currently known to the client. The value must
    *           be either the special initial version (an empty string), or a
    *           data version string that was returned from a previous call to
    *           checkForUpdates or waitForUpdates.
    * @return Changes since the last updated version.
    * @throws InvalidCollectorVersion
    * @throws RuntimeFault
    * @throws RemoteException
    */
   public UpdateSet waitForUpdates(String version)
         throws InvalidCollectorVersion, RuntimeFault, RemoteException;
}