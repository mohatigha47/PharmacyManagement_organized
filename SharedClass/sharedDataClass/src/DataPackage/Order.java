/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataPackage;

import java.io.Serializable;
import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author MoHaTiGha
 */
public class Order implements Serializable {
     private int ID;
     private Patient Patient;
     private ArrayList<ProductOnCart> Purchases;
     private LocalDateTime Date;
    public Order(int ID,Patient Patient,ArrayList<ProductOnCart> Purchases,LocalDateTime Date){
        this.ID = ID;
        this.Patient = Patient;
        this.Date = Date;
        this.Purchases = Purchases;
    }
    public double getTotal(){
        double total = 0;
        for(ProductOnCart prod : Purchases){
            total = total + prod.getPrice();
        }
        return total;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Patient getPatient() {
        return Patient;
    }

    public void setPatient(Patient Patient) {
        this.Patient = Patient;
    }

    public ArrayList<ProductOnCart> getPurchases() {
        return Purchases;
    }

    public void setPurchases(ArrayList<ProductOnCart> Purchases) {
        this.Purchases = Purchases;
    }

    public LocalDateTime getDate() {
        return Date;
    }

    public void setDate(LocalDateTime Date) {
        this.Date = Date;
    }
    
    
}
