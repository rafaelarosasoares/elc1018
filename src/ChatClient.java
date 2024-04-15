
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import java.awt.*;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatClient {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextPane messageArea = new JTextPane();

    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;

        textField.setEditable(false);
        messageArea.setEditable(false);


        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        // Send on enter then clear to prepare for next message
        textField.addActionListener(e -> {
            out.println(textField.getText());
            textField.setText("");
        });
    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }
    private void run() throws IOException {
        try {
            try (Socket socket = new Socket(serverAddress, 59001)) {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
            }
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                } else if (line.startsWith("NAMEACCEPTED")) {
                    String name = line.substring(13);
                    this.frame.setTitle("Chatter - " + name);
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    String message = line.substring(8);
                    int separatorIdx = message.indexOf(":");
                    String name = message.substring(0, separatorIdx).trim();
                    String msg = message.substring(separatorIdx + 1).trim();
                    // Append colored name and message to the text area
                    appendColoredText(messageArea, name + ": ", Color.BLUE);
                    appendColoredText(messageArea, msg + "\n", Color.BLACK);
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    private void appendColoredText(JTextPane pane, String text, Color color) {
        StyledDocument doc = pane.getStyledDocument();
        Style style = pane.addStyle("Color Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }
        ChatClient client = new ChatClient(args[0]);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}