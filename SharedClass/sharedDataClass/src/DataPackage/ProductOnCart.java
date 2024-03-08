/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataPackage;

import java.io.Serializable;

/**
 *
 * @author moh
 */
public class ProductOnCart implements Serializable{
    private Product Product;
    private int Quantity;
    public ProductOnCart(Product Product,int Quantity){
        this.Product = Product;
        this.Quantity = Quantity;
    }
    public double getPrice(){
        return Product.getSellPrice()*Quantity;
    }

    public Product getProduct() {
        return Product;
    }

    public void setProduct(Product Product) {
        this.Product = Product;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int Quantity) {
        this.Quantity = Quantity;
    }
    
    
}
