/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.agent;

import java.lang.instrument.Instrumentation;

/**
 *
 * @author dcowden
 */
public class RadiumAgent {

    public static void premain(String args, Instrumentation inst) throws Exception {
        inst.addTransformer(new GroovyShardTransformer() );
    }
    
    public static void agentmain(String args, Instrumentation inst) throws Exception
    {
        premain(args, inst);
    }    
    
}
