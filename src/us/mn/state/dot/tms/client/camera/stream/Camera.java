package us.mn.state.dot.tms.client.camera.stream;

import java.awt.Point;

public class Camera {

	protected String id = null;
	
	protected String freeway = "";
	
	protected String crossStreet = "";
	
	protected Point location = new Point(0,0);

	public Camera(){
	}

	public void setCrossStreet(String xStreet) {
		if(xStreet==null) return;
		this.crossStreet = xStreet;
	}

	public String getCrossStreet() { return crossStreet; }

	public String getFreeway() { return freeway; }

	public String getId() { return id; }

	public int getEasting(){ return location.x; }

	public int getNorthing(){ return location.y; }

	public void setFreeway(String fwy) {
		if(fwy==null) return;
		this.freeway = fwy;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setEasting(int e){
		location.x = e;
	}

	public void setNorthing(int n){
		location.y = n;
	}
	
	public String toString(){
		return id + ": (" + getFreeway() + " @ " + getCrossStreet() + ")";
	}

	public static String createStandardId(String id){
		if(id == null) return null;
		id = id.toUpperCase();
		if(id.startsWith("C")) id = id.substring(1);
		while(id.length()<3) id = "0" + id;
		return "C" + id;
	}
}
