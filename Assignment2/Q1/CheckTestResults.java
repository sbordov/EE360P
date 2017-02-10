
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
public class CheckTestResults {
    
    private static ArrayList<Round> rounds = new ArrayList<>();
    private static String input;
    private static int num_rounds = 10;
    private static int num_threads = 10;
    private static int barrier_size = 5;
    private static String file_name = "hw2_q1_1.txt";
    
    public CheckTestResults(int number_of_rounds, int number_of_threads, int size, String file){
        num_rounds = number_of_rounds;
        num_threads = number_of_threads;
        barrier_size = size;
        file_name = file;
    }
    
    public static void check() throws IOException{
        ArrayList<String> lines = readThemLines(file_name);
        
                
                
        readOutput(lines);
    }
    
    public static ArrayList<String> readThemLines(String file_name) throws IOException{
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String line;
            while ((line = br.readLine()) != null) {
               lines.add(line);
            }
        }
        return lines;
    }
    
    public static void readOutput(ArrayList<String> lines){
        String round_title = "";
        for(int j = 0; j < num_rounds; j++){
            rounds.add(new Round(num_threads));
        }
        for(int j = 0; j < num_rounds; j++){
            round_title = "round:" + Integer.toString(j);
            for(int i = 0; i < lines.size(); i++){
                if(lines.get(i).indexOf(round_title) > -1){
                    processLine(lines.get(i), j);
                }
            }
        }
        boolean correct = verifyOutput();
        if(correct){
            System.out.println("It seemed to work!");
        } else{
            System.out.println("Damn...");
        }
    }
    
    public static boolean verifyOutput(){
        boolean all_good = true;
        for(Round r : rounds){
            all_good = r.threadValuesCheckOut();
            if(!all_good){
                return all_good;
            }
        }
        return all_good;
    }
    
    public static void processLine(String line, int round_number){
        if(line.indexOf("WAITING") > -1){
            processWaiting(line, round_number);
        } else if(line.indexOf("leaving") > -1){
            processLeaving(line, round_number);
        }
    }
    
    public static void processWaiting(String line, int round_number){
        String[] parsed_line = line.split("\\s");
        int thread_number = Integer.parseInt(parsed_line[1]);
        if(rounds.get(round_number).threads.containsKey(thread_number)){
            rounds.get(round_number).threads.get(thread_number).setArrivalIndex(rounds.get(round_number).round_arrival_count);
            rounds.get(round_number).round_arrival_count--;
            if(rounds.get(round_number).round_arrival_count < 0){
                rounds.get(round_number).round_arrival_count = barrier_size - 1;
            }
        } else{
            CyclicThread t = new CyclicThread();
            t.setArrivalIndex(rounds.get(round_number).round_arrival_count);
            rounds.get(round_number).round_arrival_count--;
            if(rounds.get(round_number).round_arrival_count < 0){
                rounds.get(round_number).round_arrival_count = barrier_size - 1;
            }
            rounds.get(round_number).threads.put(thread_number, t);
        }
    }
    
    public static void processLeaving(String line, int round_number){
        String[] parsed_line = line.split("\\s");
        int thread_number = Integer.parseInt(parsed_line[1]);
        if(rounds.get(round_number).threads.containsKey(thread_number)){
            rounds.get(round_number).threads.get(thread_number).setDepartureIndex(Integer.parseInt(parsed_line[6]));
        } else{
            CyclicThread t = new CyclicThread();
            t.setDepartureIndex(Integer.parseInt(parsed_line[6]));
            rounds.get(round_number).threads.put(thread_number, t);
        }
    }
            
    private static class Round{
        public HashMap<Integer, CyclicThread> threads = new HashMap<>();
        public int round_arrival_count;
        
        public Round(int number_of_threads){
            round_arrival_count = barrier_size - 1;
        }
        
        public boolean threadValuesCheckOut(){
            boolean aww_yiss = true;
            for(Integer thread_number : threads.keySet()){
                aww_yiss = threads.get(thread_number).hasMatchingIndices();
                if(!aww_yiss){
                    return aww_yiss;
                }
            }
            return aww_yiss;
        }
        
    }
 
    private static class CyclicThread{
        int arrival_index;
        int departure_index;
        
        public CyclicThread(){
            arrival_index = -1;
            departure_index = -2;
        }
        
        public void setArrivalIndex(int index){
            arrival_index = index;
        }
        
        public void setDepartureIndex(int index){
            departure_index = index;
        }
        
        public boolean hasMatchingIndices(){
            return (arrival_index == departure_index);
        }
    }
}
