/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.testweb.it;

import java.net.URI;
import java.util.Map;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Tests that the test application behaves as expected.
 * Generally, this application does things that are bad from a security
 * perspective, so that we can fix them adaptively.
 * @author dcowden
 */
public class ITTestWeb {
    
    
    
    @Test
    public void testRuntimeExec() throws Exception{
       Response resp = Request.Get("http://localhost:9000/radium-testweb/runtimeExec").execute();
       String resultText = resp.returnContent().toString();
       assertTrue(resultText.contains("SUCCESS"));
    }
    
    @Test
    public void testEchoServlet() throws Exception{
        
    }
    
}

