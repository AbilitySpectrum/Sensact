package lyricom.sensactConfig.ui;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lyricom.sensactConfig.comms.Serial;
import lyricom.sensactConfig.model.IOError;
import lyricom.sensactConfig.model.InStream;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.OutStream;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.model.Triggers;

/**
 * Defines the main frame of the application, its control buttons
 * and its tabs.
 * Holds a list of SensorGroupPanel items.
 * @author Andrew
 */
public class MainFrame extends JFrame {
    
    public static JFrame TheFrame;
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");

    private final List<SensorGroupPanel> sensorGroups = new ArrayList<>();
    
    public MainFrame(String version) {
        setTitle(RES.getString("PROGRAM_NAME"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (inSyncCheck()) {
                    TheFrame.dispose();
                    System.exit(0);
                }
            }
        });

        TheFrame = this;
        
        setLayout(new BorderLayout());

        JComponent vLabel = versionLabel(version);
        add(buttonPanel(vLabel), BorderLayout.WEST); 

        add(tabbedPanes(), BorderLayout.CENTER);
        pack();
 
        // Center on screen
        Dimension dim = new Dimension(1100,600);
        setSize(dim);
        Point center = ScreenInfo.getCenter();
        setLocation(center.x-dim.width/2, center.y-dim.height/2);

        setVisible(true);
        
        // Get triggers after frame is set up.
        // This will happen on the initial connection only.
        // Not on a reconnection - since that does not rebuild the main frame.
        Serial.getInstance().writeByte(Model.CMD_GET_TRIGGERS);
    }
    
    private static final int BTN_SPACING = 10;
    
    private JComponent versionLabel(String version) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
//        p.setBorder(new LineBorder(Color.YELLOW, 1));
        JLabel l = new JLabel("V" + version);
        p.add(l);
        return p;
    }
    
    private JButton getBtn;
    private JButton saveBtn;
    private JButton runBtn;
    private JButton idleBtn;
    private JButton importBtn;
    private JButton exportBtn;
    private JButton exitBtn;
