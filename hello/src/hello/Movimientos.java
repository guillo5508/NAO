package hello;

import java.util.ArrayList;

public class Movimientos {
	private ArrayList<Integer> origen;
	private ArrayList<Integer> destino;
	
	
	public ArrayList<Integer> getOrigen() {
		return origen;
	}
	public void setOrigen(ArrayList<Integer> origen) {
		this.origen = origen;
	}
	public ArrayList<Integer> getDestino() {
		return destino;
	}
	public void setDestino(ArrayList<Integer> destino) {
		this.destino = destino;
	}
	public Movimientos(ArrayList<Integer> origen, ArrayList<Integer> destino) {
		super();
		this.origen=origen;
		this.destino=destino;
	}
	
	public static void main(String[] args) {
		Movimientos movimientos = HelloNao.torresHanoi(3);
		for(int i=0; i<movimientos.getOrigen().size();i++) {
			System.out.println(movimientos.getOrigen().get(i)+ " "+ movimientos.getDestino().get(i));
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	

}
