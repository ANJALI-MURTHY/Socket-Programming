/**
 * This class implements Client fuctions that initiates a server connection and performs the required file operations
 * Author: Anjali Murthy
 * GNumber : (G01373209)
 */


import java.io.*;
import java.rmi.*;
import java.util.Arrays;
public class Client {
	private String env = System.getenv("PA1_SERVER");
	private int fileSize ;
	private int code ;
	private int filePos = 0;
	// private int fileSize,code,filePos=0;
	private BufferedOutputStream BufOutStream;
	private ClientHandlerInterface server;
	private boolean resend=false;
	private boolean isShutDown=false;
	// private boolean resend=false, isShutdown=false;
	private byte[] fileBytes;
	private FileOutputStream fileOutStream;
	private String cmd;
	private String serverPath= "";
	private String clientPath="";
	private String msg="";
	// private String cmd,serverPath= "",clientPath="",msg="";
	private BufferedInputStream  BufInStream;
	private File file;

	//Method that sets the serverpath and client path correctly based on the command prompt input
	public Client(String comm, String path1,String path2) {
		this.cmd=comm.toLowerCase();
		if(cmd.equals("upload")) {
			this.clientPath=System.getProperty("user.dir")+"/"+path1;
			this.serverPath=path2;
		}else {
			this.serverPath=path1;
			this.clientPath=System.getProperty("user.dir")+"/"+path2;
		}
	}
	public Client(String comm,String path1) {
		this.serverPath=path1;
		this.cmd=comm;
	}

	public Client(String comm) {
		this.cmd=comm;
	}

	//Method for the client server connection
	private void Connect() {
		String name = "rmi://"+ env+"/FileServer";
		try {
			server = (ClientHandlerInterface)Naming.lookup(name);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}

	//Helper Method to perform file download
	public void DownloadFile() {
		try {
			fileSize = server.getFsize(serverPath);
		}catch(RemoteException e) {
			e.printStackTrace();
		}

		fileBytes=new byte[10*1024];
		file=new File(clientPath);

		//If file with the same name already exists
		if(file.exists() &&(int)file.length()==fileSize) {
			Close(1, "File already exists!");
		}
		else if(fileSize==0) {
			Close(-1,"File not found"); //When no file is found
		}
		try {
			if((int)file.length()<fileSize && file.length()!=0){
				fileOutStream = new FileOutputStream(file,true);
				filePos=(int)file.length();
				resend=true;
			}
			else {
				fileOutStream =new FileOutputStream(file);
			}

		} catch (FileNotFoundException e) {
			Close(-1,"File not found");
		}
		//If the file download is interuppted in the middle
		if(resend) {
			System.out.printf("Redownloading from %.1f%%\n",((double)filePos/fileSize)*100);
		}
		BufOutStream= new BufferedOutputStream(fileOutStream);

		int pos=filePos;
		int read=0;
		int remaining = fileSize-filePos;
		while(remaining>0) {
			try {
				fileBytes=server.download(serverPath,pos);
				read=server.getRead();
				pos += read;
				remaining -= read;
				System.out.printf("Downloading....%.1f%%\r",((double)pos/fileSize)*100);
				BufOutStream.write(fileBytes, 0, read);
			}catch(IOException e) {
				if(isShutDown)
					Close(-1,"Server shutdown.");
				else
					Close(-1,"Error: File not downloaded.");
			}
		}
		CloseFile();
		Close(0,"File Download Complete");
	}

	public void UploadFile() {
		int read=0;
		int pos=0;
		int remainingSize=0;
		file = new File(clientPath);
		fileSize = (int)file.length();
		fileBytes = new byte[10*1024];

		try {
			BufInStream = new BufferedInputStream(new FileInputStream(file));
		}catch(FileNotFoundException e) {
			Close(-1,"File not found");
		}
		try {
			filePos = server.checkResend(serverPath,fileSize);
			pos=filePos;
			remainingSize = fileSize-filePos;
			BufInStream.skip(pos);
			while((read=BufInStream.read(fileBytes,0,Math.min(fileBytes.length,remainingSize)))>0) {
				double progress=server.upload(serverPath,fileBytes, read,fileSize,pos);
				pos += read;
				remainingSize -=read;
				if(progress==-1)
					break;
				System.out.printf("Uploading ....%.1f%%\r",progress);
			}

		}catch(IOException e) {
			if(isShutDown) {
				Close(-1,"server down.");
			}
			else
				Close(-1,"Error sending file");
		}
	}

//Function that makes calls to the respective operations
	public void runClientOperations() {
		String[] list;
		String message="";
		Connect();
		try {
			switch(cmd) {
				case "dir":
					list=server.dir(serverPath);
					if(list==null) {
						code =1;
						msg="Error: No dir. Cannot list directories";
					}else {
						Arrays.stream(list).forEach((String s) ->System.out.println(s));
					}
					break;
				case "upload":
					UploadFile();
					break;
				case "download":
					DownloadFile();
					break;
				case "shutdown":
					shutdown();
					break;
				case "mkdir":
					server.mkdir(serverPath);
					break;
				case "rmdir":
					server.rmdir(serverPath);
					break;
				case "rm":
					server.rm(serverPath);
					break;
				default:
					Close(1,"Invalid Command.");
			}
			message = server.getExitStatus();
			msg = message.replaceAll("-*?\\d$","");
			code=Integer.parseInt(message.substring(message.length()-2).trim());
		}catch(RemoteException e) {
			if(isShutDown) {
				msg="server shutdown";
				code=-1;
			}
			else
				System.out.println("Error: "+e.getMessage());
		}
		Close(code,msg);
	}


	//Method to shutdown the server
	public void shutdown() {
		try {
			isShutDown=server.shutdown();
		}catch(RemoteException e) {
			Close(-1,"Cannot shutdown server");
		}
		if(isShutDown)
			Close(0,"Server-Client disconnected.");
		else
			Close(-1,"Cannot shutdown server");
	}

	//Helper Method to Close the file stream
	private void CloseFile() {
		try {
			BufOutStream.flush();
			BufOutStream.close();
		}
		catch(IOException e ) {
			System.out.println("Error closing file: "+e.getMessage());
		}
	}

	//Sends message to the user and calls system exit with the code
	private void Close(int code, String message) {
		if(!message.equals(" "))
			System.out.println("\n"+message);
		System.exit(code);
	}
	public static void main(String[] args) {
		String path1="",path2="",cmd="";
		cmd=args[0];
		Client c=null;

		if(args.length==3) {
			path1=args[1];
			path2=args[2];
			c = new Client(cmd,path1,path2);
		}
		else if(args.length==2) {
			path1=args[1];
			c = new Client(cmd,path1);
		}
		else if(args.length==1) {
			c=new Client(cmd);
		}
		else {
			System.out.println("Command Syntax:java Client <command> [<path1>] [<path2>]");
			System.exit(1);
		}
		c.runClientOperations();
	}
}
