package us.mn.state.dot.tms.client;

import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/** A FilteredMonitorModel is a ProxyListModel filtered to just the
 * VideoMonitors for which the user can set the camera attribute.
 */
public class FilteredMonitorModel extends ProxyListModel<VideoMonitor> {
	
	protected User user = null;

	protected SonarState state = null;
	
	public FilteredMonitorModel(SonarState st){
		super(st.getVideoMonitors());
		this.state = st;
		this.user = getUser();
	}
	
	protected User getUser(){
		if(user != null) return user;
		try{
			TypeCache<Connection> connections = state.getConnections();
			Connection c = connections.lookupObject(state.getConnection());
			user = c.getUser();
		}catch(Exception e){
		}
		return user;
	}

	protected String createUpdateString(VideoMonitor proxy){
		return VideoMonitor.SONAR_TYPE + "/" + proxy.getName() + "/camera";
	}
	
	/** Add a new proxy to the list model */
	public void proxyAdded(VideoMonitor proxy) {
		if(getUser() == null) return;
		if(user.canUpdate(createUpdateString(proxy))){
			super.proxyAdded(proxy);
		}
	}

	/** Change a proxy in the model */
	public void proxyChanged(VideoMonitor proxy, String attrib) {
		if(getUser() == null) return;
		boolean exists = proxies.contains(proxy);
		boolean canUpdate = user.canUpdate(createUpdateString(proxy));
		if(canUpdate && !exists){
			super.proxyAdded(proxy);
		}else if(!canUpdate && exists){
			super.proxyRemoved(proxy);
		}
	}
}