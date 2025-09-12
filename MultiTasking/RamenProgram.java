package MultiTasking;
/**
 *  참고 사이트: [얄팍한 코딩사전](https://www.youtube.com/watch?v=iks_Xb9DtTM)
 * How to run:
 * 1) Open the terminal.
 * 2) Type the following command to compile the program: javac -encoding UTF-8 RamenProgram.java
 * 3) Type the following command to run the program: java RamenProgram 10
*/
public class RamenProgram {
  
  public static void main(String[] args) {

    try{
      RamenCook ramenCook = new RamenCook(Integer.parseInt(args[0]));
      new Thread(ramenCook, "A").start();
      new Thread(ramenCook, "B").start();
      new Thread(ramenCook, "C").start();
      new Thread(ramenCook, "D").start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

class RamenCook implements Runnable {
  private int ramenCount;
  private String[] burners = {"-", "-", "-", "-"};

  String[] test = new String[]{};

  public RamenCook(int ramenCount) {
    this.ramenCount = ramenCount;
  }

  @Override
  public void run() {
    while ( ramenCount > 0) {

      synchronized(this) {
        ramenCount--;
        System.out.println(
          Thread.currentThread().getName()
          + ": " + ramenCount + "개 남음");
      }
      
      for(int i = 0; i < burners.length; i++) {
        if(!burners[i].equals("-")) continue;
      
        synchronized(this) {
          burners[i] = Thread.currentThread().getName();
          System.out.println(
            "                   "
            + Thread.currentThread().getName()
            + ": [" + (i+1) + "]번 버너 ON");
          ShowBurners();
        }

        try{
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        synchronized(this) {
          burners[i] = "-";
          System.out.println(
            "                                     "
            + Thread.currentThread().getName()
            + ": [" + (i+1) + "]번 버너 OFF");
          ShowBurners();
        }
        break;
      }

      try {
        Thread.sleep(Math.round(1000 * Math.random()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void ShowBurners() {
    String stringToPrint = "                                                        ";
    
    for(int i =0; i<burners.length; i++) {
      stringToPrint += (" " + burners[i]);
    }
    System.out.println(stringToPrint);
  }
  
}