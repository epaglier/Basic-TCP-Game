public class Robot extends Cell
{
  private String direction;
  private int health;
  private int originalHealth;
  private int number;
  private String originalDirec;
  private String name;
  
  public Robot(String direc, int position)
  {
    name = "";
    direction = direc;
    originalDirec = direc;
    health = 5;
    originalHealth = 5;
    number = position;
  }
  
  public String getCode()
  {
    return "robot:" + direction;
  }
  
  public String getDirection()
  {
    return direction;
  }
  
  public void turn()
  {
    if (direction.equals("N"))
    {
      direction = "W";
    }
    else if (direction.equals("W"))
    {
      direction = "S";
    }
    else if (direction.equals("S"))
    {
      direction = "E";
    }
    else if (direction.equals("E"))
    {
      direction = "N";
    }
  }
  
  public int getHealth()
  {
    return health;
  }
  
  public void hit()
  {
    health--;
  }
  
  public void resetHealth()
  {
    health = originalHealth;
  }
  
  public int getNumber()
  {
    return number;
  }
  
  public void reset()
  {
    direction = originalDirec;
  }
  
  public void setNombre(String s)
  {
    name = s;
  }
  
  public String getNombre()
  {
    return name;
  }
}