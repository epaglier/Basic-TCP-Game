import java.io.*;
import java.net.*;

public class Game extends Thread {
  //display properties
  private GridDisplay display;
  private Location location;
  private String image;
  
  
  //scoreboard
  private scoreboard scoreboard;
  private int time;
 
  //communication
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  
  //startup menu
  private startup menu;
  boolean sendName;
  
  public Game() throws IOException {
    
    scoreboard = new scoreboard();
 menu = new startup(this);
    sendName = false;
    
    //connect to server
    socket = new Socket("localhost", 8000);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new PrintWriter(socket.getOutputStream(), true);
    
    //create GridDisplay with 3 rows and 5 columns, and title "Game"
    String[] command = in.readLine().split(":");
    if (command[0].equals("size"))
    {
      display = new GridDisplay(Integer.parseInt(command[1]), Integer.parseInt(command[2]));
    }
    display.setTitle("Robot wars");
    
    start();
  }
  
  public void run() {
    
    try {
      
      while (true)
      {
        boolean menuExit = false; 
        
        if (!menuExit) {
          menu.exit();
          menuExit = true;
        }
        
        String[] command = in.readLine().split(":");
        
        //changes title of scoreboard to update the time as it increments
        if(command[0].equals("time")) {
          if(command[1].equals("5") || command[1].equals("0"))
               scoreboard.toggleVisible();
             
          scoreboard.setTitle(Integer.parseInt(command[1]));
        }
        
        //sets the winner to be name given by server so scoreboard can update winner
        else if(command[0].equals("winner")) { //lowercase
          
  
          scoreboard.setWinner(command[1]);
          
          
        }
        if (command[0].equals("paint"))
        {
          //Menu
          menuExit = true;
          
          if (command.length == 4)
            paint(command[1], Integer.parseInt(command[2]), Integer.parseInt(command[3]));
          
          else if (command.length == 5)
            paint(command[1], command[2], Integer.parseInt(command[3]), Integer.parseInt(command[4]));
          
          //paint:robot: String type, String direction, int player, int row, int col
          else if(command.length == 6) 
            paint(command[1], command[2], Integer.parseInt(command[3]), 
                  Integer.parseInt(command[4]), Integer.parseInt(command[5]));
          
        }
        
      }
    }
    catch (IOException e)
    {
      System.out.println("Something went wrong");
    }
  }
  
  public void play()
  {
    //out.println(startScreen.getName());
    while (true)
    {
      //wait 100 milliseconds for keyboard
      display.pause(100);
      
      //check if any keys pressed
      int key = display.checkLastKeyPressed(); 
      
      if (key != -1)
      {
        //some key was pressed
        if (key == 87)
        {
          //w key was pressed
          sendMove();
        }
        if (key == 65)
        {
          sendTurn();
        }
        if(key == 68) {
          
          out.println("fire");
          
        }
        
      }
    }
  }
  
  //called when the user presses a key.
  //each key on the keyboard has a unique key code.
  //that key code is passed to this method.
  private void keyPressed(int key)
  {
    //print key code
    System.out.println("key code:  " + key);
  }
  
  //pre: key is pressed
  //post: sends message to server in format("move:row:col")
  public void sendMove() {
    
    out.println("move");
    
  }
  
  
  public void paint(String type, int row, int col)
  {
    if (type.equals("wall"))
      display.setImage(new Location(row,col), "wall.gif");
    else if (type.equals("empty"))
    {
      display.setImage(new Location(row,col), null);
      display.setColor(new Location(row, col), new Color(230, 170, 120));
    }
    
  }
  
  public void paint(String type, String direction, int row, int col)
  {
    //paint:bullet:direction:r:c
    if(type.equals("bullet")) {
      display.setImage(new Location(row, col), "Bullet" + direction + ".png");
    }
  }
  
  public void paint(String type, String direction, int player, int row, int col) {
    
    if (type.equals("robot")) {
      
      display.setImage(new Location(row, col), "robot" + player + direction + ".gif");
      display.setColor(new Location(row, col), new Color(230, 170, 120));
      
    }
    
  }
  
  public static void main(String[] args) {
    
    
    try {
      Game g = new Game();
      g.play();
    }
    catch (IOException e) {
      System.out.println("Didn't start");
      System.exit(0);
    }
  }
  
  public void sendTurn() {
    
    out.println("turn");
  }
  
  public void sendName() {
    if(!sendName) {
      
      out.println(menu.getName());
      sendName = true;
      
    }
    
  }
}
