/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.web;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ReqTool {

    public static String getString(HttpServletRequest request, String name) {
        Object retObj = request.getParameter(name);
        if (retObj == null) {
            retObj = request.getAttribute(name);
            if (retObj == null) {
                return "";
            } else {
                return (String) retObj;
            }
        } else {
            return (String) retObj;
        }
    }
    public static String getString(HttpSession request, String name) {
      
          Object  retObj = request.getAttribute(name);
            if (retObj == null) {
                return "";
            } else {
                return (String) retObj;
            }
       
    }
    public static int getInt(HttpServletRequest request, String name) {
        Object retObj = request.getParameter(name);
        if (retObj == null) {
            retObj = request.getAttribute(name);
            if (retObj == null) {
                return 0;
            } else {
                return Integer.parseInt((String) retObj);
            }
        } else {
            return (Integer.parseInt((String) retObj));
        }
    }
      public static long getLong(HttpServletRequest request, String name) {
        Object retObj = request.getParameter(name);
        if (retObj == null) {
            retObj = request.getAttribute(name);
            if (retObj == null) {
                return 0;
            } else {
                return Long.parseLong((String) retObj);
            }
        } else {
            return (Long.parseLong((String) retObj));
        }
    }

    public static double getDouble(HttpServletRequest request, String name) {
        double result = 0.01d;
        Object retObj = request.getParameter(name);
        if (retObj == null) {
            retObj = request.getAttribute(name);
            if (retObj == null) {
                return 0.01d;
            } else {
                try {
                    result = Double.parseDouble((String) retObj);
                } catch (NumberFormatException ex) {
                    Logger.getLogger(ReqTool.class.getName()).log(Level.WARNING, ex.toString());
                }
                return result;
            }
        } else {
            try {
                result = Double.parseDouble((String) retObj);
            } catch (NumberFormatException ex) {
                Logger.getLogger(ReqTool.class.getName()).log(Level.WARNING, ex.toString());
            }
            return result;
        }
    }

}
