import de.sciss.jump3r.Main;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessedSourceFolder {

    private final File folder;
    private List<File> wavFiles = new ArrayList<>();
    private int index = 0;

    private MediaPlayer player;

    public ProcessedSourceFolder(File folder) {
        this.folder = folder;
        read();
    }

    public void read(){
        if (folder.listFiles() == null) {
            return;
        }
        for (File file : folder.listFiles()) {
            if (!file.isDirectory()){
                if (file.getName().endsWith(".wav") || file.getName().endsWith(".mp3")){
                    System.out.println("added wave file: " + file.getName());
                    wavFiles.add(file);
                    if (file.getName().endsWith(".wav")){
                        file.renameTo(new File(file.getAbsolutePath().replace(".wav", ".mp3")));
                        System.out.println("Renamed to: " + file.getName());
                    }
                }
            }
        }
    }

    public static void convertWavFileToMp3File(File source, File target) throws IOException {
        String[] mp3Args = { "--preset","standard",
                "-q","0",
                "-m","s",
                source.getAbsolutePath(),
                target.getAbsolutePath()
        };
        System.out.println("converting " + source.getAbsolutePath() + " to " + target.getAbsolutePath());
        (new Main()).run(mp3Args);
        System.out.println("Conversion finished");
        System.out.println("Replacing end of target file name with .wav");
        source.delete();
        target.renameTo(new File(target.getAbsolutePath().replace(".mp3", ".wav")));
    }

    public void playCurrent() {
        File file = getCurrent();
        // Turn the file into playable audio with AudioTrack
        Media media = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();

        if (player != null) {
            stop();
        }
        player = mediaPlayer;
    }

    public void stop() {
        if (player != null) {
            player.stop();
            player = null;
        }
    }

    public File getNext(){
        if (index >= wavFiles.size()-1){
            return getCurrent();
        }
        return wavFiles.get(index++);
    }

    public File getPrevious(){
        if (index <= 0){
            return getCurrent();
        }
        return wavFiles.get(index--);
    }

    public File getCurrent(){
        return wavFiles.get(index);
    }
}
