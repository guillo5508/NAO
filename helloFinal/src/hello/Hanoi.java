package hello;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALPhotoCapture;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Hanoi {



	private static Application applicacion;
	private static ALMemory memoria;
	private static ALSpeechRecognition reconocedorVoz;
	private static ALTextToSpeech tts;
	private static String cadena = "Hanoi";

	public static int procesar(String input, String output) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat original = new Mat();
		original = Imgcodecs.imread(input);

		Mat changeColor = new Mat();
		Imgproc.cvtColor(original, changeColor, Imgproc.COLOR_RGB2BGR);
		Imgcodecs.imwrite(output, changeColor);
		
		Mat gauss = new Mat();
		Imgproc.GaussianBlur(changeColor, gauss, new Size(5,5), 0);
		Imgcodecs.imwrite("gauss.png",gauss);
		
		double a = 50;
		double b= 150;
		Mat cany= new Mat();
		Imgproc.Canny(gauss, cany, a, b);
		Imgcodecs.imwrite("bordes.png", cany);
		
		Mat contornos= new Mat();
		ArrayList<MatOfPoint> listaContornos = new ArrayList<MatOfPoint>();
		Imgproc.findContours(cany.clone(),listaContornos,contornos, Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
		return listaContornos.size();
	}


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
			listOfWords.add("play");

			reconocedorVoz.subscribe(cadena);
			reconocedorVoz.pause(true);
			reconocedorVoz.setVocabulary(listOfWords, false);
			reconocedorVoz.pause(false);

			memoria.subscribeToEvent("WordRecognized", (value) -> {
				List<String> words = (List<String>) value;
				String word = words.get(0);
				System.out.println(word);
				reconocedorVoz.pause(true);
				if (word.compareTo("play") == 0) {

					try {
						ALPhotoCapture a = new ALPhotoCapture(applicacion.session());
						a.setResolution(2);
						a.setPictureFormat("jpg");
						a.takePicture("/home/nao/recordings/cameras/", "photo");

						Session session = null;
						JSch jsch = new JSch();
						try {
							session = jsch.getSession("nao", "192.168.43.219", 22);
							session.setConfig("StrictHostKeyChecking", "no");
							session.setPassword("nao");
							session.connect();
							System.out.println("..............");

							Channel channel = session.openChannel("sftp");
							channel.connect();
							ChannelSftp sftpChannel = (ChannelSftp) channel;
							sftpChannel.get("/home/nao/recordings/cameras/photo.jpg", "photo.jpg");
							sftpChannel.exit();
							session.disconnect();
							System.out.println("Imagen capturada con exito");
						} catch (JSchException e) {
							e.printStackTrace();
						} catch (SftpException e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
					}

					try {
						int proceso= procesar("photo.jpg", "salida.jpg");
						System.out.println(proceso);
						Movimientos movimientos = HelloNao
								.torresHanoi(proceso);

						for (int i = 0; i < movimientos.getOrigen().size(); i++) {
							tts.say("move from tower " + String.valueOf(movimientos.getOrigen().get(i)) + " "
									+ "to tower " + String.valueOf(movimientos.getDestino().get(i)));
							Thread.sleep(1500);
						}
						reconocedorVoz.pause(false);
						reconocedorVoz.unsubscribe(cadena);
						System.exit(0);
					} catch (IOException e) {
						e.printStackTrace();

					}
				}
			});
			applicacion.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
