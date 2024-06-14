/**
 * This class implements server fuctions
 * Author: Anjali Murthy
 * GNumber : (G01373209)
 */


import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;

public class FileServer {
	private int portNumber=0;
	static String name;

	//Method that starts the server at the port mentioned
	public void startServer() {
		try {
			LocateRegistry.createRegistry(portNumber);
			Naming.rebind(name,	new ClientHandler());
			System.err.println("Server ready at port "+portNumber);
		}catch(Exception e) {
			System.err.println("Server Connection failed due to the Exception: "+e.toString());;
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		int port=0;
		String commandPromptInput="";
		if(args.length==2) {
			commandPromptInput=args[0];
			port = Integer.parseInt(args[1]);
			if(commandPromptInput.equals("start")) {
				FileServer f = new FileServer(port);
				f.startServer();
			}
		}
		else {
			System.out.println("Invalid Command Input Try : java FileServer start <portnumber>");
		}
	}
	public FileServer(int port) {
	 portNumber=port;
	 name="rmi://0.0.0.0:"+portNumber+"/FileServer";
	}

}
