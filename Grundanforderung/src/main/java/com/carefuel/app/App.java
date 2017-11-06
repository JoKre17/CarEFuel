<<<<<<< Upstream, based on origin/basicProject
=======

>>>>>>> 8127c5f parser implemented + fixedPath Algorithm
package com.carefuel.app;

import java.io.File;

import com.carefuel.util.Parser;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        File file = new File(System.getProperty("user.dir") + "/resource/Bertha Benz Memorial Route.csv");
        System.out.println(file.exists());
        
        Parser parser = new Parser(file);
        parser.parse();
    }
}
