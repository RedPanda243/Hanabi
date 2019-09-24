package sjson;

public class JSONString extends JSONData
{
	private String s;

	@SuppressWarnings("WeakerAccess")
	public JSONString(String s)
	{
		this.s=""+s;
	}

	public JSONString clone()
	{
		return (JSONString)super.clone();
	}

	public Type getJSONType()
	{
		return Type.STRING;
	}


	private static String quote(String s)
	{
		return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\r","\\r")
				.replace("\n", "\\n").replace("\"", "\\\"").replace("{", "\\{")
				.replace("[", "\\[").replace("}", "\\}").replace("]", "\\]");
	}

	@SuppressWarnings("unused")
	public int size()
	{
		return s.length();
	}

	public String toString()
	{
		return toString(0);
	}

	public final String toString(int indent)
	{
		/*
		if (indent<0)
			return "\""+quote(s)+"\"";
		*/
		String val = ""+s;
		if (indent>0)
			val = val.replace("\n", "\n"+ tabstring(indent)).replace("\r", "\r"+tabstring(indent));
		return "\""+val+"\"";

	}

	private static String unquote(String s)
	{
		return s.replace("\\\\", "\\").replace("\\t", "\t").replace("\\r","\r")
				.replace("\\n", "\n").replace("\\\"", "\"").replace("\\{", "{")
				.replace("\\[", "[").replace("\\}", "}").replace("\\]", "]");
	}

}
