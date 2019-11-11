package sjson;

import java.io.*;
import java.util.*;

import static sjson.JSONUtils.readUntil;
import static sjson.JSONUtils.readWhile;

/**
 * Definisce un oggetto json come mappa di JSONData
 */
@SuppressWarnings({"WeakerAccess","unused"})
public class JSONObject extends JSONData
{
	private HashMap<String, JSONData> map;

	/**
	 * Costruisce l'oggetto json vuoto {}
	 */
	public JSONObject()
	{
		super("");
		map = new HashMap<>();
	}

	/**
	 * @see JSONObject#JSONObject(Reader)
	 * @param in InputStream da cui leggere una rappresentazione testuale di un oggetto json
	 * @throws JSONException se la stringa letta &egrave; malformata
	 */
	public JSONObject(InputStream in) throws JSONException
	{
		this(new InputStreamReader(in));
	}

	/**
	 * @see JSONObject#JSONObject(Reader)
	 * @param s rappresentazione testuale di un oggetto json
	 * @throws JSONException se la stringa &egrave; malformata
	 */
	public JSONObject(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	/**
	 * Legge un carattere alla volta fino a quando non ottiene una rappresentazione testuale di un oggetto json
	 * @param r Reader da cui leggere una rappresentazione testuale di un oggetto json
	 * @throws JSONException se la stringa letta &egrave; malformata o in caso di errore I/O
	 */
	public JSONObject(Reader r) throws JSONException
	{
		this();
		try
		{
			boolean flag = true;
			String name;
			char t,e;
			readWhile(r,'\t',' ','\n','\r');//skip spaces
			t = (char)r.read();
			if (t != '{')
				throw new JSONException("JSONObject must starts with '{', founded '"+t+"'");
			r.mark(2);
			if (r.read()=='}')
				flag = false;
			else
				r.reset();
			char c;
			while(flag)
			{
				readWhile(r,'\t',' ','\n','\r');//skip spaces

				if ((c = (char)r.read())!='\"')
					throw new JSONException("Name definition begins, expected a string beginning with '\"' but '"+c+"' founded!");

				name = readUntil(r,'"');
				if (has(name))
					throw new JSONException("Property "+name+" already defined");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				if (r.read()!=':')
					throw new JSONException("Name definition ends, expected ':'");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				r.mark(2);
				t = (char) r.read();
				r.reset();
				if (t == '{')
					set(name, new JSONObject(r));
				else if (t == '[')
					set(name, new JSONArray(r));
				else if (t == '"')
					set(name, new JSONString(r));
				else
					throw new IOException("Unrecognized field!");

				readWhile(r,'\t',' ','\n','\r');//skip spaces

				e = (char) r.read();
				if (e == '}')
					flag = false;
				else if (e != ',')
					throw new IOException("Value definition ends, expected '}' or ','");
			}
		}
		catch(IOException je)
		{
			throw new JSONException(je);
		}
	}

	/**
	 * @see JSONData#clone()
	 * @return una copia di questo JSONObject
	 */
	public JSONObject clone()
	{
		return (JSONObject)super.clone();
	}

	/**
	 * Inserisce una copia di tutti gli attributi di questo JSONObject in quello passato come parametro.
	 * Se i due oggetti possiedono attributi con lo stesso nome viene mantenuto quello di questo oggetto.
	 * @param o un JSONObject da modificare
	 * @return il JSONObject modificato
	 */
	public JSONObject copyIn(JSONObject o)
	{
		for (String name: map.keySet())
			o.map.put(name,this.get(name).clone());
		return o;
	}

	/**
	 * @return {@link JSONData.Type#OBJECT}
	 */
	public Type getJSONType()
	{
		return Type.OBJECT;
	}

	/**
	 * @param name nome dell'attributo di cui si vuole verificare l'esistenza
	 * @return true se questo JSONObject possiede un attributo di nome specificato, false altrimenti
	 */
	public boolean has(String name)
	{
		return get(name)!=null;
	}

	/**
	 * @return un {@link Iterator} dei nomi dei valori di questo JSONObject
	 */
	public Iterator<String> nameIterator()
	{
		return map.keySet().iterator();
	}

	/**
	 * @return una copia del Set di nomi della mappa contenuta da questo JSONObject
	 */
	public Set<String> names()
	{
		return map.keySet();
	}

	/**
	 * L'oggetto restituito &egrave; una copia per indirizzo di quello mantenuto dal JSONObject: si consiglia di non modificarlo
	 * @param name nome dell'attributo richiesto
	 * @return L'attributo di nome specificato, null se non esiste attributo con quel nome
	 */
	public JSONData get(String name)
	{
		if (!(name.startsWith("\"")&&name.endsWith("\"")))
			name = "\""+name+"\"";
		return map.get(name);
	}

	/**
	 * @see JSONObject#get(String)
	 * @param name nome dell'attributo richiesto
	 * @return L'attributo di nome specificato come JSONArray, null se non esiste attributo con quel nome o se non &egrave; un array
	 */
	public JSONArray getArray(String name)
	{
		try
		{
			return (JSONArray) get(name);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			return null;
		}
	}

	/**
	 * @see JSONObject#get(String)
	 * @param name nome dell'attributo richiesto
	 * @return L'attributo di nome specificato come JSONObject, null se non esiste attributo con quel nome o se non &egrave; un oggetto
	 */
	public JSONObject getObject(String name)
	{
		try
		{
			return (JSONObject) get(name);
		}
		catch(ClassCastException | NullPointerException cce)
		{
			return null;
		}
	}

	/**
	 * @see JSONObject#get(String)
	 * @param name nome dell'attributo richiesto
	 * @return L'attributo di nome specificato come JSONObject, null se non esiste attributo con quel nome o se non &egrave; una stringa
	 */
	public String getString(String name)
	{
		try
		{
			JSONData data = get(name);
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
	 * Assegna un attributo di nome e valore specificati
	 * @param name nome dell'attributo da impostare
	 * @param value attributo da assegnare al nome specificato
	 * @return questo JSONObject modificato
	 */
	public JSONObject set(String name, JSONData value)
	{
		if (value != null)
		{
			if (!(name.startsWith("\"")&&name.endsWith("\"")))
				name = "\""+name+"\"";
			map.put(name, value);
		}
		return this;
	}

	/**
	 * @see JSONObject#set(String, JSONData)
	 * @param name nome dell'attributo da impostare
	 * @param value attributo stringa da assegnare al nome specificato
	 * @return questo JSONObject modificato
	 */
	public JSONObject set(String name, String value)
	{
		return set(name,new JSONString(value));
	}

	/**
	 * Rimuove l'attributo di nome specificato
	 * @param name nome dell'attributo da rimuovere
	 * @return questo JSONObject
	 */
	public JSONObject remove(String name)
	{
		map.remove(name);
		return this;
	}

	/**
	 * @return il numero di attributi contenuti in questo JSONObject
	 */
	public int size()
	{
		return map.size();
	}

	/**
	 * Assegna ad ogni attributo innestato un nuovo livello di indentazione
	 * @see JSONData#toString(int)
	 */
	public final String toString(int indent)
	{
		if (indent<0)
			return JSONUtils.quote(toString(0));

		StringBuilder ret = new StringBuilder("{");
		int indname;
		for (String name:names())
		{
			if (indent>0)
				ret.append("\n");
			JSONData d = get(name);
			ret.append(tabstring(indent));
			ret.append(name);
			ret.append(":");
			indname = 1+name.length();
			int newindent = indent+indname+1;
			if (!d.getJSONType().equals(Type.STRING))
				newindent += 2;
			if (indent>0)
				ret.append(d.toString(newindent));
			else
				ret.append(d.toString(indent));
			ret.append(",");
		}

		if (names().size() > 0)
		{

			ret = new StringBuilder(ret.substring(0, ret.length()-1));
			if (indent>0)
			{
				ret.append("\n");
				ret.append(tabstring(indent-baseindent));
			}
		}
		ret.append("}");
		return ret.toString();
	}

	/**
	 * @return una {@link Collection} contenente una copia di tutti i valori degli attributi di questo JSONObject
	 */
	public Collection<JSONData> values()
	{
		ArrayList<JSONData> l = new ArrayList<>();
		for (JSONData d:map.values())
			l.add(d.clone());
		return l;
	}

}
