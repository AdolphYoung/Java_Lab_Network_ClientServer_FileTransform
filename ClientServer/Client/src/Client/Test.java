package Client;

import java.awt.*;
import java.io.*;

/**
 * Created by Adolph on 2016/12/15.
 */
public class Test {
    public static void main(String args[]) {
        EventQueue.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        GUI G=new GUI();
                    }
                }
        );
    }
}
