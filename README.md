# Chatroom_CN

## A brief description
This code is the assignment of Computer Network homework 3 chat-room

## Details on development environment
This java code is developed in Sublime.

## How to run this code
The code contains two .txt files and three .java files. First compile logIn.java, then compile Server.java, and finally Client.java.
Run Server.class first, use ‘java Server <Port number>’ command. Default port number is 2222, you can change it as you wish. Then run Client.class file, and use ‘java Client <IP address> <Port number>’. IP address is the server’s IP address, you can use 127.0.0.1 for test. Port number is the server socket port number you set before. If you use the default number, it’s 2222.

## Sample commands to invoke this code
Commands include ‘who’, ‘last’, ‘broadcast’, ‘send’, ‘exit’(or ‘logout’). You’ll see the usage of commands when you running the code.

## Additional functionalities
(a) Server can see the who is logged in, who is locked and some other clients information. Usage: enter ‘1’ to ‘4’ in terminal.
(b) The code also records all the message clients have sent. The record is in chat_record.txt file.
