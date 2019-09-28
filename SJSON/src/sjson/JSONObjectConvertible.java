package sjson;

import java.lang.reflect.Constructor;

@Deprecated
public class JSONObjectConvertible extends JSONObject
{
	private JSONObject json;

	public JSONObjectConvertible(JSONObject json)
	{
		this.json = json.clone();
	}

	public JSONObjectConvertible clone()
	{
		super.clone();
		try
		{
			Constructor con = this.getClass().getConstructor(json.getClass());
			return (JSONObjectConvertible) con.newInstance(json);
		}
		catch(Exception e)
		{
			return null;
		}
	}
}
