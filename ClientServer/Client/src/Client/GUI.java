package Client;

/**
 * Created by Adolph on 2016/12/10.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

class GUI extends JFrame implements ActionListener {
    private JFrame frame;
    private JTextField fieldChoose;
    private JLabel labelSpeed;
    private JLabel labelRemain;
    private JProgressBar progressBar;
    private JTextArea fileList;
    private File fileChosen;

    private fileSender Task;
    private ArrayList<File> allFile = new ArrayList<>();
    private long sizeOfFiles = 0;
    private long currentSizeOfFiles = 0;
    private long startTime = 0;
    private int speed = 0;
    private long endTime = 0;

    private class fileSender extends SwingWorker<Void, Void> {
        Socket client;

        void calProgress() {
//            System.out.println("curr"+currentSizeOfFiles+"--"+"all"+sizeOfFiles);
            long progress = (long) (((double) currentSizeOfFiles / sizeOfFiles) * 100);
//            System.out.println(progress);
            progressBar.setValue((int) progress);
        }

        void calSpeed(long size) {
//            System.out.println("start calSpeed");
            long time = (endTime - startTime) / 1000;//(s)
//            System.out.println("start:" + startTime + "--end:" + endTime + "--time is: " + time);
            if (time == 0) {
                speed = (int) size;
            } else {
                speed = (int) (size / time);//(Byte/s)
            }
//            System.out.println("speed is: " + speed);
            labelSpeed.setText("Speed : " + speed + " (Byte/s)");
//            System.out.println("already calSpeed");
        }

        void calRemainTime() {
            int remainTime = (int) ((sizeOfFiles - currentSizeOfFiles) / speed);
            labelRemain.setText("Remaining Time : " + remainTime + " (s)");
        }


        @Override
        protected Void doInBackground() throws Exception {
            StringBuffer command = new StringBuffer();//协议,文件数#文件名#文件大小#文件名#文件大小...
            command.append(allFile.size());
//            System.out.println(allFile.size()+" "+command);
            //文件合并为一个文件
            File mergerFile = new File(allFile.get(0).getParent() + File.separator + "mergerFile.txt");
            Vector<FileInputStream> v = new Vector<>();
            for (File file : allFile) {
                command.append("#" + file.getName() + "#" + file.length());
//                System.out.println(command);
                v.add(new FileInputStream(file));
            }
            Enumeration<FileInputStream> en = v.elements();
            SequenceInputStream sis = new SequenceInputStream(en);
            FileOutputStream fos = new FileOutputStream(mergerFile);
            int len = (int) sizeOfFiles;
            byte[] buff = new byte[len];
            while ((len = sis.read(buff)) != -1) {
//                System.out.println(new String(buff));
                fos.write(buff, 0, len);
            }
//            System.out.println("already merger");
            //新客户端
            client = new Socket(InetAddress.getLocalHost(), 2345);
            FileInputStream fis = new FileInputStream(mergerFile);
            PrintStream ps = new PrintStream(client.getOutputStream());
            //发送协议
            ps.println(command);
            ps.flush();
            //等服务器预处理完成，及保证网络延迟
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //进行文件传输
            OutputStream out = client.getOutputStream();
            int index = 0;
            int sizeSend = (int) allFile.get(index).length();//发送的数据大小
            long sizeSent = 0;//已发送数据大小
            byte[] buffer = new byte[sizeSend];
            while ((sizeSend = fis.read(buffer)) != -1) {
//                System.out.println(new String(buffer));
                currentSizeOfFiles += sizeSend;
                startTime = System.currentTimeMillis();
                out.write(buffer, 0, sizeSend);
                endTime = System.currentTimeMillis();
                calProgress();
                calSpeed((sizeSent += sizeSend));
                calRemainTime();
                out.flush();
                if (index + 1 < allFile.size()) {
                    index++;
                    sizeSend = (int) allFile.get(index).length();
                    buffer = new byte[sizeSend];
                }
            }
            return null;
        }

        @Override
        public void done() {
            fileList.append("All Files have already been sent.\r\n");
            allFile = new ArrayList<>();
            sizeOfFiles = 0;
            currentSizeOfFiles = 0;
            startTime = 0;
            speed = 0;
            endTime = 0;
            labelRemain.setText("Remaining Time : 0 (s)");
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    GUI() {
        //设置框架
        frame = new JFrame();
        frame.setTitle("My Client");
        frame.setBounds(1100, 300, 740, 480);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setPreferredSize(new Dimension(740, 480));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        /*----------------------------------------------------------------*/
        //设置内容区
        Container pane = frame.getContentPane();
        pane.setLayout(null);
        /*----------------------------------------------------------------*/
        //设置标签
        JLabel labelChoose = new JLabel("Choose File :");
        labelChoose.setFont(new Font("宋体", Font.PLAIN, 20));
        labelChoose.setBounds(40, 20, 180, 20);

        JLabel labelFileList = new JLabel("File List :");
        labelFileList.setFont(new Font("宋体", Font.PLAIN, 20));
        labelFileList.setBounds(40, 60, 300, 20);

        JLabel labelProgress = new JLabel("Progress :");
        labelProgress.setFont(new Font("宋体", Font.PLAIN, 20));
        labelProgress.setBounds(40, 300, 180, 20);

        labelSpeed = new JLabel("Speed : 0 (Byte/s)");
        labelSpeed.setFont(new Font("宋体", Font.PLAIN, 20));
        labelSpeed.setBounds(40, 340, 300, 20);

        labelRemain = new JLabel("Remaining Time : 0 (s)");
        labelRemain.setFont(new Font("宋体", Font.PLAIN, 20));
        labelRemain.setBounds(40, 380, 300, 20);
        /*----------------------------------------------------------------*/
        //设置文本框
        fieldChoose = new JTextField();
        fieldChoose.setText(null);
        fieldChoose.setBounds(200, 20, 300, 20);
        /*----------------------------------------------------------------*/
        //设置按钮
        JButton buttonBrowse = new JButton("···");
        buttonBrowse.setBounds(510, 20, 30, 20);
        buttonBrowse.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int i = fileChooser.showOpenDialog(frame);
            if (i == JFileChooser.APPROVE_OPTION) {
                fileChosen = fileChooser.getSelectedFile();
                fieldChoose.setText(fileChosen.getAbsolutePath());
            }
        });

        JButton buttonSend = new JButton("Send");
        buttonSend.setBounds(630, 20, 70, 20);
        buttonSend.addActionListener(e -> {
            fileList.setText("");
            if (allFile.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No File chosen.");
            } else {
                Task = new fileSender();
                Task.execute();
            }
        });

        JButton buttonAdd = new JButton("Add");
        buttonAdd.setBounds(550, 20, 70, 20);
        buttonAdd.addActionListener(e -> {
            //TODO:send action
            boolean existFlag = true;//判断文件是否已加入列表
            if (fileChosen != null) {
                for (File file : allFile) {
                    if (fileChosen.getAbsolutePath().equals(file.getAbsolutePath())) {
                        existFlag = false;
                    }
                }
                if (existFlag) {
                    allFile.add(new File(fileChosen.getAbsolutePath()));
                    fileList.append(fileChosen.getName() + "( " + fileChosen.getPath() + " )\r\n");
                    sizeOfFiles += fileChosen.length();
                } else {
                    JOptionPane.showMessageDialog(null, "File is already exist.", "Message", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "No file is chosen.", "Message", JOptionPane.INFORMATION_MESSAGE);
            }
            fileChosen = null;
            fieldChoose.setText("");
        });

        /*----------------------------------------------------------------*/
        //设置文件显示区
        fileList = new JTextArea();
        JScrollPane fileListScroll = new JScrollPane(fileList);
        fileList.setEditable(false);
        fileList.setBackground(Color.DARK_GRAY);
        fileList.setForeground(Color.LIGHT_GRAY);
        fileList.setFont(new Font("宋体", Font.PLAIN, 18));
        fileListScroll.setBounds(40, 90, 660, 200);
        /*----------------------------------------------------------------*/
        //设置进度条
        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(160, 300, 538, 20);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        /*----------------------------------------------------------------*/
        //添加组件
        pane.add(labelChoose);
        pane.add(labelFileList);
        pane.add(labelProgress);
        pane.add(labelSpeed);
        pane.add(labelRemain);
        pane.add(fieldChoose);
        pane.add(buttonBrowse);
        pane.add(buttonAdd);
        pane.add(buttonSend);
        pane.add(fileListScroll);
        pane.add(progressBar);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

