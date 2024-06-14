/**
 * This class implements Client fuctions that initiates a server connection and performs the required file operations
 * Author: Anjali Murthy
 * GNumber : (G01373209)
 */
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
	protected static Socket serverSocket;
	protected static DataInputStream inputStream;
	protected static DataOutputStream outputStream;
	private static void FileOperations(String[] args) throws IOException {
		switch(args[0].toLowerCase()) {
        case "download":
            if (args.length == 3)
                Download(args[1], args[2]);
            else {
                System.out.println("Enter a Valid Command.");
                return;
            }
            break;
		case "upload":
			if (args.length == 3)
				UploadFile(args[1],args[2]);
			else {
            	System.out.println(" Enter Valid Command.");
            	return;
            }
			break;
		case "dir":
			if (args.length == 2)
				ListDir(args[1]);
			else {
				System.out.println("Enter a Valid Command.");
            	return;
			}
			break;
		case "mkdir":
			if (args.length == 2)
				MakeDir(args[1]);
			else {
				System.out.println("Enter a Valid Command.");
            	return;
			}
			break;
		case "rmdir":
			if (args.length == 2)
				RemoveDir(args[1]);
			else {
				System.out.println("Enter a Valid Command.");
            	return;
			}
			break;
		case "rm":
			if (args.length == 2)
			    RemoveFile(args[1]);
			else {
			    System.out.println("Enter a Valid Command.");
                return;
			}
			break;
		case "shutdown":
            ShutDown();
            break;
		default: {
            System.out.println("Enter a Valid Command.");
            try {
            	CloseAllStreams();
            } catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            break;
			}
		}
	}

	//Helper Method to upload files to server
	protected static void UploadClientFile(String file,long fileSize) throws IOException{
		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.skip(fileSize);
		byte[] buffer = new byte[1024];
		while(fileInputStream.read(buffer) > 0){
			outputStream.write(buffer);
		}
		fileInputStream.close();
	}

	//Helper function to initiate a server connection
	private static void InitiateServerConnection(String host, int port) {
		try {
			serverSocket = new Socket(host, port);
		}catch(Exception e) {
			System.out.println("Server Connection Failed.");
		}
	}

	//Function to set up Input and Output streams for reading and writing file data
	protected static void InitiateStreams() throws IOException {
		outputStream = new DataOutputStream(serverSocket.getOutputStream());
		inputStream = new DataInputStream(serverSocket.getInputStream());
	}

	//Function to close all connections
	protected static void CloseAllStreams() throws IOException {
		outputStream.flush();
        outputStream.close();
        inputStream.close();
	}

	//Helper function that writes the bytes to a file on downloading from the server
	protected static void writeBytesToFile(long fileSize, String fileName,long downloaded) throws IOException {
		FileOutputStream fileOutputStream;
		if(downloaded == 0)
            fileOutputStream = new FileOutputStream(fileName, false);
        else
            fileOutputStream = new FileOutputStream(fileName, true);
        try {
	        byte[] buffer = new byte[1024];
	        int read = 0;
	    	long totalRead = downloaded;
	    	int remaining = (int) fileSize;
	    	//To calculate the percentage of remaining file download
	        while((read = inputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
	    		totalRead += read;
	    		remaining -= read;
	    		System.out.print("\rDownloading file - "+ (int)((double)(totalRead)/fileSize * 100)+"% complete");
	    		fileOutputStream.write(buffer, 0, read);
	    	}
        }catch (Exception e){
        	System.out.println("ERROR: " + e.getMessage());
        }finally {
        	fileOutputStream.flush();  fileOutputStream.close();
        }
	}

    //function to upload files to server
	private static void UploadFile(String file,String destination) throws IOException {
		System.out.println("File Location: " + file + " Destination: " + destination);
		long uploaded = new File(file).length();
		try {
			InitiateStreams();
			outputStream.writeUTF("upload");
			outputStream.writeUTF(destination);
			outputStream.writeLong(uploaded);
			long serverFileSize = inputStream.readLong();
			UploadClientFile(file,serverFileSize);
			System.out.println("\nFile upload complete");
		}
        catch (FileNotFoundException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }finally {
        	CloseAllStreams();
		}
	}


	//Method performs download from server
	private static void Download(String serverPath, String ClientPath) throws IOException{
		System.out.println("Server Path: " + serverPath + " Client Path:" + ClientPath);
		try {
			InitiateStreams();
			outputStream.writeUTF("download");
			outputStream.writeUTF(serverPath);

			if(inputStream.readBoolean()){
				long fileSize = inputStream.readLong();
				long downloaded = 0;
				downloaded = new File(ClientPath).length();
				outputStream.writeLong(downloaded);
				writeBytesToFile(fileSize,ClientPath,downloaded);
			}else
				System.out.println("\nFile does not exist on server");
		}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
		}finally{
			CloseAllStreams();
		}
	}



    	//Function to fetch all the directories/files in the given path
	private static void ListDir(String dir) throws IOException{
		String dirName = dir;
		System.out.println("Fetching Server Directory : " + dirName);
		try{
			InitiateStreams();
			outputStream.writeUTF("dir");
			outputStream.writeUTF(dirName);
			if (!inputStream.readBoolean())
                System.out.println("\nCould not find directory on server");
			else{
				System.out.println("\n"+inputStream.readUTF().trim());
				System.out.println("\nFinish.");
			}
		}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
		}finally{
			CloseAllStreams();
		}
	}

    //Funtion to remove directory specified by user
	private static void RemoveDir(String dir) throws IOException {
		String dirName = dir;
		System.out.print("Removing directory: " + dirName);
		try {
			InitiateStreams();
			outputStream.writeUTF("rmdir");
			outputStream.writeUTF(dirName);
			if (!inputStream.readBoolean())
                System.out.println("\nFailed to remove directory from the server");
			else
				System.out.println("\nDirectory Removed.");
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}finally {
			CloseAllStreams();
		}
	}

	//Function to create a new directory in the path specified
	private static void MakeDir(String dir) throws IOException {
		String dirName = dir;
		System.out.print("Creating directory: " + dirName);
		try {
			InitiateStreams();
			outputStream.writeUTF("mkdir");
			outputStream.writeUTF(dirName);
            if (!inputStream.readBoolean())
                System.out.println("\nFailed to create directory on the server.");
            else
            	System.out.println("\nDirectory created successfully");
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}finally {
			CloseAllStreams();
		}
	}

	//Function to remove file in the given path
	private static void RemoveFile(String removeFile) throws IOException {
		String file = removeFile;
		System.out.print("Deleting file: " + file);
		try {
			InitiateStreams();
			outputStream.writeUTF("rm");
			outputStream.writeUTF(file);
			if (!inputStream.readBoolean())
                System.out.println("\nFile could not be removed from the server");
			else
				System.out.println("\nFile removal successfully");
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}finally {
			CloseAllStreams();
		}
	}

    //Function to perform client shutdown
	private static void ShutDown() throws IOException {
		try {
			InitiateStreams();
			outputStream.writeUTF("shutdown");
			serverSocket.close();
			System.out.println("Server-Client connection Closed");
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}finally {
			CloseAllStreams();
		}
	}


	public static void main(String[] args) throws IOException {
		String hostname = System.getenv("PA1_SERVER");
		if (hostname != null) {
	           try {
				String[] env = System.getenv("PA1_SERVER").split(":");
                String host = env[0];
                int port = Integer.parseInt(env[1]);
				InitiateServerConnection(host,port);
				// InitiateServerConnection(env[0],Integer.parseInt(env[1]));

			}catch(Exception e) {
				System.out.println("ERROR: Server connection failed" + e.getMessage());
			}
			FileOperations(args);
		}
		else
			System.out.println("PA1_SERVER is not set.");
	}
}
