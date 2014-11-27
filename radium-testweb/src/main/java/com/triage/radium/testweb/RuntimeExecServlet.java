/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.testweb;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.SystemUtils;

/**
 * A Servlet that deliberately does vulnerable, bad things, that we'd like to fix
 * Intentionally written to older spec to be deployable in older containers
 * @author dcowden
 */
public class RuntimeExecServlet extends HttpServlet{

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        String listCommand = "ls";
        PrintWriter out = resp.getWriter();  
        
        if ( SystemUtils.IS_OS_WINDOWS ){
            listCommand = "dir";
        }
        try{
            String output = new ExecCommand().executeCommand(listCommand);
            out.println("SUCCESS");
            out.println(output);                      
        }
        catch (Exception e){
            out.println("ERROR");
            e.printStackTrace(out);
        }        
        
    }
    
}
