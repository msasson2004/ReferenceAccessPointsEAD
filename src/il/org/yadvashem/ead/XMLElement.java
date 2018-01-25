package il.org.yadvashem.ead;

import java.util.ArrayList;

public class XMLElement {
	public String localName;
	public String text;
	
	public ArrayList<Attribute> attributeList;
	
	public XMLElement() {
		attributeList = new ArrayList<Attribute>();
	}
	
	public XMLElement(String name) {
		localName = name;
		attributeList = new ArrayList<Attribute>();
	}
	
	public void addAttribute(String prefix, String name, String value) {
		Attribute elem = new Attribute(prefix, name, value);
		attributeList.add(elem);
	}
}
