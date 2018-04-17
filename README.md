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

### Sending Speed
We tested both CP1 and CP2 over 350 text files of varying sizes and plot the file transfer time to compare the performance of the two different encryption methods. 

Here are the results.


![alt text][plot]

[plot]: https://github.com/nosyarlin/SecureTransfer/blob/master/plot.png?raw=true

Note: The programs were written and tested on a Mac machine. 