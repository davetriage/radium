/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.agent;

import com.triage.radium.testobjects.Calculator;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author dcowden
 */
public class TestCalculator {
    
    //does it work withotu bytecode screwiness?
    @Test
    public void testThatCalcultorWorks(){
        Calculator c = new Calculator();
        assertEquals(27,c.add(1,3,7,16));
    }
}
