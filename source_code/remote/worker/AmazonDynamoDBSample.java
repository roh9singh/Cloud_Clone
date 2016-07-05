import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

/**
 * This perform the connection of the Worker with the 
 * Amazon DynamoDB service. It creates a Table named Jobs which keeps track of the duplicated items 
 * It also reads the URL of the SQS which allows 
 * the workers to access the SQS. 
 */
 
public class AmazonDynamoDBSample 
{

	String tableName = "QueueURL",tableName2 = "Jobs";
    static AmazonDynamoDBClient dynamoDB;

    private static void init() throws Exception 
    {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (/home/rohit/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (/home/rohit/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = new AmazonDynamoDBClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
    }

    public static void main(String[] args) throws Exception 
    {
        init();
        int nos=1000;//Integer.parseInt(args[0]);

        String tableName = "QueueURL",tableName2 = "Jobs";
        
        try {
            // Create table if it does not exist yet
            if (Tables.doesTableExist(dynamoDB, tableName2))
            {
                System.out.println("Table " + tableName2 + " is already ACTIVE");
            }
            else 
            {
                // Create a table with a primary hash key named 'name', which holds a string
                CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName2)
                    .withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
                    TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
                System.out.println("Created Table: " + createdTableDescription);

                // Wait for it to become active
                System.out.println("Waiting for " + tableName + " to become ACTIVE...");
                Tables.awaitTableToBecomeActive(dynamoDB, tableName);
            }

                        
            //acess item
            System.out.println("Access URL:");
            ScanRequest scanRequest=new ScanRequest().withTableName(tableName);
            ScanResult result=dynamoDB.scan(scanRequest);
            
            String item=result.toString();
            String t=item.substring(item.indexOf("S: ") + 3, item.indexOf(","));
            System.out.println(t);
            
            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName2);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("\n Table Description: " + tableDescription);


            
            //SimpleQueueServiceSample squeue=new SimpleQueueServiceSample(t);
            //squeue.read();squeue.read();squeue.read();
            
            WorkerThreadExecuter worker = new WorkerThreadExecuter(t,nos); 
            worker.start();
        } 
        catch (AmazonServiceException ase) 
		{
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        }
		catch (AmazonClientException ace) 
		{
            System.out.println("Caught an AmazonClientException, which means the client encountered " + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

	//this function adds the URL into a Hash map
    public void addJob(String id,String job) 
    {
    	Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("name", new AttributeValue(id));
        item.put("job", new AttributeValue(job));
        PutItemRequest putItemRequest = new PutItemRequest(tableName2, item);//.withConditionExpression("attribute_not_exists");        
        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
        //System.out.println(Thread.currentThread().getName()+": "+"Input Result: " + putItemResult);
        //PutItemRequest putItemRequest = new PutItemRequest(tableName, item).withConditionExpression("attribute_not_exists(task)");
    }
    
	//this function checks if the id is already present in the table or not. If present then sends true else false
    public boolean queryJob(String id)
    {
    	// Scan items for movies with a year attribute greater than 1985
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
        
        Condition condition = new Condition()
        .withComparisonOperator(ComparisonOperator.EQ.toString())
        .withAttributeValueList(new AttributeValue().withS(id));
        
        //System.out.println("----"+id);
        scanFilter.put("name", condition);
        
        ScanRequest scanRequest = new ScanRequest(tableName2).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        //System.out.println(Thread.currentThread().getName()+": "+"Output Result: " + scanResult);
  
        int s=scanResult.toString().indexOf("S: ")+3,f=scanResult.toString().indexOf(",");
        //System.out.println(":"+scanResult.toString().subSequence(s,f));
        if (scanResult.toString().subSequence(s,f).equals(id))
        	return true;
        else
        	return false;
        	//System.out.println("ok");       	
    }

}
