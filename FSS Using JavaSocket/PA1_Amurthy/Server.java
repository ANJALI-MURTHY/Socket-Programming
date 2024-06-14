/**
 * This class implements server fuctions
 * Author: Anjali Murthy
 * GNumber : (G01373209)
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

public class Server {
	protected static DataInputStream inputStream;
	protected static DataOutputStream outputStream;
	protected static Socket sock;
	private static ServerSocket serverSock;
	private static boolean isActive = true;

	//Switch case to manage server operations
	private static void ServerCommands(String args) throws IOException {
		switch(args.toLowerCase()) {
		case "download":
			Download();
			break;
		case "upload":
			Upload();
			break;
		case "dir":
			ListDir();
			break;
		case "mkdir":
			MakeDir();
			break;
		case "rmdir":
			RemoveDir();
			break;
		case "rm":
			RemoveFile();
			break;
		case "shutdown":
			ShutDown();
			break;
		default: System.out.println("Requested Operation is not supported.");
		}
	}



	//Function creates server sockets
	private Server(int port) {
		try {
			serverSock = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("ERROR: "+ e.getMessage());
		}
	}

	//Function to initialize Input and Output streams
	protected static void InitializeStreams() throws IOException {
		outputStream = new DataOutputStream(sock.getOutputStream());
		inputStream = new DataInputStream(sock.getInputStream());
	}

	//Function to close all connections and flush
	protected static void CloseStreams() throws IOException {
		outputStream.flush();
		outputStream.close();
		inputStream.close();
	}

	//Function checks if the server is  up and runing.
	private void run() throws IOException {
		while (isActive) {
			try {
				sock = serverSock.accept(); //Accepts client connections
				InitializeStreams();
				String argument = inputStream.readUTF();
				ServerCommands(argument);
			} catch (IOException e) {
				System.out.println("ERROR: " + e.getMessage());
			}finally {
				CloseStreams();
			}
		}
	}

	//Helper Function to write to a file
	protected static void writeBytesToFile(long fileSize, String fileName,long uploadedData) throws IOException {
		FileOutputStream fileOutputStream;
		if(uploadedData == 0)
            fileOutputStream = new FileOutputStream(fileName, false);
        else
            fileOutputStream = new FileOutputStream(fileName, true);
        try {
        	int fs = (int) fileSize;
        	int read = 0;
	    	long dt = uploadedData;
	        byte[] cz = new byte[1024];
	        while((read = inputStream.read(cz, 0, Math.min(cz.length, fs))) > 0) {
	    		dt += read;
				fs -= read;
	    		System.out.println("\r Uploading File - "+ (int)((double)(dt)/fileSize * 100)+"% complete");
	    		fileOutputStream.write(cz,0,read);
	    	}
        }catch (Exception e) {
        	System.out.println("ERROR: " + e.getMessage());
        }finally {
        	fileOutputStream.flush();
			fileOutputStream.close();
        }
	}

	//Function to upload file to the server
	private static void Upload() throws IOException {
		String fileData = inputStream.readUTF().trim();
		long fileSize = inputStream.readLong();
		 try {
			 long uploadedData = 0;
			 uploadedData = new File(fileData).length();
			 outputStream.writeLong(uploadedData);
	         writeBytesToFile(fileSize, fileData, uploadedData);
	    }catch (Exception e){
	         System.out.println("ERROR: " + e.getMessage());
	    }
	}


	//Helper Function used while downloaded the file from the server , to upload the file in the given path
	protected static void UploadFile(String f,long serverFileSize) throws IOException{
		FileInputStream fileInputStream = new FileInputStream(f);
		fileInputStream.skip(serverFileSize);
		byte[] bt = new byte[1024];
		while(fileInputStream.read(bt) > 0)
			outputStream.write(bt);
		fileInputStream.close();
	}

	//Function to download file from server to the desired location
	private static void Download() throws IOException{
		try {
			String fileData = inputStream.readUTF().trim();
			if(!(new File(fileData).exists())){
				outputStream.writeBoolean(false);
			}else {
				outputStream.writeBoolean(true);
				long dataLen = new File(fileData).length();
				outputStream.writeLong(dataLen);
				long clientFileSize = inputStream.readLong();
				System.out.println(clientFileSize);
				UploadFile(fileData,clientFileSize);
				System.out.println("\nFile download Complete");
			}
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	//Function Lists out all the files present in the directory
	private static void ListDir() throws IOException{
		String dirData = inputStream.readUTF().trim();
		if (!(new File(dirData).exists()) || !(new File(dirData).isDirectory())){
			outputStream.writeBoolean(false);
		}else{
			outputStream.writeBoolean(true);
			File folder = new File(dirData);
			File[] lff = folder.listFiles();
			outputStream.writeUTF(Arrays.toString(lff));
		}
	}

	//Function to create directories on the server
	private static void MakeDir() throws IOException {
		String dirPath = inputStream.readUTF().trim();
		outputStream.writeBoolean(new File(dirPath).mkdir());
	}

	//Function to remove directory on the server
	private static void RemoveDir() throws IOException {
		String dirData = inputStream.readUTF().trim();
        if (!(new File(dirData).exists()) || !(new File(dirData).isDirectory())) {
        	outputStream.writeBoolean(false);
        }
        else {
        	outputStream.writeBoolean(new File(dirData).delete());
        	}
	}

	//Function to remove a file from the server
	private static void RemoveFile() throws IOException {
		String fileData = inputStream.readUTF().trim();
		if (!(new File(fileData).exists()) || (new File(fileData).isDirectory()))
			outputStream.writeBoolean(false);
		else
			outputStream.writeBoolean(new File(fileData).delete());
	}

	//Function to shutdown the server
	private static void ShutDown() throws IOException{
		CloseStreams();
		serverSock.close(); //Closes the server socket
		isActive = false; //Makes the server inactive
	}

	public static void main(String[] args) throws IOException {
		if(args.length !=2)
			System.out.println("ERROR: Enter 2 arguements.Hint:<operation> <port number>");
		if(args[0].equalsIgnoreCase("start")) {
			Server servsock = new Server(Integer.parseInt(args[1]));
			servsock.run();
		}else
			System.out.println("Enter a Valid Command");
	}
}
