
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Main {

    static final int QUEUE_LIMIT = 100;
    static final int BUSY_STATE = 1;
    static final int IDLE_STATE = 0;

    static File inputFile;
    static File outputFile;

    static int next_event_type, num_custs_delayed, num_delays_required, num_events,
            num_in_q, server_status;
    static double area_num_in_q, area_server_status, mean_interarrival, mean_service,sim_time, time_last_event,
            total_of_delays;
    static double[] time_arrival = new double[QUEUE_LIMIT+1];
    static double[] time_next_event = new double[3];
    static String value1 = null;
    static String value2 = null;
    static String value3 = null;


    public static void main(String[] args) {

        try {
            inputFile = new File("INPUT_FILE.txt");
            outputFile = new File("OUTPUT_FILE.txt");
            if (inputFile.createNewFile() && outputFile.createNewFile()){
                System.out.println(inputFile.getAbsolutePath());
                System.out.println(outputFile.getAbsolutePath());
            }

        }catch (Exception e){
            System.out.println("an error has occurred");
            e.printStackTrace();
        }

        num_events = 2;

        try {
            Scanner reader = new Scanner(inputFile);
            while (reader.hasNextLine()){
                String data = reader.nextLine();
                String[] store = data.split("\\s");
                value1 = store[1];
                value2 = store[3];
                value3 = store[5];
                mean_interarrival = Double.parseDouble(value1);
                mean_service = Double.parseDouble(value2);
                num_delays_required = Integer.parseInt(value3);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            FileWriter writer = new FileWriter("OUTPUT_FILE.txt");
            writer.write("Single server queueing system\n");
            writer.write("\n");
            writer.write("Mean inter_arrival time: "+value1+"\n");
            writer.write("\n");
            writer.write("Mean service time: "+value2+"\n");
            writer.write("\n");
            writer.write("Number of customers: "+value3+"\n");
            writer.write("\n");
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        //initialize the simulation
        initialize();

        //run the simulation while more delays are still needed
        while (num_custs_delayed < num_delays_required){

            //determine the next event
            timing();

            // Update time-average statistical accumulators.
            update_time_avg_stats();

            //invoke the appropriate event function
            switch (next_event_type) {
                case 1 -> arrive();
                case 2 -> depart();
                default -> {
                }
            }
        }
        //invoke the report generator and end the simulation
        report();

    }

    static void initialize(){
        //initializing the simulation clock
        sim_time = 0.0;

        //initialize the state variables
        server_status = IDLE_STATE;
        num_in_q = 0;
        time_last_event = 0.0;

        //initialize the statistical counters
        num_custs_delayed = 0;
        total_of_delays = 0.0;
        area_num_in_q = 0.0;
        area_server_status = 0.0;

        //initialize the event list. Since no customers are present, the departure(service completion) event is eliminated frm consideration.
        time_next_event[1] = sim_time + expon(mean_interarrival);
        time_next_event[2] = Math.exp(30);

    }

    private static void timing(){
        int i;
        double min_time_next_event = Math.exp(29);
        next_event_type = 0;

        //determine the event type of the next event to occur
        for (i = 1; i <= num_events; ++i) {
            if (time_next_event[i] < min_time_next_event){
                min_time_next_event = time_next_event[i];
                next_event_type = i;
            }
        }
        //check to see whether the event list is empty
        if (next_event_type == 0){
            //the event list is empty, so stop the simulation.
            try {
                FileWriter writer = new FileWriter("OUTPUT_FILE.txt");
                writer.write("\n");
                writer.write("Event list empty at time: "+sim_time);
                writer.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        sim_time = min_time_next_event;
    }

    private static void arrive(){
        double delay;

        //schedule the next arrival
        time_next_event[1]= sim_time+expon(mean_interarrival);

        //check to see whether the server is busy
        if (server_status == BUSY_STATE){

            //server is busy, so increment the number of customers in queue
            ++num_in_q;

            //check to see whether an overflow condition exists
            if (num_in_q>QUEUE_LIMIT){

                //the queue has overflowed, so stop the simulation
                try {
                    FileWriter writer = new FileWriter("OUTPUT_FILE.txt");
                    writer.write("\n");
                    writer.write("Overflow of the array time arrival at time: "+sim_time);
                    writer.close();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            time_arrival[num_in_q] = sim_time;
        }
        else {
            //server is idle, arriving customer has a delay of 0.
            delay = 0.0;
            total_of_delays += delay;

            //increment the number of customers delayed, and make the server busy
            ++num_custs_delayed;
            server_status = BUSY_STATE;

            //schedule a departure(service completion)
            time_next_event[2] = sim_time +expon(mean_service);

        }
    }

    private static void depart(){
        int i;
        double delay;

        //check to see whether the queue is empty
        if (num_in_q == 0){

            // The queue is empty so make the server idle and eliminate the departure (service completion) event from consideration
            server_status = IDLE_STATE;
            time_next_event[2] = Math.exp(30);
        }
        else{
            //the queue is not empty, so decrement the number of customers in queue
            --num_in_q;

            // Compute the delay of the customer who is beginning service and update the total delay accumulator.
            delay = sim_time - time_arrival[1];
            total_of_delays += delay;

            // Increment the number of customers delayed, and schedule departure.
            ++num_custs_delayed;
            time_next_event[2] = sim_time + expon(mean_service);

            //Move each customer in queue (if any) up one place.
            for (i = 1; i <= num_in_q; ++i){
                time_arrival[i] = time_arrival[i + 1];}
        }
    }

    private static void report(){

        // Compute and write estimates of desired measures of performance.
        try {
            FileWriter writer = new FileWriter("OUTPUT_FILE.txt");
            writer.write("Single server queueing system\n");
            writer.write("\n");
            writer.write("Mean inter_arrival time: "+value1+" minutes"+"\n");
            writer.write("\n");
            writer.write("Mean service time: "+value2+" minutes"+"\n");
            writer.write("\n");
            writer.write("Number of customers: "+value3+"\n");
            writer.write("\n");
            writer.write("Average delay in queue: "+String.format("%,.3f",(total_of_delays/num_custs_delayed))+" minutes"+"\n");
            writer.write("\n");
            writer.write("Average number in queue: "+String.format("%,.3f",(area_num_in_q/sim_time))+"\n");
            writer.write("\n");
            writer.write("Server utilization: "+String.format("%,.3f",(area_server_status/sim_time))+"\n");
            writer.write("\n");
            writer.write("Time simulation ended: "+String.format("%,.3f",sim_time)+" minutes"+"\n");
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void update_time_avg_stats(){
        double time_since_last_event;

        // Compute time since last event, and update last-event-time marker.
        time_since_last_event = sim_time - time_last_event;
        time_last_event = sim_time;

        //  Update area under number-in-queue function.
        area_num_in_q += num_in_q * time_since_last_event;

        // Update area under server-busy indicator function.
        area_server_status += server_status * time_since_last_event;
        //this is an example of a comment in the new font

    }

    private static double expon(double mean) {
        return -(mean) * Math.log(Math.random());
    }
}
