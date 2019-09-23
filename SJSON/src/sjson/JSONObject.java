package sjson;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

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
		String[] names = template.names();
		Type ta,tb;
		String sbox;
		JSONObject obox;
		JSONArray abox;
		for (String name:names)
		{
			ta = this.opt(name).getJSONType();
			tb = template.opt(name).getJSONType();
/*			System.err.println("\nField "+name+" has to be a "+ta);
			System.err.println("Template value: "+template.opt(name));
			System.err.println("Object value: "+this.opt(name));
*/
			if (tb.equals(Type.STRING))
			{
				sbox = template.optString(name);
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
						if (!sbox.equals(this.optString(name)))
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
			else if (tb.equals(Type.JSON))
			{
				obox = template.optJSON(name);
				if (obox.equals(new JSONObject()) && ! ta.equals(Type.JSON))
					return false;
				try
				{
					if (!obox.equals(new JSONObject()) && !this.optJSON(name).compatible(obox))
						return false;
				}
				catch (NullPointerException e)
				{
					return false;
				}
			}
			else //JSONArray
			{
				abox = template.optArray(name);
				if (abox.equals(new JSONArray()) && !ta.equals(Type.ARRAY))
					return false;
				try
				{
					if (!abox.equals(new JSONArray()))
					{
						JSONArray a = this.optArray(name);
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
		String[] n = names();
		for (int i=0; i<this.size(); i++)
		{
			o.put(n[i], this.opt(n[i]).clone());
		}
		return o;
	}
/*
	private Object get(String name) throws JSONException
	{
		Object o = opt(name);
		if (o == null)
			throw new JSONException("Field "+name+" missing");
		return o;
	}
	
	public JSONArray getJSONArray(String name) throws JSONException
	{
		try
		{
			return (JSONArray)get(name);
		}
		catch(ClassCastException cce)
		{
			throw new JSONException(cce);
		}
	}
	
	public JSONObject getJSONObject(String name) throws JSONException
	{
		try
		{
			return (JSONObject)get(name);
		}
		catch(ClassCastException cce)
		{
			throw new JSONException(cce);
		}
	}
	
	public String getString(String name) throws JSONException
	{
		try
		{
			return (String)get(name);
		}
		catch(ClassCastException cce)
		{
			throw new JSONException(cce);
		}
	}

	public Type getJSONType(String name) throws JSONException
	{
		Type t = optType(name);
		if (t == null)
			throw new JSONException("Field "+name+" missing");
		return t;
	}
*/
	public Type getJSONType()
	{
		return Type.JSON;
	}

	@SuppressWarnings("WeakerAccess")
	public boolean has(String name)
	{
		return opt(name)!=null;
	}
	/*
	public static void main(String args[]) throws JSONException
	{
		System.out.println(new JSONObject("{}"));
		JSONObject j = new JSONObject();
		j.put("ciaobellobello", "pippo\npippo");
		JSONObject j1 = new JSONObject();
		j1.put("ciao1", "yo");
		j.put("sjson", j1);
		JSONArray a = new JSONArray("[\"Questo\",\"�	un	esempio	di	array\"]");
		a.put(j1);
		j.put("a", a);
		j.put("j2", new JSONObject());
		System.out.println(j);
		System.out.println(j.toStringLine());
		
		JSONObject J = new JSONObject();
		JSONArray A = new JSONArray();
		A.put(j1);
		J.put("ciaobellobello", "pippo\npippo");
		J.put("a", A);
		J.put("sjson", j1);
		J.put("j2", j1);
		System.out.println(J.compatible(j));
	}
	*/

	@SuppressWarnings("unused")
	public Iterator<JSONData> iterator()
	{
		return map.values().iterator();
	}

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

	@SuppressWarnings("WeakerAccess")
	public JSONData opt(String name)
	{
		if (!(name.startsWith("\"")&&name.endsWith("\"")))
			name = "\""+name+"\"";
		return map.get(name);
	}

	@SuppressWarnings("WeakerAccess")
	public JSONArray optArray(String name)
	{
		try
		{
			return (JSONArray) opt(name);
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
			return (JSONObject) opt(name);
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
			JSONData data = opt(name);
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
	public JSONObject put(String name, JSONData value)
	{
		if (value != null)
		{
			if (!(name.startsWith("\"")&&name.endsWith("\"")))
				name = "\""+name+"\"";
			map.put(name, value);
		}
		return this;
	}
/*
	@SuppressWarnings("unused")
	public JSONObject put(String name, String value)
	{
		return put(name, new JSONString(value));
	}
*/
	@SuppressWarnings("unused")
	public void remove(String name)
	{
		map.remove(name);
	}

	@SuppressWarnings("WeakerAccess")
	public int size()
	{
		return map.size();
	}

	public String toString(int indent)
	{
		StringBuilder ret = new StringBuilder("{");
		String[] names = names();
		int indname;
		for (String name:names)
		{
			if (indent>0)
				ret.append("\n");
			JSONData d = opt(name);
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

		if (names.length > 0)
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
}
