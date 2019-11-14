/**
 * Il package HanAPI contiene le classi che permettono di rappresentare una partita di Hanabi.
 * HanAPI &egrave; progettato per essere usato insieme
 * Dato che HanabiEngine rappresenta i giocatori come connessioni tcp, gli elementi di gioco devono essere serializzabili.
 * A questo scopo HanAPI definisce ogni elemento che pu&ograve; essere comunicato ad un giocatore come
 * oggetto json estendendo le classi della libreria SJSON.
 * @author Francesco Pandolfi, Mihail Bida
 */
package api.game;