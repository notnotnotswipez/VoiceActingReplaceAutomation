import javax.sound.sampled.*;
import java.io.*;

public class Recorder {

    public static TargetDataLine mainLine = null;
    static AudioInputStream audioInputStream = null;

    private static final float SAMPLE_RATE = 48000; //8kHz
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    private static AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);

    public static void record(long milliseconds) {
        new Thread(() -> {
            try {
                System.out.println("Recording...");
                if (mainLine == null) {
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                    // checks if system supports the data line
                    if (!AudioSystem.isLineSupported(info)) {
                        System.out.println("Line not supported");
                        return;
                    }

                    mainLine = (TargetDataLine) AudioSystem.getLine(info);
                    if (!mainLine.isOpen()){
                        mainLine.open(format);
                    }
                }

                new Thread(() -> {
                    try {
                        Thread.sleep(milliseconds);
                        System.out.println("Called stop...");
                        stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

                mainLine.start();   // start capturing
                System.out.println("Start capturing...");

                audioInputStream = new AudioInputStream(mainLine);
                System.out.println("Start recording...");
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, Main.getDestinationMirror(GUI.processedSourceFolder.getCurrent()));
            } catch (LineUnavailableException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void stop() {
        System.out.println("Stopped recording...");
        mainLine.stop();
        try {
            ProcessedSourceFolder.convertWavFileToMp3File(Main.getDestinationMirror(GUI.processedSourceFolder.getCurrent()), new File(Main.getDestinationMirror(GUI.processedSourceFolder.getCurrent()).getPath().replace(".wav", ".mp3")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mainLine.close();
    }
}
