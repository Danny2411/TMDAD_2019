import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.xmlpull.v1.XmlPullParserException;

import io.minio.MinioClient;
import io.minio.errors.MinioException;

// This class supports MinIO fule uploading
public class FileHandler {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeyException, XmlPullParserException {
	    try {
		      // Create a minioClient with the MinIO Server name, Port, Access key and Secret key.
		      MinioClient minioClient = new MinioClient("https://play.min.io:9000", 
		    		  "84G0K3SX5JHK990E0ZG4", 
		    		  "Ul10LpuHTUChUwuFgnps8A+fpNYnA7MsLYmFZtQw");
	
		      // Check if the bucket already exists.
		      boolean isExist = minioClient.bucketExists("xxxx");
		      if(isExist) {
		    	  System.out.println("Bucket already exists.");
		      } else {
		        // Make a new bucket to hold files.
		    	  minioClient.makeBucket("asiatrip");
		      }
	
		      // Upload the zip file to the bucket with putObject
		      //minioClient.putObject("asiatrip","asiaphotos.zip", "/home/user/Photos/asiaphotos.zip");
	      
	      	System.out.println("[MinIO] Successfully uploaded.");
	    } catch(MinioException e) {
	    	System.out.println("[MinIO] Error occurred: " + e);
	    }
	  }
	
}
