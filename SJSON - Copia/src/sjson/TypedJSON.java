package sjson;

import java.lang.reflect.InvocationTargetException;

@Deprecated
public abstract class TypedJSON extends JSONObject
{
	public TypedJSON(JSONData d)
	{
		super();
		JSONObject m;
		if (d!=null && d.getJSONType().equals(Type.OBJECT))
		{
			m = (JSONObject)d;
			m.copyIn(this);
			if (!verifyType())
				throw new ClassCastException(this.getClass().getName() + ": message is not compatible with this type.");
		}
		else
			getTemplate().copyIn(this);
	}

	public TypedJSON cast(JSONObject m)
	{
		if (m.compatible(this.getTemplate()))
		{
			try {
				return this.getClass().getConstructor(JSONObject.class).newInstance(m);
			}
			catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e)
			{
				throw new ClassCastException();
			}
		}
		throw new ClassCastException();
	}

	public abstract JSONObject getTemplate();

	protected boolean valueCheck() 
	{
		return true;
	}
	
	private boolean verifyType()
	{
		return this.compatible(this.getTemplate()) && valueCheck();
	}
	/*
	public static <T extends TypedJSON> T fromBytes(Class<T> c, byte[] b)
	{
		try 
		{
			return c.getConstructor(InputStream.class).newInstance(new ByteArrayInputStream(b));
		} 
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) 
		{
			return null;
		}
	}
	
	public static <T extends TypedJSON> T fromString(Class<T> c, String s)
	{
		return fromBytes(c,s.getBytes(StandardCharsets.UTF_8));
	}
	
	public static <T extends TypedJSON> T fromJSON(Class<T> c, JSONObject m)
	{
		return fromString(c,m.toString());
	}
	*/
}
