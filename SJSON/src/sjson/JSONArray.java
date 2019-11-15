package sjson;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import static sjson.JSONUtils.readWhile;

/**
 * Definisce un'array json come lista
 */
@SuppressWarnings({"WeakerAccess","unused"})
public class JSONArray extends JSONData implements Iterable<JSONData>
{
	private ArrayList<JSONData> list;

	/**
	 * Costruisce l'array vuoto []
	 */
	public JSONArray()
	{
		list = new ArrayList<>();
	}

	/**
	 * @see JSONArray#JSONArray(Reader)
	 * @param in InputStream da cui leggere una rappresentazione testuale di un array json
	 * @throws JSONException se la stringa letta &egrave; malformata
	 */
	public JSONArray(InputStream in) throws JSONException
	{
		this(new InputStreamReader(in));
	}

	/**
	 * @see JSONArray#JSONArray(Reader)
	 * @param s rappresentazione testuale di un array json
	 * @throws JSONException se la stringa &egrave; malformata
	 */
	public JSONArray(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	/**
	 * Legge un carattere alla volta fino a quando non ottiene una rappresentazione testuale di un array json
	 * @param r Reader da cui leggere una rappresentazione testuale di un array json
	 * @throws JSONException se la stringa letta &egrave; malformata
	 */
	public JSONArray(Reader r) throws JSONException
	{
		this();
		boolean flag = true;
		char t,e;
		try
		{
			readWhile(r,'\t',' ','\n','\r');//skip spaces
			if (r.read() != '[')
				throw new JSONException("JSONArray must starts with '['");
			r.mark(2);
			if (r.read()==']')
				flag = false;
			else
				r.reset();
			while(flag)
			{
				readWhile(r,'\t',' ','\n','\r');//skip spaces
				r.mark(2);
				t = (char) r.read();
				r.reset();
				if (t == '{')
					add(new JSONObject(r));
				else if (t == '[')
					add(new JSONArray(r));
				else if (t == '"')
					add(new JSONString(r));
				else
					throw new IOException("Unrecognized field!");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				e = (char) r.read();
				if (e == ']')
					flag = false;
				else if (e != ',')
					throw new IOException("Value definition ends, expected ']' or ','");
			}
		}
		catch(IOException je)
		{
			throw new JSONException(je);
		}
	}

	/**
	 * Aggiunge un attributo in coda all'array
	 * @param d l'attributo da aggiungere
	 * @return questo JSONArray
	 */
	public JSONArray add(JSONData d)
	{
		list.add(d);
		return this;
	}

	/**
	 * @see JSONArray#add(JSONData)
	 * @param s l'attributo stringa da aggiungere
	 * @return questo JSONArray
	 */
	public JSONArray add(String s)
	{
		return add(new JSONString(s));
	}

	/**
	 * @see JSONData#clone()
	 * @return una copia di questo JSONArray
	 */
	public JSONArray clone()
	{
		return (JSONArray) super.clone();
	}

	/**
	 * Inserisce una copia di tutti gli attributi di questo JSONArray in coda a quello passato come parametro.
	 * @param array un JSONArray da modificare
	 * @return il JSONArray modificato
	 */
	public JSONArray copyIn(JSONArray array)
	{
		for (JSONData d:this)
			array.list.add(d.clone());
		return this;
	}

	/**
	 * @return {@link JSONData.Type#ARRAY}
	 */
	public Type getJSONType()
	{
		return Type.ARRAY;
	}

	/**
	 * Inserisce un attributo nella posizione desiderata. L'attributo che si trovava in quella posizione e tutti i successivi
	 * sono scalati a destra.
	 * @param index la posizione in cui aggiungere l'attributo
	 * @param value l'attributo da aggiungere
	 * @return questo JSONArray modificato
	 */
	public JSONArray insert(int index, JSONData value)
	{
		list.add(index,value);
		return this;
	}

	/**
	 * @see JSONArray#insert(int, JSONData)
	 * @param index la posizione in cui aggiungere l'attributo
	 * @param s l'attributo stringa da aggiungere
	 * @return questo JSONArray modificato
	 */
	public JSONArray insert(int index, String s)
	{
		return insert(index,new JSONString(s));
	}

	/**
	 * @return un Iterator su tutti gli attributi di questo JSONArray
	 */
	public Iterator<JSONData> iterator()
	{
		return list.iterator();
	}

	/**
	 * Restituisce l'attributo di posizione desiderata
	 * @param index la posizione dell'attributo nel JSONArray
	 * @return l'attributo
	 */
	public JSONData get(int index)
	{
		return list.get(index);
	}

	/**
	 * @see JSONArray#get(int)
	 * @param index la posizione dell'attributo nel JSONArray
	 * @return l'attributo come JSONArray, null se non è un JSONArray
	 */
	public JSONArray getArray(int index)
	{
		try
		{
			return (JSONArray) get(index);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			System.err.println(cce);
			return null;
		}
	}

	/**
	 * @see JSONArray#get(int)
	 * @param index la posizione dell'attributo nel JSONArray
	 * @return l'attributo come JSONObject, null se non è un JSONObject
	 */
	public JSONObject getObject(int index)
	{
		try
		{
			return (JSONObject) get(index);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			return null;
		}
	}

	/**
	 * @see JSONArray#get(int)
	 * @param index la posizione dell'attributo nel JSONArray
	 * @return l'attributo come String, null se non è un JSONString
	 */
	public String getString(int index)
	{
		try
		{
			JSONData data = get(index);
			if (data.getJSONType().equals((Type.STRING)))
			{
				String s = data.toString();
				return s.substring(1, s.length() - 1); //TOLGO I DOPPI APICI!
			}
			return null;
		}
		catch(NullPointerException cce)
		{
			return null;
		}
	}

	/**
	 * @see JSONArray#has(JSONData)
	 * @param s l'attributo stringa da cercare in questo JSONArray
	 * @return true se questo JSONArray contiene un attributo uguale a quello passato come parametro, false altrimenti
	 */
	public boolean has(String s)
	{
		return has(new JSONString(s));
	}

	/**
	 * @see JSONArray#indexOf(JSONData)
	 * @param d l'attributo da cercare in questo JSONArray
	 * @return true se questo JSONArray contiene un attributo uguale a quello passato come parametro, false altrimenti
	 */
	public boolean has(JSONData d)
	{
		return indexOf(d)>-1;
	}

	/**
	 * Controlla se all'interno del JSONArray esiste un attributo uguale a quello passato come parametro
	 * @param d l'attributo da cercare in questo JSONArray
	 * @return la posizione nel JSONArray di un attributo uguale a quello passato come parametro
	 */
	public int indexOf(JSONData d)
	{
		for(int i=0; i<this.size(); i++)
		{
			if (get(i).equals(d))
				return i;
			i++;
		}
		return -1;
	}

	/**
	 * @see JSONArray#indexOf(JSONData)
	 * @param s l'attributo stringa da cercare in questo JSONArray
	 * @return la posizione nel JSONArray di un attributo uguale a quello passato come parametro
	 */
	public int indexOf(String s)
	{
		return indexOf(new JSONString(s));
	}

	/**
	 * Rimuove da questo JSONArray l'attributo di posizione passata come parametro. Tutti gli attributi successivi sono scalati
	 * verso sinistra.
	 * @param index la posizione dell'attributo da rimuovere
	 * @return questo JSONArray modificato
	 */
	public JSONArray remove(int index)
	{
		list.remove(index);
		return this;
	}

	/**
	 * Sostituisce l'attributo di posizione indicata con quello passato come parametro
	 * @param index la posizione dell'attributo da sostituire
	 * @param d il nuovo attributo
	 * @return questo JSONArray modificato
	 */
	public JSONArray replace(int index, JSONData d)
	{
		list.set(index,d);
		return this;
	}

	/**
	 * @see JSONArray#replace(int, JSONData)
	 * @param index la posizione dell'attributo da sostituire
	 * @param s il nuovo attributo stringa
	 * @return questo JSONArray modificato
	 */
	public JSONArray replace(int index, String s)
	{
		return replace(index,new JSONString(s));
	}

	/**
	 * @return il numero di attributi di questo JSONArray
	 */
	public int size()
	{
		return list.size();
	}

	/**
	 * Assegna ad ogni attributo innestato un nuovo livello di indentazione
	 * @see JSONData#toString(int)
	 */
	public final String toString(int indent)
	{
		if (indent<0)
			return JSONUtils.quote(toString(0));

		StringBuilder ret = new StringBuilder("[");
		for (JSONData data:list)
		{
			if (indent>0) {
				ret.append("\n").append(tabstring(indent));
				ret.append(data.toString(indent+baseindent));
			}
			else
				ret.append(data.toString(indent));
			ret.append(",");
		}

		if (size() > 0)
		{
			ret = new StringBuilder(ret.substring(0, ret.length()-1));
			if (indent>0)
			{
				ret.append("\n");
				ret.append(tabstring(indent-baseindent));
			}
		}
		ret.append("]");
		return ret.toString();
	}
}
