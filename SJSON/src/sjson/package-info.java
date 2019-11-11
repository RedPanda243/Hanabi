/**
 * Il package sjson (Simplified-JSON) definisce classi java per una gestione semplificata di oggetti json. <br>
 * Il formato JSON (vedi <a href=http://www.json.org/json-it.html>http://www.json.org/json-it.html</a>) &egrave; basato su due tipi
 * di strutture (oggetti e array) contenenti valori di tipo strutturato (quindi altri oggetti o array) o non strutturato,
 * ossia stringa, numero, boolean o null. <br>
 * Il package sjson implementa una versione semplificata prevedendo che i valori possano essere solo oggetti, array o stringhe. <br>
 * Nel caso in cui si abbia necessità di leggere/scrivere un numero o un boolean se ne deve effettuare il parse esplicitamente.
 * Se un valore deve essere null si consiglia di assegnargli una stringa vuota "".
 */
package sjson;