/* $Id: ComputeParametersDialog.java,v 1.1.1.1 2001/01/08 23:10:15 gregoire Exp $
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.Border;


/**
 * Class defining the dialog for the ray tracing parameters.
 */
class ComputeParametersDialog extends JDialog
{
  private NumberTextField xResolTextField, yResolTextField, depthTextField;
  private JRadioButton defaultSamplingRadioButton, diadicSamplingRadioButton, antialiasSamplingRadioButton;
  private JComboBox antialiasLevelComboBox;
  private JCheckBox exactCheckBox;
  private String antialiasLevelString;
  
  int xResol, yResol, depth;
  int antialiasLevel;
  boolean diadic, antialias, exact;
  
  ComputeParametersDialog(Frame owner)
  {
    super(owner, "Ray tracing parameters", true);
    
    // Closing the dialog has the same effect as cancel
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e)
        {
          restoreParameters();
          setVisible(false);
        }
    });
    
    // Init fields of this dialog
    
    // Init text fields
    xResolTextField = new NumberTextField(256);
    yResolTextField = new NumberTextField(192);
    depthTextField = new NumberTextField(5);
    
    // Init radio buttons
    defaultSamplingRadioButton = new JRadioButton("Default");
    diadicSamplingRadioButton = new JRadioButton("Diadic");
    antialiasSamplingRadioButton = new JRadioButton("Antialias");
    ButtonGroup samplingGroup = new ButtonGroup();
    samplingGroup.add(defaultSamplingRadioButton);
    samplingGroup.add(diadicSamplingRadioButton);
    samplingGroup.add(antialiasSamplingRadioButton);
    diadicSamplingRadioButton.setSelected(true);
    
    // Init antialiasLevel combo box
    String[] antialiasLevels = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
    antialiasLevelComboBox = new JComboBox(antialiasLevels);
    antialiasLevelComboBox.setSelectedIndex(1);
    
    // Init exact check box
    exactCheckBox = new JCheckBox("Exact");
    exactCheckBox.setSelected(false);
    
    // Save these parameters
    saveParameters();
    
    // Enable/Disable antialiasLevel combo box according to the state
    // of the antialias radio button
    antialiasLevelComboBox.setEnabled(false);
    antialiasSamplingRadioButton.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e)
        {
          antialiasLevelComboBox.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        }
    });
    
    // Init buttons
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // Check that the text fields are non empty
          String emptyTxtField = null;
          
          if (xResolTextField.getText().equals(""))
          {
            emptyTxtField = "X Resolution";
          }
          else if (yResolTextField.getText().equals(""))
          {
            emptyTxtField = "Y Resolution";
          }
          else if (depthTextField.getText().equals(""))
          {
            emptyTxtField = "Depth";
          }
          
          if (emptyTxtField != null)
          {
            // At least one text field is empty
            // Show an error message
            JOptionPane.showMessageDialog(ComputeParametersDialog.this,
              emptyTxtField + " field cannot be empty.",
              "Empty Field Error",
            JOptionPane.ERROR_MESSAGE);
            return;
          }
          
          // Text fields are non empty
          // Save parameters and hide
          saveParameters();
          setVisible(false);
        }
    });
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          restoreParameters();
          setVisible(false);
        }
    });
    
    // Finally, layout the GUI
    JPanel pane = new JPanel();
    setContentPane(pane);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    
    // Init the resolution pane
    JPanel paneResolution = new JPanel();
    paneResolution.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Resolution"),
    emptyBorder));
    paneResolution.setLayout(new GridLayout(2, 2, 5, 5));
    paneResolution.add(new JLabel("X resolution"));
    paneResolution.add(xResolTextField);
    paneResolution.add(new JLabel("Y resolution"));
    paneResolution.add(yResolTextField);
    pane.add(paneResolution);
    
    pane.add(Box.createRigidArea(new Dimension(0, 10)));
    
    // Init the sampling pane
    JPanel paneSampling = new JPanel();
    paneSampling.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Sampling"),
    emptyBorder));
    paneSampling.setLayout(new GridLayout(3, 1, 5, 5));
    paneSampling.add(defaultSamplingRadioButton);
    paneSampling.add(diadicSamplingRadioButton);
    Box antialiasBox = new Box(BoxLayout.X_AXIS);
    antialiasBox.add(antialiasSamplingRadioButton);
    antialiasBox.add(antialiasLevelComboBox);
    antialiasBox.add(Box.createHorizontalGlue());
    paneSampling.add(antialiasBox);
    pane.add(paneSampling);
    
    pane.add(Box.createRigidArea(new Dimension(0, 15)));
    
    // Init the recursivity pane
    JPanel paneRecursivity = new JPanel();
    paneRecursivity.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Recursivity"),
    emptyBorder));
    paneRecursivity.setLayout(new GridLayout(2, 2, 5, 5));
    paneRecursivity.add(new JLabel("Depth"));
    paneRecursivity.add(depthTextField);
    paneRecursivity.add(exactCheckBox);
    pane.add(paneRecursivity);
    
    pane.add(Box.createRigidArea(new Dimension(0, 15)));
    
    // Init the button pane
    JPanel paneButtons = new JPanel();
    paneButtons.setLayout(new BoxLayout(paneButtons, BoxLayout.X_AXIS));
    paneButtons.add(Box.createHorizontalGlue());
    paneButtons.add(okButton);
    paneButtons.add(Box.createRigidArea(new Dimension(10, 0)));
    paneButtons.add(cancelButton);
    pane.add(paneButtons);
  }
  
  /**
   * Saves the parameters in the package accessible fields.
   */
  private void saveParameters()
  {
    xResol = xResolTextField.getValue();
    yResol = yResolTextField.getValue();
    depth = depthTextField.getValue();
    
    antialias = antialiasSamplingRadioButton.isSelected();
    antialiasLevelString = (String) antialiasLevelComboBox.getSelectedItem();
    antialiasLevel = antialias ? Integer.parseInt(antialiasLevelString) : 0;
    
    diadic = diadicSamplingRadioButton.isSelected();
    exact = exactCheckBox.isSelected();
  }
  
  /**
   * Restores the parameters from the package accessible fields.
   */
  private void restoreParameters()
  {
    xResolTextField.setValue(xResol);
    yResolTextField.setValue(yResol);
    depthTextField.setValue(depth);
    
    antialiasLevelComboBox.setSelectedItem(antialiasLevelString);
    
    diadicSamplingRadioButton.setSelected(diadic);
    antialiasSamplingRadioButton.setSelected(antialias);
    exactCheckBox.setSelected(exact);
  }
}


/**
 * Class defining a text field accepting only digit input characters.
 */
class NumberTextField extends JTextField
{
  private Toolkit toolkit;
  
  NumberTextField(int value)
  {
    super(String.valueOf(value));
    toolkit = Toolkit.getDefaultToolkit();
  }
  
  int getValue()
  {
    int retVal;
    retVal = Integer.parseInt(getText());
    return retVal;
  }
  
  void setValue(int value)
  {
    setText(String.valueOf(value));
  }
  
  protected Document createDefaultModel()
  {
    return new WholeNumberDocument();
  }
  
  class WholeNumberDocument extends PlainDocument
  {
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
    {
      for (int i = 0 ; i < str.length() ; i++)
      {
        if (! Character.isDigit(str.charAt(i)))
        {
          toolkit.beep();
          return;
        }
      }
      super.insertString(offs, str, a);
    }
  }
}
