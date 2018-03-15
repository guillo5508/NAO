package hello;

public class Pruebas {
	static int h;
	public static String torreDeHanoiRecu(int discos, int a, int b, int c) {
		h += 1;
		String s = null;

		if (discos == 1) {

			//System.out.println();
		    s=("Mover disco de torre " + a + " a torre " + c);
			return s;
		} else {

			System.out.println(torreDeHanoiRecu(discos - 1, a, c, b));
			System.out.println("Mover disco de torre " + a + " a torre " + c);
			System.out.println(torreDeHanoiRecu(discos - 1, b, a, c));
			//return  torreDeHanoiRecu(discos - 1, b, a, c);
			//se que el error esta aca cuando retorno, pero nose como arreglarlo
			return "";
		}
	}
	public static void main(String[] args) {
		torreDeHanoiRecu(3, 1, 2, 3);
	}
}
