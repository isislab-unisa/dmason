/**
 * Copyright 2017 Universita' degli Studi di Salerno
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.amazonaws.services.ec2.model.InstanceStateName;
import com.jcraft.jsch.Session;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util.LocalInstanceStateManager;

/**
 * 
 * The class <code>LocalInstanceState</code> maps instance state and other
 * metadata:
 * <ul>
 * 		<li><strong>ID</strong>: the instance identifier;</li>
 * 		<li><strong>DNS name</strong>: the instance Domain Name Server name;</li>
 * 		<li><strong>master DNS</strong>: the master instance Domain Name Server name;</li>
 * 		<li><strong>running state</strong>: the instance running state;</li>
 * 		<li><strong>DMASON installed</strong>: the status of DMASON installation;</li>
 * 		<li><strong>DMASON running</strong>: the status of DMASON execution;</li>
 * 		<li><strong>instance role</strong>: the role of instance if DMASON is running;</li>
 * 		<li><strong>instance termination</strong>: the instance termination status;</li>
 * 		<li><strong style="color:red;">instance session</strong>: the instance session;</li>
 * 		<li><strong>last edit time</strong>: the local instance state last update time.</li>
 * </ul>
 * All parameters but <em>instance session</em> are persisted into a file as a serialized stream by <code>{@link LocalInstanceStateManager}</code>.<br>
 * Setter methods invocation automatically update last edit time.
 * 
 * @author Simone Bisogno
 *
 */
