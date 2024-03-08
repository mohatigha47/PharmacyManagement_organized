/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataPackage;

import java.io.Serializable;

/**
 *
 * @author MoHaTiGha
 */
public class Patient implements Serializable{
    private int ID;
    private String FirstName;
    private String LastName;
    private int Age;
    private String Gender;
    private String PhoneNumber;
    private String Adress;
    private String Note;
    public Patient(int ID,String FirstName,String LastName,String PhoneNumber,String Adress,String Note,int Age,String Gender){
        this.ID = ID;
        this.FirstName = FirstName;
        this.Gender = Gender;
        this.PhoneNumber = PhoneNumber;
        this.LastName = LastName;
        this.Age = Age;
        this.Adress = Adress;
        this.Note = Note;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String LastName) {
        this.LastName = LastName;
    }

    public int getAge() {
        return Age;
    }

    public void setAge(int Age) {
        this.Age = Age;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String PhoneNumber) {
        this.PhoneNumber = PhoneNumber;
    }

    public String getAdress() {
        return Adress;
    }

    public void setAdress(String Adress) {
        this.Adress = Adress;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String Note) {
        this.Note = Note;
    }
    
}
