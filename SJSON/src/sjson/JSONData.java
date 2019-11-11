package sjson;

/**
 * Classe astratta madre di tutte le classi che implementano un tipo di dato json. <br>
 * Definisce i 3 tipi di dato usati in sjson e implementa alcuni metodi accessori comuni a tutti i tipi.
 * Nella definizione di una classe figlia di JSONData, in particolar modo in quelle che gestiscono una collezione di json,
 * si deve tener conto che un JSONData non è considerato immutabile. <br>
 * JSONData offre il metodo clone() per ottenere una copia di se stesso.
 *
 * @see JSONObject
 * @see JSONArray
 * @see JSONString
 */
@SuppressWarnings({"WeakerAccess","unused"})
public abstract class JSONData implements Cloneable
{
	/**
	 * Definisce il numero di whitespace di default in un livello di indentazione
	 */
	static final int baseindent = 3;

	/**
	 * JSONData non specifica operazioni nel costruttore, ma, visto che ogni tipo di dato json può essere convertito biunivocamente
	 * in una stringa, prevede in ingresso una stringa da convertire in tipo di dato json.
	 *
	 * @param text rappresentazione testuale del dato json da costruire
	 */
	public JSONData(String text)
	{

	}

	/**
	 * Nel formato sjson, un dato pu&ograve; essere di 3 tipi: stringa, array o oggetto. Il package sjson fornisce una classe figlia
	 * di JSONData per ognuno di questi tipi di dato.
	 */
	public enum Type
	{
		STRING,OBJECT,ARRAY
	}

	/**
	 * Il metodo clone di JSONData elimina la possibilit&agrave; di ottenere una CloneNotSupportedException in quanto un JSONData
	 * pu&ograve; sempre essere ottenuto da una stringa formattata adeguatamente. Inoltre, passando come parametro
	 * di costruzione del JSONData la rappresentazione testuale di un altro dato dello stesso tipo si &egrave; sicuri che la stringa
	 * sia ben formattata, il che garantisce la non restituzione di eccezioni.
	 * Nelle classi che estendono JSONData il metodo pu&ograve; essere ridefinito affinch&eacute; restituisca un oggetto del tipo
	 * corretto. Sia T extends JSONData, la ridefinizione di clone deve limitarsi a return (T)super.clone()
	 *
	 * @return un nuovo oggetto copia di questo JSONData
	 */
	public JSONData clone()
	{
		try
		{
			super.clone();
		}
		catch(CloneNotSupportedException e){e.printStackTrace(System.err);}

		try
		{
			if (this.getJSONType().equals(Type.STRING))
				return new JSONString(this.toString(0));
			else if (this.getJSONType().equals(Type.OBJECT))
				return new JSONObject(this.toString(0));
			else
				return new JSONArray(this.toString(0));
		}
		catch(JSONException ioe)
		{
//			ioe.printStackTrace(System.out);
			return null;
		}
	}

	/**
	 * @see Type
	 * @return il tipo di JSONData
	 */
	public abstract Type getJSONType();

	/**
	 * @param d JSONData da confrontare con questo
	 * @return true se le rappresentazioni testuali dei due dati sono uguali, false altrimenti
	 */
	public boolean equals(JSONData d)
	{
		return this.toStringLine().equals(d.toStringLine());
	}

	/**
	 *
	 * @param indent numero di whitespaces
	 * @return una stringa con "indent" caratteri whitespace
	 */
	static String tabstring(int indent)
	{
		StringBuilder s = new StringBuilder();
		for (int i=0; i<indent; i++)
			s.append(" ");
		return s.toString();
	}

	/**
	 * @return una rappresentazione testuale del dato json usando il numero di default di whitespaces in ogni livello di indentazione
	 */
	public String toString()
	{
		return toString(baseindent);
	}

	/**
	 * @param indent numero di whitespaces per livello di indentazione. Se &lt; 0 si ha una stringa di una sola riga.
	 * @see JSONData#toStringLine()
	 * @return una rappresentazione testuale del dato json usando "indent" whitespaces in ogni livello di indentazione
	 */
	public abstract String toString(int indent);

	/**
	 * Equivalente a toString(-1)
	 * @see JSONData#toString(int)
	 * @see JSONUtils#quote(String)
	 * @return una rappresentazione testuale del dato in una sola riga
	 */
	public String toStringLine()
	{
		return toString(-1);
	}
}
