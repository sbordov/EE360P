
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Garden {
    
  public Garden() {   }; 
  
  private AtomicInteger numDugHoles = new AtomicInteger(0);
  private AtomicInteger numSeededHoles = new AtomicInteger(0);
  private AtomicInteger numFilledHoles = new AtomicInteger(0);
  private AtomicInteger numUnfilledHoles = new AtomicInteger(0);
  
  private AtomicInteger holesNewtonDug = new AtomicInteger(0);
  private AtomicInteger holesBenjaminSeeded = new AtomicInteger(0);
  private AtomicInteger holesMaryFilled = new AtomicInteger(0);
  
  private boolean currentlyFilling = false;
  private boolean currentlyDigging = false;
  
  
  final Lock diggingLock = new ReentrantLock(); // for Newton
  final Condition notFilling = diggingLock.newCondition();
  final Condition fewerUnfilledHoles = diggingLock.newCondition();
  
  final Lock fillingLock = new ReentrantLock(); // for Mary
  final Condition notDigging = fillingLock.newCondition();
  final Condition seededHole = fillingLock.newCondition();
  
  final Lock plantingLock = new ReentrantLock(); // for Benjamin
  final Condition emptyHole = plantingLock.newCondition();
  
  public void startDigging() throws InterruptedException {  
      fillingLock.lock();
      diggingLock.lock();
      try{
          while(numDugHoles.get() >= 4){
              seededHole.await();
          }
          while(numUnfilledHoles.get() >= 8){
              fewerUnfilledHoles.await();
          }
          while(currentlyFilling){
            notFilling.await();
          }
      } finally{
          currentlyDigging = true;
      }
  }; 
  
  public void doneDigging() {  
      numDugHoles.getAndIncrement();
      numUnfilledHoles.getAndIncrement();
      holesNewtonDug.getAndIncrement();
      currentlyDigging = false;
      fillingLock.unlock();
      diggingLock.unlock();
      notDigging.signal();
      emptyHole.signal();
  }; 
  
  public void startSeeding() throws InterruptedException { 
      plantingLock.lock();
      try{
          while(numDugHoles.get() == 0){
              emptyHole.await();
          }
      } finally{
      }
  };
  
  public void doneSeeding() { 
      numSeededHoles.getAndIncrement();
      holesBenjaminSeeded.getAndIncrement();
      plantingLock.unlock();
      seededHole.signal();
  }; 
  
  public void startFilling() throws InterruptedException {  
      fillingLock.lock();
      diggingLock.lock();
      try{
          while(numSeededHoles.get() == 0){
              seededHole.await();
          }
          while(currentlyDigging){
            notDigging.await();
          }
      } finally{
          currentlyFilling = true;
      }
  }; 
  
  public void doneFilling() {  
      numFilledHoles.getAndIncrement();
      holesMaryFilled.getAndIncrement();
      numUnfilledHoles.getAndDecrement();
      currentlyFilling = false;
      fillingLock.unlock();
      diggingLock.unlock();
      fewerUnfilledHoles.signal();
      notFilling.signal();
  }; 
 
    /*
    * The following methods return the total number of holes dug, seeded or 
    * filled by Newton, Benjamin or Mary at the time the methods' are 
    * invoked on the garden class. */
   public int totalHolesDugByNewton() {  return holesNewtonDug.get(); }; 
   public int totalHolesSeededByBenjamin() { return holesBenjaminSeeded.get();  }; 
   public int totalHolesFilledByMary() { return holesMaryFilled.get();  }; 
}