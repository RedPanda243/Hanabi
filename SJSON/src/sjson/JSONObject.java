package sjson;

import java.io.*;
import java.util.*;

import static sjson.JSONUtils.readUntil;
import static sjson.JSONUtils.readWhile;

//@SuppressWarnings({"WeakerAccess","unused"})
public class JSONObject extends JSONData
{
	private HashMap<String, JSONData> map;

	public JSONObject()
	{
		map = new HashMap<>();
	}

	public JSONObject(InputStream in) throws JSONException
	{
		this(new InputStreamReader(in));
	}

	public JSONObject(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public JSONObject(Reader r) throws JSONException
	{
		this();
		try
		{
			boolean flag = true;
			String name;
			char t,e;
			readWhile(r,'\t',' ','\n','\r');//skip spaces
			if (r.read() != '{')
				throw new JSONException("JSONObject must starts with '{'");
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

				name = readUntil(r,'"');
				if (has(name))
					throw new JSONException("Property "+name+" already defined");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				if (r.read()!=':')
					throw new JSONException("Name definition ends, expected ':'");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				r.mark(2);
				t = (char) r.read();
				r.reset();
				if (t == '{')
					set(name, new JSONObject(r));
				else if (t == '[')
					set(name, new JSONArray(r));
				else if (t == '"')
					set(name, new JSONString(r));
				else
					throw new IOException("Unrecognized field!");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				e = (char) r.read();
				if (e == '}')
					flag = false;
				else if (e != ',')
					throw new IOException("Value definition ends, expected '}' or ','");
			}
		}
		catch(IOException je)
		{
			throw new JSONException(je);
		}
	}

	@Deprecated
	public boolean compatible(JSONObject template)
	{
//		String[] names = template.names();
		Type ta,tb;
		String sbox;
		JSONObject obox;
		JSONArray abox;
		for (String name:template.names())
		{
			ta = this.get(name).getJSONType();
			tb = template.get(name).getJSONType();
/*			System.err.println("\nField "+name+" has to be a "+ta);
			System.err.println("Template value: "+template.get(name));
			System.err.println("Object value: "+this.get(name));
*/
			if (tb.equals(Type.STRING))
			{
				sbox = template.getString(name);
//				System.err.println("SBOX = "+sbox+"\t\t"+sbox.equals("\"\""));

				if (sbox.equals(""))
				{
					if (!ta.equals(Type.STRING))
					{
//						System.err.println("Not same type!");
						return false;
					}
//					System.err.println("Same type, OK");
				}
				else
				{
					try
					{
						if (!sbox.equals(this.getString(name)))
						{
							System.err.println("Not same value!");
							return false;
						}
					}
					catch (NullPointerException e)
					{
						System.err.println("Null value!");
						return false;
					}
				}
			}
			else if (tb.equals(Type.OBJECT))
			{
				obox = template.getObject(name);
				if (obox.equals(new JSONObject()) && ! ta.equals(Type.OBJECT))
					return false;
				try
				{
					if (!obox.equals(new JSONObject()) && !this.getObject(name).compatible(obox))
						return false;
				}
				catch (NullPointerException e)
				{
					return false;
				}
			}
			else //JSONArray
			{
				abox = template.getArray(name);
				if (abox.equals(new JSONArray()) && !ta.equals(Type.ARRAY))
					return false;
				try
				{
					if (!abox.equals(new JSONArray()))
					{
						JSONArray a = this.getArray(name);
						JSONObject o,aobox;
						boolean flag = true;
						for (int j=0; j<a.size(); j++)
						{
							o =  a.getObject(j);
							for (int k=0; k<abox.size() && flag; k++)
							{
								aobox = abox.getObject(k);
								if (aobox!=null)
								{
									if (o.compatible(aobox))
										flag = false;
								}
							}
							if (flag)
								return false;
						}
					}
				}
				catch (NullPointerException e)
				{
					return false;
				}
			}
		}
		return true;
	}

	public JSONObject clone()
	{
		return (JSONObject)super.clone();
	}

	public JSONObject copyIn(JSONObject o)
	{
		for (String name: map.keySet())
			o.map.put(name,this.get(name).clone());
		return o;
	}

	public Type getJSONType()
	{
		return Type.OBJECT;
	}

	public boolean has(String name)
	{
		return get(name)!=null;
	}

	public Iterator<String> nameIterator()
	{
		return map.keySet().iterator();
	}

	/**
	 * @return una copia del Set di nomi della mappa contenuta da questo JSONObject
	 */
	public Set<String> names()
	{
		return map.keySet();
	}
/*
	@SuppressWarnings("WeakerAccess")
	public String[] names()
	{
		String[] a = new String[map.size()];
		Iterator<String> i = map.keySet().iterator();
		int c = 0;
		while (i.hasNext())
		{
			a[c] = i.next();
			c++;
		}
		Arrays.sort(a);
		return a;
	}
*/

	public JSONData get(String name)
	{
		return map.get(name);
	}
/*
	public <T extends JSONData> T get(Class<T> cl,String name)
	{
		JSONData d;
		if (!(name.startsWith("\"")&&name.endsWith("\"")))
			d = map.get("\""+name+"\"").clone();
		else
			d = map.get(name).clone();
		if (d == null)
			return null;
		return (T)d;
	}

 */

	@SuppressWarnings("WeakerAccess")
	public JSONArray getArray(String name)
	{
		try
		{
			return (JSONArray) get(name);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			return null;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public JSONObject getObject(String name)
	{
		try
		{
			return (JSONObject) get(name);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			return null;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public String getString(String name)
	{
		try
		{
			JSONData data = get(name);
			String s = data.toString();
			if (data.getJSONType().equals((Type.STRING)))
				return s.substring(1,s.length()-1); //TOLGO I DOPPI APICI!
			return null;
		}
		catch(NullPointerException cce)
		{
			return null;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public JSONObject set(String name, JSONData value)
	{
		if (value != null)
		{
			if (!(name.startsWith("\"")&&name.endsWith("\"")))
				name = "\""+name+"\"";
			map.put(name, value);
		}
		return this;
	}

	public JSONObject set(String name, String value)
	{
		return set(name,new JSONString(value));
	}

	@SuppressWarnings("unused")
	public JSONObject remove(String name)
	{
		map.remove(name);
		return this;
	}

	@SuppressWarnings("WeakerAccess")
	public int size()
	{
		return map.size();
	}

	public final String toString(int indent)
	{
		if (indent<0)
			return JSONUtils.quote(toString(0));

		StringBuilder ret = new StringBuilder("{");
		int indname;
		for (String name:names())
		{
			if (indent>0)
				ret.append("\n");
			JSONData d = get(name);
			ret.append(tabstring(indent));
			ret.append(name);
			ret.append(":");
			indname = 1+name.length();
			int newindent = indent+indname+1;
			if (!d.getJSONType().equals(Type.STRING))
				newindent += 2;
			if (indent>0)
				ret.append(d.toString(newindent));
			else
				ret.append(d.toString(indent));
			ret.append(",");
		}

		if (names().size() > 0)
		{

			ret = new StringBuilder(ret.substring(0, ret.length()-1));
			if (indent>0)
			{
				ret.append("\n");
				ret.append(tabstring(indent-baseindent));
			}
		}
		ret.append("}");
		return ret.toString();
	}

	public Collection<JSONData> values()
	{
		ArrayList<JSONData> l = new ArrayList<>();
		for (JSONData d:map.values())
			l.add(d.clone());
		return l;
	}

}
