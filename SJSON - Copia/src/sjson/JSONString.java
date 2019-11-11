package sjson;

import java.io.IOException;
import java.io.Reader;

import static sjson.JSONUtils.readUntil;

public class JSONString extends JSONData
{
	private String s;

	@SuppressWarnings("WeakerAccess")
	public JSONString(String s)
	{
		this.s=""+s;
		if (s.startsWith("\"") && s.endsWith("\""))
			this.s = s.substring(1,s.length()-1);
	}

	public JSONString(Reader r) throws JSONException
	{
		try
		{
			if (r.read() == '"')
				s = readUntil(r, '"');
			else
				throw new IOException("JSONString must starts and ends with '\"'");
		}
		catch(IOException e)
		{
			throw new JSONException(e);
		}
	}

	public JSONString clone()
	{
		return (JSONString)super.clone();
	}

	public Type getJSONType()
	{
		return Type.STRING;
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
		if (indent<0)
			return "\""+JSONUtils.quote(s)+"\"";
		String val = ""+s;
		if (indent>0)
			val = val.replace("\n", "\n"+ tabstring(indent)).replace("\r", "\r"+tabstring(indent));
		return "\""+val+"\"";
	}
}
