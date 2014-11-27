/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.testobjects;

/**
 * Tests transformations to see if local variables and scopes are right or not.
 * The test intentionally trys to create a scenario where local variable
 * tables get screwed up in bytecode injection
 * 
 * Bytecode will be injected at the TOP of the method that will introduce
 * local variables.
 * 
 * when bytecode for the method below is produced, the vars table will be:
 * w,x,y,z,sum1,sum2,sum3
 * 
 * when a new var, 'blah', is injected, it will be
 * w,x,y,z,blah,sum1,sum2,sum3
 * 
 * will that screw up the tally?
 * 
 * @author dcowden
 */
public class Calculator {
    
    public static int specialSum = 0;
    
    public int add(int w, int x, int y, int z){
        
        //local vars will be injected here in an agent script
        //  int blah=y+z+w 
        //  println(\"Blah=\" + blah );                          \n
        //  //So we can prove we were here 
        //  com.triage.bytecodemaster.fortesting.TestFlag.specialSum=blah 
        
        int sum1 = w + x;
        
        int sum2 = y + z;
        
        int sum3 = sum1 + sum2;
        return sum3;
    }
    
}
