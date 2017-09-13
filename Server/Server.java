import java.io.*;
import java.net.*;
import java.util.*; 
import java.util.concurrent.*;

public class Server
{
  public static void main(String[] args) throws IOException
  {
    new Server("test.txt");
  }
  
  private ArrayList<Connection> connections;
  
  private Cell[][] display;
  private ConcurrentLinkedQueue<Storage> queue;
  private Cell[][] original;
  private ArrayList<Robot> robots;
  private ArrayList<String> names;
  private boolean startUp;
  private ArrayList<String> winTally;
  
  public Server(String mapFileName) throws IOException
  {
    robots = new ArrayList<Robot>();
    names = new ArrayList<String>();
    winTally = new ArrayList<String>();
    
    while (Util.hasMoreLines("highscore.txt"))
      winTally.add(Util.readLine("highscore.txt"));
    Util.close("highscore.txt");
    
    startUp = true;
    
    queue = new ConcurrentLinkedQueue<Storage>();
    ServerSocket serverSocket = new ServerSocket(8000);
    connections = new ArrayList<Connection>();
    
    //Feinbergs code modified
    ArrayList<String> lines = new ArrayList<String>();
    while (Util.hasMoreLines(mapFileName))
      lines.add(Util.readLine(mapFileName));
    Util.close(mapFileName);
    
    int numRows = lines.size();
    int numCols = lines.get(0).length();
    
    //important
    display = new Cell[numRows][numCols];
    int robotNumber = 1;
    
    for (int row = 0; row < numRows; row++)
    {
      for (int col = 0; col < numCols; col++)
      {
        if (lines.get(row).length() != numCols)
          throw new RuntimeException("Inconsistent line length in map file \"" + mapFileName + "\"");
        
        char ch = lines.get(row).charAt(col);
        if ("NSEWnsew".indexOf(ch) != -1)
        {
          
          if (ch == 'N' || ch == 'n')
          {
            display[row][col] = new Robot("N", robotNumber);
            Connection temp = new Connection(serverSocket.accept(), this, (Robot)(display[row][col]));
            connections.add(temp);
            robotNumber++;
          }
          else if (ch == 'S' || ch == 's')
          {
            display[row][col] = new Robot("S", robotNumber);
            Connection temp = new Connection(serverSocket.accept(), this, (Robot)(display[row][col]));
            connections.add(temp);
            robotNumber++;
          }
          else if (ch == 'E' || ch == 'e')
          {
            display[row][col] = new Robot("E", robotNumber);
            Connection temp = new Connection(serverSocket.accept(), this, (Robot)(display[row][col]));
            connections.add(temp);
            robotNumber++;
          }
          else
          {
            display[row][col] = new Robot("W", robotNumber);
            Connection temp = new Connection(serverSocket.accept(), this, (Robot)(display[row][col]));
            connections.add(temp);
            robotNumber++;
          }
        }
        else if (ch == 'X')
        {
          display[row][col] = new Wall();
        }
        else if (ch == '.')
          display[row][col] = new Empty();
        else
          throw new RuntimeException("Invalid character '" + ch + "' in map file \"" + mapFileName + "\"");
      }
    }
    System.out.println("made it out!!");
    
    //create the origional map again
    original = new Cell[display.length][display[0].length];
    for (int r = 0; r < display.length; r++)
    {
      for (int c = 0; c < display[0].length; c++)
      {
        original[r][c] = display[r][c];
      }
    }
    
    //wait for all names to be entered
    while(names.size() < connections.size())
    {
      System.out.println(names.size() + "");
    }
    
    //sleep
    try{Thread.sleep(100);}
    catch (InterruptedException e){}
    
    
    //Ask Feinberg about ConcurrentModificationException
    //
    PrintWriter out = new PrintWriter(new FileWriter("highscore.txt"), true);
    Boolean b = true;
    for (int i = 0; i < names.size(); i++)
    {
      for (int w = 0; w < winTally.size(); w++)
      {
        String[] s = winTally.get(w).split(":");
        if (names.get(i).equals(s[0]))
        {
          System.out.println("did it");
          b = false;
        }
      }
      if (b)
      {
        winTally.add(names.get(i) + ":0");
      }
      try {reWrieHighscores();}
      catch (IOException e){}
    }
    
    //send the map to all clients
    sendMap(display);
    
    //start executing commands
    step();
  }
  
