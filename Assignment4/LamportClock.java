/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
public class LamportClock {
    int c;
    public LamportClock() {
        c = 1;
    }
    public synchronized int getValue() {
        return c;
    }
    public synchronized void tick() { // on internal events
        c = c + 1;
    }
    public synchronized int sendAction() {
       // include c in message
        c = c + 1;      
        return c;
    }
    public synchronized void receiveAction(int sentValue) {
        c = Math.max(c, sentValue) + 1;
    }
}
