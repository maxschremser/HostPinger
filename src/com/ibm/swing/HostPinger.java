package com.ibm.swing;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

public class HostPinger extends JFrame implements Runnable {
    JList list;
    HashMap<String, Integer> hashMap = new HashMap<String, Integer>();

    public HostPinger(String... hosts) throws HeadlessException {
        super("HostPinger");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(256, 384);
        setLocation(384, 256);
        setVisible(true);
        setResizable(true);

        initHosts(hosts);

        new Thread(this).start();
    }

    private void initHosts(String... hosts) {
        list = new JList(hosts);
        list.setOpaque(false);
        list.setFixedCellHeight(64);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(BorderFactory.createLineBorder(Color.black));
        list.setCellRenderer(new HostCellRenderer());
        list.setVisible(true);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(scrollPane);
        setPreferredSize(new Dimension(256, 300));
        pack();
    }

    @Override
    public void run() {
        while (true) {
            for (int i = 0; i < list.getModel().getSize(); i++) {
                String host = list.getModel().getElementAt(i).toString();
                // put waitIcon for startup
                hashMap.put(host, 2);
                list.repaint();
                try {
                    // try to connect to host on port 22 with a timeout of 2500 ms
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(host, 22), 2500);
                } catch (Exception e) {
                    // if timeout occurs or host is not reachable put errorIcon and repaint()
                    hashMap.put(host, 0);
                    list.repaint();
                    continue;
                }
                // host is reachable, put okIcon and repaint()
                hashMap.put(host, 1);
                list.repaint();
            }
        }
    }

    public static void main(String... args) {
        if (args.length <= 0) usage();
        new HostPinger(args);
    }

    private static void usage() {
        System.out.println("java com.ibm.swing.HostPinger host1 [host2] ... [host n]");
        System.exit(0);
    }

    private class HostCellRenderer extends JLabel implements ListCellRenderer {
        protected ImageIcon createImageIcon(String path,
                                            String description) {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL, description);
            } else {
                System.err.println("Couldn't find file: " + path);
                return null;
            }
        }

        final ImageIcon errorIcon = createImageIcon("error.png", "Host not reachable");                     // 0
        final ImageIcon okIcon = createImageIcon("check.png", "Connected to host on port 22");              // 1
        final ImageIcon waitIcon = createImageIcon("eye.png", "trying to connect to host on port 22");      // 2 || null

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String host = value.toString();
            setText(host);
            setIcon(hashMap.get(host) == null || hashMap.get(host) == 2 ? waitIcon : hashMap.get(host) == 1 ? okIcon : errorIcon);
            // display Icon on the right side of the label
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }
}
