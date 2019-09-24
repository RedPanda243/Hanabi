package sjson;

import java.util.ArrayList;
import java.util.Iterator;

public class JSONArray extends JSONData implements Iterable<JSONData>
{
	private ArrayList<JSONData> list;

	@SuppressWarnings("WeakerAccess")
	public JSONArray()
	{
		list = new ArrayList<>();
	}

	@SuppressWarnings("WeakerAccess")
	public JSONArray add(JSONData d)
	{
		list.add(d);
		return this;
	}

	public JSONArray clone()
	{
		return (JSONArray) super.clone();
	}

	@SuppressWarnings("unused")
	public JSONArray copyIn(JSONArray array)
	{
		for (JSONData d:this)
			array.add(d.clone());
		return array;
	}

	public Type getJSONType()
	{
		return Type.ARRAY;
	}

	@SuppressWarnings("unused")
	public void insert(int index, JSONData value)
	{
		list.add(index, value);
	}

	public Iterator<JSONData> iterator()
	{
		return list.iterator();
	}

	@SuppressWarnings("WeakerAccess")
	public JSONData opt(int index)
	{
		try
		{
			return list.get(index);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	@SuppressWarnings("unused")
	public JSONArray optArray(int index)
	{
		try
		{
			return (JSONArray)opt(index);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			return null;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public JSONObject optJSON(int index)
	{
		try
		{
			return (JSONObject)opt(index);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			return null;
		}
	}

	@SuppressWarnings("unused")
	public String optString(int index)
	{
		try
		{
			return opt(index).toString();
		}
		catch(NullPointerException cce)
		{
			return null;
		}
	}

	@SuppressWarnings("unused")
	public int positionOf(JSONData d)
	{
		for(int i=0; i<this.size(); i++)
		{
			if (opt(i).equals(d))
				return i;
			i++;
		}
		return -1;
	}

	@SuppressWarnings("unused")
	public void remove(int index)
	{
		list.remove(index);
	}

	@SuppressWarnings("unused")
	public void replace(int index, JSONData d)
	{
		list.set(index,d);
	}

	@SuppressWarnings("WeakerAccess")
	public int size() 
	{
		return list.size();
	}

	public final String toString(int indent)
	{
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
