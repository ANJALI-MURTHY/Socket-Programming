****************************************************************
Anjali Jayadeva Murthy
G01373209
amurthy2@gmu.edu
****************************************************************

This file contains details on how to setup client and server connections

The zip contains the following files.
1. Client.java - Source code for Client 
2. FileServer.java - Source code for Server
3.ClientHandlerInterface - Interface for the client operations
4.ClientHandler- Class with helper functions for the client file operations
5. Client.class
6. Server.class
7. pa2.jar - Jar file on which commands are run on


Commands to execute the jar file:

1. java -cp pa2.jar FileServer start 4000 #You can use any port number. 

This command needs to be run on one terminal to start the server

2.export PA1_SERVER=localhost:4000
#Use the same port number you used while running the previous command.
This command is to setup the server environment. The variable used here is PA1_SERVER (same as the prev assignment)

3.java -cp pa2.jar Client upload <filename_on_the_client> <filename_you_need_on_server>
#The file will be created at the location mentioned, since here in my case I have just mentioned file names, it will upload a file to the same directory(i.e the working directory)

4.java -cp pa2.jar Client download <filename_on_the_server> <filename_you_need_on_client>
#here you mention the file you would like to download from the server onto the client. Once the download is complete, you’ll see the message on the terminal. Incase of interruption a reattempt will occur.

---->Directory Operations

5.java -cp pa2.jar Client dir <existing_directory_on_server>
#This will return a list of all the files that are present in that path. If the <path_to_existing_directory_on_server> is not given , it will return the list of files in current directory which is our server location.

6.java -cp pa2.jar Client mkdir </new_directory_on_server_directory_name>
This command will create a new directory on the server with the name you specify. If the path isn’t mentioned and only the dir name is mentioned .A directory of the same name will be created in the current working directory.

7.java -cp pa2.jar Client rmdir </_existing_directory_on_server>
This command will remove the directory you specify in the path. If the path isn’t mentioned and only the dir name is mentioned .A directory of the same name will be removed in the current working directory.

8.java -cp pa2.jar Client rm </existing_filename_on_server>
This command will remove the file you mention in this path. If the path isn’t mentioned and only the file name is mentioned .A file of the same name will be removed in the current working directory.


9.java -cp pa2.jar Client shutdown
This command will shut down the client and server connection.

Note:

1.Error messages and expections are handled. It will get logged on the console/terminal every time an error occurs.

2.Multiple clients can connect with the server at any given time.