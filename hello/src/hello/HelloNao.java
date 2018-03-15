package hello;

import java.util.ArrayList;
import com.aldebaran.qi.Application;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;

public class HelloNao {
	public static void torresHanoi(ArrayList<Integer> origen, ArrayList<Integer> destino, int discos, int a, int b, int c) {
		if (discos == 1) {
			origen.add(a);
			destino.add(c);
			
		} else {
			torresHanoi(origen,destino, discos - 1, a, c, b);
			origen.add(a);
			destino.add(c);
			torresHanoi(origen, destino, discos - 1, b, a, c);
		}
	}

	public static Movimientos torresHanoi(int discos) {
		Movimientos movimientos = new Movimientos(new ArrayList<Integer>(), new ArrayList<Integer>());
		torresHanoi(movimientos.getOrigen(), movimientos.getDestino(), discos, 1, 2, 3);
		return movimientos;
	}

	public static void main(String[] args) {
		String robotUrl = "tcp://nao.local:9559";
		try {
			Application application = new Application(args, robotUrl);
			application.start();
			ALTextToSpeech tts = new ALTextToSpeech(application.session());
			Movimientos movimientos = torresHanoi(3);
			for (int i=0; i<movimientos.getOrigen().size();i++) {
				tts.say("move from tower "+String.valueOf(movimientos.getOrigen().get(i))+ " " + "to tower"+String.valueOf(movimientos.getDestino().get(i)));
				Thread.sleep(1500);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	}
}