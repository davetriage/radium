/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.it;

import com.triage.radium.testobjects.Calculator;
import com.triage.radium.testobjects.TestFlag;
import com.triage.radium.testobjects.TestPerson;
import com.triage.radium.testobjects.TestFriend;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author dcowden
 */
public class ITGroovySharding {
    
    
    //NOTE: this test will fail when run from your IDE-- it has to run from
    //failsafe, which attaches the agent
    //this is the easy case: the script references classes that are in the system
    //classloader only.
    @Test
    public void testBasicTestClassFromSystemClassLoader(){
        TestPerson friendDave = new TestPerson("Dave");
        TestFriend tf = new TestFriend();
        String shouldSayFromGroovy = tf.whatDoIThinkAbout( friendDave );
        System.out.println("Got Response: " + shouldSayFromGroovy);
        assertTrue(shouldSayFromGroovy.equals("Dave Is Groovy!"));        
    }
    
    @Test
    public void testCalculator(){
        //this calculator has had its bytecode manipulated
        //the static variable specialSum is manipulated in the hook script
        //so we can prove that we modified the code, AND used intermediate
        //variables, AND didnt mess up the total
        
        Calculator c = new Calculator();                
        
        //make sure sum still works right
        assertEquals(27,c.add(1,3,7,16));
        
        //prove the code was instrumented
        assertEquals(24, TestFlag.specialSum);        
        
    }
    
    //this one is hard to set up a test case for! 
    //in this case, we transform classes that are NOT in the system 
    //classloader.
    //Groovy IS in the system classloader
    //a groovy script referenes classes NOT in the system classloader
    //we transform the class correclty, we hope.
    //NOTE: the transformation happens in the agent, which is loaded by the failsafe plugin
    //@see GroovyShardTransformer
    @Test
    public void testThatClassesNotInSystemClassloaderWork() throws Exception{
        
        TestPerson testClass = new TestPerson("Dave"); //loaded from this classloader
        
        //here we are loading classes from a special location that has classes
        //NOT in the project.
        //com.triage.example.TestPerson and com.triage.example.TestFriend
        //are copies of com.triage.bytecodemaster.TestPerson/TestFriend, but compiled
        //and removed from source so that they are NOT available in the system classloader
        File targetDir = new File("testdata/testclasses");          
        URLClassLoader custom = new URLClassLoader(new URL[]{targetDir.toURI().toURL() },getClass().getClassLoader());
        
        //not assignable in this class because its in a different classloader
        Class personClazz = custom.loadClass("com.triage.example.TestPerson");      
        Class friendClazz = custom.loadClass("com.triage.example.TestFriend");
        
        try{
            Class.forName("com.triage.example.TestPerson");
            fail("Expected Not to be able to load com.triage.example.TestPerson through system classloader");
        }
        catch ( Exception e){
            //good
        }
        
        assertTrue(personClazz.getSimpleName().equals("TestPerson"));
        assertTrue(friendClazz.getSimpleName().equals("TestFriend"));
        
        //this is a pain all the code is reflection since the classes are not addressible in
        //this classloader
        Object dave = personClazz.newInstance();
        Object jen = friendClazz.newInstance();
        
        Method m = dave.getClass().getMethod("setName", String.class);
        m.invoke(dave, "Dave");
        
        Method m2 = jen.getClass().getMethod("whatDoIThinkAbout",personClazz);
        Object response = m2.invoke(jen,dave);
        assertEquals(response,"Dave Is Groovy!");

    }
     
    
}