  public void commands(String message, Connection me) throws IOException
  {
    if (message == null)
    {
      //has reconnecting problem
      connections.remove(me);
      System.out.println("user left");
    }
    else
    {
      String[] command = message.split(":");
      System.out.println(command[0]);
      if (command[0].equals("move"))
      {
        //move(me.getRobot());
        queue.add(new Storage("move", me));
      }
      else if (command[0].equals("fire"))
      {
        //fire(me.getRobot());
        queue.add(new Storage("fire", me));
      }
      else if (command[0].equals("turn"))
      {
        queue.add(new Storage("turn", me));
      }
      else if (command[0].equals("name") && command.length > 1)
      {
        System.out.println(command[1]);
        me.getRobot().setNombre(command[1]);
        names.add(command[1]);
        
        boolean b = false;
        for (int i = 0; i < winTally.size(); i++)
        {
          String[] tem = winTally.get(i).split(":");
          System.out.println(tem[0] + ":" + command[1]);
          if(command[1].equals(tem[0]))
          {
            b = true;
          }
        }
        if (!b)
        {
          winTally.add(command[1] + ":" + 0);
          reWrieHighscores();
        }
      }
      else if (command[0].equals("highscores"))
      {
        sendHighScores(me);
        System.out.println("Sending highscores...");
      }
    }
  }
  
  public void move(Robot robot)
  {
    for (int r = 0; r < display.length; r++)
    {
      for (int c = 0; c < display[0].length; c++)
      {
        if (robot == display[r][c])
        {
          String direction = robot.getDirection();
          if (direction.equals("N"))
          {
            if (isValidLoc(r - 1, c))
            {
              display[r - 1][c] = display[r][c];
              display[r][c] = new Empty();
              
              //send to client
              for (Connection connection: connections)
              {
                connection.println("paint:robot:N:" + ((Robot)display[r - 1][c]).getNumber() + ":" + (r - 1) + ":" + c);
                connection.println("paint:empty:" + r + ":" + c);
              }
            }
          }
          else if (direction.equals("W"))
          {
            if (isValidLoc(r, c - 1))
            {
              display[r][c - 1] = display[r][c];
              display[r][c] = new Empty();
              for (Connection connection: connections)
              {
                connection.println("paint:robot:W:" + ((Robot)display[r][c - 1]).getNumber() + ":" +  r + ":" + (c - 1));
                connection.println("paint:empty:" + r + ":" + c);
              }
            }
          }
          else if (direction.equals("S"))
          {
            if (isValidLoc(r + 1, c))
            {
              display[r + 1][c] = display[r][c];
              display[r][c] = new Empty();
              
              for (Connection connection: connections)
              {
                connection.println("paint:robot:S:" + ((Robot)display[r + 1][c]).getNumber() + ":" + (r + 1) + ":" + c);
                connection.println("paint:empty:" + r + ":" + c);
              }
            }
          }
          else if (direction.equals("E"))
          {
            if (isValidLoc(r, c + 1))
            {
              display[r][c + 1] = display[r][c];
              display[r][c] = new Empty();
              
              for (Connection connection: connections)
              {
                connection.println("paint:robot:E:" + ((Robot)display[r][c + 1]).getNumber() + ":" + r + ":" + (c + 1));
                connection.println("paint:empty:" + r + ":" + c);
              }
            }
          }
          return;
        }
      }
    }
  }
  
  public void turnLeft(Robot robot)
  {
    robot.turn();
  }
  
  public boolean isValidLoc(int row, int col)
  {
    if (row < 0 || col < 0)
      return false;
    else if (row >= display.length || col >= display[0].length)
      return false;
    else
    {
      if (display[row][col] instanceof Wall)
        return false;
      else if (display[row][col] instanceof Robot)
        return false;
      else
        return true;
    }
  }
  
  public boolean isValidLocNoRobot(int row, int col)
  {
    if (row < 0 || col < 0)
      return false;
    else if (row >= display.length || col >= display[0].length)
      return false;
    else
    {
      if (display[row][col] instanceof Wall)
        return false;
      else
        return true;
    }
  }
  
  
  
  public void sendMap(Cell[][] map)
  {
    for (Connection connection: connections)
    {
      connection.println("size:" + map.length + ":" + map[0].length);
      for (int row = 0; row < map.length; row++)
      {
        for (int col = 0; col < map[0].length; col++)
        {
          //edit later
          if (map[row][col] instanceof Wall)
            connection.println("paint:wall:" + row + ":" + col);
          else if (map[row][col] instanceof Robot)
          {
            connection.println("paint:robot:" + ((Robot)map[row][col]).getDirection() + ":" + ((Robot)map[row][col]).getNumber() +  ":" + row + ":" + col);
          }
          else if (map[row][col] instanceof Empty)
            connection.println("paint:empty:" + row + ":" + col);
        }
      }
    }
    for (int row = 0; row < map.length; row++)
    {
      for (int col = 0; col < map[0].length; col++)
      {
        if (map[row][col] instanceof Robot)
        {
          robots.add((Robot)map[row][col]);
        }
      }
    }
    System.out.println(robots.size());
  }
  
  
  
  
  
