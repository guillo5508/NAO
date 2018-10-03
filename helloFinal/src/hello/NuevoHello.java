package hello;

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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class NuevoHello {

	private static Application applicacion;
	private static ALMemory memoria;
	private static ALSpeechRecognition reconocedorVoz;
	private static ALTextToSpeech tts;
	private static String cadena = "Hanoi";

	public static int procesar(String input, String output) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		ArrayList<Integer> rango = new ArrayList<Integer>();

		rango.add(170); // Amarillo
		rango.add(140); // Verde
		rango.add(70); // Azul
		rango.add(40); // Violeta
		rango.add(30); // Rojo

		int limit = 10; // menor que el número de pixeles en un disco
		int i = 200;

		// Matrices
		Mat original = new Mat();
		original = Imgcodecs.imread(input);

		Mat gray = new Mat();
		Imgproc.cvtColor(original, gray, Imgproc.COLOR_RGB2GRAY);
		Imgcodecs.imwrite(output, gray);
		int k = 0;
		int j = 0;
		while (i > limit) {
			// Eliminar discos
			Mat temp = new Mat();
			Imgproc.threshold(gray, temp, rango.get(j), 255, Imgproc.THRESH_BINARY);
			// Convertir a Buffered Image
			BufferedImage BI = null;
			BI = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
			BI = M2B(temp);
			i = validarColor(BI);
			j++;
			k++;
		}
		System.out.println(i);
		System.out.println(k);
		return k;
	}

	public static int validarColor(BufferedImage img) {
		int cont = 0;
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				Color color;
				color = new Color(img.getRGB(j, i));
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				if (r != 255 && g != 255 && b != 255) {
					cont++;
				}
			}
		}
		return cont;
	}

	public static Mat B2M(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", byteArrayOutputStream);
		byteArrayOutputStream.flush();
		return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()),
				Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
	}

	public static BufferedImage M2B(Mat matrix) throws IOException {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
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
						Movimientos movimientos = HelloNao
								.torresHanoi(Integer.valueOf(procesar("photo.jpg", "salida.jpg")));

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
