import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class nameEntry {

  JTextField enterName;
  JLabel enter;
  
  boolean isVisible;
  String name;
  
  JFrame frame;
  
  
  public nameEntry() {
    frame = new JFrame();
    frame.setSize(500, 590);
    frame.setLayout(new FlowLayout());
    
    enter = new JLabel("Enter your username here:");
    enterName = new JTextField(20);
    
    isVisible = true;
    name = "";
    
    
    frame.add(enter);
    frame.add(enterName);
    
    frame.setVisible(true);
 
  }
  
  public static void main(String[] argc) {
    
  nameEntry entry = new nameEntry();
  
  }
}