
import java.util.Comparator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
public class ServerUpdateRequestComparator implements Comparator<ServerUpdateRequest>{
    
    @Override
    public int compare(ServerUpdateRequest request1, ServerUpdateRequest request2){
        if(request1.timeStamp < request2.timeStamp){
            return -1;
        } else if(request1.timeStamp > request2.timeStamp){
            return 1;
        } else { // In this case, the time stamps are equal.
            if(request1.processId < request2.processId){
                return -11;
            } else if(request1.processId > request2.processId){
                return 1;
            } else{ // Timestamp and processId are equivalent.
                return 0;
            }
        }
    }
}
