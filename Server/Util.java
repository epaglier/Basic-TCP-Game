import java.io.*;
import java.util.*;

public class Util
{
  private static Map<String, BufferedReader> files = new HashMap<String, BufferedReader>();
  private static Map<String, String> lines = new HashMap<String, String>();
  private static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
  
  public static String readLine(String fileName)
  {
    try
    {
      if (!files.containsKey(fileName))
        files.put(fileName, new BufferedReader(new FileReader(fileName)));
      String line = lines.get(fileName);
      if (line == null)
        line = files.get(fileName).readLine();
      lines.remove(fileName);
      return line;
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public static boolean hasMoreLines(String fileName)
  {
    try
    {
      if (!files.containsKey(fileName))
        files.put(fileName, new BufferedReader(new FileReader(fileName)));
      String line = lines.get(fileName);
      if (line == null)
      {
        line = files.get(fileName).readLine();
        lines.put(fileName, line);
      }
      return line != null;
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public static void close(String fileName)
  {
    try
    {
      if (!files.containsKey(fileName))
        throw new RuntimeException("File " + fileName + " is not open");
      files.get(fileName).close();
      files.remove(fileName);
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public static String input()
  {
    try
    {
      return input.readLine();
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public static void pause(double seconds)
  {
    try
    {
      Thread.sleep((int)(seconds * 1000));
    }
    catch(InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }  
}