package il.org.yadvashem.ead;

public class Attribute {
	public String prefix;
	public String name;
	public String value;
	
	public Attribute() { }
	
	public Attribute(String pfx, String nam, String val) {
		prefix = pfx;
		name = nam;
		value = val;
	}
}
