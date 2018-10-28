package lyricom.sensactConfig.solutions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.swing.*;
import lyricom.sensactConfig.model.ActionName;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.model.SensorGroup;
import lyricom.sensactConfig.model.Trigger;
import lyricom.sensactConfig.ui.MainFrame;
import lyricom.sensactConfig.ui.ScreenInfo;
import lyricom.sensactConfig.widgets.WT_Action;
import lyricom.sensactConfig.widgets.W_Number;
import lyricom.sensactConfig.widgets.W_Spinner;

/**
 *
 * @author Andrew
 */
public class SolutionsUI extends JDialog {
    
    private JLabel messageBox;
    private final Box options;
    private JButton cancelBtn;
    private final SolutionsUI thisDlg;
    private SolutionBase theSolution = null;
    private final Semaphore answerSemaphore;
    private final SensorGroup theGroup;
    private Description popupDesc = null;
    private String popupTarget = null;
    private static Dimension ACTION_SIZE = null;
    
    // Returned values.  Volatile since they are shared between threads.
    private volatile String lastAnswer;
    private volatile int lastNumber;
    private volatile List<ActionRow> actions = new ArrayList<>();  
    private volatile Trigger singleAction;
    
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
        showSolutions( SolutionRegister.getInstance().
                getApplicableSolutions(theGroup.getName()) );
        
        pack();
        // Center on screen
        Dimension dim = new Dimension(300, 300);
        setMinimumSize(dim);
        Point center = ScreenInfo.getCenter();
        setLocation(center.x-dim.width/2, center.y-dim.height/2 - 100);
        
