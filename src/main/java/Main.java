import javafx.embed.swing.JFXPanel;

import javax.sound.sampled.Mixer;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {

    static File destinationFolder = null;
    static File sourceFolder = null;

    static Mixer.Info mixerInfo = null;

    public static void main(String[] args) {
        new JFXPanel();
        new GUI();
    }

    public static File getDestinationMirror(File file){
        return new File(destinationFolder.getAbsolutePath() + "\\" + file.getName().replace(".mp3", ".wav"));
    }
}
