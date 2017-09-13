public class Bullet extends Cell
{
  private String direction;
  private long creationTime;
  
  public Bullet(String d, long time)
  {
    direction = d;
    creationTime = time;
  }
  
  public String getCode()
  {
    return "bullet";
  }
  
  public String getDirection()
  {
    return direction;
  }
  
  public long getTimeCreated()
  {
    return creationTime;
  }
  
  public void timeCreated(long n)
  {
    creationTime =  n;
  }
}