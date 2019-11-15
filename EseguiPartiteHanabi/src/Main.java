import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	static String[] strategie= {"strategy1"};
	static BufferedReader reader = null;
	static BufferedReader reader1 = null;
	static Process procEngine = null;
	static PrintStream pos;
	public static void main(String[] args) throws IOException{
		String enginePath ="/home/francesco/Scrivania/Hanabi/Hanabi/HanabiEngine.jar";
		String agentPath ="/home/francesco/Scrivania/Hanabi/Hanabi/HanabiAgent.jar";

//		pos = new PrintStream(new FileOutputStream("prova.txt"));
		/**
		 * neccessari
		 *  -numeroPartite -numeroGiocatori -fileDoveStampare -s strategiaGiocatore1
		 *
		 *  opzionali
		 *  strategiaGiocatore2 strategiagiocatoreN  (dopo il parametro -s, se omessi verrà usata la strategia specificata per il primo giocatore
		 *  -f logfilePath
		 *  -p porta
		 *
		 */
		String exMode = "Esecuzione:\n" +
				"-numeroPartite -numeroGiocatori -fileDoveStampare -s strategiaGiocatore1"+
				"\nopzionali:\n"+
				"strategiaGiocatore2 strategiagiocatoreN -f logfilePath -p porta";

		if(args.length <5) {
			System.err.println("Parametri Sbagliati\n"+exMode);
			System.exit(1);
		}

		int numeroPartite = Integer.parseInt(args[0].replace("-",""));
		int numeroGiocatori = Integer.parseInt(args[1].replace("-",""));
		String fileName = args[2].replaceFirst("-","");
		ArrayList<String> strategieGiocatori = new ArrayList<>();
		int porta = 9494;
		String logFile = "";

		if(!args[3].equals("-s")){
			System.err.println("Mancanza strategia \"-s strategiaGiocatore\" \n"+exMode);
			System.exit(2);
		}
		else{
			for(int i = 0; i< numeroGiocatori; i++){
				boolean result = Arrays.stream(strategie).anyMatch(args[4+i]::equals);
				if(result)
					strategieGiocatori.add(args[4+i]);
				else if (strategieGiocatori.size()==1) break;
				else if (strategieGiocatori.isEmpty()){
					System.err.println("Mancanza strategia \"-s strategiaGiocatore\" \n"+exMode);
					System.exit(3);
				}
			}
		}
		if(args.length>4+strategieGiocatori.size()){
			if(args[4+strategieGiocatori.size()].equals("-f")){
				logFile = args[5+strategieGiocatori.size()];
				if(args.length>5+strategieGiocatori.size()){
					porta = Integer.parseInt(args[7+strategieGiocatori.size()]);
				}
			}else if (args[4+strategieGiocatori.size()].equals("-p")){
				porta = Integer.parseInt(args[5+strategieGiocatori.size()]);
			}
		}

		//EXEC ENGINE
		String commandEngine = "java -jar "+enginePath;
		if(numeroGiocatori !=2)
			commandEngine += " -g "+numeroGiocatori;
		commandEngine +=" -p "+porta;
		commandEngine += " -f ../log.txt";
		//exec

		try {
			System.out.println("LINEA exec Engine= "+commandEngine);
			procEngine = Runtime.getRuntime().exec(commandEngine);
			reader = new BufferedReader(new InputStreamReader(procEngine.getInputStream()));

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}





		//EXEC AGENTS

		for (int i =0; i< numeroGiocatori; i++){
			String commandAgent = "java -jar "+agentPath+ " 0 "+porta;
			commandAgent += " "+i;
			if(strategie.length !=1){
				commandAgent += " "+strategie[i];
			}else commandAgent += " "+strategie[0];

			//exec
			try {
				System.out.println("LINEA exec Agent= "+commandAgent);
				reader1 = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(commandAgent).getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}

		}

		//       new Thread("input") {
		//           public void run() {
		String line = "";
		while (true) {
			try {
	//			System.out.println(procEngine.isAlive() + " " + procEngine.getInputStream().available());

				if ((line = reader.readLine()) == null) break;
				System.out.println(line);
	//			pos.print("[INPUT] "+line + "\n");
	//			pos.flush();
                 /*       if (procEngine.getInputStream().available() > 0)
                        {

                            int kar = reader.read();
                            if (kar != -1) {
                                StringBuilder buffer = new StringBuilder(30);
                                while (kar != -1 && kar != (int) '\n') {
                                    buffer.append((char) kar);
                                    kar = reader.read();
                                }
                                line = buffer.toString();
                            }
                            pos.print("[INPUT] "+line + "\n");
                            pos.flush();
                        }*/

			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
                  /*  try{

                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }*/
		}
		//           }
		//      }.start();
/*
        new Thread("error") {
            public void run() {
                String line = "";
                while (true) {
                    try {
                        if (!((line = reader1.readLine()) != null)) break;

                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println(e);
                    }
                    System.out.print("[ERROR] "+line + "\n");
                }
            }
        }.start();
*/
        try
        {
            while(procEngine.isAlive())
			{
				Thread.sleep(1000);
				System.out.println(procEngine.isAlive());
			}
        }
        catch(InterruptedException e)
        {e.printStackTrace();}
        System.out.println("FINITO");

	}
}
