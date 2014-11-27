/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.testobjects;

import java.util.Date;

/**
 * A Test class
 * @author dcowden
 */
public class TestPerson {
    
    public TestPerson(){
        
    }
    public TestPerson(String name){
        this.name = name;
    }
    
    public String executeLogic(String stringParam, Date dateParam){
        return stringParam + dateParam;
    }
    public String getDescription(){
        return "Name=" + name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected String name;
    protected int age;
}
