import java.util.HashMap;

public class WorkerThread implements Runnable 
{
	int time;
    
    public WorkerThread(int t)
    {
    	time = t;
    }

    @Override
    public void run()
    {
        try
		{	Thread.sleep(time);		//in milli seconds
        }
		catch (InterruptedException e) 
		{
            e.printStackTrace();
        }
		//System.out.println(Thread.currentThread().getName()+": "+time);
        
        
    }
}