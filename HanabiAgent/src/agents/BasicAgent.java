package agents;

import api.game.*;
import game.MathState;
import sjson.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class BasicAgent
{
    private static BufferedReader keyboard;
    private static String name;


    private static Action chooseAction()
    {
        Action action = playKnown();
        if(action == null) action = discardKnown();
        if(action == null) action = hint();
        if(action == null) action = discardGuess();
        if(action == null) action = playGuess();
        if(action == null) action = hintRandom();

        return action;
    }

    private static Action hintRandom() {

        return  null;
    }

    private static Action playGuess() {

        return null;
    }

    private static Action discardGuess() {

        return null;

    }

    private static Action hint() {

        return null;
    }

    private static Action discardKnown() {

        return null;
    }

    private static Action playKnown() {

        return null;
    }

    /**
     *
     * @param args 0-host, 1-port, 2-name
     */
    //come HumanAgent
    public static void main(String... args) throws IOException,JSONException
    {
        keyboard = new BufferedReader(new InputStreamReader(System.in));
        if (args.length == 0)
        {
            System.out.println("Inserisci indirizzo remoto");
            String s = keyboard.readLine();
            if (s.contains(":"))
            {
                String[] split = s.split(":");
                main(split[0],split[1]);
            }
            else
                main(s);
        }
        else if (args.length == 1)
        {
            System.out.println("Inserisci porta remota");
            main(args[0],keyboard.readLine());
        }
        else if (args.length == 2)
        {
            System.out.println("Inserisci nome giocatore");
            main(args[0],args[1],keyboard.readLine());
        }
        else
        {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            name = args[2];
            Socket socket = new Socket(host, port);
            PrintStream out = new PrintStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(name);
            out.flush();
            name = in.readLine();

            new Game(in);

            MathState last = new MathState(new State(in));

            while(!last.gameOver())
            {
                System.out.println(last);
                if (last.getCurrentPlayer().equals(name))
                {
                    Action a = chooseAction();
                    out.print(a.toString(0));
                    out.flush();
                    //		System.err.println(a.toString(0));
                }
                else
                    System.out.println(new Turn(in));
                last = new MathState(new State(in));
            }
            System.out.println(last);
            System.out.println("Score: "+last.getScore());
        }
    }

}
