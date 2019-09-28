package sjson;

@Deprecated
public class TypedJSONArray<T extends JSONData> extends JSONConvertible<JSONArray>
{
	public TypedJSONArray(JSONArray json) throws JSONException
	{
		super(json);
		for (JSONData d: json)
		{
			if (!isOfType(d))
				throw new JSONException("Array contains an object of wrong type");
		}
	}

	public static void main(String args[])
	{
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		obj = obj.set("testo ciao","Ciao");
		System.out.println("1) "+obj.toString(0));
		System.out.println("2) "+obj.clone().toString(0));
		arr = arr.add("yo");
		System.out.println(arr.clone());
		arr.add("yo");
		arr = arr.add(obj);
		System.out.println(arr.clone());
		obj = obj.set("array",arr);
		System.out.println(obj);

//		System.out.println(arr.toStringLine());
	}

	private boolean isOfType(JSONData d)
	{
		try
		{
			Object o = (T)d;
			return true;
		}
		catch(ClassCastException cce)
		{
			return false;
		}
	}

	/**
	 *
	 * @param d
	 * @return null se d non &egrave; del tipo dichiarato
	 */
	public TypedJSONArray add(JSONData d) {
		if (isOfType(d))
		{
			TypedJSONArray<T> a = this.clone();
			a.json.add(d);
			return a;
		}
		else
			return null;
	}

	public TypedJSONArray<T> clone()
	{
		return (TypedJSONArray<T>)super.clone();
	}
/*
	public TypedJSONArray copyIn(JSONArray array)
	{
		if (array instanceof TypedJSONArray)
		{
			if (((TypedJSONArray) array).c.isAssignableFrom(this.c))
				return (TypedJSONArray)super.copyIn(array);
		}
		return null;
	}

	public TypedJSONArray insert(int index, JSONData d)
	{
		if (c.isAssignableFrom(d.getClass()))
			return (TypedJSONArray) super.insert(index,d);
		else
			return null;
	}

 */
}
