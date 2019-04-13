import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.xmlpull.v1.XmlPullParserException;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import spark.utils.IOUtils;

// This class supports MinIO fule uploading
public class FileHandler {
	
	static MinioClient minioClient = null;
	private String last_name = "foo.txt";

	public FileHandler(){
	    try {
	    	
    		InputStream input = new FileInputStream("config/config.properties");

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);
	            
		    // Create a minioClient with the MinIO Server name, Port, Access key and Secret key.
		    minioClient = new MinioClient(prop.getProperty("minio.url"), 
		    		prop.getProperty("minio.access"), 
		    		prop.getProperty("minio.key"));
	
		    // Check if the bucket already exists.
		    boolean isExist = minioClient.bucketExists("chatfiles");
		    if(isExist) {
		    	System.out.println("[MinIO] Bucket already exists.");
		    } else {
		        // Make a new bucket to hold files.
		    	minioClient.makeBucket("chatfiles");
		    }

	    } catch(MinioException e) {
	    	System.out.println("[MinIO] Error occurred: " + e);
	    } catch (IOException ioe) {
	    	System.out.println("[MinIO] Error occurred: " + ioe);
	    } catch (Exception oe) {
	    	System.out.println("[MinIO] Error occurred: " + oe);
	    }
	  }
	
	public void setName(String n) {
		last_name = n;
	}
	
	public void insertFile(byte buf[]) {
		try {
			InputStream myInputStream = new ByteArrayInputStream(buf); 
			 // create object
			 minioClient.putObject("chatfiles", last_name, myInputStream, "application/octet-stream");
			 myInputStream.close();
			 System.out.println("[MinIO] Uploaded successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Pair<String, byte[]> getFile(String name) {
		try {
			InputStream myInputStream = minioClient.getObject("chatfiles", name);
			if(myInputStream != null) {
				Pair<String, byte[]> res = new Pair<String, byte[]>(minioClient.getObjectUrl("chatfiles", name), IOUtils.toByteArray(myInputStream));
				return res;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
