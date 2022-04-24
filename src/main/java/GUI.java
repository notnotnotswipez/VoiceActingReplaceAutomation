import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.MultimediaInfo;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class GUI {

    public static ProcessedSourceFolder processedSourceFolder = null;
    private Thread playThread;
    private Thread recordThread;

    public GUI() {
        //Creating the Frame
        JFrame frame = new JFrame("Valve Voice Actor Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add close listener
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (Recorder.mainLine != null){
                    Recorder.mainLine.close();
                }
            }
                                });

        frame.setSize(600, 400);

        //Creating the MenuBar and adding components


        // CENTER TEXT

        JPanel textPanel = new JPanel();
        textPanel.setBorder(BorderFactory.createTitledBorder("Recording Suite"));

        textPanel.setLayout(new GridLayout(10, 10));

        String destinationPathText = "Not set.";
        if (Main.destinationFolder != null) {
            destinationPathText = Main.destinationFolder.getAbsolutePath();
        }

        String sourcePathText = "Not set.";
        if (Main.sourceFolder != null) {
            sourcePathText = Main.sourceFolder.getAbsolutePath();
        }

        JProgressBar playProgressBar = new JProgressBar();
        playProgressBar.setStringPainted(true);
        playProgressBar.setString("Not Playing.");
        playProgressBar.setValue(0);
        playProgressBar.setMaximum(100);

        JProgressBar recordProgressBar = new JProgressBar();
        recordProgressBar.setStringPainted(true);
        recordProgressBar.setString("Not Recording.");
        recordProgressBar.setValue(0);
        recordProgressBar.setMaximum(100);

        JLabel destinationText = new JLabel("Destination folder: " + destinationPathText);
        JLabel sourceText = new JLabel("Source folder: " + sourcePathText);
        JLabel selectedFileText = new JLabel("Selected File: " + "Nothing to show.");
        JLabel recordingText = new JLabel("Not recording.");
        JLabel playingText = new JLabel("Not playing.");

        textPanel.add(selectedFileText);
        textPanel.add(destinationText);
        textPanel.add(sourceText);
        textPanel.add(recordingText);
        textPanel.add(playingText);
        textPanel.add(playProgressBar);
        textPanel.add(recordProgressBar);

        // TOP MENU

        JMenuBar menuBar = new JMenuBar();

        JMenu foldersDropdown = new JMenu("Folders");


        JMenu audacity = new JMenu("Audacity");

        menuBar.add(audacity);
        menuBar.add(foldersDropdown);

        JMenuItem destination = new JMenuItem("Destination");
        destination.addActionListener(e -> {

                    FolderSelector folderSelector = new FolderSelector();
                    if (folderSelector.getResult() != null) {
                        Main.destinationFolder = new File(folderSelector.getResult());
                        updateComponent(destinationText, o -> {
                            ((JLabel) o).setText("Destination folder: " + Main.destinationFolder.getAbsolutePath());
                        });
                    }
                }
        );

        JMenuItem source = new JMenuItem("Source");
        source.addActionListener(e -> {

                    FolderSelector folderSelector = new FolderSelector();
                    if (folderSelector.getResult() != null) {
                        Main.sourceFolder = new File(folderSelector.getResult());
                        processedSourceFolder = new ProcessedSourceFolder(Main.sourceFolder);
                        updateComponent(sourceText, o -> {
                            ((JLabel) o).setText("Source folder: " + Main.sourceFolder.getAbsolutePath());
                        });
                        updateComponent(selectedFileText, o -> {
                            selectedFileText.setText("Selected File: " + processedSourceFolder.getCurrent().getName());
                        });

                    }
                }
        );

        JMenuItem audacityFinalize = new JMenuItem("Audacity Finalize");
        audacityFinalize.addActionListener(e -> {
                    System.out.println("Audacity Finalize");
                    if (Main.destinationFolder != null){
                        for (File file : Main.destinationFolder.listFiles()) {
                            if (file.isDirectory()){
                                System.out.println("Found directory: " + file.getName());
                                if (file.getName().equals("macro-output")){
                                    for (File file1 : file.listFiles()) {
                                        if (file1.getName().endsWith(".mp3")){
                                            System.out.println("Finalizing " + file1.getName());
                                            file1.renameTo(new File(file1.getAbsolutePath().replace(".mp3", ".wav")));
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
        );

        audacity.add(audacityFinalize);

        // BOTTOM BUTTONS
        JPanel panel = new JPanel();

        JButton play = new JButton("Play");
        play.addActionListener(e -> {
                    if (processedSourceFolder != null) {
                        if (playThread != null) {
                            playThread.interrupt();
                            playThread = null;
                        }
                        processedSourceFolder.playCurrent();
                        long milliseconds = 0;
                        Encoder encoder = new Encoder();
                        try {
                            MultimediaInfo mi = encoder.getInfo(processedSourceFolder.getCurrent());
                            long ls = mi.getDuration();
                            milliseconds = (ls) + 200;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        double finalMilliseconds = milliseconds;
                        playProgressBar.setString("Playing!");
                        Thread thread = new Thread() {
                            double currentSecond = 0;

                            @Override
                            public void run() {
                                while (currentSecond <= finalMilliseconds) {
                                    playProgressBar.setValue((int) ((currentSecond/finalMilliseconds)*100));
                                    playingText.setText("Playing: " + trimDouble((currentSecond / 1000), 1) + "/" + (finalMilliseconds / 1000));
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException ex) {
                                        currentSecond = finalMilliseconds + 4;
                                        return;
                                    }
                                    playingText.setText("Playing: " + trimDouble((currentSecond / 1000), 1) + "/" + (finalMilliseconds / 1000));
                                    currentSecond += 2;
                                    if (currentSecond > finalMilliseconds) {
                                        playProgressBar.setValue(0);
                                        playProgressBar.setString("Not Playing.");
                                        playingText.setText("Not playing.");
                                    }
                                }
                            }
                        };
                        thread.start();

                        playThread = thread;
                    }
                }
        );
        JButton record = new JButton("Record");
        record.addActionListener(e -> {
                    if (processedSourceFolder != null) {
                        if (recordThread != null) {
                            recordThread.interrupt();
                            recordThread = null;
                        }
                        long milliseconds = 0;
                        Encoder encoder = new Encoder();
                        try {
                            MultimediaInfo mi = encoder.getInfo(processedSourceFolder.getCurrent());
                            long ls = mi.getDuration();
                            milliseconds = (ls) + 200;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        double finalMilliseconds = milliseconds;
                        recordProgressBar.setString("Recording!");
                        Thread thread = new Thread() {
                            double currentSecond = 0;

                            @Override
                            public void run() {
                                while (currentSecond <= finalMilliseconds) {
                                    recordProgressBar.setValue((int) ((currentSecond/finalMilliseconds)*100));
                                    recordingText.setText("Recording: " + trimDouble((currentSecond / 1000), 1) + "/" + (finalMilliseconds / 1000));
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException ex) {
                                        currentSecond = finalMilliseconds + 4;
                                        return;
                                    }
                                    recordingText.setText("Recording: " + trimDouble((currentSecond / 1000), 1) + "/" + (finalMilliseconds / 1000));
                                    currentSecond += 2;
                                    if (currentSecond > finalMilliseconds) {
                                        recordProgressBar.setValue(0);
                                        recordProgressBar.setString("Not Recording.");
                                        recordingText.setText("Not recording.");
                                    }
                                }
                            }
                        };
                        thread.start();
                        Recorder.record(milliseconds + 200);

                        recordThread = thread;
                    }
                }
        );
        // Make previous button
        JButton previous = new JButton("Previous");
        previous.addActionListener(e -> {
                    if (processedSourceFolder != null) {
                        processedSourceFolder.getPrevious();
                        updateComponent(selectedFileText, o -> {
                            selectedFileText.setText("Selected File: " + processedSourceFolder.getCurrent().getName());
                        });
                    }
                }
        );

        JButton next = new JButton("Next");
        next.addActionListener(e -> {
                    if (processedSourceFolder != null) {
                        processedSourceFolder.getNext();
                        updateComponent(selectedFileText, o -> {
                            selectedFileText.setText("Selected File: " + processedSourceFolder.getCurrent().getName());
                        });
                    }
                }
        );

        panel.add(previous);
        panel.add(next);
        panel.add(play);
        panel.add(record);


        foldersDropdown.add(destination);
        foldersDropdown.add(source);

        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.PAGE_START, textPanel);
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, menuBar);
        frame.add(textPanel);


        // Center the frame
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);

        frame.setVisible(true);
    }

    double trimDouble(double value, int trimAmount){
        String doubleString = String.valueOf(value);
        String subString = doubleString.substring(0, doubleString.length() - trimAmount);

        if (subString.split("\\.").length < 3){
            subString += "00";
        }
        if (subString.split("\\.").length < 2){
            subString += "0";
        }
        return Double.parseDouble(subString);
    }

    void updateComponent(Component component, Operation operation) {
        component.setVisible(false);
        operation.operate(component);
        component.setVisible(true);
    }

    interface Operation {
        void operate(Component component);
    }
}
