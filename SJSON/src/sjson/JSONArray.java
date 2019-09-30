package sjson;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import static sjson.JSONUtils.readWhile;

public class JSONArray extends JSONData implements Iterable<JSONData>
{
	private ArrayList<JSONData> list;

	@SuppressWarnings("WeakerAccess")
	public JSONArray()
	{
		list = new ArrayList<>();
	}

	public JSONArray(InputStream in) throws JSONException
	{
		this(new InputStreamReader(in));
	}

	public JSONArray(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public JSONArray(Reader r) throws JSONException
	{
		this();
		boolean flag = true;
		char t,e;
		try
		{
			readWhile(r,'\t',' ','\n','\r');//skip spaces
			if (r.read() != '[')
				throw new JSONException("JSONArray must starts with '['");
			r.mark(2);
			if (r.read()==']')
				flag = false;
			else
				r.reset();
			while(flag)
			{
				readWhile(r,'\t',' ','\n','\r');//skip spaces
				r.mark(2);
				t = (char) r.read();
				r.reset();
				if (t == '{')
					add(new JSONObject(r));
				else if (t == '[')
					add(new JSONArray(r));
				else if (t == '"')
					add(new JSONString(r));
				else
					throw new IOException("Unrecognized field!");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				e = (char) r.read();
				if (e == ']')
					flag = false;
				else if (e != ',')
					throw new IOException("Value definition ends, expected ']' or ','");
			}
		}
		catch(IOException je)
		{
			throw new JSONException(je);
		}
	}

	@SuppressWarnings("WeakerAccess")
	public JSONArray add(JSONData d)
	{
		list.add(d);
		return this;
	}

	public JSONArray add(String s)
	{
		return add(new JSONString(s));
	}

	public JSONArray clone()
	{
		return (JSONArray) super.clone();
	}

	@SuppressWarnings("unused")
	public JSONArray copyIn(JSONArray array)
	{
		for (JSONData d:this)
			array.list.add(d.clone());
		return this;
	}

	public Type getJSONType()
	{
		return Type.ARRAY;
	}

	public JSONArray insert(int index, JSONData value)
	{
		list.add(index,value);
		return this;
	}

	public JSONArray insert(int index, String s)
	{
		return insert(index,new JSONString(s));
	}

	public Iterator<JSONData> iterator()
	{
		return list.iterator();
	}

	public JSONData get(int index)
	{
		return list.get(index);
	}
/*
	public <T extends JSONData> T get(Class<T> cl, int index)
	{
		try
		{
			return (T)list.get(index).clone();
		}
		catch(IndexOutOfBoundsException | ClassCastException e)
		{
			return null;
		}
	}

*/

	@SuppressWarnings("unused")
	public JSONArray getArray(int index)
	{
		try
		{
			return (JSONArray) get(index);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			return null;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public JSONObject getObject(int index)
	{
		try
		{
			return (JSONObject) get(index);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			return null;
		}
	}

	@SuppressWarnings("unused")
	public String getString(int index)
	{
		try
		{
			return get(index).toString();
		}
		catch(NullPointerException cce)
		{
			return null;
		}
	}

	public boolean has(String s)
	{
		return has(new JSONString(s));
	}

	public boolean has(JSONData d)
	{
		for(JSONData ad: this)
		{
			if (ad.equals(d))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	public int indexOf(JSONData d)
	{
		for(int i=0; i<this.size(); i++)
		{
			if (get(i).equals(d))
				return i;
			i++;
		}
		return -1;
	}

	@SuppressWarnings("unused")
	public JSONArray remove(int index)
	{
		list.remove(index);
		return this;
	}

	@SuppressWarnings("unused")
	public JSONArray replace(int index, JSONData d)
	{
		list.set(index,d);
		return this;
	}

	public JSONArray replace(int index, String s)
	{
		return replace(index,new JSONString(s));
	}

	@SuppressWarnings("WeakerAccess")
	public int size() 
	{
		return list.size();
	}

	public final String toString(int indent)
	{
		if (indent<0)
			return JSONUtils.quote(toString(0));

		StringBuilder ret = new StringBuilder("[");
		for (JSONData data:list)
		{
			if (indent>0) {
				ret.append("\n").append(tabstring(indent));
				ret.append(data.toString(indent+baseindent));
			}
			else
				ret.append(data.toString(indent));
			ret.append(",");
		}

		if (size() > 0)
		{
			ret = new StringBuilder(ret.substring(0, ret.length()-1));
			if (indent>0)
			{
				ret.append("\n");
				ret.append(tabstring(indent-baseindent));
			}
		}
		ret.append("]");
		return ret.toString();
	}
}
