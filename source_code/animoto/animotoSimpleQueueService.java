import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;


/**
 * This class demonstrates how the client makes basic requests to 
 * Amazon SQS using the AWS SDK. 
 * The client will read the data from the files and then will populate 
 * the queue. after it has created the queue it will call the 
 * AmazonDynamoDBSample class to add the Queue URL into a common table with 
 * name QeueURL for the workers to access.
 */

public class SimpleQueueServiceSample 
{  
    public String url;
    
	//a constructor
    SimpleQueueServiceSample ()
    {
    	
    }
    
	//a constructor to initialize the url 
    SimpleQueueServiceSample (String U)
    {
    	url=U;
    	System.out.println("\n At Constructor  SimpleQueueServiceSample URL:"+url);
    }
    
    
	//read items from the SQS with the url as got from the constructor
    public String read ()
    {
		//Receiving messages from the Queue in the url.
    	String tableName = "Jobs";
    	
    	AWSCredentials credentials = null;
        try 
        {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } 
		catch (Exception e) 
		{
            throw new AmazonClientException
					(
                    "Cannot load the credentials from the credential profiles file. " + "Please make sure that your credentials file is at the correct " +
                    "location (/home/rohit/.aws/credentials), and is in valid format.",
                    e);
        }
        
        AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        sqs.setRegion(usWest2);
        
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(url);
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        
        //System.out.println("Items in queue are:");
        String item="";
        
        int i=0;
        //while(i<2)
        {
        //System.out.println(messages);
        for (Message message : messages) 
        {
        	item=message.getBody();						//pops the job and stores it into item
            //System.out.println("Q:"+item);       	
        }
        i++;
        }
        //System.out.println();    
        
       
        return item;					//returns the job to the WorkerThreadExecutor class
    }

}

