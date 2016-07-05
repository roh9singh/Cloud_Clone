import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WorkerThreadExecuter {

	
	String url="";
	int nos;
	
	WorkerThreadExecuter(String url,int nos)
	{
		this.url=url;
		this.nos=nos;
	}
	
	public void start() 
	{
		
		int Th=1;
		
		//create a pool of threads
        ExecutorService executor = Executors.newFixedThreadPool(Th);
        
        //run the jobs as read from the file
        long startTime = System.currentTimeMillis();   
        SimpleQueueServiceSample queue = new SimpleQueueServiceSample(url);
        int i=0;       
        System.out.println("Starting the Jobs for the Worker..... \n\n");
        System.out.println("Reading the Jobs from the Queue and checking for Duplication..... \n\n");
        while(i<nos)//queue.isEmpty()==false)
        {
        	
        	String temp=queue.read();
        
        	i++;
        	if (temp=="")
        		continue;
        	
        	//System.out.println(temp+" "+temp.indexOf(" "));
        	String id=temp.substring(0,temp.indexOf(" "));
        	String job=temp.substring(temp.indexOf("p ") + 2, temp.length());
            
            AmazonDynamoDBSample db = new AmazonDynamoDBSample();
            //AmazonDynamoDBSample db = new AmazonDynamoDBSample();
        	//db.addJob(job); 	
        	//System.out.println("::::"+temp+":"+id+":"+job);
            
            if (db.queryJob(id)==false)
            {
            	System.out.println("Unique id:"+id);
            	db.addJob(id,job);
            	Runnable worker = new WorkerThread(Integer.parseInt(job));
                executor.execute(worker);
            }
            //else
            	//System.out.println("The job with id:"+id+" Exists...Moving onto next job in SQS");
           
            //System.out.println(queue.isEmpty());
        }
       
        executor.shutdown();
        
        //wait till the executor service ends
        while (!executor.isTerminated()) 
        {
        	
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        
        System.out.println("\n Finished all threads in time:"+estimatedTime);//:"+  watch.getTotalTimeMillis() + "ms or "+  watch.getTotalTimeMillis()/1000.0 +"s");


	}

}
