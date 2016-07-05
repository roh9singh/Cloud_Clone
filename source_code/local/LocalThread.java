// A class to implement the Executer service. 
// This class implements the Runnable class and does the sleep operation 
// for the treads by overriding the function run()

public class LocalThread implements Runnable 
{
	int time;
    
    public WorkerThread(int t)   	//a constructor to get the time value to sleep  
    {
    	time = t;
    }

    public void run() 				//the run function to override
    {
        try
		{
            Thread.sleep(time);		//thread sleep in milli seconds
        }
		catch (InterruptedException e) 		//in case of any error print the StackTrace
		{
            e.printStackTrace();
        }
		
		//System.out.println(Thread.currentThread().getName()+": "+time);
    }
}