/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package general;

/**
 *
 * @author Fcaty
 */

//This class file simply stores the class Entity, used in JournalizingForm to create temporary records, stored locally C:
public class Entry {
    private String aName;
    private int AID;
    private char recType;
    private double amount;
    
    //Constructor
    public Entry(int AID, String aName, char recType, double amount) {
        this.AID = AID;
        this.aName = aName;
        this.recType = recType;
        this.amount = amount;
    }
    
    //public getter methods
    public int getAID(){
        return this.AID;
    }
    
    public String getAName(){
        return this.aName;
    }
    
    public char getRecType(){
        return this.recType;
    }
    
    public double getAmount(){
        return this.amount;
    }
}
