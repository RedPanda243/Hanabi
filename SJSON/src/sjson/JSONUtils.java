package sjson;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class JSONUtils
{
	private JSONUtils(){}
/*
	public static JSONArray createNewArray()
	{
		return new JSONArray();
	}

	public static JSONObject createNewMessage()
	{
		return new JSONObject();
	}

	public static api1.interfaces.Request createNewRequest(String query) throws IOException
	{
		JSONObject m = createNewMessage();
		m.set(api1.interfaces.Request.NAME,JSONUtils.fromString(query));
		m.set(api1.interfaces.Request.FROM, Drone.getInstance().getLocalHive().get(Device.NAME));
		m.set(api1.interfaces.Request.ID, Drone.getInstance().generateRequestId());
		return new Request(m);
	}

	public static api1.interfaces.Response createNewResponse(String status) throws IOException
	{
		JSONObject m = createNewMessage();
		m.set(api1.interfaces.Response.STATUS,JSONUtils.fromString(status));
		return new Response(m);
	}
*/
	public static <T extends JSONData> T fromBytes(Class<T> cl, byte[] b) throws IOException
	{
		return fromString(cl,StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(b)).toString());
	}

	public static <T extends JSONData> T fromReader(Class<T> cl, BufferedReader r) throws IOException
	{
		if (r==null)
			throw new IOException(new NullPointerException());
		r.mark(2);
		int a = r.read();
		if (a=='{')
		{
			return (T) readJSONObject(r);
		}
		if (a=='[')
			return (T)readArray(r);
		if (a=='"')
			return (T)readString(r);
		r.reset();
		return null;
	}

	public static <T extends JSONData> T fromReader(Class<T> cl, Reader r) throws IOException
	{
		return fromReader(cl,new BufferedReader(r));
	}

	public static <T extends JSONData> T fromStream(Class<T> cl, InputStream is) throws IOException
	{
		return fromReader(cl,new InputStreamReader(is));
	}

	public static <T extends JSONData> T fromString(Class<T> cl, String s) throws IOException
	{
		if (!((s.startsWith("{")&&s.endsWith("}"))||(s.startsWith("[")&&s.endsWith("]"))||(s.startsWith("\"")&&s.endsWith("\""))))
			s = "\""+s+"\"";
		return fromReader(cl,new StringReader(s));
	}

	private static JSONArray readArray(BufferedReader r) throws IOException
	{
		boolean flag = true;
		char t,e;
		JSONArray a = new JSONArray();
		try
		{
			readWhile(r,'\t',' ','\n','\r');//skip spaces
			r.mark(2);
			if (r.read()==']')
				flag = false;
			else
				r.reset();
			while(flag)
			{
				readWhile(r,'\t',' ','\n','\r');//skip spaces
				t = (char) r.read();
				if (t == '{')
					a.add(readJSONObject(r));
				else if (t == '[')
					a.add(readArray(r));
				else if (t == '"')
					a.add(readString(r));

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				e = (char) r.read();
				if (e == ']')
					flag = false;
				else if (e != ',')
					throw new JSONException("Value definition ends, expected '}' or ','");
			}
		}
		catch(JSONException je)
		{
			throw new IOException(je);
		}
		return a;
	}

	private static JSONObject readJSONObject(BufferedReader r) throws IOException
	{
		boolean flag = true;
		char t,e;
		String name;
		JSONObject m = new JSONObject();
		try
		{
			readWhile(r,'\t',' ','\n','\r');//skip spaces
			r.mark(2);
			if (r.read()=='}')
				flag = false;
			else
				r.reset();
			char c;
			while(flag)
			{
				readWhile(r,'\t',' ','\n','\r');//skip spaces

				if ((c = (char)r.read())!='\"')
					throw new JSONException("Name definition begins, expected a string beginning with '\"' but '"+c+"' founded!");

				name = readString(r).toString();
				if (m.has(name))
					throw new JSONException("Property "+name+" already defined");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				if (r.read()!=':')
					throw new JSONException("Name definition ends, expected ':'");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				t = (char) r.read();
				if (t == '{')
					m.set(name, readJSONObject(r));
				else if (t == '[')
					m.set(name, readArray(r));
				else if (t == '"')
					m.set(name, readString(r));

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				e = (char) r.read();
				if (e == '}')
					flag = false;
				else if (e != ',')
					throw new JSONException("Value definition ends, expected '}' or ','");
			}
		}
		catch(JSONException je)
		{
			throw new IOException(je);
		}
		return m;
	}

	private static JSONData readString(BufferedReader r) throws IOException
	{
		String s = readUntil(r,'"');
		return new JSONString(s.substring(0,s.length()-1));
	}

	private static String readUntil(Reader r, char... cs) throws IOException
	{
		StringBuilder s = new StringBuilder();
		char box = ' ',box1;
		boolean flag = true;
		while(flag)
		{
			box1 = box;
			box = (char) r.read();
			if (box == (char)-1)
				throw new EOFException();
			s.append(box);
			if (box1 != '\\')
			{
				for (char c:cs)
				{
					if (box == c)
						flag = false;
				}
			}
		}
		return s.toString();
	}

	private static String readWhile(Reader r, char...cs) throws IOException
	{
		StringBuilder s = new StringBuilder();
		boolean flag = true;
		char box = ' ',box1;
		while(flag)
		{
			flag = false;
			r.mark(2);
			box1 = box;
			box = (char) r.read();
			if (box1 != '\\')
			{
				for (char c:cs)
				{
					if (box == c)
						flag = true;
				}
			}
			if (flag)
				s.append(box);
		}
		r.reset();
		return s.toString();
	}
}
