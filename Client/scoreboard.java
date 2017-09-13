import java.awt.*;
import javax.swing.*;
import java.awt.Color;

public class scoreboard {
  
  private JFrame scoreboard;
  JLabel win;
  private String winner;
  private boolean isVisible;
  
  
  public scoreboard() {
    
    isVisible = false;
    scoreboard = new JFrame("Time remaining: 5");
    scoreboard.setSize(500, 500);
    scoreboard.setBackground(new Color(200, 200, 200));
    scoreboard.setLayout(new FlowLayout());
    
    
    
    //(Personal User): " + "(int) total wins implement soon
    win = new JLabel("<html><br>" + 
                     getWinner() + " won the round </br> </html>");
    
    win.setHorizontalTextPosition(JLabel.CENTER);
    win.setVerticalTextPosition(JLabel.BOTTOM);
    
    scoreboard.add(win);
    
    scoreboard.setVisible(isVisible);
  }
  
  public static void main(String[] argc) {
    
    scoreboard board = new scoreboard();
  }
  
  public void setTitle(int time) {
    scoreboard.setTitle("Time remaining: " + time);
  }
  
  public void setWinner(String name) {
    winner = name;
  win.setText(getWinner() + " won the round");
  
  
  }
  
  public String getWinner() {
  
   
    return winner;
 
  }
  
  
  public void toggleVisible() {
    if(isVisible == false) {
    scoreboard.setVisible(true);
    isVisible = true;
    }
    else { 
     scoreboard.setVisible(false);
    isVisible = false;
    }
  }
}