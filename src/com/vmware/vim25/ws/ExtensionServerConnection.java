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

import java.net.URL;

import com.vmware.vim25.VimPortType;
import com.vmware.vim25.mo.ServerConnection;
import com.vmware.vim25.mo.ServiceInstance;

/**
 * Base class for VIM extension server connections. Encapsulates VC server
 * connection as well as that for the extension.
 *
 * @author Dan Parrella
 */
public abstract class ExtensionServerConnection extends ServerConnection {

   private final Stub extensionStub;
   private final URL extensionUrl;

   /**
    * Constructor.
    *
    * @param vimStub
    * @param vimServiceInstance
    * @param extensionStub
    */
   public ExtensionServerConnection(VimPortType vimStub,
                                    ServiceInstance vimServiceInstance,
                                    Stub extensionStub) {
      super(vimStub, vimServiceInstance);
      this.extensionStub = extensionStub;
      this.extensionUrl = extensionStub.getWsc().getBaseUrl();
   }

   /**
    * Constructor.
    *
    * @param vimServiceInstance
    * @param extensionStub
    */
   public ExtensionServerConnection(ServiceInstance vimServiceInstance,
                                    Stub extensionStub) {
      this(vimServiceInstance.getServerConnection().getVimService(),
           vimServiceInstance,
           extensionStub);
   }

   /**
    * Get the Stub object for the extension.
    *
    * @return Stub
    */
   public Stub getExtensionStub() {
      return extensionStub;
   }

   /**
    * Get the URL of this extension.
    *
    * @return URL for the extension.
    */
   public URL getExtensionUrl() {
      return extensionUrl;
   }
}