  public void fire(Robot robot)
  {
    for (int r = 0; r < display.length; r++)
    {
      for (int c = 0; c < display[0].length; c++)
      {
        if (robot == display[r][c])
        {
          String direc = ((Robot)display[r][c]).getDirection();
          if (direc.equals("N") && !(display[r-1][c] instanceof Wall))
          {
            if (display[r-1][c] instanceof Robot)
            {
              for (Connection connection: connections)
              {
                if (connection.getRobot() == display[r-1][c])
                {
                  connection.println("hit");
                  connection.getRobot().hit();
                  if (connection.getRobot().getHealth() <= 0)
                  {
                    robots.remove(display[r - 1][c]);
                    display[r-1][c] = new Empty();
                    for (Connection connection2: connections)
                    {
                      connection2.println("paint:empty:" + (r-1) + ":" + c);
                    }
                  }
                }
              }
            }
            else
            {
              display[r - 1][c] = new Bullet("N", System.currentTimeMillis());
              for (Connection connection: connections)
              {
                connection.println("paint:bullet:N:" + (r-1) + ":" + c);
              }
            }
          }
          else if (direc.equals("W") && !(display[r][c-1] instanceof Wall))
          {
            if (display[r][c -1] instanceof Robot)
            {
              for (Connection connection: connections)
              {
                if (connection.getRobot() == display[r][c - 1])
                {
                  connection.println("hit");
                  connection.getRobot().hit();
                  if (connection.getRobot().getHealth() <= 0)
                  {
                    robots.remove(display[r][c - 1]);
                    display[r][c - 1] = new Empty();
                    for (Connection connection2: connections)
                    {
                      connection2.println("paint:empty:" + r + ":" + (c - 1));
                    }
                  }
                }
              }
            }
            else
            {
              display[r][c-1] = new Bullet("W", System.currentTimeMillis());
              for (Connection connection: connections)
              {
                connection.println("paint:bullet:W:" + r + ":" + (c - 1));
              }
            }
          }
          else if (direc.equals("S") && !(display[r+1][c] instanceof Wall))
          {
            if (display[r + 1][c] instanceof Robot)
            {
              for (Connection connection: connections)
              {
                if (connection.getRobot() == display[r + 1][c])
                {
                  connection.println("hit");
                  connection.getRobot().hit();
                  if (connection.getRobot().getHealth() <= 0)
                  {
                    robots.remove(display[r + 1][c]);
                    display[r + 1][c] = new Empty();
                    for (Connection connection2: connections)
                    {
                      connection2.println("paint:empty:" + (r+1) + ":" + c);
                    }
                  }
                }
              }
            }
            else
            {
              display[r + 1][c] = new Bullet("S", System.currentTimeMillis());
              for (Connection connection: connections)
              {
                connection.println("paint:bullet:S:" + (r+1) + ":" + c);
              }
            }
          }
          else if (direc.equals("E") && !(display[r][c+1] instanceof Wall))
          {
            if (display[r][c + 1] instanceof Robot)
            {
              for (Connection connection: connections)
              {
                if (connection.getRobot() == display[r][c + 1])
                {
                  connection.println("hit");
                  connection.getRobot().hit();
                  if (connection.getRobot().getHealth() <= 0)
                  {
                    robots.remove(display[r][c + 1]);
                    display[r][c + 1] = new Empty();
                    for (Connection connection2: connections)
                    {
                      connection2.println("paint:empty:" + r + ":" + (c+1));
                    }
                  }
                }
              }
            }
            else
            {
              display[r][c + 1] = new Bullet("E", System.currentTimeMillis());
              for (Connection connection: connections)
              {
                connection.println("paint:bullet:E:" + r + ":" + (c+ 1));
              }
            }
          }
          return;
        }
      }
    }
  } 
  
  
  
  
  
  
  
