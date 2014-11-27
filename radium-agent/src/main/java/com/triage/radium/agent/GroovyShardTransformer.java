/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.agent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Transforms classes using groovyShards
 * @author dcowden
 */
public class GroovyShardTransformer implements ClassFileTransformer{

   //used when we are transforming a class in teh system classloader
   protected      String TAP_CLASS_BASIC = "@groovy.transform.CompileStatic\n" +
                   "class ScriptTestClass{\n" +
                   "    String beforeWhatDoIThinkAbout(com.triage.radium.testobjects.TestPerson person){\n" +
                   "    def r = person.getName() + \" Is Groovy!\"             \n" +
                   "    println(r);\n" +
                   "    return r;\n" + 
                   "    }\n" +
                   "}";
   
   //for transforming a class OUTSIDE the system class loader
   protected      String TAP_CLASS_OTHER = "@groovy.transform.CompileStatic\n" +
                   "class ScriptTestClass{\n" +
                   "    String beforeWhatDoIThinkAbout(com.triage.example.TestPerson person){\n" +
                   "    def r = person.getName() + \" Is Groovy!\"             \n" +
                   "    println(r);\n" +
                   "    return r;\n" + 
                   "    }\n" +
                   "}";   
   
   //for transformation of variable table testing
   //this script tries intentionally to mess up the local variable table
   protected String TAP_FOR_CALCULATOR = "@groovy.transform.CompileStatic\n" +
                   "class ScriptTestClass{\n" +
                   "    int beforeAdd(int w, int x, int y, int z){\n" +
                   "    int blah=y+z+w            \n" +
                   "    println(\"Blah=\" + blah );                          \n " + 
                   "    //So we can prove we were here  \n" +
                    "    com.triage.radium.testobjects.TestFlag.specialSum=blah   \n" +
                   "    }\n" +
                   "}";   
   
   protected Map<String,String> tapXref = new HashMap<String,String>();
   
