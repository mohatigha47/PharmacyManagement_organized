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
public class Data implements Serializable{
    private String instruction;
    private Object object;
    public Data(String instruction,Object object){
        this.instruction = instruction;
        this.object = object; 
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
    
    
}
