package sjson;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Definisce metodi statici di supporto.
 */
@SuppressWarnings("WeakerAccess")
public final class JSONUtils
{
	/**
	 * Effettua escape sui seguenti caratteri: \n,\r,\t,\,",{,[,],}
	 * @param s Una stringa qualsiasi
	 * @return Una stringa di una sola riga con escape su alcuni caratteri
	 */
	public static String quote(String s)
	{
		return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\r","\\r")
				.replace("\n", "\\n").replace("\"", "\\\"").replace("{", "\\{")
				.replace("[", "\\[").replace("}", "\\}").replace("]", "\\]");
	}

	/**
	 * Questo metodo legge da Reader un carattere alla volta fino alla lettura di uno dei caratteri terminatori passati come parametro.<br>
	 * Restituisce la concatenazione di tutti i caratteri letti, terminatore escluso.
	 * @param reader Reader da cui leggere caratteri
	 * @param cs Array di caratteri
	 * @return String letta da reader
	 * @throws IOException in caso di errori I/O, vedi {@link Reader#read()}
	 * @throws EOFException se il metodo non termina entro l'EOF del Reader
	 */
	public static String readUntil(Reader reader, char... cs) throws IOException
	{
		StringBuilder s = new StringBuilder();
		char box = ' ',box1;
		boolean flag = true;
		while(flag)
		{
			box1 = box;
			box = (char) reader.read();
			if (box == (char)-1)
				throw new EOFException();
			s.append(box);
			if (box1 != '\\')
			{
				for (char c:cs)
				{
					if (box == c)
						flag = false;
				}
			}
		}
		return s.toString().substring(0,s.length()-1);
	}

	/**
	 * Questo metodo legge da Reader un carattere alla volta fino alla lettura di un carattere terminatore diverso
	 * da quelli passati come parametro.<br>
	 * Restituisce la concatenazione di tutti i caratteri letti, terminatore escluso.
	 * @param reader Reader da cui leggere caratteri
	 * @param cs Array di caratteri
	 * @return String letta da reader
	 * @throws IOException in caso di errori I/O, vedi {@link Reader#read()}
	 * @throws EOFException se il metodo non termina entro l'EOF del Reader
	 */
	public static String readWhile(Reader reader, char...cs) throws IOException
	{
		StringBuilder s = new StringBuilder();
		boolean flag = true;
		char box = ' ',box1;
		while(flag)
		{
			flag = false;
			reader.mark(2);
			box1 = box;
			box = (char) reader.read();
			if (box == (char)-1)
				throw new EOFException();
			if (box1 != '\\')
			{
				for (char c:cs)
				{
					if (box == c)
						flag = true;
				}
			}
			if (flag)
				s.append(box);
		}
		reader.reset();
		return s.toString();
	}

	/**
	 * Operazione inversa di {@link JSONUtils#quote(String)}
	 * @param s stringa ottenuta tramite {@link JSONUtils#quote(String)}
	 * @return la stringa originale parametro di {@link JSONUtils#quote(String)}
	 */
	public static String unquote(String s)
	{
		return s.replace("\\\\", "\\").replace("\\t", "\t").replace("\\r","\r")
				.replace("\\n", "\n").replace("\\\"", "\"").replace("\\{", "{")
				.replace("\\[", "[").replace("\\}", "}").replace("\\]", "]");
	}
}
