import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * This class demonstrates how the client makes basic requests to 
 * Amazon SQS using the AWS SDK. 
 * The client will read the data from the files and then will populate 
 * the queue. after it has created the queue it will call the 
 * AmazonDynamoDBSample class to add the Queue URL into a common table with 
 * name QeueURL for the workers to access.
 */

public class ClientSimpleQueueService 
{
	public static String myQueueUrl;

	public static void main(String[] args) throws Exception 
	{

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (/home/rohit/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try 
        {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } 
        catch (Exception e) 
        {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (/home/rohit/.aws/credentials), and is in valid format.",
                    e);
        }

        AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        sqs.setRegion(usWest2);

        System.out.println("===========================================");
        System.out.println("Accessing Amazon SQS for Client");
        System.out.println("===========================================\n");

        try 
        {
            // Create a queue
            System.out.println("Creating a new SQS queue called "+ args[1] +".\n");
            CreateQueueRequest createQueueRequest = new CreateQueueRequest(args[1]);
            myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

            // List queues
            System.out.println("Listing all queues in your account.\n");
            for (String queueUrl : sqs.listQueues().getQueueUrls()) 
            {
                System.out.println("  QueueUrl: " + queueUrl);
            }
            System.out.println();

            // Send a message
            
            // The file to open.
    		String fileName = args[0];
    		
            System.out.println("Sending a message to MyQueue.\n");
            String line ; // reference to one line at a time 
            
            // FileReader reads text files
            FileReader fileReader = new FileReader(fileName);

            // Wrapping FileReader in BufferedReader to increase efficiency of I/O
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            
            //reading file
            System.out.println("Read File");
            
            while((line = bufferedReader.readLine()) != null) 
            {
                String t=line;	 //feed the lines read from the file into the queue
                //line.substring(line.indexOf(" ") + 1, line.length());
            	//queue.add(Integer.parseInt(t));
                
                //System.out.println(t);
                
                //if((!t.isEmpty() && !t.equals(" ")))
                sqs.sendMessage(new SendMessageRequest(myQueueUrl, t));
            }  
            System.out.println(myQueueUrl);
            ClientAmazonDynamoDB db=new ClientAmazonDynamoDB();
            
            db.db(myQueueUrl);
            bufferedReader.close();
            
			
			// Receive messages
            System.out.println("Receiving messages from MyQueue.\n");
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            for (Message message : messages) 
			{
                System.out.println("  Message");
                System.out.println("    MessageId:     " + message.getMessageId());
                System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
                System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
                System.out.println("    Body:          " + message.getBody());
                for (Entry<String, String> entry : message.getAttributes().entrySet()) 
				{
                    System.out.println("  Attribute");
                    System.out.println("    Name:  " + entry.getKey());
                    System.out.println("    Value: " + entry.getValue());
                }
            }
            System.out.println();
            
        } 
        catch (AmazonServiceException ase) 
        {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        }
        catch (AmazonClientException ace) 
        {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
