package Server;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

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
