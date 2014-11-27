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

/**
 * Echo out whatever is sent in. 
 * This is a major XSS violation
 * Intentionally written to older spec to be deployable in older containers
 * @author dcowden
 */

public class EchoServlet extends HttpServlet{
    public static final String INPUT_PARAM = "in";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();  
        out.println("SUCCESS");
        out.println(req.getParameter(INPUT_PARAM));  
    }    
    
}
