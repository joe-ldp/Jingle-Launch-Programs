package me.ravalle.programlauncher.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.ravalle.programlauncher.ProgramLauncher;
import me.ravalle.programlauncher.ProgramLauncherSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.io.File;

public class ProgramLauncherPanel {
    public JPanel mainPanel;
    private JLabel lblTitle;
    private JButton btnLaunchProgsNow;
    private JList launchPrograms;
    private DefaultListModel launchProgramsListModel;
    private JCheckBox launchProgramsWhenJingleOpens;

    public ProgramLauncherPanel() {
        $$$setupUI$$$();
        launchProgramsListModel = new DefaultListModel();
        launchPrograms.setModel(launchProgramsListModel);

        saveAndReloadGUI();

        launchProgramsWhenJingleOpens.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ProgramLauncherSettings.getInstance().launchOnStart = (e.getStateChange() == ItemEvent.SELECTED);
                saveAndReloadGUI();
            }
        });

        launchPrograms.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int index = launchPrograms.locationToIndex(e.getPoint());
                    if (!launchPrograms.isSelectedIndex(index)) {
                        launchPrograms.setSelectedIndex(index);
                    }

                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem addBtn = new JMenuItem("Add");
                    addBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            FileDialog fd = new FileDialog((Frame) null, "Choose a program");
                            fd.setMode(FileDialog.LOAD);
                            fd.setMultipleMode(true);
                            fd.setVisible(true);
                            ProgramLauncherSettings.getInstance().launchProgramPaths.addAll(Arrays.stream(fd.getFiles()).map(File::getPath).collect(Collectors.toList()));
                            fd.dispose();
                        }
                    });
                    JMenuItem removeBtn = new JMenuItem("Remove");
                    removeBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            launchPrograms.getSelectedValuesList().forEach(path -> ProgramLauncherSettings.getInstance().launchProgramPaths.remove(path));
                        }
                    });

                    menu.add(addBtn);
                    menu.add(removeBtn);
                    menu.show(launchPrograms, e.getX(), e.getY());
                }
            }
        });
        btnLaunchProgsNow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    new Thread(ProgramLauncher::launchNotOpenPrograms).start();
                }
                super.mouseReleased(e);
            }
        });
    }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(4, 3, new Insets(5, 5, 5, 5), -1, -1));
        lblTitle = new JLabel();
        lblTitle.setText("Right click to add or remove programs");
        mainPanel.add(lblTitle, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(2, 1, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        launchProgramsWhenJingleOpens = new JCheckBox();
        launchProgramsWhenJingleOpens.setText("Launch programs when Jingle opens");
        mainPanel.add(launchProgramsWhenJingleOpens, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        launchPrograms = new JList();
        mainPanel.add(launchPrograms, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        btnLaunchProgsNow = new JButton();
        btnLaunchProgsNow.setText("Launch Programs Now");
        mainPanel.add(btnLaunchProgsNow, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
