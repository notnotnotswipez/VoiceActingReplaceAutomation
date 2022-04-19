import javax.swing.*;

public class FolderSelector {
    String result = null;

    public FolderSelector(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            result = chooser.getSelectedFile().getAbsolutePath();
        }
    }

    public String getResult() {
        return result;
    }
}
