package it.isislab.dmason.sim.engine;

public class StateVariable {
	public String name;
	public Class type;
	
	public StateVariable(String name, Class type) {
		super();
		this.name = name;
		this.type = type;
	}
	/**
	 * @return the name
	 */
	protected String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	protected void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the type
	 */
	protected Class getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	protected void setType(Class type) {
		this.type = type;
	}
	

}
