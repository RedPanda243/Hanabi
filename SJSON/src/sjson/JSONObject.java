package sjson;

import java.util.*;

public class JSONObject extends JSONData
{
	private HashMap<String, JSONData> map;

	@SuppressWarnings("WeakerAccess")
	public JSONObject()
	{
		map = new HashMap<>();
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
				sbox = template.get(JSONString.class,name).toString();
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
						if (!sbox.equals(this.get(JSONString.class,name).toString()))
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
				obox = template.get(JSONObject.class,name);
				if (obox.equals(new JSONObject()) && ! ta.equals(Type.OBJECT))
					return false;
				try
				{
					if (!obox.equals(new JSONObject()) && !this.get(JSONObject.class,name).compatible(obox))
						return false;
				}
				catch (NullPointerException e)
				{
					return false;
				}
			}
			else //JSONArray
			{
				abox = template.get(JSONArray.class,name);
				if (abox.equals(new JSONArray()) && !ta.equals(Type.ARRAY))
					return false;
				try
				{
					if (!abox.equals(new JSONArray()))
					{
						JSONArray a = this.get(JSONArray.class,name);
						JSONObject o,aobox;
						boolean flag = true;
						for (int j=0; j<a.size(); j++)
						{
							o =  a.optJSON(j);
							for (int k=0; k<abox.size() && flag; k++)
							{
								aobox = abox.optJSON(k);
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

	@SuppressWarnings("WeakerAccess")
	public JSONObject copyIn(JSONObject o)
	{
		JSONObject c = o.clone();
		for (String name: map.keySet())
			c.set(name,this.get(name).clone());
		return c;
	}

	public Type getJSONType()
	{
		return Type.OBJECT;
	}

	@SuppressWarnings("WeakerAccess")
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
		Set<String> c = new HashSet<>();
		for (String n:map.keySet())
			c.add(""+n);
		return c;
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
		return get(JSONData.class,name);
	}

	@SuppressWarnings("WeakerAccess")
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
/*
	@SuppressWarnings("WeakerAccess")
	public JSONArray optArray(String name)
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
	public JSONObject optJSON(String name)
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
	public String optString(String name)
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
	*/

	@SuppressWarnings("WeakerAccess")
	public JSONObject set(String name, JSONData value)
	{
		JSONObject obj = this.clone();
		if (value != null)
		{
			if (!(name.startsWith("\"")&&name.endsWith("\"")))
				name = "\""+name+"\"";
			obj.map.put(name, value);
		}
		return obj;
	}

	@SuppressWarnings("unused")
	public JSONObject remove(String name)
	{
		JSONObject obj = this.clone();
		obj.map.remove(name);
		return obj;
	}

	@SuppressWarnings("WeakerAccess")
	public int size()
	{
		return map.size();
	}

	public String toString(int indent)
	{
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
