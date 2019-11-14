# Hanabi
Il progetto Hanabi prende il nome dall'omonimo gioco e ne rappresenta un implementazione java.

#Descrizione
Il progetto git è composto da 3 moduli java (progetti indipendenti): SJSON, HanAPI, HanabiEngine.
Ogni modulo contiene un jar: SJSON.jar e HanAPI.jar sono librerie, mentre HanabiEngine.jar è eseguibile ed implementa un server tcp che mantiene la partita.
I giocatori, identificati dalla connessione tcp, devono comunicare al server le proprie mosse e ottenere lo stato corrente della partita.
Le comunicazioni avvengono tramite un protocollo che sfrutta il formato json per la rappresentazione dei dati.

Il modulo SJSON implementa i concetti propri del formato json (in realtà implementa una versione modificata, vedi documentazione SJSON).

Il modulo HanAPI è suddiviso in due package

##api.game

##api.client
