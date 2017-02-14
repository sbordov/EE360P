/*
 * sb39782
 * spf363
 */
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Garden {
    
  public Garden() {   }; 
  
  private AtomicInteger numEmptyHoles = new AtomicInteger(0);
  private AtomicInteger numSeededHoles = new AtomicInteger(0);
  private AtomicInteger numFilledHoles = new AtomicInteger(0);
  private AtomicInteger numUnfilledHoles = new AtomicInteger(0);
  
  private AtomicInteger holesNewtonDug = new AtomicInteger(0);
  private AtomicInteger holesBenjaminSeeded = new AtomicInteger(0);
  private AtomicInteger holesMaryFilled = new AtomicInteger(0);
  
  private boolean currentlyFilling = false;
  private boolean currentlyDigging = false;
  
  private boolean mustWakeNewton;
  private boolean mustWakeMary;
  
  
  final Lock shovelLock = new ReentrantLock(); // for Newton
  final Condition notFilling = shovelLock.newCondition();
  final Condition fewerUnfilledHoles = shovelLock.newCondition();
  final Condition notDigging = shovelLock.newCondition();
  final Condition seededHole = shovelLock.newCondition();
  
  final Lock plantingLock = new ReentrantLock(); // for Benjamin
  final Condition emptyHole = plantingLock.newCondition();
  
  public void startDigging(){  
      shovelLock.lock();
      try{
          while(numEmptyHoles.get() >= 4){
              mustWakeNewton = true;
              seededHole.await();
          }
          while(numUnfilledHoles.get() >= 8){
              mustWakeNewton = true;
              fewerUnfilledHoles.await();
          }
          while(currentlyFilling){
              mustWakeNewton = true;
              notFilling.await();
          }
      } catch(InterruptedException e){
      } finally{
          mustWakeNewton = false;
          currentlyDigging = true;
      }
  }; 
  
  public void doneDigging() {  
      numEmptyHoles.getAndIncrement();
      numUnfilledHoles.getAndIncrement();
      holesNewtonDug.getAndIncrement();
      currentlyDigging = false;
      try{
        notDigging.signalAll();
      } finally{
          shovelLock.unlock();
      }
      plantingLock.lock();
      try{
        emptyHole.signal();
      } finally{
          plantingLock.unlock();
      }
  }; 
  
  public void startSeeding(){ 
      plantingLock.lock();
      try{
          while(numEmptyHoles.get() == 0){
              emptyHole.await();
          }
      } catch(InterruptedException e){
      } finally{
      }
  };
  
  public void doneSeeding() {
      numEmptyHoles.getAndDecrement();
      numSeededHoles.getAndIncrement();
      holesBenjaminSeeded.getAndIncrement();
      plantingLock.unlock();
      if(mustWakeMary || mustWakeNewton){
          shovelLock.lock();
          try{
              seededHole.signal();
          } finally{
              shovelLock.unlock();
          }
      }
  }; 
  
  public void startFilling(){  
      shovelLock.lock();
      try{
          while(numSeededHoles.get() == 0){
              mustWakeMary = true;
              seededHole.await();
          }
          while(currentlyDigging){
              mustWakeMary = true;
              notDigging.await();
          }
      } catch(InterruptedException e){
      }finally{
          mustWakeMary = false;
          currentlyFilling = true;
      }
  }; 
  
  public void doneFilling() {  
      numFilledHoles.getAndIncrement();
      holesMaryFilled.getAndIncrement();
      numUnfilledHoles.getAndDecrement();
      numSeededHoles.getAndDecrement();
      currentlyFilling = false;
      try{
          fewerUnfilledHoles.signal();
      } catch(Exception e){ // Catch DispatchUncaughtException
          
      }
      try{
          notFilling.signal();
      } finally{
          shovelLock.unlock();
      }
  }; 
 
    /*
    * The following methods return the total number of holes dug, seeded or 
    * filled by Newton, Benjamin or Mary at the time the methods' are 
    * invoked on the garden class. */
   public int totalHolesDugByNewton() {  return holesNewtonDug.get(); }; 
   public int totalHolesSeededByBenjamin() { return holesBenjaminSeeded.get();  }; 
   public int totalHolesFilledByMary() { return holesMaryFilled.get();  }; 
}