# Hanabi
Il progetto Hanabi prende il nome dall'omonimo gioco e ne rappresenta un implementazione java.

# Descrizione
Il progetto git è composto da 3 moduli java (progetti java indipendenti): SJSON, HanAPI, HanabiEngine.
Ogni modulo contiene codice sorgente, documentazione e relativo jar. 

HanabiEngine.jar è eseguibile ed implementa un server tcp che mantiene la partita.
I giocatori, identificati dalla connessione tcp, devono comunicare al server le proprie mosse e ottenere lo stato corrente della partita.
Le comunicazioni avvengono tramite un protocollo che sfrutta il formato json per la rappresentazione dei dati.
HanabiEngine.jar importa gli altri due jar.

SJSON.jar è una libreria che implementa i concetti propri del formato json (in realtà implementa una versione modificata, vedi documentazione SJSON).

HanAPI.jar è una libreria, che importa SJSON.jar, e implementa le classi che permettono di rappresentare una partita di Hanabi (package api.game) e classi, astratte
e concrete, che rappresentano i giocatori (package api.client).