  public void step()
  {
    //run forever
    while (true)
    {
      //sleep
      try{Thread.sleep(10);}
      catch (InterruptedException e){}
      
      //if there is only one robot on the map
      if (robots.size() == 1)
      {
        //print the winner
        System.out.println("winner:" + robots.get(0).getNombre());
        
        //send the winner to connections
        for (Connection connection: connections)
        {
          connection.println("winner:" + robots.get(0).getNombre());
        }
        
        //increment the winners wins by one
        for (int i = 0; i < winTally.size(); i++)
        {
          String[] tem = winTally.get(i).split(":");
          if (robots.get(0).getNombre().equals(tem[0]))
          {
            winTally.set(i, tem[0] + ":" + (Integer.parseInt(tem[1]) + 1));
          }
        }
        
        //rewrites highscores
        try{reWrieHighscores();}
        catch (IOException e){System.out.println("Welp no highscores");}
        
        //sleep and send times
        try
        {
          for (Connection connection: connections)
            connection.println("time:5");
          Thread.sleep(1000);
          for (Connection connection: connections)
            connection.println("time:4");
          Thread.sleep(1000);
          for (Connection connection: connections)
            connection.println("time:3");
          Thread.sleep(1000);
          for (Connection connection: connections)
            connection.println("time:2");
          Thread.sleep(1000);
          for (Connection connection: connections)
            connection.println("time:1");
          Thread.sleep(1000);
          for (Connection connection: connections)
            connection.println("time:0");
        }
        catch (InterruptedException e){}
        
        //creates empty queue
        queue = new ConcurrentLinkedQueue<Storage>();
        
        //creates new robot lists
        robots = new ArrayList<Robot>();
        
        //full reset of the board
        reset();
        
        //output to consol
        System.out.println("reseting");
      }
      
      //when there is something in the queue do it
      while (!queue.isEmpty())
      {
        Storage temp = queue.poll();
        if (temp.getCommand().equals("move"))
          move(temp.getConnection().getRobot());
        else if (temp.getCommand().equals("turn"))
        {
          boolean bail = false;
          
          //turn the robot
          turnLeft(temp.getConnection().getRobot());
          
          //turn for client
          for (int r = 0; r < display.length; r++)
          {
            for (int c = 0; c < display[0].length; c++)
            {
              if (temp.getConnection().getRobot() == display[r][c] && !bail)
              {
                for (Connection connection: connections)
                {
                  connection.println("paint:robot:" + temp.getConnection().getRobot().getDirection() + ":" + temp.getConnection().getRobot().getNumber() + ":" + r + ":" + c);
                }
                bail = true;
              }
            }
          }
        }
        else if (temp.getCommand().equals("fire"))
        {
          fire(temp.getConnection().getRobot());
        }
      }
      
      long toCompare = System.currentTimeMillis();
      //needs to move all bullets at certain time
      for (int r = 0; r < display.length; r++)
      {
        for (int c = 0; c < display[0].length; c++)
        {
          if (display[r][c] instanceof Bullet && toCompare - ((Bullet)display[r][c]).getTimeCreated() > 100)
          {
            ((Bullet)display[r][c]).timeCreated(System.currentTimeMillis());
            advanceBullet((Bullet)display[r][c], r , c);
          }
        }
      }
    }
  }
  
  
  
  
  
  
  
  
  public void advanceBullet(Bullet bullet, int row, int col)
  {
    boolean wallInFront = false;
    if (bullet.getDirection().equals("N")) { wallInFront = display[row - 1][col] instanceof Wall;}
    if (bullet.getDirection().equals("S")) { wallInFront = display[row + 1][col] instanceof Wall;}
    if (bullet.getDirection().equals("W")) { wallInFront = display[row][col - 1] instanceof Wall;}
    if (bullet.getDirection().equals("E")) { wallInFront = display[row][col + 1] instanceof Wall;}
    //if it hits robot bullet stays, hit does not work
    if (!wallInFront)
    {
      if (isValidLocNoRobot(row - 1, col) && bullet.getDirection().equals("N"))
      {
        if (display[row - 1][col] instanceof Robot)
        {
          display[row][col] = new Empty();
          ((Robot)display[row - 1][col]).hit();
          for (Connection connection: connections)
          {
            connection.println("paint:empty:" + row + ":" + col);
          }
          if (((Robot)display[row - 1][col]).getHealth() <= 0)
          {
            robots.remove(display[row-1][col]);
            display[row - 1][col] = new Empty();
            for (Connection connection: connections)
            {
              connection.println("paint:empty:" + (row - 1) + ":" + col);
            }
          }
        }
        else
        {
          display[row][col] = new Empty();
          display[row - 1][col] = bullet;
          for (Connection connection: connections)
          {
            connection.println("paint:empty:" + row + ":" + col);
            connection.println("paint:bullet:N:" + (row - 1) + ":" + col);
          }
        }
      }
      else if (isValidLocNoRobot(row + 1, col) && bullet.getDirection().equals("S"))
      {
        if (display[row + 1][col] instanceof Robot)
        {
          display[row][col] = new Empty();
          ((Robot)display[row + 1][col]).hit();
          for (Connection connection: connections)
          {
            connection.println("paint:empty:" + row + ":" + col);
          }
          if (((Robot)display[row + 1][col]).getHealth() <= 0)
          {
            robots.remove(display[row + 1][col]);
            display[row + 1][col] = new Empty();
            for (Connection connection: connections)
            {
              connection.println("paint:empty:" + (row + 1) + ":" + col);
            }
          }
        }
        else
        {
          display[row][col] = new Empty();
          display[row + 1][col] = bullet;
          for (Connection connection: connections)
          {
            connection.println("paint:empty:" + row + ":" + col);
            connection.println("paint:bullet:S:" + (row + 1) + ":" + col);
          }
        }
      }
      else if (isValidLocNoRobot(row,col-1) && bullet.getDirection().equals("W"))
      {
        if (display[row][col - 1] instanceof Robot)
        {
          display[row][col] = new Empty();
          ((Robot)display[row][col - 1]).hit();
          for (Connection connection: connections)
          {
            connection.println("paint:empty:" + row + ":" + col);
          }
          if (((Robot)display[row][col - 1]).getHealth() <= 0)
          {
            robots.remove(display[row][col - 1]);
            display[row][col - 1] = new Empty();
            for (Connection connection: connections)
            {
              connection.println("paint:empty:" + row + ":" + (col - 1));
            }
          }
        }
        else
        {
          display[row][col] = new Empty();
          display[row][col - 1] = bullet;
          for (Connection connection: connections)
          {
            connection.println("paint:empty:" + row + ":" + col);
            connection.println("paint:bullet:W:" + row + ":" + (col - 1));
          }
        }
      }
      else if (isValidLocNoRobot(row, col + 1) && bullet.getDirection().equals("E"))
      {
        if (display[row][col + 1] instanceof Robot)
        {
          display[row][col] = new Empty();
          ((Robot)display[row][col + 1]).hit();
          for (Connection connection: connections)
          {
            connection.println("paint:empty:" + row + ":" + col);
          }
          if (((Robot)display[row][col + 1]).getHealth() <= 0)
          {
            robots.remove(display[row][col + 1]);
            display[row][col + 1] = new Empty();
            for (Connection connection: connections)
            {
              connection.println("paint:empty:" + row + ":" + (col + 1));
            }
          }
        }
        else
        {
          display[row][col] = new Empty();
          display[row][col + 1] = bullet;
          for (Connection connection: connections)
          {
            connection.println("paint:empty:" + row + ":" + col);
            connection.println("paint:bullet:E:" + row + ":" + (col + 1));
          }
        }
      }
    }
    else
    {
      display[row][col] = new Empty();
      for (Connection connection: connections)
      {
        connection.println("paint:empty:" + row + ":" + col);
      }
    }
  }
  
