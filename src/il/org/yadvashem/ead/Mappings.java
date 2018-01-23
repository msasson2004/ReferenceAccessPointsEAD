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
        	
            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) 
            {
//                System.out.println(readLine);
            	if(nextRecord.length == 3) 
            	{
                	String key = nextRecord[0].replace("\"", "");
                	String auth = nextRecord[1].replace("\"", "");
                	String id = nextRecord[2].replace("\"", "");
                	SubjectTerms st = new SubjectTerms(key, auth, id);
                	hash.put(key, st);
                }
            	else {
            		System.out.println("Incorrect mapping line: " + nextRecord[0]);
            	}
            }
            csvReader.close();
            return hash;
        } 
        catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            return null;
        }
    }
}
