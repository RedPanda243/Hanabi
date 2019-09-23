package sjson;

import java.io.IOException;

public abstract class JSONData implements Cloneable
{
	static final int baseindent = 3;

	enum Type
	{
		STRING,JSON,ARRAY
	}

	private JSONData copy()
	{
		try
		{
			return JSONUtils.fromString(this.getClass(),this.toString(0));
		}
		catch(IOException ioe)
		{
			//Impossibile
			return null;
		}
	}

	public JSONData clone()
	{
		try
		{
			super.clone();
		}
		catch(CloneNotSupportedException e)
		{

		}
		return this.copy();
	}

	public abstract Type getJSONType();

	@SuppressWarnings("WeakerAccess")
	public boolean equals(JSONData d)
	{
		return this.toStringLine().equals(d.toStringLine());
	}

	@SuppressWarnings("unused")
	public boolean equals(JSONConvertible obj)
	{
		return this.equals(obj.toJSON());
	}

	static String tabstring(int indent)
	{
		StringBuilder s = new StringBuilder();
		for (int i=0; i<indent; i++)
			s.append(" ");
		return s.toString();
	}

	public String toString()
	{
		return toString(baseindent);
	}

	public abstract String toString(int indent);

	public String toStringLine()
	{
		return toString(-1);
	}
}
