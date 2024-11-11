package me.ravalle.programlauncher.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.ravalle.programlauncher.ProgramLauncher;
import me.ravalle.programlauncher.ProgramLauncherSettings;
import xyz.duncanruns.jingle.Jingle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProgramLauncherPanel {
    public JPanel mainPanel;
    private JLabel lblTitle;
    private JButton btnLaunchProgsNow;
    private JList launchPrograms;
    private final DefaultListModel launchProgramsListModel;
    private JCheckBox launchProgramsWhenJingleOpens;
    private JCheckBox launchMCInstance;
    private JButton instanceLaunchPath;
    private JButton launcherExecutablePath;

    public ProgramLauncherPanel() {
        $$$setupUI$$$();
        launchProgramsListModel = new DefaultListModel();
        launchPrograms.setModel(launchProgramsListModel);

        saveAndReloadGUI();

        launchProgramsWhenJingleOpens.addItemListener(e -> {
            ProgramLauncherSettings.getInstance().launchOnStart = (e.getStateChange() == ItemEvent.SELECTED);
            saveAndReloadGUI();
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
                    addBtn.addActionListener(e2 -> {
                        FileDialog fd = new FileDialog((Frame) null, "Choose a program");
                        fd.setMode(FileDialog.LOAD);
                        fd.setMultipleMode(true);
                        fd.setVisible(true);
                        ProgramLauncherSettings.getInstance().launchProgramPaths.addAll(Arrays.stream(fd.getFiles()).map(File::getPath).collect(Collectors.toList()));
                        fd.dispose();
                        saveAndReloadGUI();
                    });
                    JMenuItem removeBtn = new JMenuItem("Remove");
                    removeBtn.addActionListener(e1 -> {
                        launchPrograms.getSelectedValuesList().forEach(ProgramLauncherSettings.getInstance().launchProgramPaths::remove);
                        saveAndReloadGUI();
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
                    if (ProgramLauncherSettings.getInstance().launchMC) {
                        new Thread(() -> ProgramLauncher.launchInstance(ProgramLauncherSettings.getInstance().dotMinecraftPath)).start();
                    }
                }
                super.mouseReleased(e);
            }
        });

        launchMCInstance.addItemListener(e -> {
            ProgramLauncherSettings.getInstance().launchMC = (e.getStateChange() == ItemEvent.SELECTED);
            if (ProgramLauncherSettings.getInstance().launchMC && Objects.equals(ProgramLauncherSettings.getInstance().dotMinecraftPath, "")) {
                setMCLaunchPathPopup(launchMCInstance, MouseInfo.getPointerInfo().getLocation().x - launchMCInstance.getLocationOnScreen().x, MouseInfo.getPointerInfo().getLocation().y - launchMCInstance.getLocationOnScreen().y);
            }
            saveAndReloadGUI();
        });

        instanceLaunchPath.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.getComponent().isEnabled()) {
                    return;
                }
                setMCLaunchPathPopup(instanceLaunchPath, e.getX(), e.getY());
                saveAndReloadGUI();
                super.mouseReleased(e);
            }
        });

        launcherExecutablePath.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.getComponent().isEnabled()) {
                    return;
                }
                trySetLauncherExecutablePath(ProgramLauncherSettings.getInstance().dotMinecraftPath);
                setLauncherExecutablePathPopup(launcherExecutablePath, e.getX(), e.getY());
                super.mouseReleased(e);
            }
        });
    }

    private void setMCLaunchPathPopup(Component component, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        Jingle.options.seenPaths.keySet().forEach((path) -> {
            JMenuItem item = new JMenuItem(path);
            item.addActionListener(e -> setDotMinecraftPath(item.getText()));
            menu.add(item);
        });
        JMenuItem addOther = new JMenuItem("Search for a different instance...");
        addOther.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose a .minecraft folder");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.showDialog(component, "OK");
            setDotMinecraftPath(fc.getSelectedFile().toString());
        });
        menu.add(addOther);
        menu.show(component, x, y);
    }

    private void setLauncherExecutablePathPopup(Component component, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem add = new JMenuItem("Search for a launcher executable...");
        add.addActionListener(e -> {
            FileDialog fd = new FileDialog((Frame) null, "Select your Minecraft launcher's .exe file");
            fd.setMode(FileDialog.LOAD);
            fd.setMultipleMode(false);
            fd.setVisible(true);
            if (fd.getFile() != null) {
                ProgramLauncherSettings.getInstance().launcherExecutable = fd.getDirectory() + fd.getFile();
            }
            fd.dispose();
            saveAndReloadGUI();
        });
        menu.add(add);
        menu.show(component, x, y);
    }

    private void setDotMinecraftPath(String path) {
        ProgramLauncherSettings.getInstance().dotMinecraftPath = path;
        trySetLauncherExecutablePath(path);
        saveAndReloadGUI();
    }

    private void trySetLauncherExecutablePath(String dotMinecraftPath) {
        String candidate = ProgramLauncher.tryGetLauncherExecutable(dotMinecraftPath);
        if (!candidate.equals("not found")) {
            ProgramLauncherSettings.getInstance().launcherExecutable = candidate;
        }
        saveAndReloadGUI();
    }

    private void saveAndReloadGUI() {
        ProgramLauncherSettings.save();

        launchProgramsWhenJingleOpens.setSelected(ProgramLauncherSettings.getInstance().launchOnStart);

        launchProgramsListModel.removeAllElements();
        ProgramLauncherSettings.getInstance().launchProgramPaths.forEach(launchProgramsListModel::addElement);

        launchMCInstance.setSelected(ProgramLauncherSettings.getInstance().launchMC);
        btnLaunchProgsNow.setText("Launch Programs" + (ProgramLauncherSettings.getInstance().launchMC ? " and Minecraft" : "") + " now");
        launchProgramsWhenJingleOpens.setText("Launch Programs" + (ProgramLauncherSettings.getInstance().launchMC ? " and Minecraft" : "") + " when Jingle opens");
        instanceLaunchPath.setEnabled(ProgramLauncherSettings.getInstance().launchMC);
        instanceLaunchPath.setText("Instance path: " + ProgramLauncherSettings.getInstance().dotMinecraftPath);

        launcherExecutablePath.setText("Launcher executable: " + ProgramLauncherSettings.getInstance().launcherExecutable);
        launcherExecutablePath.setEnabled(ProgramLauncherSettings.getInstance().launchMC);
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
        mainPanel.setLayout(new GridLayoutManager(7, 3, new Insets(5, 5, 5, 5), -1, -1));
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
        launchMCInstance = new JCheckBox();
        launchMCInstance.setText("Launch Minecraft Instance (requires MultiMC or Prism)");
        mainPanel.add(launchMCInstance, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        instanceLaunchPath = new JButton();
        instanceLaunchPath.setText("Instance path:");
        mainPanel.add(instanceLaunchPath, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        launcherExecutablePath = new JButton();
        launcherExecutablePath.setText("Launcher executable:");
        mainPanel.add(launcherExecutablePath, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
