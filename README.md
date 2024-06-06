# File Server
A HTTP File Server demo, featuring the storage, retrieval and deletion of files between the server and client(s).

## Technologies

This program uses:
- Java 17

## Getting Started

Start by running `Server.java` from the `server` package:
```
Server started!
```
Then, in a new terminal, run `Client.java` from the `client` package. 
Follow the menu options to send a request to the server.

## Features

- save any file type to the server
- each saved file is assigned an ID, which can be used in place of the file's name to access it
- clients can save files from the server to their local `/data/` directory
- clients can requeest the deletion of files
- after each operation, the server will respond to the client with a HTTP status code

### Examples

Saving a file:
```
Enter action (1 - get a file, 2 - save a file, 3 - delete a file): 
> 2
Enter name of the file: 
> ok
Enter name of the file to be saved on the server: 
> 
The request was sent.
Response says that file is saved! ID = 1
```
Getting a file:
```
Enter action (1 - get a file, 2 - save a file, 3 - delete a file): 
> 1
Do you want to get the file by name or by id (1 - name, 2 - id): 
> 2
Enter id: 
> 1
The request was sent.
The file was downloaded! Specify a name for it: 
> ok
File saved on the hard drive!
```
Deleting a file:
```
Enter action (1 - get a file, 2 - save a file, 3 - delete a file): 
> 3
Do you want to delete the file by name or by id (1 - name, 2 - id): 
> 2
Enter id: 
> 1
The request was sent.
The response says that the file was successfully deleted!
```



