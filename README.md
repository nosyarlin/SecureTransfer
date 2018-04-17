# Secure-File-Transfer

### The Assignment

In this assignment, we implemented authentication and sending of encrypted data over a network. 

### How To Run

Running the programs is simple. First clone the repository and go to the folder "src" on your command prompt or terminal. 

Next, compile the relevant java files. For example if you want to run CP1,

```
javac ServerCP1.java
javac ClientCP1.java
```

Finally, run the Server program on the machine that will be receiving the files, and run the Client program on the machine sending the file. 

```
java ServerCP1
java ClientCP1 <ip-address-of-server> <path-to-file>
```

Here is an example of how we run our program.
On the server side:
```
javac ServerCP2.java
java ServerCP2
```

And on the client side:
```
javac ClientCP2.java
java ClientCP2.java 10.12.90.175 rr.txt
```

### Specifications
Our program has two parts. Authentication (AP) and Encrypted Data Transfer (CP1 and CP2). 

Here are the specs of AP:

![alt text][ap]

[ap]: https://github.com/nosyarlin/SecureTransfer/blob/master/AP%20Diagram.png?raw=true


Here are the specs of CP1:

![alt text][cp1]

[cp1]: https://github.com/nosyarlin/SecureTransfer/blob/master/CP1%20Diagram.png?raw=true


Here are the specs of CP2:

![alt text][cp2]

[cp2]: https://github.com/nosyarlin/SecureTransfer/blob/master/CP2%20Diagram.png?raw=true


### Why Nonce?
If you have seen the specs of our program, you might have noticed that we used a nonce during authentication. This is to prevent the possibility of a playback attack. With the nonce, an attacker can no longer record the message encrypted using the server's private key, and send it to the client at a later time, pretending to be the correct server. 


### Sending Speed
We tested both CP1 and CP2 over 350 text files of varying sizes and plot the file transfer time to compare the performance of the two different encryption methods. 

Here are the results.


![alt text][plot]

[plot]: https://github.com/nosyarlin/SecureTransfer/blob/master/plot.png?raw=true

For the high quality and interactive version, visit http://rpubs.com/nosyarlin/Programming_Assignment2






Note: The programs were written and tested on a Mac machine. 