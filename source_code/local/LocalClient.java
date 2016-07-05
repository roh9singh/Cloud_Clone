import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalClient
{

	public static void main(String[] args) 
	{
	
		// The file to open from command line
		String fileName = args[1];
        
		// The number of threads from command line
		int Th=Integer.parseInt(args[0]);
		
        Queue queue = new LinkedList();
        
        //push tasks from the file
        queue=pushTask(fileName);
                
        //check if the queue is empty opr not
        System.out.println("Output of queue.isEmpty():"+queue.isEmpty());
        
        //create a pool of threads
        ExecutorService executor = Executors.newFixedThreadPool(Th);
        
        //run the jobs as read from the file
        
        long startTime = System.currentTimeMillis();   //store start time
        
        //System.out.println(System.currentTimeMillis());
        
        while(queue.isEmpty()==false)
        {
            //pop tasks one by one and feed it into the executer service
            Runnable worker = new LocalThread(popTask(queue));
            executor.execute(worker);
            //System.out.println(queue.isEmpty());
        }
        
        executor.shutdown();		//shutdown executor service
        
        while (!executor.isTerminated()); //wait till the executor service ends
        
        long estimatedTime = System.currentTimeMillis() - startTime; //calculate the total time taken
        //System.out.println(System.currentTimeMillis());
        
        //print the time 
        System.out.println("Finished all "+ Th + " threads in time:" + estimatedTime + "ms");//:"+  watch.getTotalTimeMillis() + "ms or "+  watch.getTotalTimeMillis()/1000.0 +"s");
        
	}

	static Queue pushTask(String fileName)		//function to push tasks into a queue
	{
		Queue queue = new LinkedList();			//create a object of Queue
		String line = null; 					// reference to one line at a time 
		
		try 
        {
            // FileReader reads text files
            FileReader fileReader = new FileReader(fileName);

            // Wrapping FileReader in BufferedReader to increase efficiency of I/O
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            
            //reading file
            System.out.println("Read File");
            while((line = bufferedReader.readLine()) != null) 
            {
                //System.out.println(line);
            	String t=line.substring(line.indexOf(" ") + 1, line.length());  //extract the sleep times
            	queue.add(Integer.parseInt(t)); //add task to queue
            }   
            // Always close files.
            bufferedReader.close();  		//closing buffer reader 
            
            System.out.println("Queue Created");
        }
        catch(FileNotFoundException ex) 
        {
            System.out.println("Unable to open file '" + fileName);     		//error to catch in case file does not exists               
        }
        catch(IOException ex) 
        {
            System.out.println( "Error reading file '" + fileName);                  //error to catch in case any I/O exception
        }
        
		return queue;

	}
	
	static int popTask(Queue queue)			// function to pop tasks from the queue
	{
		int time;
		
		if(queue.isEmpty()==true)				//check if the queue is empty or not
			time=-1;							// if empty then send -1 as sleep task
		else
			time= (Integer) queue.remove();			//else pop the sleep time to caller
		
		return time;								//return the time
	}
	
}


