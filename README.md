# Hanabi
Il progetto Hanabi prende il nome dall'omonimo gioco e ne rappresenta un implementazione java.

# Descrizione
Il progetto git è composto da diversi moduli java (progetti java indipendenti).
Ogni modulo contiene codice sorgente, documentazione e relativo jar. 

SJSON.jar è una libreria che implementa i concetti propri del formato json (in realtà implementa una versione modificata, vedi documentazione).

HanAPI.jar è una libreria, che importa SJSON.jar, e implementa le classi che permettono di rappresentare una partita di Hanabi (package api.game) e classi di supporto all'implementazione dei giocatori (package api.client).

HanabiEngine.jar è eseguibile ed implementa un server tcp che mantiene la partita.
I giocatori, identificati dalla connessione tcp, devono comunicare al server le proprie mosse e ottenere lo stato corrente della partita.
Le comunicazioni avvengono tramite un protocollo che sfrutta il formato json per la rappresentazione dei dati.
HanabiEngine.jar importa le due precedenti librerie.

