/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads a class directly from bytecode
 * @author dcowden
 */
public class ByteCodeClassLoader extends ClassLoader{
    private final Map<String, byte[]> extraClassDefs;

    public ByteCodeClassLoader(ClassLoader parent) {
      super(parent);
      this.extraClassDefs = new HashMap<String, byte[]>();
    }

    public void addClassDef(String className, byte[] source){
        extraClassDefs.put(className, source);
    }
    
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
      byte[] classBytes = this.extraClassDefs.remove(name);
      if (classBytes != null) {
        return defineClass(name, classBytes, 0, classBytes.length); 
      }
      return super.findClass(name);
    }
  
}
