import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Scanner;


public class SingleServer {

    // Declarations and Initializations
    private static final int QUEUE_LIMIT = 100; /* Limit on queue length. */
    private static final int IDLE = 0; /* Mnemonics for server's being idle */
    private static final int BUSY = 1;/* and busy. */
    private static int next_event_type, num_customers_delayed, num_delays_required, num_events, num_in_queue, server_status;
    private static double area_num_in_q, area_server_status, mean_interArrival, mean_service;
    private static double time, time_last_event, total_of_delays;
    private static String avg_delays, time_simulation, avg_number, server;
    private static final double[] time_arrival = new double[QUEUE_LIMIT + 1];
    private static final double[] time_next_event = new double[3];

    //initialisation function to initialize values to the declared variables
    public static void initialize() {
        time = 0.0;//initialising simulation clock
        server_status = IDLE;
        num_in_queue = 0;
        time_last_event = 0.0;
        num_customers_delayed = 0;//initialising statistical counters
        total_of_delays = 0.0;
        area_num_in_q = 0.0;
        area_server_status = 0.0;
        /* initialising event list. Since no customers are present ,
        the departure (service completion) event is eliminated from consideration*/
        time_next_event[1] = time + exponential(mean_interArrival);
        time_next_event[2] = Math.pow(10.0, 30.0);

    }

    // A function that generates random values
    public static double exponential(double mean) {
        double u;
        u = Math.random(); //random generator
        return -mean * Math.log(u);

    }

    //timing function
    public static void timing() {
        double min_time_next_event = Math.pow(10.0, 29.0);
        next_event_type = 0;

        //determine the event type of the next event to occur
        for (int i = 1; i <= num_events; i++) {
            if (time_next_event[i] < min_time_next_event) {
                min_time_next_event = time_next_event[i];
                next_event_type = i;
            }
        }

        // check to see whether the event list is empty
        if (next_event_type == 0) {
            //event list empty stop simulation
            System.out.println("Event list empty at time " + time);
        }

        //the event list is not empty, so advance the simulation clock
        time = min_time_next_event;
    }

    // arrival event function
    public static void arrive() {
        double delay;
        //schedule next arrival
        time_next_event[1] = time + exponential(mean_interArrival);

        // check server status if it's busy
        if (server_status == BUSY) {
            // server is busy, so we increment number of customers in queue
            ++num_in_queue;
            //check for overflow condition
            if (num_in_queue > QUEUE_LIMIT) {
                // Queue overflowed, stop simulation
                System.out.println("Overflow of the array time_arrival at time" + time);
            }
            /* There is still room in the queue, so store the time of arrival of the
            arriving customer at the (new) end of time_arrival. */
            time_arrival[num_in_queue] = time;
        } else {
            /* Server is idle, so arriving customer has a delay of zero. (The
            following two statements are for program clarity and do not affect
            the results of the simulation.) */
            delay = 0.0;
            total_of_delays += delay;
            /* Increment the number of customers delayed, and make server busy. */
            ++num_customers_delayed;
            server_status = BUSY;
            /* Schedule a departure (service completion). */
            time_next_event[2] = time + exponential(mean_service);
        }
    }

    /* Departure event function. */
    public static void depart() {
        double delay;

        /* Check to see whether the queue is empty. */
        if (num_in_queue == 0) {

            /* The queue is empty so make the server idle and eliminate the
            departure (service completion) event from consideration. */
            server_status = IDLE;
            time_next_event[2] = Math.pow(10.0, 30.0);
        } else {

            /* The queue is nonempty, so decrement the number of customers in
            queue. */
            --num_in_queue;

            /* Compute the delay of the customer who is beginning service and update
               the total delay accumulator. */
            delay = time - time_arrival[1];
            total_of_delays += delay;
            /* Increment the number of customers delayed, and schedule departure. */
            ++num_customers_delayed;
            time_next_event[2] = time + exponential(mean_service);

            /* Move each customer in queue (if any) up one place. */
            for (int i = 1; i <= num_in_queue; i++) {
                time_arrival[i] = time_arrival[i + 1];
            }
        }
    }

    /* Update area accumulators for time-average
       statistics. */
    public static void update_time_avg_statistics() {
        double time_since_last_event;
        /* Compute time since last event, and update last-event-time marker. */
        time_since_last_event = time - time_last_event;
        time_last_event = time;
        /* Update area under number-in-queue function. */
        area_num_in_q += num_in_queue * time_since_last_event;

        /* Update area under server-busy indicator function. */
        area_server_status += server_status * time_since_last_event;

    }

    /* Report generator function. */
    public static void report() {
        DecimalFormat df = new DecimalFormat("###.##");
        avg_delays = df.format(total_of_delays / num_customers_delayed);
        avg_number = df.format(area_num_in_q / time);
        server = df.format(area_server_status / time);
        time_simulation = df.format(time);

        /* Compute and write estimates of desired measures of performance. */
        try {
            FileWriter fw = new FileWriter("C:\\Users\\Felix\\Downloads\\SingleServer\\out.txt");
            fw.write("Single Server Queueing System Simulation\n");
            fw.write("________________________________________________\n\n");
            fw.write(" Mean Inter-Arrival time: ");
            fw.write(String.valueOf(mean_interArrival));
            fw.write(" \n");
            fw.write(" Mean service time: ");
            fw.write(String.valueOf(mean_service));
            fw.write(" \n");
            fw.write(" Number of customers: ");
            fw.write(String.valueOf(num_delays_required));
            fw.write(" \n\n");
            fw.write(" Average delay in Queue: ");
            fw.write(String.valueOf(avg_delays));
            fw.write(" \n");
            fw.write(" Average number in Queue: ");
            fw.write(String.valueOf(avg_number));
            fw.write(" \n");
            fw.write(" Server utilization: ");
            fw.write(String.valueOf(server));
            fw.write(" \n");
            fw.write(" Time Simulation ended: ");
            fw.write(String.valueOf(time_simulation));

            fw.close();
        } catch (Exception e) {

            /*Print any errors found*/
            System.out.println(e);
        }


    }

    /*Main function*/
    public static void main(String[] args) {

        //Specify number of events for the timing function.
        num_events = 2;

        //Open and read input file
        try {
            File file = new File("C:\\Users\\Felix\\Downloads\\SingleServer\\in.txt");
            Scanner scan;
            scan = new Scanner(file);

            while (scan.hasNextDouble()) {
                mean_interArrival = scan.nextDouble();
                mean_service = scan.nextDouble();
                num_delays_required = scan.nextInt();
            }

        } catch (FileNotFoundException e1) {

            /*Print any errors found*/
            e1.printStackTrace();
        }


        initialize();
        //Run the simulation while more delays are still needed.
        while (num_customers_delayed < num_delays_required) {
            timing();

            //Determine the next event.
            update_time_avg_statistics();

            //Invoke the appropriate event function.
            switch (next_event_type) {
                case 1:
                    arrive();
                    break;
                case 2:
                    depart();
                    break;
            }
        }

        //Invoke the report generator and end of simulation.
        report();
    }
}
