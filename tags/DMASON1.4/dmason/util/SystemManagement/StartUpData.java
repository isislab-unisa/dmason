package dmason.util.SystemManagement;

import java.io.Serializable;



public class StartUpData implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Class def;
	private Object[] param;
	public boolean graphic;
	public boolean step = false;
	public int pos_x,pos_y;
	public boolean local;

	public StartUpData(Class def, Object[] param, boolean graphic,boolean local) {
		super();
		this.def = def;
		this.param = param;
		this.graphic = graphic;
		this.local = local;
	}
	
	public StartUpData() { }

	public Class getDef() {
		return def;
	}

	public void setDef(Class def) {
		this.def = def;
	}

	public Object[] getParam() {
		return param;
	}

	public void setParam(Object[] param) {
		this.param = param;
	}
	
	public boolean isStep() {
		return step;
	}

	public void setStep(boolean step) {
		this.step = step;
	}
	
	public void setPos_x(int pos_x){
		this.pos_x = pos_x;
	}

	public int getPos_x() {
		return pos_x;
	}
	
	public void setPos_y(int pos_y){
		this.pos_y = pos_y;
	}
	
	public int getPos_y() {
		return pos_y;
	}
	
	public void setLocal(boolean flag){
		this.local = flag;
	}
	
	public boolean getLocal(){
		return this.local;
	}

	public boolean isGraphic() {
		return graphic;
	}

	public void setGraphic(boolean graphic) {
		this.graphic = graphic;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		super.clone();
		Object[] obj = new Object[param.length];
		StartUpData s = new StartUpData();
		s.setDef(def);
		s.setStep(step);
		s.setGraphic(graphic);
		s.setLocal(local);
		s.setPos_x(pos_x);
		s.setPos_y(pos_y);
		s.setParam(obj);
		return s;
	}
	
	

}
