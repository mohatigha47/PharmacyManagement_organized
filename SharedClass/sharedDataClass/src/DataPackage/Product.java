/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataPackage;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author moh
 */
public class Product implements Serializable{
    private int ID;
    private String Name;
    private String Category;
    private int Quantity;
    private double BuyPrice;
    private double SellPrice;
    private String ExpDate;
    private Boolean Expired;
    public Product(int ID,String Name,String Category,int Quantity,double BuyPrice,double SellPrice,String ExpDate){
        this.ID = ID;
        this.Name = Name;
        this.Category = Category;
        this.Quantity = Quantity;
        this.BuyPrice = BuyPrice;
        this.SellPrice = SellPrice;
        this.ExpDate = ExpDate;
        checkExpired();
    }
    public void checkExpired(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ExpDateParsed = LocalDateTime.parse(ExpDate, formatter);
        LocalDateTime Now = LocalDateTime.now();
        Duration duration = Duration.between(Now, ExpDateParsed);
        if(duration.toDays() > 0){
            Expired = false;
        } else {
            Expired = true;
        }
    }
    public void updateQty(int qtyBought){
        Quantity = Quantity - qtyBought;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String Category) {
        this.Category = Category;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int Quantity) {
        this.Quantity = Quantity;
    }

    public double getBuyPrice() {
        return BuyPrice;
    }

    public void setBuyPrice(double BuyPrice) {
        this.BuyPrice = BuyPrice;
    }

    public double getSellPrice() {
        return SellPrice;
    }

    public void setSellPrice(double SellPrice) {
        this.SellPrice = SellPrice;
    }

    public String getExpDate() {
        return ExpDate;
    }

    public void setExpDate(String ExpDate) {
        this.ExpDate = ExpDate;
    }

    public Boolean getExpired() {
        return Expired;
    }

    public void setExpired(Boolean Expired) {
        this.Expired = Expired;
    }
    
}
