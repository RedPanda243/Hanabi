package api.game;

import sjson.JSONData;

public abstract class TypedJSON<T extends JSONData> extends JSONData
{
	protected T json;

	@Override
	public Type getJSONType() {
		return json.getJSONType();
	}

	public T toJSON()
	{
		return json;
	}

	public String toString(int indent)
	{
		return json.toString(indent);
	}

}
