package sjson;

import java.io.Reader;

/**
 * Tipo di eccezione ritornata quando si tenta di costruire JSONData.
 * @see JSONObject#JSONObject(Reader)
 * @see JSONArray#JSONArray(Reader)
 * @see JSONString#JSONString(Reader)
 */
@SuppressWarnings({"unused"})
public class JSONException extends Exception
{
	private static final long serialVersionUID = 1L;

	public JSONException(Exception e)
	{
		super(e);
	}

	public JSONException(String s)
	{
		super(s); 
	}
}