    public GroovyShardTransformer(){
        tapXref.put("com/triage/radium/testobjects/TestFriend", TAP_CLASS_BASIC);
        tapXref.put("com/triage/example/TestFriend", TAP_CLASS_OTHER);
        tapXref.put("com/triage/radium/testobjects/Calculator", TAP_FOR_CALCULATOR);
    }
    
    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain pd, byte[] classFileBuffer) throws IllegalClassFormatException {
        //System.err.println("Inspecting Class: " + className);
        //hard coded for now basically!
        //obviously not filtered later!
        if ( className.contains("triage")){
            try{
                System.out.println("Trying to Transform:" + className + " in classloader '" + classLoader + "'");
                String tapToUse = tapXref.get(className);
                if ( tapToUse != null){
                    System.out.println("Transforming With script" + tapToUse);
                    return transformClass(tapToUse, className, classLoader, classFileBuffer);
                }
                else{
                    System.out.println("No Tap available-- Skipping");
                    return null;
                }
                
            }
            catch ( Exception e){
                System.err.println("Could not transform!");
                e.printStackTrace();
                return null;
            }
        }
        else{
            //System.out.println("Not Transforming: " + className);
        }
        return null;
    }
    
    //just a test implementation: sharding
    protected byte[] transformClass(String tapScript, String className, ClassLoader cl, byte[] classFileBuffer) throws Exception{
        Timer timer = new Timer();
        timer.start();
        //so that we can reference classes available in the caller when we compile the script
        
        //MAKES A KEY ASSUMPTION: that all classloaders we will have a path back to the system
        //classloader, and thus will have our groovy stuff in them!
        //if this isnt the case, we may want to have a dual-parent classloader where
        //we can search both the target classloader and the system
        CachingGroovyClassLoader groovyClassLoader = new CachingGroovyClassLoader(cl);
        
        //load the script in ASM
        ClassNode shimClassNode = new ClassNode();
        String scriptName = className + "-Tap.groovy";
        
        timer.mark("startParse");
        Class groovyClass = groovyClassLoader.parseClass(tapScript,scriptName);
        timer.mark("endParse");
        
        String generatedClassName = groovyClass.getName() + ".class";
        byte[] classBytes = groovyClassLoader.getClassBytes(generatedClassName);
        timer.mark("getClassBytes");
        ClassReader shimClassReader = new ClassReader(classBytes);
        shimClassReader.accept(shimClassNode,0); 
        timer.mark("readShimClass");

        ClassNode targetClassNode = new ClassNode();        
        ClassReader targetClassReader = new ClassReader(classFileBuffer);
        targetClassReader.accept(targetClassNode,0);
        timer.mark("readTargetClass");
        //copy instructions        
        //TODO: this is just a POC-- of course all this hardcoded stuff needs
        //to be replaced with real code
        MethodNode targetMethod = null;
        MethodNode sourceMethod = null;
        InsnList instructionsToInject = null;
        
        if ( className.contains("TestFriend")){
            targetMethod = findMethod(targetClassNode,"whatDoIThinkAbout");        
            sourceMethod = findMethod(shimClassNode, "beforeWhatDoIThinkAbout"); 
            instructionsToInject = sourceMethod.instructions;
        }
        else if ( className.contains("Calculator")){
            targetMethod = findMethod(targetClassNode,"add");        
            sourceMethod = findMethod(shimClassNode, "beforeAdd");
            
            //HACK: in the calculator script, we do not want a premature
            //return, so lets remove RETURNs from the source.
            //that DOESNT work in gneeral because sometimes we do want a return
            //but this will just see if removing the returns works
            instructionsToInject = sourceMethod.instructions;
            ListIterator li = instructionsToInject.iterator();
            while ( li.hasNext()){
                AbstractInsnNode node = (AbstractInsnNode)li.next();
                
                if ( isReturnOpCode(node.getOpcode()) ){
                    li.remove();
                }
            }
        }        
        timer.mark("gotInstructionsToInject");
        System.out.println("Transforming Target Method:");
        printMethodNode(targetMethod);
        System.out.println("Transforming Source Method:");
        printMethodNode(sourceMethod);
        
        //insert source instructions in target
        targetMethod.instructions.insert(sourceMethod.instructions);
        timer.mark("injectedInstructions");
        
       //write a class 
       
       ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
       targetClassNode.accept(cw);
       timer.mark("finishedWrite");
       System.out.println("Successfully transformed class" + className);
       timer.stop();
       System.out.println("Timings:"+timer);
       return cw.toByteArray();
        
    }
    
   protected boolean isReturnOpCode(int opCode){
       return opCode == Opcodes.ARETURN || opCode == Opcodes.LRETURN || opCode == Opcodes.IRETURN ||
                opCode == Opcodes.DRETURN || opCode == Opcodes.FRETURN || opCode == Opcodes.RETURN;
   }
   protected MethodNode findMethod(ClassNode classNode, String name){
       for ( Object o: classNode.methods){
           MethodNode mn = (MethodNode)o;
           
           //not worrying about signatures now
           if ( mn.name.equals(name)){
               return mn;
           }
       }
       return null;
   }    
   

   protected void printMethodNode(MethodNode mn){
       StringBuffer sb = new StringBuffer();
       sb.append("MethodNode: ").append(mn.name);
       sb.append("LocalVariables:\n");
       for ( Object o: mn.localVariables){
           LocalVariableNode vn =(LocalVariableNode)o;
           sb.append(vn.name).append("\n");
       }
       sb.append("Instructions:\n");
       for ( AbstractInsnNode aa: mn.instructions.toArray()){
           sb.append(printNode(aa));
       }
       System.out.println(sb.toString());
   }
   
   protected String printNode(AbstractInsnNode insnNode ){
        /* Create a "printer" that renders text versions of instructions */
        Printer printer = new Textifier();
        TraceMethodVisitor methodPrinter = new TraceMethodVisitor(printer);

        /* render the instruction as a string and add it to printer's internal buffer */
        insnNode.accept(methodPrinter);

        /* convert printer's internal buffer to string and clear the buffer (so we can reuse it later) */
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        return sw.toString();
     
   }   
}
