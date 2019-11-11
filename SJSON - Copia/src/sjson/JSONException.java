package sjson;

public class JSONException extends Exception
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	public JSONException(Exception e)
	{
		super(e);
	}

	@SuppressWarnings("WeakerAccess")
	public JSONException(String s) 
	{
		super(s); 
	}
}
