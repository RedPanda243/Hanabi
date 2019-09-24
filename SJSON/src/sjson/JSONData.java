package sjson;

import java.io.IOException;

/**
 * Classe astratta madre di tutte le classi che implementano un attributo json (3 tipi possibili: STRING,OBJECT,ARRAY).
 * Un JSONData dovrebbe essere immutabile. Una classe che estenda JSONData e che rappresenti un tipo di dato strutturato
 * dovrebbe ritornare con i metodi get una copia dell'attributo richiesto e con i metodi set una copia dell'oggetto corrente
 * modificato.
 * @see JSONObject
 * @see JSONArray
 * @see JSONString
 */
public abstract class JSONData implements Cloneable
{
	static final int baseindent = 3;

	enum Type
	{
		STRING,OBJECT,ARRAY
	}

	/**
	 * Metodo usato da clone(). All'esterno di questa classe usa clone()
	 * @return un nuovo oggetto copia di questo JSONData
	 */
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

	/**
	 * Il metodo clone di JSONData elimina la possibilit&agrave; di ottenere una CloneNotSupportedException in quanto un JSONData
	 * pu&ograve; sempre essere ottenuto da una stringa formattata adeguatamente.
	 * Nelle classi che estendono JSONData il metodo pu&ograve; essere ridefinito affinch&eacute; restituisca un oggetto del tipo
	 * corretto. Sia T extends JSONData, la ridefinizione di clone deve limitarsi a return (T)super.clone()
	 * @see JSONData#copy()
	 * @return un nuovo oggetto copia di questo JSONData
	 */
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
