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
import org.apache.http.client.utils.URIBuilder;
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
    
    public static final String WEBAPP_BASE_URL = "http://localhost:9000/radium-testweb";
    
    @Test
    public void testRuntimeExecListsDirectory() throws Exception{
       Response resp = Request.Get(WEBAPP_BASE_URL + "/runtimeExec").execute();
       String resultText = resp.returnContent().toString();
       assertTrue(resultText.contains("SUCCESS"));
    }
    
    @Test
    public void testThatEchoServletReturnsUnsanitizedContent() throws Exception{
        String unsanitizedInput="<xml>Unsantized!</xml>";
        URI uri = new URIBuilder(WEBAPP_BASE_URL + "/echo").addParameter("in", unsanitizedInput).build();
        Response resp = Request.Get(uri).execute();
        String resultText = resp.returnContent().toString().trim();
        
        assertEquals(resultText, "SUCCESS\n" + unsanitizedInput);
        
    }
    
}

