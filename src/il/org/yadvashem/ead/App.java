package il.org.yadvashem.ead;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import java.util.HashMap;

public class App {

	public static void main(String[] args) {
    	String table_mappings, folder_input, result_file, out_folder;
    	File[] listOfFiles;

    	if(args.length > 1) {
        	table_mappings = args[0];
        	folder_input = args[1];
        	out_folder = folder_input + "_result";
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

    
    private static void parseXML(HashMap<String, SubjectTerms> hash, String in_file, 
    							 String out_file) {
    	boolean inOrig = false, inCtrlAccs = false, inName = false,
    			inCorp = false, inPers = false, inSubj = false;
    	String name;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        
        XMLOutputFactory xof =  XMLOutputFactory.newInstance();
//        xof.setProperty("escapeCharacters", false);
        XMLStreamWriter xtw = null;
        SubjectTerms st = null;
        
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
	                    else if(name.equals("persname"))
	                    {
	                    	inPers = true;
	                    }
	                    else if(name.equals("subject"))
	                    {
	                    	inSubj = true;
	                    }
	                    else if(name.equals("origination"))
	                    {
	                    	inOrig = true;
	                    }
	                    else if(name.equals("controlaccess"))
	                    {
	                    	inCtrlAccs = true;
	                    }
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
	                    
            			xtw.flush();
	                    break;
	                case XMLStreamConstants.CHARACTERS:
	                    if(inName || inCorp || inPers || inSubj)
	                    {
		        			String txt = xmlStreamReader.getText().replace("&lt;", "<").replaceAll("&gt;", ">");
		        			String utf8Hash = new String(txt.getBytes("UTF-8"));
		        			st = hash.get(utf8Hash);
		        			if(st == null) {
		        				xtw.writeCharacters(txt);
		        				System.out.println("st name not found: " + txt);
		        			}
		        			else 
		        			{
		        				xtw.writeAttribute("authfilenumber", st.Num);
		        				xtw.writeAttribute("source", st.Auth);
		        				xtw.writeCharacters(txt);		        				
		        			}
	                    }
	                    else if(inOrig)
	                    {
		        			name = xmlStreamReader.getText().replace("&lt;", "<").replaceAll("&gt;", ">");
		        			String utf8Hash = new String(name.getBytes("UTF-8"));
		        			st = hash.get(utf8Hash);
		        			if(st == null)
		        				xtw.writeCharacters(name);
		        			else {
		        				xtw.writeAttribute("authfilenumber", st.Num);
		        				xtw.writeAttribute("source", st.Auth);
		        				xtw.writeCharacters(name);	
		        			}
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
	                    else if(inPers)
	                    {
	                        inPers=false;
	                    }
	                    else if(inSubj)
	                    {
	                        inSubj=false;
	                    }
	                    else if(inOrig)
	                    {
	                        inOrig=false;
	                    }
	                    else if(inCtrlAccs)
	                    {
	                        inCtrlAccs=false;
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