  public void reset()
  {
    for (int r = 0; r < original.length; r++)
    {
      for (int c = 0; c < original[0].length; c++)
      {
        if (original[r][c] instanceof Robot)
        {
          ((Robot)original[r][c]).reset();
          ((Robot)original[r][c]).resetHealth();
        }
        display[r][c] = original[r][c];
      }
    }
    sendMap(original);
  }
  
  public void reWrieHighscores() throws IOException
  {
    PrintWriter out = new PrintWriter(new FileWriter("highscore.txt"), true);
    for (String score: winTally)
      out.println(score);
    out.close();
    winTally = new ArrayList<String>();
    while (Util.hasMoreLines("highscore.txt"))
      winTally.add(Util.readLine("highscore.txt"));
    Util.close("highscore.txt");
  }
  
  public void sendHighScores(Connection connection)
  {
    sort(winTally);
    for (int i = 0; i < winTally.size(); i++)
    {
      String[] tem = winTally.get(i).split(":");
      connection.println("highscore:" + tem[0] + ":" + tem[1]);
    }
  }
  
  public void sort(ArrayList<String> a)
  {
    for (int i = 0; i < a.size(); i++)
    {
      int min = indexOfMin(a, i);
      String save = a.get(i);
      a.set(i, a.get(min));
      a.set(min, save);
    }
  }
  
  public static int indexOfMin(ArrayList<String> a, int startIndex)
  {
    int lowest = startIndex;
    String y = a.get(lowest);
    String[] temp = y.split(":");
    int variable1 = Integer.parseInt(temp[1]);
    for (int i = startIndex; i < a.size(); i++)
    {
      temp = a.get(i).split(":");
      int variable2 = Integer.parseInt(temp[1]);
      if (variable1 > variable2)
      {
        y = a.get(i);
        lowest = i;
      }
    }
    return lowest;
  }
}