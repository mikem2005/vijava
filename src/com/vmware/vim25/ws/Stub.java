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

/**
 * Abstract class for a Stub compatible with the VIM web service client engine.
 *
 * @author Dan Parrella
 */
public abstract class Stub {

   protected WSClient wsc = null;

   /**
    * Constructor.
    *
    * @param url
    *           Endpoint URL of the web service
    * @param ignoreCert
    *           Whether or not all certificates should be trusted
    * @throws java.net.MalformedURLException
    */
   public Stub(String url, boolean ignoreCert) throws java.net.MalformedURLException
   {
      this.wsc =
            new WSClient(url, ignoreCert, this.getClass()
                                              .getPackage()
                                              .getName());
   }

   /**
    * Constructor for a stub with a pre-configured web service client.
    *
    * @param wsc
    */
   public Stub(WSClient wsc)
   {
     this.wsc = wsc;
   }

   /**
    * Get the web service client associated with this stub.
    *
    * @return
    */
   public WSClient getWsc()
   {
     return wsc;
   }
}