        setVisible(true);
    }
    
    private JPanel titleLine() {
        JPanel p = new JPanel();
        messageBox = new JLabel();
        p.add(messageBox);
        return p;
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
    
    private void clearOptions() {
        options.removeAll();
        repaint();        
    }
    
    // Used to show solution options
    private void showSolutions(String[] opts) {
        clearOptions();
        options.add(Box.createVerticalStrut(10));
        for(String s: opts) {
            final String fs = s;
            JButton b = new JButton(fs);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.addActionListener(e -> {
                if (popupDesc != null) {
                    popupDesc.dispose();
                    popupDesc = null;
                }
                // This is the response to "Select a solution"
                // Start the solution thread.
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
               
            });
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (popupDesc != null && !popupTarget.equals(fs)) {
                        popupDesc.dispose();
                        popupTarget = null;
                        popupDesc = null;
                    }
                    if (popupTarget == null) {
                        // Only popup once for a given button
                        // This is to supress multiple mouseEnter events 
                        // on the Mac - poor Mac!
                        popupDesc = new Description(thisDlg, SolutionRegister.getInstance().getToolTip(fs));
                        popupTarget = fs;
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                }
            }); 
            options.add(b);
            options.add(Box.createVerticalStrut(10));
        }
        revalidate();
    }

    // Called when the solution is finished and the UI dialog should be closed.
    void solutionComplete() {
        SwingUtilities.invokeLater(() -> {
            thisDlg.dispose();
            theSolution = null;
        });       
    }

    void presentMessage(String action) {
        SwingUtilities.invokeLater(() -> {
            clearOptions();
            messageBox.setText(action);
        });
    }
    
    /*
     * The following sets of functions allow the Solutions thread to
     * get information from the user via the UI thread.
    
     * The get* methods are called by the solution to get some value,
     * and they return the value(s) selected by the user. The Solutions 
     * thread blocks in these routines until the correponding show* method
     * signals that a response is ready.
     *
     * The present* methods are called by the get* methods.  These
     * trigger the Swing UI thread and call the required show* method.  
     *
     * The show* methods actually build the UI display.  The Swing
     * UI thread handles the interaction with the user and signals
     * the Solutions thread (via the answerSemaphore) when the answer
     * is ready.
    */
    
    // -- Ask the user to select from a set of options --
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
    
    private void presentOptions(String question, String[] answers) {        
        SwingUtilities.invokeLater(() -> {
            messageBox.setText(question);
            showOptions(answers);
        });
    }
    
    private void showOptions(String[] opts) {
        clearOptions();
        options.add(Box.createVerticalStrut(10));
        for(String s: opts) {
            final String fs = s;
            JButton b = new JButton(fs);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.addActionListener(e -> {
                // This is the response to a question from 
               // the Solution thread.
               lastAnswer = fs;
               answerSemaphore.release();
            });
            options.add(b);
            options.add(Box.createVerticalStrut(10));
        }
        revalidate();
    }
    
    // -- Ask the user for a numeric value --  
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
    
    private void presentDelayOption(String question, int dftValue) {
        SwingUtilities.invokeLater(() -> {
            messageBox.setText(question);
            showNumberOption(dftValue);
        });
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
    
    // -- Ask the user to select a value from a list limited by a spinner --
    private static final String ACTION_COUNTS[] = {"2", "3", "4", "5"};

    int getActionCount(String prompt) {
        try {
            answerSemaphore.acquire();
            presentValueList(prompt, ACTION_COUNTS);
            answerSemaphore.acquire();  // blocks until user selects an answer.
            answerSemaphore.release();
            return lastNumber;
        } catch (InterruptedException ex) {
            return 0;
        }
    }

    private void presentValueList (String prompt, String[] values) {
        SwingUtilities.invokeLater(() -> {
            messageBox.setText(prompt);
            showValueList(values);
        });          
    }
    
    
    private void showValueList(String[] values) {
        clearOptions();
        final W_Spinner spinner = new W_Spinner("", values);
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        spinner.setLayout(new FlowLayout(FlowLayout.CENTER));
        spinner.setMaximumSize(new Dimension(150, 30));

        JButton doneBtn = new JButton("Done");
        doneBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        options.add(Box.createVerticalStrut(10));
        options.add(spinner);
        options.add(Box.createVerticalStrut(10));
        options.add(doneBtn);
        options.add(Box.createVerticalGlue());
        
        doneBtn.addActionListener(e -> {
            String tmp = spinner.getValue();
            lastNumber = new Integer(tmp);
            answerSemaphore.release();
        });
        revalidate();
    }
    
    // -- Ask for a single action --
    Trigger getSingleAction(String prompt) {
        try {
            answerSemaphore.acquire();
            presentAction(prompt);
            answerSemaphore.acquire();  // blocks until user selects an answer.
            answerSemaphore.release();
            return singleAction;
        } catch (InterruptedException ex) {
            return null;
        }        
    }
    
    private void presentAction(String prompt) {
        SwingUtilities.invokeLater(() -> {
            messageBox.setText(prompt);
            showAction();
        });
    }
    
    private void showAction() {
        singleAction = new Trigger(Model.getSensorByID(1));
        clearOptions();
        JPanel p = new JPanel();
        p.add(new WT_Action("", singleAction));
        p.setPreferredSize(ACTION_SIZE);
        options.add(p);
        
        JButton doneBtn = new JButton("Done");
        doneBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        options.add(Box.createVerticalStrut(10));
        options.add(doneBtn);
        
        doneBtn.addActionListener(e -> {
            answerSemaphore.release();
        });        
    }
    
    
    // -- Ask the user to define all the prompts and actions for a 
    //    multi-function button.  This is the most complex of all
    //    by far.
     
    List<ActionRow> getActions(int actionCount, boolean showAction1) {
        try {
            answerSemaphore.acquire();
            presentActionUI(actionCount, showAction1);
            answerSemaphore.acquire();  // blocks until user selects an answer.
            answerSemaphore.release();
            List<ActionRow> actionCopy = new ArrayList<ActionRow>(actions);
            return actionCopy;
        } catch (InterruptedException ex) {
            return null;
        }
    }
    
    private void presentActionUI(int actionCount, boolean showAction1) {
        SwingUtilities.invokeLater(() -> {
            messageBox.setText("Define the prompts and actions");
            showActionUI(actionCount, showAction1);
        });
    }
    
    private void showActionUI(int actionCount, boolean showAction1) {
        if (ACTION_SIZE == null) {
            // Calibration - done once.
            Trigger t = new Trigger(Model.getSensorByID(1));
            t.setAction(Model.getActionByName(ActionName.IR));
            t.setActionParam(2);
            WT_Action actionUI = new WT_Action("Prompt 1:", t);
            ACTION_SIZE = actionUI.getPreferredSize();  
//            System.out.println("Action - H: " + Integer.toString(ACTION_SIZE.height) + " W: " + Integer.toString(ACTION_SIZE.width));
        }
        clearOptions();
        actions.clear();
        Box box = Box.createVerticalBox();
        options.add(Box.createVerticalStrut(10));
        options.add(box);
        for(int i=1; i<= actionCount; i++) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            
            Trigger tp;
            Trigger ta = new Trigger(Model.getSensorByID(1));
            String num = Integer.toString(i);
            JComponent pr;
            if (i == 1 && !showAction1) {
                tp = null;
                pr = new JPanel();
            } else {
                tp = new Trigger(Model.getSensorByID(1));
                pr = new WT_Action("Prompt " + num + ":", tp);
            }
            JComponent ac = new WT_Action("Action " + num + ":", ta);
            pr.setPreferredSize(ACTION_SIZE);
            ac.setPreferredSize(ACTION_SIZE);
            p.add(pr);
            p.add(ac);
            JCheckBox cb;
            if (i != 1 || showAction1) {
                p.add(new JLabel("Latch:"));
                cb = new JCheckBox("");
                p.add(cb);
            } else {
                cb = null;
            }
            ActionRow ar = new ActionRow(tp, ta, cb);
            actions.add(ar);
            box.add(p);
        }
        
        JButton doneBtn = new JButton("Done");
        doneBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        options.add(Box.createVerticalStrut(10));
        options.add(doneBtn);
        
        doneBtn.addActionListener(e -> {
            answerSemaphore.release();
        });
        
        // Resize and reposition.
        setVisible(false);
        pack();
        revalidate();
        Point loc = getLocation();
        loc.x -= 200;
        setLocation(loc);
        setVisible(true);
    }
    

}
