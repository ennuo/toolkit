/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ennuo.craftworld.swing;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.ResourcePtr;

public class SearchParameters {
   public String path;
   public ResourcePtr pointer;
   
   public SearchParameters(String query) {
       path = query.toLowerCase();
       if (query.startsWith("res:")) {
           pointer = new ResourcePtr();
           String res = query.substring(4);
           if (res.startsWith("g"))
               pointer.GUID = getLong(res);
           else if (res.startsWith("h"))
               pointer.hash = Bytes.toBytes(res.substring(1).replaceAll("\\s", ""));
       }
   }
   
   private long getLong(String number) {
        long integer;
        try {
            if (number.toLowerCase().startsWith("0x"))
                integer = Long.parseLong(number.substring(2), 16);
            else if (number.startsWith("g"))
                integer = Long.parseLong(number.substring(1));
            else
                integer = Long.parseLong(number);
            return integer;
        } catch (NumberFormatException e) { return -1; }
   }
   
   
   
   
}
