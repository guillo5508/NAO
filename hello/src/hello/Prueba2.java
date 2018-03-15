package hello;
import java.util.ArrayList;

import com.aldebaran.*;
import com.aldebaran.qi.Application;
import com.aldebaran.qi.CallError;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
public class Prueba2 {

	public static void main(String[] args) {
		String robotUrl = "tcp://nao.local:9559";
		Application application = new Application(args, robotUrl);
		application.start();
		ALSpeechRecognition a;
		try {
			a = new ALSpeechRecognition(application.session());
			a.setLanguage("English");
			ArrayList<String> listOfWords = new ArrayList<String>();
			listOfWords.add("play 1");
			listOfWords.add("play 2");
			listOfWords.add("play 3");
			a.setVocabulary(listOfWords, true);
			a.subscribe("hola");
			System.out.println("hola");
			Thread.sleep(20000);
			a.unsubscribe("hola");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
}
