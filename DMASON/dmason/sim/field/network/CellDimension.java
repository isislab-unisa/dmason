package dmason.sim.field.network;

/*Classe che contiene le dimensioni di una singola cella di un worker*/

public class CellDimension {
	private double own_x,own_y,my_width,my_height,end_x,end_y;
	
	public CellDimension(double own_x,double own_y,double my_width,double my_height,double end_x,double end_y){
		this.own_x=own_x;
		this.own_y=own_y;
		this.my_width=my_width;
		this.my_height=my_height;
		this.end_x=end_x;
		this.end_y=end_y;
	}

	public double getOwn_x() {
		return own_x;
	}

	public double getOwn_y() {
		return own_y;
	}

	public double getMy_width() {
		return my_width;
	}

	public double getMy_height() {
		return my_height;
	}

	public double getEnd_x() {
		return end_x;
	}

	public double getEnd_y() {
		return end_y;
	}
}
