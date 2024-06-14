/**
 * This class is a client handler class which is a helper for the client operations
 * Author: Anjali Murthy
 * GNumber : (G01373209)
 */
import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientHandler extends UnicastRemoteObject implements ClientHandlerInterface {
	private int errorCode=0;
	private int read=0;
	private String message="";
	private static final long serialVersionUID = 1L;
	private final static String root= System.getProperty("user.dir")+"/";


	protected ClientHandler() throws RemoteException {
		super();
	}

	//Helper function to Close the file stream
	private void CloseFile(BufferedOutputStream bufOutput) {
		try {
			bufOutput.flush();
			bufOutput.close();
		}
		catch(IOException e ) {
			System.out.println("Error: Cannot close filestream: "+e.getMessage());
		}
	}
	//Helper function to create a new file object
	public File CreateFileObj(String path) {
		String serverPath="";
		File file=null;
		if(path.equals("/") || path.equals("\\"))
			serverPath=root;
		else
			serverPath=root+path;
		if(!path.equals(" "))
			file = new File(serverPath);
		return file;
	}

	//Method to send the client the return code and messages
	public String getExitStatus() {
		return message+" "+errorCode;
	}

	//Helper function to perform the upload operation
	public double upload(String path,byte[] arr, int rem,int size,int prog) {
		File file = CreateFileObj(path);
		BufferedOutputStream bufOut=null;
		double progress=0.0;
		try {
			file.createNewFile();
		}catch(IOException e) {}
		try {
			bufOut = new BufferedOutputStream(new FileOutputStream(file,true));
		}catch(FileNotFoundException e) {
			System.out.println("File Not Found");
			message="File not found";
			errorCode =-1;
			return -1;
		}
		int remaining = size-prog;
		try {
			if(remaining >0) {
				remaining -= rem;
				prog+=rem;
				message="";
				progress=((double)prog/size)*100;
				bufOut.write(arr, 0, rem);
			}

		}catch(IOException e ) {
			System.out.println("Error uploading file. Try again.");
			message="Error uploading file. Try again.";
			errorCode=-1;
		}

		message="File upload successful.";
		errorCode=0;
		CloseFile(bufOut);
		return progress;
	}

	//Helper function to download the file from server
	public byte[] download(String path,int fp) {
		File file = CreateFileObj(path);
		byte[] fileArr=new byte[10*1024];
		BufferedInputStream bufInputStrm=null;

		int size=(int) file.length();
		int pos=fp;
		int remaining = size-pos;
		try {
			 bufInputStrm= new BufferedInputStream(new FileInputStream(file));
		}catch(FileNotFoundException e) {
			message = "File not found";
			errorCode =-1;
		}
		try {
			bufInputStrm.skip(pos);
			if((read=bufInputStrm.read(fileArr,0,Math.min(fileArr.length,remaining)))>0) {
				remaining -=read;
				pos += read;
				bufInputStrm.close();
				return fileArr;
			}
		}catch(IOException e) {
			System.out.println("Error sending file.");
			message = "Error downloading file.";
			errorCode=-1;
		}
		return null;
	}


	//Method to list the files in the directory to the client
	public String[] dir(String path) {
		String[] list=null;
		File file = CreateFileObj(path);
		message="";
		errorCode=0;
		if(!file.exists()) {  //when directory doesnt exist in the location
			message = file.getName() + " not exist";
			errorCode= 1;
		}
		else if(!file.isDirectory()) {//when the filename is not a directory
			message=file.getName()+" is not a directory";
			errorCode= 1;
		}
		else {
			list =file.list();
		}

		return list;
	}

	//Method to Create new directory
	public void mkdir(String path) {
		File file = CreateFileObj(path);
		if(file.exists()) {
			message = "Directory "+file.getName()+"already exists";
			errorCode =1;
		}
		else if(!file.mkdir()) {
			message = "Create directory failed";
			errorCode= 1;
		}else
			message="Directory "+file.getName()+"created successfully.";
	}

	//Function to Delete the mentioned directory
	public void rmdir(String path) {
		File file = CreateFileObj(path);
		if(!file.isDirectory()) {//When mentioned file is not a directory
			message = file.getName() + " is not a directory";
			errorCode =1;
		}
		else if(file.list().length>0) { //when the given directory isnt empty
			message = "Directory "+file.getName()+ " is not empty";
			errorCode =1;
		}
		else if(!file.delete()) {//If the mentioned dir is already deleted
			message = "Cannot Delete "+file.getName();
			errorCode =1;
		}
		else
			message="Directory " +file.getName()+" deleted successfully.";
	}

	//Function to Delete the file in the specified path
	public void rm(String path) {
		File file = CreateFileObj(path);
		if(!file.exists()) {//when file doesnt exist, update the errorcode
			message = file.getName() + " does not exist";
			errorCode= 1;

		}
		else if(!file.isFile()) {//when specified path doesnt not have a file
			message=file.getName()+" is not a file";
			errorCode= 1;
		}
		else if(!file.delete()) {//if the file is already deleted, update the error code
			message = "Cannot Delete "+file.getName();
			errorCode= 1;
		}
		else
			message ="File "+ file.getName() +" deleted";
	}


	//Method returns the file size of the given file from the path
	@Override public int getFsize(String path) {
		return (int)CreateFileObj(path).length();
	}


	//Function returns the number of bytes
	@Override public int getRead() {
		return read;
	}

	//Helper method during the file resend during any interupptions during the file operations
	@Override
	public int checkResend(String path,int size) {
		Boolean resend=false;
		int pos=0;
		File file = CreateFileObj(path);
		if((int)file.length()<size && file.length()!=0){
			pos=(int)file.length();
			resend=true;
		}
		if(resend) {
			System.out.printf("Reuploading from %.1f%%\n",((double)file.length()/size)*100);
		}
		return pos;
	}


	//Function to Shutdown the server
	@Override
	public boolean shutdown() throws RemoteException {
		boolean shutdown=false;
		 try{
		        Naming.unbind(FileServer.name);
		 }
		 catch(Exception e){
			System.out.println(e.getMessage());
			return false;
		}

		 UnicastRemoteObject.unexportObject(this, true);
         System.out.println("FileServer exiting.");
         shutdown=true;
         return shutdown;
	}


}
