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

import java.rmi.RemoteException;
import java.util.Calendar;

import com.vmware.vim25.*;

/**
 * The managed object class corresponding to the one defined in VI SDK API reference.
 * @author Steve JIN (sjin@vmware.com)
 */

public class PerformanceManager extends VimManagedObject
{

	public PerformanceManager(ServerConnection serverConnection, ManagedObjectReference mor)
	{
		super(serverConnection, mor);
	}

	public PerformanceDescription getDescription()
	{
		return (PerformanceDescription) getCurrentProperty("description");
	}

	public PerfInterval[] getHistoricalInterval()
	{
		return (PerfInterval[]) getCurrentProperty("historicalInterval");
	}

	public PerfCounterInfo[] getPerfCounter()
	{
		return (PerfCounterInfo[]) getCurrentProperty("perfCounter");
	}

	/**
	 * @throws RemoteException
	 * @throws RuntimeFault
	 * @deprecated use UpdatePerfInterval instead
	 */
	@Deprecated
   public void createPerfInterval(PerfInterval intervalId) throws RuntimeFault, RemoteException
	{
		getVimService().createPerfInterval(getMOR(), intervalId);
	}

	public PerfMetricId[] queryAvailablePerfMetric(ManagedEntity entity, Calendar beginTime, Calendar endTime, Integer intervalId) throws RuntimeFault, RemoteException
	{
		if(entity==null)
		{
			throw new IllegalArgumentException("entity must not be null.");
		}
		return getVimService().queryAvailablePerfMetric(getMOR(), entity.getMOR(), beginTime, endTime, intervalId);
	}

	public PerfEntityMetricBase[] queryPerf(PerfQuerySpec[] querySpec) throws RuntimeFault, RemoteException
	{
		return getVimService().queryPerf(getMOR(), querySpec);
	}

	public PerfCompositeMetric queryPerfComposite(PerfQuerySpec querySpec) throws RuntimeFault, RemoteException
	{
		return getVimService().queryPerfComposite(getMOR(), querySpec);
	}

	public PerfCounterInfo[] queryPerfCounter(int[] counterIds) throws RuntimeFault, RemoteException
	{
		return getVimService().queryPerfCounter(getMOR(), counterIds);
	}

	public PerfCounterInfo[] queryPerfCounterByLevel(int level) throws RuntimeFault, RemoteException
	{
		return getVimService().queryPerfCounterByLevel(getMOR(), level);
	}

	public PerfProviderSummary queryPerfProviderSummary(ManagedEntity entity) throws RuntimeFault, RemoteException
	{
		if(entity==null)
		{
			throw new IllegalArgumentException("entity must not be null.");
		}
		return getVimService().queryPerfProviderSummary(getMOR(), entity.getMOR());
	}

	public void removePerfInterval(int samplePeriod) throws RuntimeFault, RemoteException
	{
		getVimService().removePerfInterval(getMOR(), samplePeriod);
	}

	public void updatePerfInterval(PerfInterval interval) throws RuntimeFault, RemoteException
	{
		getVimService().updatePerfInterval(getMOR(), interval);
	}

}
