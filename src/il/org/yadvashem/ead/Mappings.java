package il.org.yadvashem.ead;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import com.opencsv.CSVReader;

public class Mappings 
{
	private String file_path;
	public Mappings(String filePath) {
		file_path = filePath;
	}
	
	public HashMap<String, SubjectTerms> build() 
	{
        try 
        {
        	HashMap<String, SubjectTerms> hash = new HashMap<String, SubjectTerms>(); 
        	Charset set = Charset.forName("UTF-8");
        	Reader reader = Files.newBufferedReader(Paths.get(file_path), set);
            CSVReader csvReader = new CSVReader(reader);
        	int count = 0;
            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) 
            {
            	if(nextRecord.length == 3) 
            	{
            		String rawKey = nextRecord[0];
            		String key = nextRecord[0].trim();
                	String auth = nextRecord[1];
                	String id = nextRecord[2];
                	SubjectTerms st = new SubjectTerms(rawKey, auth, id);
                	SubjectTerms st2 = hash.get(key);
                	if(st2 != null) {
                		if(!st2.Num.equals(id))
                			System.out.println("collision: " + key + " id mismatch " + st2.Num + " - " + id);
                		if(!st2.Auth.equals(auth))
                			System.out.println("collision: " + key + " auth mismatch " + st2.Auth + " - " + auth);
                	}
                	hash.put(key.toLowerCase(), st);
                	count++;
                }
            	else 
            	{
            		System.out.println("Incorrect mapping line: " + nextRecord[0]);
            	}
            }
            csvReader.close();
            System.out.println("Total of " + count);
            System.out.println("In hash " + hash.size());
            return hash;
        } 
        catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            return null;
        }
    }
}
