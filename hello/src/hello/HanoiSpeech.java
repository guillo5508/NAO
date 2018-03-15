package hello;

import java.util.ArrayList;
import java.util.List;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.CallError;
import com.aldebaran.qi.helper.EventCallback;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;

public class HanoiSpeech {

	private static boolean isAwake = false;
	private static Application application;
	private static ALMemory alMemory;
	private static ALSpeechRecognition alSpeechRecognition;
	private static ALTextToSpeech tts;
	private static String APP_NAME = "Hanoi";

	public static void main(String[] args) {

		String robotUrl = "tcp://nao.local:9559";
		application = new Application(args, robotUrl);

		try {
			application.start();

			alMemory = new ALMemory(application.session());
			
			//alSpeechRecognition.ex
			
			alSpeechRecognition = new ALSpeechRecognition(application.session());
			alSpeechRecognition.pause(false);
			alSpeechRecognition.setLanguage("English");
			tts = new ALTextToSpeech(application.session());

			ArrayList<String> listOfWords = new ArrayList<String>();
			//listOfWords.add("play 1");
			listOfWords.add("play 2");
			//listOfWords.add("play 3");
			
			alSpeechRecognition.pause(true);
			Thread.sleep(5000);
			//alSpeechRecognition.pause(false);
			
			alSpeechRecognition.setVocabulary(listOfWords, false);
			alSpeechRecognition.subscribe(APP_NAME);
			
			//alSpeechRecognition.pause(true);
			alMemory.subscribeToEvent("WordRecognized", new EventCallback<List<Object>>() {
			

				@Override
				public void onEvent(List<Object> words) throws InterruptedException, CallError {
					//alSpeechRecognition.pause(true);
					String word = (String) words.get(0);
					System.out.println("Word " + word);
					alSpeechRecognition.pause(false);
					String aux1 = "play ";
					int aux2 = Integer.valueOf(word.charAt(word.length() - 1));
					
					if (word.equals(aux1 + aux2)) {
						Movimientos movimientos = HelloNao.torresHanoi(aux2);
						for (int i = 0; i < movimientos.getOrigen().size(); i++) {
							tts.say("move from tower " + String.valueOf(movimientos.getOrigen().get(i)) + " "
									+ "to tower" + String.valueOf(movimientos.getDestino().get(i)));
							Thread.sleep(1500);
						}
						
						isAwake = true;
						alSpeechRecognition.unsubscribe(APP_NAME);
					}
				}
			});
			
			application.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
