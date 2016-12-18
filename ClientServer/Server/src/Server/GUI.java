/**
 * Created by Adolph on 2016/12/10.
 */
package Server;

import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

class GUI extends JFrame implements ActionListener {
    private JLabel labelRemain;
    private JLabel labelSpeed;
    private JLabel labelProgress;
    private JFrame frame;
    private JTextArea clientList;
    private JProgressBar progressBar;

    private File serverSaveDir = new File("C:\\Users\\Administrator\\Desktop\\SeverSaver");
    private long sizeOfAllFiles = 0;
    private long currentSizeOfAllFiles = 0;
    private long startTime = 0;
    private int speed = 0;
    private long endTime = 0;
    private long progress = 0;

    private class fileReceiver extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            ServerSocket server = new ServerSocket(2345);
            while (true) {
                Socket client = server.accept();
                Thread t = new Thread(new threadHandler(client));
                t.start();
            }
        }
    }

    private class threadHandler implements Runnable {
        Socket client;
        String command;//客户端发来的协议"文件数#文件名#文件大小#文件名……"
        String[] commandAnalysis;
        int fileNum;
        String[] fileName;
        long[] fileSize;

        threadHandler(Socket socket) {
            client = socket;
            clientList.append("Accepted: " + client.getInetAddress() + "\r\n");
        }

        void calProgress() {
            progress = (long) ((double) (currentSizeOfAllFiles / sizeOfAllFiles * 100));
            progressBar.setValue((int) progress);
        }

        void calSpeed(long size) {
            long time = (endTime - startTime) / 1000;//(s)
            if (time == 0) {
                speed = (int) size;
            } else {
                speed = (int) (size / time);//(Byte/s)
            }

            labelSpeed.setText("Speed : " + speed + " (Byte/s)");
        }

        void calRemainTime() {
            int remainTime = (int) ((sizeOfAllFiles - currentSizeOfAllFiles) / speed);
            labelRemain.setText("Remaining Time : " + remainTime + " (s)");
        }

        @Override
        public void run() {
            try {
                //解析命令，读取文件数目，文件名，文件大小
                InputStreamReader inReader = new InputStreamReader(client.getInputStream());
                BufferedReader bf = new BufferedReader(inReader);
                command = bf.readLine();
                commandAnalysis = command.split("#");
                int i, j, k;
                for (i = 0, j = 0, k = 0; i < commandAnalysis.length; i++) {
                    if (i == 0) {
                        fileNum = Integer.valueOf(commandAnalysis[i]);
                        fileName = new String[fileNum];
                        fileSize = new long[fileNum];
                    } else if (i % 2 == 1) {
                        fileName[j] = commandAnalysis[i];
                        j++;
                    } else if (i % 2 == 0) {
                        fileSize[k] = Long.valueOf(commandAnalysis[i]);
                        k++;
                    }
                }
                //接收文件
                for (i = 0; i < fileNum; i++) {
                    File file = new File(serverSaveDir + File.separator + fileName[i]);
                    sizeOfAllFiles += fileSize[i];
                }

                InputStream in = client.getInputStream();
                int index = 0;
                int sizeReceive = (int) fileSize[index];
                byte[] buff = new byte[sizeReceive];
                while ((sizeReceive = in.read(buff)) != -1) {
//                    System.out.println(new String(buff));
                    File file = new File(serverSaveDir + File.separator + fileName[index]);
                    FileOutputStream fos = new FileOutputStream(file);
                    startTime = System.currentTimeMillis();
                    fos.write(buff, 0, sizeReceive);
                    endTime = System.currentTimeMillis();
                    currentSizeOfAllFiles += sizeReceive;
                    calProgress();
                    calSpeed(sizeReceive);
                    calRemainTime();
                    if (index + 1 < fileNum) {
                        index++;
                        sizeReceive = (int) fileSize[index];
                        buff = new byte[sizeReceive];
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    GUI() {
        //设置框架
        frame = new JFrame();
        frame.setTitle("My Server");
        frame.setBounds(200, 300, 700, 500);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setPreferredSize(new Dimension(700, 480));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        /*----------------------------------------------------------------*/
        //设置内容区
        Container pane = frame.getContentPane();
        pane.setLayout(null);
        /*----------------------------------------------------------------*/
        //设置标签
        JLabel labelList = new JLabel("Client List :");
        labelList.setFont(new Font("宋体", Font.PLAIN, 20));
        labelList.setBounds(40, 20, 180, 20);

        labelProgress = new JLabel("Progress :");
        labelProgress.setFont(new Font("宋体", Font.PLAIN, 20));
        labelProgress.setBounds(40, 300, 180, 20);

        labelSpeed = new JLabel("Speed : 0 (Byte/s)");
        labelSpeed.setFont(new Font("宋体", Font.PLAIN, 20));
        labelSpeed.setBounds(40, 340, 300, 20);

        labelRemain = new JLabel("Remaining Time : 0 (s)");
        labelRemain.setFont(new Font("宋体", Font.PLAIN, 20));
        labelRemain.setBounds(40, 380, 300, 20);
        /*----------------------------------------------------------------*/
        //设置用户列表区
        clientList = new JTextArea();
        JScrollPane clientListScroll = new JScrollPane(clientList);
        clientList.setEditable(false);
        clientList.setBackground(Color.DARK_GRAY);
        clientList.setForeground(Color.LIGHT_GRAY);
        clientList.setFont(new Font("宋体", Font.PLAIN, 18));
        clientListScroll.setBounds(40, 60, 600, 220);
        /*----------------------------------------------------------------*/
        //设置进度条
        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(160, 300, 480, 20);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        /*----------------------------------------------------------------*/
        //添加组件
        pane.add(labelList);
        pane.add(labelProgress);
        pane.add(labelSpeed);
        pane.add(labelRemain);
        pane.add(progressBar);
        pane.add(clientListScroll);
        fileReceiver Task = new fileReceiver();
        Task.execute();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}