//    private JButton testBtn;
    
    private JComponent buttonPanel(JComponent vLabel) {
        JPanel p = new JPanel();
        Border boarder = new LineBorder(Color.BLACK, 2);
        Border margin = new EmptyBorder(0, 10, 10, 10);
        p.setBorder(new CompoundBorder(boarder, margin));
        
        Box vb = Box.createVerticalBox();
        vb.add(vLabel);
        
        JButton[] buttons = createButtons();
        
        // Make all buttons the same size
        int height = 0;
        int maxWidth = 0;
        Dimension dim = new Dimension();
        for(JButton b: buttons) {
            dim = b.getPreferredSize();
            height = dim.height;
            if (dim.width > maxWidth) {
                maxWidth = dim.width;
            }
        }
        dim.height = height;
        dim.width = maxWidth;
        for(JButton b: buttons) {
            b.setPreferredSize(dim);
            b.setMaximumSize(dim);
        }

        // Display buttons
        vb.add(getBtn);
        vb.add(Box.createVerticalStrut(BTN_SPACING));
        vb.add(saveBtn);
        vb.add(Box.createVerticalStrut(BTN_SPACING));
        vb.add(new JSeparator(JSeparator.HORIZONTAL));
        vb.add(Box.createVerticalStrut(BTN_SPACING));
        vb.add(runBtn);
        vb.add(Box.createVerticalStrut(BTN_SPACING));
        vb.add(idleBtn);
        vb.add(Box.createVerticalStrut(BTN_SPACING));
        vb.add(new JSeparator(JSeparator.HORIZONTAL));
        vb.add(Box.createVerticalStrut(BTN_SPACING));
        vb.add(importBtn);
        vb.add(Box.createVerticalStrut(BTN_SPACING));
        vb.add(exportBtn);
        vb.add(Box.createVerticalStrut(BTN_SPACING));
        vb.add(new JSeparator(JSeparator.HORIZONTAL));
        vb.add(Box.createVerticalStrut(BTN_SPACING));
        vb.add(exitBtn);        
                     
        p.add(vb);
        return p;
    }
    
    private JButton[] createButtons() {
        getBtn = newBtn(RES.getString("BTN_GET"));
        saveBtn = newBtn(RES.getString("BTN_SAVE"));
        runBtn = newBtn(RES.getString("BTN_RUN"));
        idleBtn = newBtn(RES.getString("BTN_IDLE"));
        importBtn = newBtn(RES.getString("BTN_IMPORT"));
        exportBtn = newBtn(RES.getString("BTN_EXPORT"));
        exitBtn = newBtn(RES.getString("BTN_EXIT"));
        
        getBtn.addActionListener(e -> Serial.getInstance().writeByte(Model.CMD_GET_TRIGGERS));
        saveBtn.addActionListener(e -> doSave());
        runBtn.addActionListener (e -> {
            if (inSyncCheck()) {
                Serial.getInstance().writeByte(Model.CMD_RUN);
            }
        });
        idleBtn.addActionListener(e -> Serial.getInstance().writeByte(Model.CMD_VERSION));
        importBtn.addActionListener(e -> doImport());
        exportBtn.addActionListener(e -> doExport());
        exitBtn.addActionListener(e -> {
            if (inSyncCheck()) {
                System.exit(0);
            }
        });
        
//        JButton testBtn = newBtn("Test");
//        testBtn.addActionListener(e -> displayTriggers());

        JButton[] buttons =  {
            getBtn,
            saveBtn,
            runBtn,
            idleBtn,
            importBtn,
            exportBtn,
//            testBtn,
            exitBtn
        };
        
        return buttons;
    }
    
    private JButton newBtn(String title) {
        JButton b = new JButton(title);
        b.setAlignmentX(0.5F);
        
        return b;
    }
    
    private void doSave() {
        OutStream os = Triggers.getInstance().getTriggerData();
        Serial.getInstance().writeList(os.getBuffer());
        Triggers.DATA_IN_SYNC = true;
    }
    
    private void doImport() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(MainFrame.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File input = fileChooser.getSelectedFile();
            try {
                FileInputStream fis = new FileInputStream(input);
                List<Byte> bytes = new ArrayList<>();
                int val;
                do {
                    val = fis.read();
                    if (val != -1) {
                        bytes.add((byte)val);
                    }
                } while (val != Model.END_OF_BLOCK && val != -1);
                
                InStream is = new InStream(bytes);
                Triggers.getInstance().loadTriggers(is);
                SensorPanel.reloadTriggers();      
                               
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(MainFrame.this,
                    "File reading failed. ",
                    "Import failed",
                    JOptionPane.ERROR_MESSAGE);
            
            } catch(IOError e) {
                JOptionPane.showMessageDialog(MainFrame.TheFrame, 
                        "Error loading data.\n" + e.getMessage(),
                        "Data Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }        
    }
    
    private void doExport() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(MainFrame.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File output = fileChooser.getSelectedFile();
            boolean writeIt = false;
            if (output.exists()) {
                result = JOptionPane.showConfirmDialog(MainFrame.this,
                        "That file exists.\nDo you want to overwrite it?",
                        "File Exists",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    writeIt = true;
                }
            } else {    // File does not exist
                writeIt = true;
            }
            if (writeIt) {
                OutStream os = Triggers.getInstance().getTriggerData();
                try {
                    FileOutputStream fos = new FileOutputStream(output);
                    for(Byte b: os.getBuffer()) {
                        fos.write(b);
                    }
                    fos.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(MainFrame.this, 
                            "Write failed.",
                            "IO Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }        
    }
    
    private void displayTriggers() {
        OutStream os = Triggers.getInstance().getTriggerData();
        byte[] bytes = Utils.listToArray(os.getBuffer());
        String s = new String(bytes);
        System.out.println(s);
    }
    
    
    private JComponent tabbedPanes() {
        JTabbedPane pane = new JTabbedPane();
        
        for(SensorGroup g: Model.getSensorGroups()) {
            SensorGroupPanel p = new SensorGroupPanel(g);
            sensorGroups.add(p);
            pane.add(g.getName(), p);
        }
        
        JPanel p = new MouseSpeedPanel();
        pane.add("Mouse Speed", p);
        return pane;
    }
    
    // If true is returned continue with the operation.
    // If false is returned cancel the operation.
    private boolean inSyncCheck() {
        if (Triggers.DATA_IN_SYNC == false) {
            int opt = JOptionPane.showConfirmDialog(this, 
                    "There are unsaved changes.\n"
                  + "Do you want to save them before continuing?",
                    "Warning", 
                    JOptionPane.YES_NO_CANCEL_OPTION
            );
            if (opt == JOptionPane.YES_OPTION) {
                doSave();
                return true;
            } else if (opt == JOptionPane.NO_OPTION) {
                return true;
            } else {
                return false;  // must be cancel
            }
        }
        return true;
    }
    
}
