public class Storage
{
  private String command;
  private Connection connection;
  
  public Storage(String s, Connection c)
  {
    command = s;
    connection = c;
  }
  
  public String getCommand()
  {
    return command;
  }
  
  public Connection getConnection()
  {
    return connection;
  }
}