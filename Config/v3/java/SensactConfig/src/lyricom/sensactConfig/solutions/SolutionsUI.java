package lyricom.sensactConfig.solutions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.util.concurrent.Semaphore;
import javax.swing.*;
import javax.swing.border.LineBorder;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.ui.MainFrame;
import lyricom.sensactConfig.ui.ScreenInfo;
import lyricom.sensactConfig.widgets.W_Number;

/**
 *
 * @author Andrew
 */
public class SolutionsUI extends JDialog {
    
    private JLabel messageBox;
    private final Box options;
    private JButton cancelBtn;
    private final SolutionsUI thisDlg;
    private volatile String lastAnswer;
    private volatile int lastNumber;
    private SolutionBase theSolution = null;
    private final Semaphore answerSemaphore;
    private final SensorGroup theGroup;
    
    public SolutionsUI(SensorGroup sg) {
        super(MainFrame.TheFrame, true);
        thisDlg = this;
        answerSemaphore = new Semaphore(1);
        theGroup = sg;
        
        setLayout(new BorderLayout());
        add(titleLine(), BorderLayout.NORTH);
        options = Box.createVerticalBox();
        add(options, BorderLayout.CENTER);
        add(cancelBtn(), BorderLayout.SOUTH);
        
        messageBox.setText("Choose a solution");
        showOptions( SolutionRegister.getInstance().
                getApplicableSolutions(theGroup.getName()) );
        
        pack();
        // Center on screen
        Dimension dim = new Dimension(300, 300);
        setMinimumSize(dim);
        Point center = ScreenInfo.getCenter();
        setLocation(center.x-dim.width/2, center.y-dim.height/2);
        
        setVisible(true);
    }
    
    private JPanel titleLine() {
        JPanel p = new JPanel();
        messageBox = new JLabel();
        p.add(messageBox);
        return p;
    }

    private void clearOptions() {
        options.removeAll();
        repaint();        
    }
    
    // Used to show solution options
    // and to show options from solution dialog.
    private void showOptions(String[] opts) {
        clearOptions();
        options.add(Box.createVerticalStrut(10));
        for(String s: opts) {
            final String fs = s;
            JButton b = new JButton(fs);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.addActionListener(e -> {
                if (theSolution == null) {
                    // This is the response to "Select a solution"
                    theSolution = SolutionRegister.getInstance()
                            .startSolution(fs, thisDlg, theGroup);
                    if (theSolution == null) {
                        JOptionPane.showMessageDialog(
                            thisDlg,
                            "Solution failed to launch",
                            "Solution error",
                            JOptionPane.ERROR_MESSAGE);
                        thisDlg.dispose();
                    }
                } else {
                    // This is the response to a question from 
                    // the Solution thread.
                    lastAnswer = fs;
                    answerSemaphore.release();
                }
                
            });
            options.add(b);
            options.add(Box.createVerticalStrut(10));
        }
        revalidate();
    }
    
    private void showNumberOption(int dft) {
        clearOptions();
        final W_Number numberFld = new W_Number("", "Delay", 5, 0, 30000);
        numberFld.setAlignmentX(Component.CENTER_ALIGNMENT);
        numberFld.setLayout(new FlowLayout(FlowLayout.CENTER));
        numberFld.setMaximumSize(new Dimension(100, 20));

        numberFld.setValue(dft);
        JButton doneBtn = new JButton("Done");
        doneBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        options.add(Box.createVerticalStrut(10));
        options.add(numberFld);
        options.add(Box.createVerticalStrut(10));
        options.add(doneBtn);
        options.add(Box.createVerticalGlue());
        
        doneBtn.addActionListener(e -> {
            lastNumber = numberFld.getValue();
            answerSemaphore.release();
        });
        revalidate();
    }
    
    private JPanel cancelBtn() {
        JPanel p = new JPanel();
        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            if (theSolution != null) {
                theSolution.cancel();
                theSolution = null;
            }
            thisDlg.dispose();
        });
        
        p.add(cancelBtn);
        return p;
    }
    
    // The following methods are called on the thread running the
    // solution - which is not the thread running the Swing UI.
    private void presentOptions(String question, String[] answers) {        
        SwingUtilities.invokeLater(() -> {
            messageBox.setText(question);
            showOptions(answers);
        });
    }
    
    private void presentDelayOption(String question, int dftValue) {
        SwingUtilities.invokeLater(() -> {
            messageBox.setText(question);
            showNumberOption(dftValue);
        });
    }
    
    String getOption(String question, String[] answers) {
        try {
            answerSemaphore.acquire();
            presentOptions(question, answers);
            answerSemaphore.acquire();  // blocks until user selects an answer.
            answerSemaphore.release();
            return lastAnswer;
        } catch(InterruptedException ex) {
            return null;
        }
    }
    
    int getDelay(String question, int dft) {
        try {
            answerSemaphore.acquire();
            presentDelayOption(question, dft);
            answerSemaphore.acquire();  // blocks until user selects an answer.
            answerSemaphore.release();
            return lastNumber;
        } catch (InterruptedException ex) {
            return 0;
        }
    }
    
    void presentMessage(String action) {
        SwingUtilities.invokeLater(() -> {
            clearOptions();
            messageBox.setText(action);
        });
    }
    
    void solutionComplete() {
        SwingUtilities.invokeLater(() -> {
            thisDlg.dispose();
            theSolution = null;
        });       
    }
}
