package sjson;

import java.io.IOException;
import java.io.Reader;

import static sjson.JSONUtils.readUntil;

/**
 * Classe che rappresenta i dati sjson di tipo stringa, ossia un qualsiasi valore non strutturato. <br>
 * Nelle rappresentazioni testuali un JSONString &egrave; sempre racchiuso tra doppi apici.
 */
@SuppressWarnings({"unused"})
public class JSONString extends JSONData
{
	/**
	 * Mantiene il valore come stringa, doppi apici esclusi
	 */
	private String s;

	/**
	 * @param s la rappresentazione testuale del dato stringa da creare
	 */
	public JSONString(String s)
	{
		this.s=""+s;
		if (s.startsWith("\"") && s.endsWith("\""))
			this.s = s.substring(1,s.length()-1);
	}

	/**
	 * Legge da Reader una stringa inclusa in doppi apici.
	 * @param r Reader da cui leggere la rappresentazione testuale del dato stringa da creare
	 * @throws JSONException se la stringa letta dal Reader &egrave; malformata
	 */
	public JSONString(Reader r) throws JSONException
	{
		try
		{
			if (r.read() == '"')
				s = readUntil(r, '"');
			else
				throw new IOException("JSONString must starts and ends with '\"'");
		}
		catch(IOException e)
		{
			throw new JSONException(e);
		}
	}

	/**
	 * @see JSONData#clone()
	 * @return una copia di questa JSONString
	 */
	public JSONString clone()
	{
		return (JSONString)super.clone();
	}

	/**
	 * @return {@link JSONData.Type#STRING}
	 */
	public Type getJSONType()
	{
		return Type.STRING;
	}

	/**
	 * @return il numero di caratteri che compongono il dato di tipo stringa, doppi apici esclusi
	 */
	public int size()
	{
		return s.length();
	}

	/**
	 * Ridefinisce l'indentazione di default di {@link JSONData#toString()}
	 * @return la stringa usata per la costruzione di questo oggetto, inclusa in doppi apici
	 */
	public String toString()
	{
		return toString(0);
	}

	/**
	 * Attribuisce ad ogni riga successiva alla prima lo stesso livello di indentazione
	 * @param indent numero di whitespaces. Se &lt; 0 si ha una stringa di una sola riga.
	 * @return la stringa usata per la costruzione di questo oggetto, inclusa in doppi apici e indentata
	 */
	public final String toString(int indent)
	{
		if (indent<0)
			return "\""+JSONUtils.quote(s)+"\"";
		String val = ""+s;
		if (indent>0)
			val = val.replace("\n", "\n"+ tabstring(indent)).replace("\r", "\r"+tabstring(indent));
		return "\""+val+"\"";
	}
}
