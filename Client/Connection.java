import java.io.*;
import java.net.*;

public class Connection extends Thread
{
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private Server server;
  
  public Connection(Socket socket, Server server) throws IOException
  {
    this.socket = socket;
    this.server = server;
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new PrintWriter(socket.getOutputStream(), true);
    start();
  }
  
  public void run()
  {
    try
    {
      while (true)
      {
        String message = in.readLine();
        server.sendToAll(message);
      }
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public void send(String message)
  {
    out.println(message);
  }
}