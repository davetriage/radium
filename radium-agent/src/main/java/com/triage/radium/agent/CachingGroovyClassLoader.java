/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.agent;

import groovy.lang.GroovyClassLoader;
import groovyjarjarasm.asm.ClassWriter;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 * Groovy does not cache the bytecode sequences for generated classes.
 * BytecodeReadingParanamer needs these to get paramater names from classes The
 * Groovy compiler does create the debug tables, and they are the same as the
 * ones made for a native Java class, so this derived GroovyClassLoader fills in
 * for the missing functionality from the base GroovyClassLoader.
 * 
 * Groovy allows a mechanism via a system property to force the dump of bytecode
 * to a (temp) directory, but caching the bytecode avoids having to clean up
 * temp directories after the run.
 */
public class CachingGroovyClassLoader extends GroovyClassLoader {

    private Map<String, byte[]> classBytes = new HashMap<String, byte[]>();

    public CachingGroovyClassLoader(){
        
    }
    public CachingGroovyClassLoader(ClassLoader parent){
        super(parent);
    }
    
    public byte[] getClassBytes(String name) throws IOException{
        return IOUtils.toByteArray(getResourceAsStream(name));
    }
    
    @Override
    public InputStream getResourceAsStream(String name) {
        if (classBytes.containsKey(name)) {
            return new ByteArrayInputStream(classBytes.get(name));
        }
        return super.getResourceAsStream(name);
    }

    @Override
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        // These six lines copied from Groovy itself, with the intention to
        // return a subclass
        InnerLoader loader = AccessController.doPrivileged(new PrivilegedAction<InnerLoader>() {
            public InnerLoader run() {
                return new InnerLoader(CachingGroovyClassLoader.this);
            }
        });
        return new BytecodeClassCollector(classBytes, loader, unit, su);
    }

    public static class BytecodeClassCollector extends ClassCollector {
        private final Map<String, byte[]> classBytes;

        public BytecodeClassCollector(Map<String, byte[]> classBytes, InnerLoader loader, CompilationUnit unit,
                SourceUnit su) {
            super(loader, unit, su);
            this.classBytes = classBytes;
        }

        @Override
        protected Class<?> onClassNode(ClassWriter classWriter, ClassNode classNode) {            
            classBytes.put(classNode.getName() + ".class", classWriter.toByteArray());
            return super.onClassNode(classWriter, classNode);
        }
    }

}