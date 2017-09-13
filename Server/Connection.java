import java.io.*;
import java.net.*;

public class Connection extends Thread
{
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private Server server;
  private Robot robot;
  
  public Connection(Socket socket, Server server, Robot r) throws IOException
  {
    this.socket = socket;
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new PrintWriter(socket.getOutputStream(), true);
    this.server = server;
    robot = r;
    start();
  }
  
  public void run()
  {
    boolean loop = true;
    try
    {
      while (loop)
      {
        String line = in.readLine();
        server.commands(line, this);
        if (line == null)
        {
          loop = false;
        }
      }
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public void println(String message)
  {
    out.println(message);
  }
  
  public Robot getRobot()
  {
    return robot;
  }
  
  public String getLine() throws IOException
  {
    return in.readLine();
  }
}