public class LocalInstanceState
		implements Serializable
{
	// constructors
	/**
	 * 
	 * <code>LocalInstanceState</code> constructor takes as parameters instance
	 * ID and DNS name.<br>All boolean parameters are initialized as
	 * <strong><code>false</code></strong>, session as <strong><code>null</code></strong>
	 * and last edit time as current construction time.
	 * 
	 * @param id - The instance ID.
	 * @param dns - The instance DNS name.
	 */
	public LocalInstanceState(String id, String dns, String type)
	{
		this.id = id;
		this.dns = dns;
		this.type = type;
		this.masterDns = null; // master DNS instance has to refer if worker
		this.running = false; // instance running state
		this.ready = false; // instance has got DMASON installed
		this.busy = false; // DMASON is running on instance
		this.master = false; // instance is a Master for DMASON
		this.terminated = false; // instance termination status
		this.session = null; // temporary session
		this.lastEditTime = LocalDateTime.now(); // last edit time
	}

	// getters
	/**
	 * 
	 * @return Instance ID.
	 * @see #id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * 
	 * @return Instance DNS name.
	 * @see #dns
	 */
	public String getDns()
	{
		return dns;
	}

	/**
	 * 
	 * @return Instance type.
	 * @see #type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * 
	 * @return Master DNS name.
	 * @see #masterDns
	 */
	public String getMasterDns()
	{
		return masterDns;
	}

	/**
	 * 
	 * @return Instance running status.
	 * @see #running
	 */
	public boolean isRunning()
	{
		return running;
	}

	/**
	 * 
	 * @return DMASON installation status on instance.
	 * @see #ready
	 */
	public boolean isReady()
	{
		return ready;
	}

	/**
	 * 
	 * @return DMASON running status on instance
	 * @see #busy
	 */
	public boolean isBusy()
	{
		return busy;
	}

	/**
	 * 
	 * @return Instance role (<em>master</em> or <em>worker</em>)
	 * 		according to DMASON execution.
	 * @see #master
	 */
	public boolean isMaster()
	{
		return master;
	}

	/**
	 * 
	 * @return Instance termination status.
	 * @see #terminated
	 */
	public boolean isTerminated()
	{
		return terminated;
	}

	/**
	 * 
	 * @return Instance session.
	 * @see #session
	 * @see Session
	 */
	public Session getSession()
	{
		return session;
	}

	/**
	 * 
	 * @return Instance last edit time.
	 * @see #lastEditTime
	 * @see LocalDateTime
	 */
	public LocalDateTime getLastEditTime()
	{
		return lastEditTime;
	}

	// setters
	/**
	 * 
	 * @param id - Instance new ID.
	 * @see #id
	 */
	public void setId(String id)
	{
		this.id = id;
		updateLastEditTime();
	}

	/**
	 * 
	 * @param dns - New instance DNS name.
	 * @see #dns
	 */
	public void setDns(String dns)
	{
		this.dns = dns;
		updateLastEditTime();
	}

	/**
	 * 
	 * @param type - New instance type.
	 * @See #type
	 */
	public void setType(String type)
	{
		this.type = type;
		updateLastEditTime();
	}

	/**
	 * 
	 * @param masterDns - New Instance master DNS name.
	 * @see #masterDns
	 */
	public void setMasterDns(String masterDns)
	{
		this.masterDns = masterDns;
		updateLastEditTime();
	}

	/**
	 * 
	 * @param running - New instance running state.
	 * @see #running
	 */
	public void setRunning(boolean running)
	{
		this.running = running;
		updateLastEditTime();
	}

	/**
	 * 
	 * @param ready - New instance DMASON installation state.
	 * @see #ready
	 */
	public void setReady(boolean ready)
	{
		this.ready = ready;
		updateLastEditTime();
	}

	/**
	 * 
	 * @param isBusy - New instance DMASON running state.
	 * @see #busy
	 */
	public void setBusy(boolean isBusy)
	{
		this.busy = isBusy;
		updateLastEditTime();
	}

	/**
	 * 
	 * @param isMaster - New instance DMASON role.
	 * @see #master
	 */
	public void setMaster(boolean isMaster)
	{
		this.master = isMaster;
		updateLastEditTime();
	}

	/**
	 * This method irreversibly marks an instance as {@link InstanceStateName}
	 * .<strong>Terminated</strong>.<br>It reflects che semantics of Terminated
	 * state.
	 * @see #terminated
	 */
	public void markTerminate()
	{
		// once the machine is in 'terminated' state
		// it cannot be rerun again in Amazon AWS
		this.terminated = true;
		updateLastEditTime();
	}

	/**
	 * 
	 * @param session - New instance session.
	 * @see #session
	 */
	public void setSession(Session session)
	{
		this.session = session;
		updateLastEditTime();
	}

	// Object methods
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalInstanceState other = (LocalInstanceState) obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		return id.equals(other.id);
	}

	@Override
	public String toString()
	{
		return "LocalInstanceState [id=" + id + ", dns=" + dns +
				", masterDns=" + masterDns + ", ready=" + ready
				+ ", running=" + running + ", busy=" + busy +
				", master=" + master + ", terminated=" + terminated + "]";
	}

	// helpers
	/**
	 * This helper method updates instance last edit time; it gets invoked
	 * when a new <code>LocalInstanceState</code> object gets created or when
	 * a setter method gets invocated.
	 * @see #lastEditTime
	 */
	private void updateLastEditTime()
	{
		this.lastEditTime = LocalDateTime.now();
	}

	// variables
	/**
	 * Instance ID.
	 */
	private String id;
	/**
	 * Instance DNS name.
	 */
	private String dns;
	/**
	 * Instance type.
	 */
	private String type;
	/**
	 * If instance is running DMASON as worker,
	 * this is the master DNS.
	 */
	private String masterDns;
	/**
	 * If instance is running, this toggle is <i>true</i>.
	 */
	private boolean running;
	/**
	 * If instance has got DMASON installed,
	 * this toggle is set to <i>true</i>.
	 */
	private boolean ready; // DMASON is installed and ready to run
	/**
	 * If instance is running DMASON,
	 * this toggle is set to <i>true</i>.
	 */
	private boolean busy;
	/**
	 * If instance is master,
	 * this toggle is set to <i>true</i>.
	 */
	private boolean master;
	/**
	 * If instance is terminated,
	 * this toggle is set to <i>true</i>.
	 */
	private boolean terminated;
	/**
	 * Instance secure session (SSH).<br>
	 * It is a <strong><code>transient</code></strong> parameter since
	 * <code>com.jcraft.jsch.{@link Session}</code> class doesn't implement
	 * <code>{@link Serializable}</code> marker interface.
	 */
	private transient Session session; // Session doesn't implement Serializable ):
	/**
	 * Last edit time for local instance state.
	 */
	private LocalDateTime lastEditTime;

	// constants
	/**
	 * Class version.
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1L;
}
