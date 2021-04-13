package de.griefed.serverpackcreator.gui;

import javax.swing.*;
import java.awt.*;

public class ServerPackCreatorLog extends Component {

    JComponent serverPackCreatorLog() {
        JComponent serverPackCreatorLog = new JPanel(false);
        serverPackCreatorLog.setPreferredSize(ReferenceGUI.panelDimension);

        JButton setModpackDir = new JButton();
        setModpackDir.setIcon(ReferenceGUI.folderIcon);
        setModpackDir.setBounds(50,50,24, 24);
        serverPackCreatorLog.add(setModpackDir);

        JButton setJavaPath = new JButton();
        setJavaPath.setIcon(ReferenceGUI.folderIcon);
        setJavaPath.setBounds(50,250,24, 24);
        serverPackCreatorLog.add(setJavaPath);

        JButton jButton3 = new JButton();
        jButton3.setIcon(ReferenceGUI.serverPackCreatorIcon);
        jButton3.setBounds(500,50,50, 50);
        serverPackCreatorLog.add(jButton3);

        JButton jButton4 = new JButton();
        jButton4.setIcon(ReferenceGUI.serverPackCreatorIcon);
        jButton4.setBounds(500,250,50, 50);
        serverPackCreatorLog.add(jButton4);

        JButton jButton5 = new JButton();
        jButton5.setIcon(ReferenceGUI.serverPackCreatorIcon);
        jButton5.setBounds(500,450,50, 50);
        serverPackCreatorLog.add(jButton5);

        return serverPackCreatorLog;
    }
}