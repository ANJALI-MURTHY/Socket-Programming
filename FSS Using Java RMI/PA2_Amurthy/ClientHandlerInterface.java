/**
  * Author: Anjali Murthy
 * GNumber : (G01373209)
 */

import java.rmi.RemoteException;
import java.rmi.Remote;


public interface ClientHandlerInterface extends Remote{
	public void mkdir(String path) throws RemoteException;
	public void rmdir(String path) throws RemoteException;
	public void rm(String path) throws RemoteException;
	public double upload(String path,byte[] arr, int r,int size,int pos)   throws RemoteException;
	public byte[] download(String path,int pos) throws RemoteException;
	public String[] dir(String path) throws RemoteException;
	public boolean shutdown() throws RemoteException;
	public int getRead() throws RemoteException;
	public int getFsize(String path) throws RemoteException;
	public int checkResend(String path,int Fsize) throws RemoteException;
	public String getExitStatus() throws RemoteException;
}
