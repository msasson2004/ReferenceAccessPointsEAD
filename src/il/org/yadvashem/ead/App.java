package il.org.yadvashem.ead;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

	private static FileWriter writer;

	public static void main(String[] args) {
    	String table_mappings, folder_input, result_file, out_folder;
    	File[] listOfFiles;

    	if(args.length > 1) {
        	table_mappings = args[0];
        	folder_input = args[1];
        	out_folder = folder_input + "_coref";
        	File theDir = new File(out_folder);
        	
        	try 
        	{
        		if(!theDir.exists())
        			theDir.mkdir();
        		File folder = new File(folder_input);
        		listOfFiles = folder.listFiles();
        	}
        	catch(Exception e) {
        		System.out.println(e.getMessage());
        		return;
        	}
    	}
    	else {
    		System.out.println("Syntax: java App.class <mapping file> <input dir>");
    		return;
    	}
    	 

	    try 
	    {	
	    	Mappings map = new Mappings(table_mappings);
	    	HashMap<String, SubjectTerms> hash = map.build();
	    	
	    	for(int i=0;i < listOfFiles.length;i++) 
	    	{
	    		if (listOfFiles[i].isFile()) 
	    		{	   
	                String file_name = listOfFiles[i].getName();
	            	result_file = out_folder + "/" + file_name;        	
			        System.out.println("working on: " + file_name);
			        parseXML(hash, folder_input + "/" + file_name, result_file);
	    		}
	    	}
	        System.out.println("Done");
	    } 
	    catch (Exception e) {
    		System.out.println(e.getMessage());
    	}	    	
    }

    private static String writeStartElement(XMLStreamReader xmlStreamReader, XMLStreamWriter xtw, String name) 
    {
    	try 
    	{
			int nsCount = xmlStreamReader.getNamespaceCount();
			if(nsCount > 0) {
				xtw.writeStartElement(name);	                			
				for(int i=0;i<nsCount;i++) {
					String prefix = xmlStreamReader.getNamespacePrefix(i);
					String uri = xmlStreamReader.getNamespaceURI(i);
	    	    	xtw.writeNamespace(prefix, uri);
				}
			}
			else xtw.writeStartElement(name);
			int count = xmlStreamReader.getAttributeCount();
			for(int i=0;i<count;i++) 
			{
				name = xmlStreamReader.getAttributeLocalName(i);
				String prefix = xmlStreamReader.getAttributePrefix(i);
				String value = xmlStreamReader.getAttributeValue(i);
				if(prefix != null && !prefix.isEmpty()) 
				{
					if(prefix.equals("xsi")) 
					{
						xtw.writeAttribute("xsi:" + name, value);
					}
					else xtw.writeAttribute(prefix, "", name, value);
				}
				else xtw.writeAttribute(name, value);
			}
			return null;
    	}
    	catch(Exception e) {
    		String err = e.getMessage();
    		System.out.println(err);
    		return err;
    	}
    }
    
    private static HashMap<String, Integer> writeSourceAuth(XMLStreamWriter xtw, SubjectTerms st, String txt, HashMap<String, Integer> map) throws XMLStreamException  
    {  	
		if(st == null) {
			xtw.writeCharacters(txt);
			Integer n = map.get(txt);
			if(n == null)
				map.put(txt, 1);
			else map.put(txt, ++n);
		}
		else 
		{
			xtw.writeAttribute("authfilenumber", st.Num);
			xtw.writeAttribute("source", st.Auth);
			xtw.writeCharacters(txt);		        				
		}
		return map;
    }
    
    private static void writeElement(XMLStreamWriter xtw, XMLElement elem) throws XMLStreamException  
    {  	
    	xtw.writeStartElement(elem.localName);
    	
		for(int i=0;i<elem.attributeList.size();i++) 
		{
			Attribute attr = elem.attributeList.get(i);
			if(attr.prefix != null && !attr.prefix.isEmpty()) 
			{
				if(attr.prefix.equals("xsi")) // workaround for illegal xsi syntax 
				{
					xtw.writeAttribute("xsi:" + attr.name, attr.value);
				}
				else xtw.writeAttribute(attr.prefix, "", attr.name, attr.value);
			}
			else xtw.writeAttribute(attr.name, attr.value);
		}
		xtw.writeEndElement();
    }

    private static XMLElement readElement(XMLStreamReader xmlStreamReader) 
    {
    	try 
    	{
    		String localName = xmlStreamReader.getLocalName();
    		XMLElement elem = new XMLElement(localName);
			int count = xmlStreamReader.getAttributeCount();
			for(int i=0;i<count;i++) 
			{
				String name = xmlStreamReader.getAttributeLocalName(i);
				String prefix = xmlStreamReader.getAttributePrefix(i);
				String value = xmlStreamReader.getAttributeValue(i);
				elem.addAttribute(prefix, name, value);
			}
	        while (xmlStreamReader.hasNext())
	        {
	        	int event = xmlStreamReader.next();
	        	if(event == XMLStreamConstants.CHARACTERS) {
	        		elem.text = xmlStreamReader.getText();
	        	}
	        	else if(event == XMLStreamConstants.END_ELEMENT) {
	        		break;
	        	}
	        }
			return elem;
    	}
    	catch(Exception e) 
    	{
    		String err = e.getMessage();
    		System.out.println(err);
    		return null;
    	}
    }
    
    private static String readCharacters(XMLStreamReader xmlStreamReader, String txt) throws XMLStreamException {
    	while(xmlStreamReader.hasNext()) 
    	{           
    		int event = xmlStreamReader.next();	
    		if(event == XMLStreamConstants.CHARACTERS) {
        		txt += xmlStreamReader.getText();
        	}
        	else if(event == XMLStreamConstants.END_ELEMENT) {
        		break;
        	}
    	}
    	return txt.trim();
    }
    
    // This method checks if the given text is potential key term or non essential data that should be skipped.
    // To extend the method reach, add a | and non essential text to be skipped in the regExp variable.
    private static boolean isSkipData(String data) {
    	if(data == null)
    		return true;
    	data = data.trim();
    	if(data.isEmpty())
    		return true;
    	String regExp = "The file was created|in year";
    	Pattern p = Pattern.compile(regExp);
    	Matcher m = p.matcher(data);
        if(m.find()) 
        	return true;
        else return false;
    }
    
    
    private static void parseXML(HashMap<String, SubjectTerms> hash, String in_file, 
    							 String out_file) {
    	boolean inOrig = false, inName = false, inGeog = false,
    			inCorp = false, inPers = false;
    	String name;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        
        XMLOutputFactory xof =  XMLOutputFactory.newInstance();
//        xof.setProperty("escapeCharacters", false);
        XMLStreamWriter xtw = null;
        SubjectTerms st = null;
        HashMap<String, Integer> missing = new HashMap<String, Integer>();
        
        try 
        {
            xtw = xof.createXMLStreamWriter(new FileOutputStream(out_file), "UTF-8");
        	XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileInputStream(in_file));
        	int event = xmlStreamReader.getEventType();
            
            while(true)
            {
                switch(event) 
                {
	                case XMLStreamConstants.START_ELEMENT: 
	                	name = xmlStreamReader.getLocalName();
	                    if(name.equals("name"))
	                    {
	                    	inName = true;
	                    }
	                    else if(name.equals("corpname"))
	                    {
	                    	inCorp = true;
	                    }
	                    else if(name.equals("geogname"))
	                    {
	                    	inGeog = true;
	                    }
	                    else if(name.equals("persname"))
	                    {
	                    	inPers = true;
	                    }
	                    else if(name.equals("origination"))
	                    {
	                    	inOrig = true;
	                    }
	                    String err = writeStartElement(xmlStreamReader, xtw, name);
	                    if(err != null && !err.isEmpty())
	                    {
	                    	System.out.println("Stopping...");
	                    	break;
	                    }
            			xtw.flush();
	                    break;
	                case XMLStreamConstants.CHARACTERS:
	                    if(inName || inCorp || inPers || inGeog)
	                    {
		        			String orig = xmlStreamReader.getText();
		        			String txt = orig.trim();

		        			// we might have a problem here. if there is an extptr element inside geogname, then 
		        			// don't want to finish writing geogname

		        			if(inGeog) 
		        			{		        				
		        				if(!txt.equals(""))
		        				{
	        						txt = readCharacters(xmlStreamReader, txt);
		        					st = hash.get(txt.toLowerCase());
		        					missing = writeSourceAuth(xtw, st, txt, missing);
		        					xtw.writeEndElement();
		        					inGeog = false;
		        				}
		        				else // txt == "" originally \n
		        				{
		        					event = xmlStreamReader.next();
			        				if(event == XMLStreamConstants.START_ELEMENT) 
			        				{
			        					// we probably have extptr here
			        					XMLElement elem = readElement(xmlStreamReader);
			        					if(elem == null) 
			        					{
			        						System.out.println("Stopping...");
			        						break;
			        					}
			        					
			        					// buf contains the extptr element in full 
			        					event = xmlStreamReader.next();
			        					if(event == XMLStreamConstants.CHARACTERS) {
			        						txt = xmlStreamReader.getText().trim();
			        						String clean = txt.trim();
			        						st = hash.get(clean.toLowerCase()); // get what's after the extptr element
				        					// write the rest of the geogname attributes
			        						if(st == null) {
			        							xtw.writeCharacters(txt);
			        							System.out.println("st name not found: " + txt);
			            						Integer n = missing.get(txt);
			            						if(n == null)
			            							missing.put(txt, 1);
			            						else missing.put(txt, ++n);
			        						}
			        						else 
			        						{
			        							xtw.writeAttribute("authfilenumber", st.Num);
			        							xtw.writeAttribute("source", st.Auth);
			        							xtw.writeCharacters("\n\t");
			        						}		 	
			        						writeElement(xtw, elem);
			        						xtw.writeCharacters("\n\t" + txt + "\n\t");
			        					}
			        				}
			        				else if(event == XMLStreamConstants.CHARACTERS)
			        				{
			        					txt = xmlStreamReader.getText().trim();
			        					st = hash.get(txt.toLowerCase());
			        					missing = writeSourceAuth(xtw, st, txt, missing);
			        				}
			        				else if(event == XMLStreamConstants.END_ELEMENT) {
			        					xtw.writeEndElement();
			        					inGeog = false;
			        				}
			        				else if(event == XMLStreamConstants.COMMENT)
			        					xtw.writeComment(xmlStreamReader.getLocalName());			
		        				}
		        			}
		        			else 
		        			{
		        				String clean = txt.trim();
			        			st = hash.get(clean.toLowerCase());	        				
	        					missing = writeSourceAuth(xtw, st, txt, missing);
		        			}
	                    }
	                    else if(inOrig)
	                    {
		        			name = xmlStreamReader.getText();
		        			if(!isSkipData(name)) {
			        			String clean = name.trim();
			        			st = hash.get(clean.toLowerCase());
	        					if(st == null) {
	        						Integer n = missing.get(name);
	        						if(n == null)
	        							missing.put(name, 1);
	        						else missing.put(name, ++n);
	        					}
	        					missing = writeSourceAuth(xtw, st, name, missing);
		        			}
		        			else xtw.writeCharacters(name);
	                    }
	                    else 
	                    {
	                    	name = xmlStreamReader.getText();
	                    	xtw.writeCharacters(name);
	                    }
                    	xtw.flush();			
        				st = null;
	                    break;
	                case XMLStreamConstants.END_ELEMENT:
	                    if(inName)
	                    {
	                        inName=false;
	                    }
	                    else if(inCorp)
	                    {
	                        inCorp=false;
	                    }
	                    else if(inGeog)
	                    {
	                    	inGeog = false;
	                    }
	                    else if(inPers)
	                    {
	                        inPers=false;
	                    }
	                    else if(inOrig)
	                    {
	                        inOrig=false;
	                    }
	                    xtw.writeEndElement();
	                    break;
	                case XMLStreamConstants.COMMENT:
	                	xtw.writeComment(xmlStreamReader.getLocalName());
	                	break;                
	                case XMLStreamConstants.START_DOCUMENT:
	                	String version = xmlStreamReader.getVersion();
	                	xtw.writeStartDocument("UTF-8", version);
	                	xtw.writeCharacters("\n");
	                    xtw.flush();
	                    break;
		            case XMLStreamConstants.END_DOCUMENT:
		            	xtw.writeEndDocument();
		            	break;
	            }            
		        if (!xmlStreamReader.hasNext())
		                break;
	              	event = xmlStreamReader.next();
	        }  
            out_file = out_file.replace(".xml", "_missing.txt");
            writer = new FileWriter( out_file );
            int missed = 0;
            
            java.util.Iterator<String> it = missing.keySet().iterator();
            while (it.hasNext()) {
            	try {
            		String key = it.next();
            		int val = missing.get(key);
            		writer.write("key: " + key + ", num missed: " + val + "\n");
            		missed = missed + val;
            	}
            	catch(Exception e) {
            		System.out.println(e.getMessage());
            	}
            }
            writer.write("Total missed keys: " + missing.size() + " total missed count: " + missed);
            writer.close();
        } 
        catch (XMLStreamException | IOException e) 
        {
        	try 
        	{
	        	if(xtw != null)
	        		xtw.flush();        	
	        	System.out.println(e.getMessage());
        	}
        	catch(Exception ex) {
        		System.out.println(ex.getMessage());
        	}
        }
    }
}
