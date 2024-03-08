package serverdemo2;


import DataPackage.User;
import java.net.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Scanner;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author moh
 */
public class ClientHandler implements Runnable{
    public String ID;
    public String Name;
    public LocalDateTime TimeJoined;
    public Socket socket;
    public User currentUser;
    public boolean isLoggedIn = false;
    public ObjectOutputStream objectOutputStream;
    public ObjectInputStream objectInputStream;
    public ClientHandler(String ID,String Name,Socket socket,ObjectInputStream objectInputStream,ObjectOutputStream objectOutputStream){
        TimeJoined = LocalDateTime.now();
        this.ID = ID;
        this.Name = Name;
        this.socket = socket;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
    }

    @Override
    public void run() {
        
        
    }
        
}
