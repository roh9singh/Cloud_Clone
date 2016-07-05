import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

/* This used the ffmpeg to create a video out of the photos which have been downloaded from the urls provided by the file 
 * as an input, these set of urls will be sent by the Client for the S3 to download using wget. 
 * This work is to be done by the workers and after each work is done by a worker it submit the video into S3, 
 * and returns the link to the S3 to a response queue (SQS) for the client to view.
*/

public class SaveImage 
{

	public static void main(String[] args) throws Exception 
	{
		String imageUrl=""; 		//the variable has the image url
		String fileName="imageurl";		//this variable has the filename with the urls.
		String line = null;// reference to one line at a time 
		
		try 
        {
            /// FileReader reads text files
            FileReader fileReader = new FileReader(fileName);

            // Wrapping FileReader in BufferedReader to increase efficiency of I/O
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            
            //reading file
            System.out.println("Read File");
            
            int i=0; //counter to name the image downloaded
            while((line = bufferedReader.readLine()) != null) 
            {
                System.out.println(line);
                {
        			String destFile = "image";
        			destFile=destFile+i+".jpg";		//sorms the destination name of the image appended with the counter and the extention
        			saveImage(line, destFile);		//the url is saved as an image with the destinationFile name
        			//System.out.println((++i));
        		}		
            }   
            // close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) 
        {
        	System.out.println("Unable to open file '" + fileName);     		//error to catch in case file does not exists                 
        }
       
		
		
		Process p = Runtime.getRuntime().exec("ffmpeg -f image2 -i ~/image%d.jpg ~/video.mpg");  // to create the video using all the images in the folder /
        Process p1 = Runtime.getRuntime().exec("ffmpeg -i a.mpg -filter:v \"setpts=10*PTS\" ~/video.mpg");   // to change the fps (frames per second) for the video
        Process p2 = Runtime.getRuntime().exec("rm image*.jpg");   // need to delete all the images so that they do not get appended onto the next video because of the next task
		
        
        
        //String command = "ffmpeg -f image2 -i /home/rohit/Desktop/MS/java/Animoto/image%d.jpg /home/rohit/Desktop/a.mpg";
		//ffmpeg -i a.mpg -filter:v "setpts=10*PTS" output.mpg
		//String output = executeCommand(command);
		
		//trasfer the data to S3
        String S3url="";		//url to to S3 with the video
        
        //push  this url into ResponseQueue
        //AnimotoAmazonDynamoDB db =new AnimotoAmazonDynamoDB();
        //String Qurl = db.read(args[1]); 
        animotoSimpleQueueService q =new animotoSimpleQueueService(Qurl); //create an object q of type SQS and pass the queue URL
        q.push(S3url);
	}
		

	public static void saveImage(String Url, String d) throws IOException 
	{
		URL url = new URL(Url);  // creates an object url of type URL
		InputStream in = url.openStream();  // 
		OutputStream ou = new FileOutputStream(d);

		int len=0;
		byte[] b = new byte[2048];		//for HD quality images

		while ((len = in.read(b)) != -1) 
			ou.write(b, 0, len);
		
		in.close();  //close the InputStream
		ou.close();  //close the OutputStream
	}	

}
