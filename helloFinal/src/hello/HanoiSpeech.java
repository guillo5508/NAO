package hello;

import java.util.ArrayList;
import java.util.List;

import com.aldebaran.qi.Application;

import com.aldebaran.qi.helper.proxies.ALMemory;

import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import com.aldebaran.qi.helper.proxies.ALPhotoCapture;

public class HanoiSpeech {

	private static Application applicacion;
	private static ALMemory memoria;
	private static ALSpeechRecognition reconocedorVoz;
	private static ALTextToSpeech tts;
	private static String cadena = "Hanoi";
	

	public static void main(String[] args) {

		String robotUrl = "tcp://nao.local:9559";
		applicacion = new Application(args, robotUrl);

		try {
			applicacion.start();
			memoria = new ALMemory(applicacion.session());
			reconocedorVoz = new ALSpeechRecognition(applicacion.session());
			reconocedorVoz.setLanguage("English");
			tts = new ALTextToSpeech(applicacion.session());

			ArrayList<String> listOfWords = new ArrayList<String>();
			listOfWords.add("play 1");
			listOfWords.add("play 2");
			listOfWords.add("play 3");

			reconocedorVoz.subscribe(cadena);
			reconocedorVoz.pause(true);
			reconocedorVoz.setVocabulary(listOfWords, false);
			reconocedorVoz.pause(false);

			memoria.subscribeToEvent("WordRecognized", (value) -> {
				List<String> words = (List<String>) value;
				String word = words.get(0);
				System.out.println(word);
				reconocedorVoz.pause(true);
				String aux1 = "play ";
				String aux2 = String.valueOf(word.charAt(word.length() - 1));
				if (word.compareTo(aux1 + aux2) == 0) {
					Movimientos movimientos = HelloNao.torresHanoi(Integer.valueOf(aux2));
					for (int i = 0; i < movimientos.getOrigen().size(); i++) {
						tts.say("move from tower " + String.valueOf(movimientos.getOrigen().get(i)) + " " + "to tower"
								+ String.valueOf(movimientos.getDestino().get(i)));
						Thread.sleep(1500);
					}
					reconocedorVoz.pause(false);
					reconocedorVoz.unsubscribe(cadena);
					System.exit(0);
				}
			});
			applicacion.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
