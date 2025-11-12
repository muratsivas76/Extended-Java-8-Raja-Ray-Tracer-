/* $Id: MiniGUI.java,v 1.2 2001/02/25 01:28:40 gregoire Exp $
 * Copyright (C) 2000 E. Fleury & G. Sutre
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package raja.ui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.*;

import raja.renderer.Scene;
import raja.renderer.Renderer;
import raja.renderer.Resolution;
import raja.io.ObjectReader;


/**
 * Main class of this Mini Graphical User Interface.
 */
public class MiniGUI extends JFrame
{
  private JTextArea txtInput, txtLog;
  private ComputeParametersDialog computeParametersDialog;
  private ComputingImageThread computingImage;
  private Action newDocumentAction, openDocumentAction, saveDocumentAction;
  private Action cutAction, copyAction, pasteAction;
  private UndoAction undoAction;
  private RedoAction redoAction;
  private Action computeParametersAction;
  private Action startAndResumeComputationAction, suspendComputationAction, stopComputationAction;
  private JProgressBar progressBar;
  private JFileChooser fc;
  private UndoManager undoManager;
  private UndoableEditListener undoEditListener;
  private boolean documentChanged;
  private DocumentListener documentChangedListener;
  
  /**
   * Creates a MiniGUI object.
   */
  MiniGUI()
  {
    super("Raja's Mini GUI");
    
    // Closing the frame exits the GUI
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e)
        {
          exit();
        }
    });
    
    // Init fields of this MiniGUI
    txtInput = new JTextArea();
    txtLog = new JTextArea();
    txtLog.setEditable(false);
    txtLog.setForeground(Color.red);
    computeParametersDialog = new ComputeParametersDialog(this);
    progressBar = new JProgressBar();
    
    // Init actions
    initActions();
    
    // Init JFileChooser
    try
    {
      fc = new JFileChooser();
      fc.addChoosableFileFilter(Util.rajaInputFileFilter);
      fc.setFileFilter(Util.rajaInputFileFilter);
    }
    catch (java.security.AccessControlException e)
    {
      // No access to local disk
      // Maybe running in a restricted environnment?
      fc = null;
      // Disabling open/save actions
      openDocumentAction.setEnabled(false);
      saveDocumentAction.setEnabled(false);
    }
    
    // Init undo capabilities
    undoManager = new UndoManager();
    undoEditListener = new UndoableEditListener() {
      public void undoableEditHappened(UndoableEditEvent e) {
        //Remember the edit and update the menus
        undoManager.addEdit(e.getEdit());
        undoAction.update();
        redoAction.update();
      }
    };
    
    // Init listener to detect change in the document
    documentChangedListener = new DocumentListener() {
      public void insertUpdate(DocumentEvent e)
      {
        documentChanged = true;
      }
      public void removeUpdate(DocumentEvent e)
      {
        documentChanged = true;
      }
      public void changedUpdate(DocumentEvent e)
      {
        //Plain text components don't fire these events
      }
    };
    
    // Registers document listeners on current document and update
    // undo/redo actions.
    documentPostSet();
    
    // Init the label displaying the line number and the column number.
    LineAndColumn lineAndColumn = new LineAndColumn();
    txtInput.addCaretListener(lineAndColumn);
    
    // Finally, layout the GUI
    JPanel pane = new JPanel();
    setContentPane(pane);
    pane.setLayout(new BorderLayout());
    
    JPanel inputTextPane = new JPanel();
    inputTextPane.setLayout(new BorderLayout());
    inputTextPane.add(new JScrollPane(txtInput));
    inputTextPane.add(lineAndColumn, BorderLayout.SOUTH);
    
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
      inputTextPane,
    new JScrollPane(txtLog));
    splitPane.setOneTouchExpandable(true);
    splitPane.setPreferredSize(new Dimension(600, 550));
    splitPane.setDividerLocation(450);
    
    pane.add(splitPane, BorderLayout.CENTER);
    
    progressBar.setStringPainted(true);
    pane.add(progressBar, BorderLayout.SOUTH);
    
    initToolBarAndMenuBar();
  }
  
  /**
   * Quits this currently running MiniGUI.
   */
  private void exit()
  {
    if (checkDocumentChanged())
    {
      System.exit(0);
    }
  }
  
  /**
   * Creates the actions used in this GUI.
   */
  private void initActions()
  {
    // Get class loader to retrieve ressources
    ClassLoader cl = this.getClass().getClassLoader();
    
    // New, open and save actions
    newDocumentAction = new NewDocumentAction();
    newDocumentAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/file_new.gif")));
    newDocumentAction.putValue(Action.SHORT_DESCRIPTION, "New document");
    
    openDocumentAction = new OpenDocumentAction();
    openDocumentAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/file_open.gif")));
    openDocumentAction.putValue(Action.SHORT_DESCRIPTION, "Open document");
    
    saveDocumentAction = new SaveDocumentAction();
    saveDocumentAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/file_save.gif")));
    saveDocumentAction.putValue(Action.SHORT_DESCRIPTION, "Save document");
    
    // Cut, copy and paste actions
    // These actions are provided by the text editor
    java.util.Hashtable actions = new java.util.Hashtable();
    Action[] actionsArray = txtInput.getActions();
    for (int i = 0 ; i < actionsArray.length; i++)
    {
      Action a = actionsArray[i];
      actions.put(a.getValue(Action.NAME), a);
    }
    
    cutAction = (Action) actions.get(DefaultEditorKit.cutAction);
    cutAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/edit_cut.gif")));
    cutAction.putValue(Action.NAME, "Cut");
    cutAction.putValue(Action.SHORT_DESCRIPTION, "Cut selection to clipboard");
    
    copyAction = (Action) actions.get(DefaultEditorKit.copyAction);
    copyAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/edit_copy.gif")));
    copyAction.putValue(Action.NAME, "Copy");
    copyAction.putValue(Action.SHORT_DESCRIPTION, "Copy selection to clipboard");
    
    pasteAction = (Action) actions.get(DefaultEditorKit.pasteAction);
    pasteAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/edit_paste.gif")));
    pasteAction.putValue(Action.NAME, "Paste");
    pasteAction.putValue(Action.SHORT_DESCRIPTION, "Paste clipboard to selection");
    
    // Undo/redo actions
    undoAction = new UndoAction();
    undoAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/edit_undo.gif")));
    undoAction.putValue(Action.SHORT_DESCRIPTION, "Undo last edit");
    
    redoAction = new RedoAction();
    redoAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/edit_redo.gif")));
    redoAction.putValue(Action.SHORT_DESCRIPTION, "Redo previous edit");
    
    // Compute parameters action
    computeParametersAction = new ComputeParametersAction();
    computeParametersAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/compute_parameters.gif")));
    computeParametersAction.putValue(Action.SHORT_DESCRIPTION, "Ray tracing parameters");
    
    // Start/resume, suspend and stop actions
    startAndResumeComputationAction = new StartAndResumeComputationAction();
    startAndResumeComputationAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/compute_start.gif")));
    startAndResumeComputationAction.putValue(Action.SHORT_DESCRIPTION, "Start/resume computation");
    
    suspendComputationAction = new SuspendComputationAction();
    suspendComputationAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/compute_suspend.gif")));
    suspendComputationAction.putValue(Action.SHORT_DESCRIPTION, "Suspend ongoing computation");
    
    stopComputationAction = new StopComputationAction();
    stopComputationAction.putValue(Action.SMALL_ICON,
    new ImageIcon(cl.getResource("raja/ui/icons/compute_stop.gif")));
    stopComputationAction.putValue(Action.SHORT_DESCRIPTION, "Stop ongoing computation");
    
    suspendComputationAction.setEnabled(false);
    stopComputationAction.setEnabled(false);
  }
  
  /**
   * Adds an action to a tool bar.  This method performs some extra
   * initialization for all toolbar buttons.
   */
  private static void addActionToToolBar(Action action, JToolBar toolBar)
  {
    JButton b = toolBar.add(action);
    b.setText("");
    b.setRequestFocusEnabled(false);
    b.setMargin(new Insets(1,1,1,1));
  }
  
  /**
   * Creates tool bar and the menus.
   */
  private void initToolBarAndMenuBar()
  {
    // Tool bar initialization
    JToolBar toolBar = new JToolBar();
    addActionToToolBar(newDocumentAction, toolBar);
    addActionToToolBar(openDocumentAction, toolBar);
    addActionToToolBar(saveDocumentAction, toolBar);
    toolBar.addSeparator();
    addActionToToolBar(cutAction, toolBar);
    addActionToToolBar(copyAction, toolBar);
    addActionToToolBar(pasteAction, toolBar);
    addActionToToolBar(undoAction, toolBar);
    addActionToToolBar(redoAction, toolBar);
    toolBar.addSeparator();
    addActionToToolBar(computeParametersAction, toolBar);
    toolBar.addSeparator();
    addActionToToolBar(startAndResumeComputationAction, toolBar);
    addActionToToolBar(suspendComputationAction, toolBar);
    addActionToToolBar(stopComputationAction, toolBar);
    
    toolBar.setMargin(new Insets(1,1,1,1));
    
    // Attach the tool bar to the content pane
    getContentPane().add(toolBar, BorderLayout.NORTH);
    
    // Menu bar initialization
    JMenuBar menuBar = new JMenuBar();
    JMenu menu;
    
    // File menu
    menu = new JMenu("File");
    menuBar.add(menu);
    menu.add(newDocumentAction);
    menu.add(openDocumentAction);
    menu.add(saveDocumentAction);
    menu.addSeparator();
    JMenuItem mi = new JMenuItem("Exit");
    mi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt)
        {
          exit();
        }
    });
    menu.add(mi);
    
    // Edit menu
    menu = new JMenu("Edit");
    menuBar.add(menu);
    menu.add(cutAction);
    menu.add(copyAction);
    menu.add(pasteAction);
    menu.addSeparator();
    menu.add(undoAction);
    menu.add(redoAction);
    
    // Compute menu
    menu = new JMenu("Compute");
    menuBar.add(menu);
    menu.add(computeParametersAction);
    menu.addSeparator();
    menu.add(startAndResumeComputationAction);
    menu.add(suspendComputationAction);
    menu.add(stopComputationAction);
    
    // Attach the menu bar to this frame
    setJMenuBar(menuBar);
  }
  
  /**
   * Saves current document.  This method returns true if the document
   * was successfully saved, and false otherwise.
   */
  private boolean saveDocument()
  {
    int returnVal = fc.showSaveDialog(MiniGUI.this);
    
    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File file = fc.getSelectedFile();
      
      try
      {
        Writer writer = new BufferedWriter(new FileWriter(file));
        txtInput.write(writer);
        writer.close();
      }
      catch (java.io.IOException err)
      {
        JOptionPane.showMessageDialog(MiniGUI.this,
          err.getMessage(),
          "I/O Error",
        JOptionPane.ERROR_MESSAGE);
        return false;
      }
      documentChanged = false;
      return true;
    }
    else
    {
      return false;
    }
  }
  
  /**
   * Checks whether the document has changed before performing some action
   * that would lose the document (such as open file or exit).  This
   * method returns true if the action can be performed and false otherwise.
   */
  private boolean checkDocumentChanged()
  {
    if (! documentChanged)
    {
      return true;
    }
    else if (fc == null)
    {
      // No access to local disk.
      int ret = JOptionPane.showConfirmDialog(this,
        "Current document will be lost.  Do you still want to continue?",
        "Warning",
        JOptionPane.YES_NO_OPTION,
      JOptionPane.WARNING_MESSAGE);
      return (ret == JFileChooser.APPROVE_OPTION);
    }
    else
    {
      int ret = JOptionPane.showConfirmDialog(this,
        "Current document has changed.  Do you want to save it?",
        "Warning",
        JOptionPane.YES_NO_CANCEL_OPTION,
      JOptionPane.WARNING_MESSAGE);
      switch (ret)
      {
        case JOptionPane.YES_OPTION:
          return saveDocument();
        
        case JOptionPane.NO_OPTION:
          return true;
        
        default:
          return false;
      }
    }
  }
  
  /**
   * Unregisters listeners on current document and reset undo manager.
   * This method shall be called just before setting a new document.
   */
  private void documentPreSet()
  {
    txtInput.getDocument().removeUndoableEditListener(undoEditListener);
    txtInput.getDocument().removeDocumentListener(documentChangedListener);
    undoManager.discardAllEdits();
  }
  
  /**
   * Registers listeners on current document and update undo/redo actions.
   * This method shall be called just after setting a new document.
   */
  private void documentPostSet()
  {
    documentChanged = false;
    txtInput.getDocument().addUndoableEditListener(undoEditListener);
    txtInput.getDocument().addDocumentListener(documentChangedListener);
    undoAction.update();
    redoAction.update();
  }
  
  /**
   * Class defining a label displaying the line number and the column
   * number of the caret in the text component it listens to.
   */
  class LineAndColumn extends JLabel implements CaretListener {
    LineAndColumn ()
    {
      super("Line: 1     Column: 0", SwingConstants.CENTER);
    }
    
    public void caretUpdate(CaretEvent e)
    {
      try
      {
        int dot = e.getDot();
        int line = txtInput.getLineOfOffset(dot);
        int column = dot - txtInput.getLineStartOffset(line);
        setText("Line: " + String.valueOf(line + 1) +
        "     Column: " + String.valueOf(column));
      }
      catch (BadLocationException ble)
      {
        // This shall not happen
        ble.printStackTrace();
      }
    }
  }
  
  /**
   * Class defining an action to create a new document.
   */
  class NewDocumentAction extends AbstractAction
  {
    NewDocumentAction()
    {
      super("New");
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      if (checkDocumentChanged())
      {
        documentPreSet();
        txtInput.setDocument(new PlainDocument());
        documentPostSet();
      }
    }
  }
  
  /**
   * Class defining an action to open a document.
   */
  class OpenDocumentAction extends AbstractAction
  {
    OpenDocumentAction()
    {
      super("Open...");
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      if (checkDocumentChanged())
      {
        int returnVal = fc.showOpenDialog(MiniGUI.this);
        
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
          File file = fc.getSelectedFile();
          
          try
          {
            documentPreSet();
            Reader reader = new BufferedReader(new FileReader(file));
            txtInput.read(reader, null);
            reader.close();
            documentPostSet();
          }
          catch (java.io.IOException err)
          {
            JOptionPane.showMessageDialog(MiniGUI.this,
              err.getMessage(),
              "I/O Error",
            JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    }
  }
  
  /**
   * Class defining an action to save a document.
   */
  class SaveDocumentAction extends AbstractAction
  {
    SaveDocumentAction()
    {
      super("Save...");
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      saveDocument();
    }
  }
  
  /**
   * Class defining the undo action.
   */
  class UndoAction extends AbstractAction
  {
    UndoAction()
    {
      super("Undo");
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      try
      {
        undoManager.undo();
      }
      catch (CannotUndoException err)
      {
        // This shall not happen
        err.printStackTrace();
      }
      update();
      redoAction.update();
    }
    
    protected void update()
    {
      setEnabled(undoManager.canUndo());
    }
  }
  
  /**
   * Class defining the redo action.
   */
  class RedoAction extends AbstractAction
  {
    RedoAction()
    {
      super("Redo");
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      try
      {
        undoManager.redo();
      }
      catch (CannotRedoException err)
      {
        // This shall not happen
        err.printStackTrace();
      }
      update();
      undoAction.update();
    }
    
    protected void update()
    {
      setEnabled(undoManager.canRedo());
    }
  }
  
  /**
   * Class defining an action to bring the Ray tracing parameters dialog.
   */
  class ComputeParametersAction extends AbstractAction
  {
    ComputeParametersAction()
    {
      super("Ray tracing parameters...");
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      computeParametersDialog.pack();
      computeParametersDialog.setVisible(true);
    }
  }
  
  /**
   * Class defining a thread computing the image.  The renderer creation
   * is part of the run() method.  This thread may be suspended, resumed
   * and stopped and it updates the start/resume, suspend and stop actions
   * accordingly.
   */
  class ComputingImageThread extends Thread
  {
    private ImageFrame imageFrame;
    
    public void run()
    {
      Renderer renderer;
      
      // First disable start/resume action
      startAndResumeComputationAction.setEnabled(false);
      
      // Parse scene
      Scene scene = null;
      try {
        Reader in = new StringReader(txtInput.getText());
        ObjectReader reader = new ObjectReader(in);
        scene = (Scene) reader.readObject();
      }
      catch (IOException err) {
        txtLog.append(err.toString() + "\n");
        
        // Enable start/resume action for the next computation
        startAndResumeComputationAction.setEnabled(true);
        
        return;
      }
      
      // Init renderer
      Resolution resolution = new Resolution(computeParametersDialog.xResol,
      computeParametersDialog.yResol);
      renderer = Util.getRenderer(scene,
        resolution,
        computeParametersDialog.exact,
        computeParametersDialog.depth,
        computeParametersDialog.diadic,
      computeParametersDialog.antialiasLevel);
      
      // Init image frame and show it
      BufferedImage image = renderer.getImage();
      imageFrame = new ImageFrame("Raja's image", image, 100);
      imageFrame.pack();
      imageFrame.setUpdating(true);
      imageFrame.setVisible(true);
      
      // Init progress bar
      progressBar.setModel(renderer.getModel());
      
      // Enable suspend and stop actions
      suspendComputationAction.setEnabled(true);
      stopComputationAction.setEnabled(true);
      
      // Compute, tuning thread priority...
      int oldPriority = getPriority();
      setPriority(Thread.MIN_PRIORITY);
      renderer.run();
      setPriority(oldPriority);
      
      // Computation finished
      
      // Disable suspend and stop actions
      suspendComputationAction.setEnabled(false);
      stopComputationAction.setEnabled(false);
      
      // Image frame should not update its image anymore
      imageFrame.setUpdating(false);
      
      // Reset progress bar
      progressBar.setValue(0);
      
      // Enable start/resume action for the next computation
      startAndResumeComputationAction.setEnabled(true);
    }
    
    void setResumed()
    {
      imageFrame.setUpdating(true);
      startAndResumeComputationAction.setEnabled(false);
      suspendComputationAction.setEnabled(true);
      super.resume();
    }
    
    void setSuspended()
    {
      imageFrame.setUpdating(false);
      suspendComputationAction.setEnabled(false);
      startAndResumeComputationAction.setEnabled(true);
      super.suspend();
    }
    
    void setStopped()
    {
      imageFrame.setUpdating(false);
      progressBar.setValue(0);
      suspendComputationAction.setEnabled(false);
      stopComputationAction.setEnabled(false);
      startAndResumeComputationAction.setEnabled(true);
      super.stop();
    }
  }
  
  /**
   * Class defining an action to start/resume the computation.
   */
  class StartAndResumeComputationAction extends AbstractAction
  {
    StartAndResumeComputationAction()
    {
      super("Start/resume");
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      if ((computingImage == null) || (! computingImage.isAlive()))
      {
        // No currently running thread
        // Start a new one
        computingImage = new ComputingImageThread();
        computingImage.start();
      }
      else
      {
        // Resume currently running thread
        computingImage.setResumed();
      }
    }
  }
  
  /**
   * Class defining an action to suspend the computation.
   */
  class SuspendComputationAction extends AbstractAction
  {
    SuspendComputationAction()
    {
      super("Suspend");
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      // Suspend currently running thread
      computingImage.setSuspended();
    }
  }
  
  /**
   * Class defining an action to stop the computation.
   */
  class StopComputationAction extends AbstractAction
  {
    StopComputationAction()
    {
      super("Stop");
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      // Stop currently running thread
      computingImage.setStopped();
    }
  }
  
  /**
   * Main method. Starts a new MiniGUI.
   */
  public static void main(String args[])
  {
    MiniGUI rajaGUI = new MiniGUI();
    rajaGUI.pack();
    rajaGUI.setVisible(true);
    if (rajaGUI.fc == null)
    {
      // No access to local disk.  Inform the user.
      JOptionPane.showMessageDialog(rajaGUI,
        "Raja's Mini GUI is running in a restricted\n" +
        "environnment (no access to local disk).\n" +
        "Open/save functions are disabled.",
        "Warning",
      JOptionPane.WARNING_MESSAGE);
    }
  }
}
