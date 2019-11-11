package sjson;

import java.lang.reflect.Constructor;

@Deprecated
public abstract class JSONConvertible<T extends JSONData> extends JSONData
{
	protected T json;

	@SuppressWarnings("unchecked")
	public JSONConvertible(T json)
	{
		this.json = (T)json.clone();
	}

	@SuppressWarnings(value = "unchecked")
	public JSONConvertible<T> clone()
	{
		super.clone();
		try
		{
			Constructor con = this.getClass().getConstructor(json.getClass());
			return (JSONConvertible<T>) con.newInstance(json);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	@Override
	public Type getJSONType()
	{
		return toJSON().getJSONType();
	}

	@SuppressWarnings(value = {"unchecked","WeakerAccess"})
	public T toJSON()
	{
		return (T)json.clone();
	}

	@Override
	public String toString(int indent)
	{
		return json.toString(indent);
	}
}
