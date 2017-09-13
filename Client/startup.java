import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class startup implements ActionListener {
  //create array of string that stores connections on index(connection - 1)
  //ask user name while waiting
  JTextField field;
  private String name;  
  
  
  JTextField nameI;
  
  JButton submit;
  JButton highscores;
  
//STYLE THESE BUTTONS
//  JButton singleplayer;
//  JButton multilayer;
//  JButton highscores;
  
  JFrame frame; 
  Game g;
  
  public startup(Game a) {
   g = a;
    //start();  
    name = "";
    frame = new JFrame("start screen");
    frame.setSize(500, 590);
    
    JLabel name = new JLabel("Type your character name here: ");
    JLabel bg = new JLabel(new ImageIcon("Startup.png"));
    
    submit = new JButton("submit");
    highscores = new JButton("Highscores");
    nameI = new JTextField(20);
    
    frame.add(highscores);
    frame.add(name);    
    frame.add(nameI);
    frame.add(submit);
    frame.add(bg);
   
    frame.setLayout(new FlowLayout());
    
    submit.addActionListener(this);
    
    frame.setVisible(true);
    
    
    
  }
  

  
  public void run() {
    
    
  }
  
  public void actionPerformed(ActionEvent e)
  {
    name = nameI.getText(); 
    g.sendName();

  }
  
  public String getName() {
    
      return "name:" + name;
    
  }
    public void exit() {
      
      frame.setVisible(false);
      
    }
  }
