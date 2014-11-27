/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.triage.radium.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dcowden
 */
public class Timer {
    
    protected long startTime = -1;
    protected long endTime = -1;
    protected List<Long> marks = new ArrayList<Long>();
    protected List<String> names = new ArrayList<String>();
    public Timer(){
        
    }
    public void start(){
        startTime = System.currentTimeMillis();
    }
    public void mark(String name){
        if ( startTime < 0) start();
        names.add(name);
        marks.add(System.currentTimeMillis());
    }
    
    public void stop(){
        endTime = System.currentTimeMillis();
    }
    public long elapsed(){
        if ( endTime > 0 ){
            return endTime - startTime;
        }
        else{
            return System.currentTimeMillis() - startTime;
        }
    }
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("\nTimer:\n");
        String FORMAT = "%-25s  %d\n";
        sb.append(String.format(FORMAT,"start",0));
        for ( int i=0;i<marks.size();i++){
            sb.append(String.format(FORMAT, names.get(i),(marks.get(i)-startTime)));
        }
        sb.append(String.format(FORMAT,"Total",elapsed()));
        return sb.toString();
    }
}
