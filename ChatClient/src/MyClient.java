import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class MyClient extends JFrame {
    Socket      _socket;
    JTextArea   _textArea;
    JTextField  _textField;
    JScrollPane _scrollPane;

    public MyClient() {
        super("Chat Client");

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                try {
                    InetAddress host = InetAddress.getLocalHost();
                    sendText(host.getHostAddress() + " Left.");

                    _socket.close();
                }
                catch (IOException ex) {
                    System.err.println("Close failed. " + ex);
                }
                System.exit(0);
            }
        });

        setLayout(new BorderLayout());

        _textArea = new JTextArea(20, 40);
        _textArea.setLineWrap(true);
        _textArea.setEditable(false);

        _scrollPane = new JScrollPane(_textArea);
        add(_scrollPane, BorderLayout.CENTER);

        _textField = new JTextField(30);
        _textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                sendText(_textField.getText());

                _textField.setText("");
            }
        });

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                sendText(_textField.getText());

                _textField.setText("");
            }
        });

        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.add(_textField, BorderLayout.CENTER);
        entryPanel.add(sendButton, BorderLayout.EAST);
        add(entryPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);

        try {
            InetAddress host    = InetAddress.getLocalHost();
            String      address = host.getHostAddress();
            _textArea.append("Connecting to: " + address + "\n");
            _socket = new Socket(host, 7777);
            _textArea.append("Connected\n");

            sendText(address + " Joined.");

            while (!_socket.isClosed()) {
                try {
                    InputStream input     = _socket.getInputStream();
                    int         available = input.available();
                    if (available > 0) {
                        byte[] bytes = new byte[available];
                        input.read(bytes);
                       
                        String undecryptedMessage = new String(bytes);
                        String decryptedMessage = decrypt(undecryptedMessage);
                        _textArea.append(decryptedMessage + "\n");

                        JScrollBar scroll = _scrollPane.getVerticalScrollBar();
                        scroll.setValue(scroll.getMaximum());
                    }

                    Thread.sleep(100);
                }
                catch (IOException ex) {
                    System.err.println("Listen failed " + ex);
                }
                catch (InterruptedException ex) {
                    System.err.println("Sleep interrupted " + ex);
                }
            }
        }
        catch (IOException ex) {
            System.err.println("Connection failed. " + ex);
            System.exit(1);
        }
    }

    private void sendText(String s) {
        try {
        	String encryptedMessage = encrypt(s);
            OutputStream output = _socket.getOutputStream();
            output.write(new String(encryptedMessage).getBytes());
            output.flush();
        }
        catch (IOException ex) {
            System.err.println("Send error: " + ex);
        }
    }
    
    static int[][] enMatrix = {{4,3},
            {5,4}};

    static int[][] deMatrix = {{4,-3},
            {-5,4}};
    
    public String transform(char c1, char c2, int[][] inputMatrix)
    {
        int intC1 = c1;
        int intC2 = c2;
        
        //System.out.println("ASCII Value : " + intC1 + " " + intC2);
        
        int multiply1 = intC1*inputMatrix[0][0] + intC2*inputMatrix[0][1];
        int multiply2 = intC1*inputMatrix[1][0] + intC2*inputMatrix[1][1];
        
        int mod1 = multiply1 % 128;
        int mod2 = multiply2 % 128;
        
        while(mod1 < 0)
        {
            mod1 += 128;
        }
            
        while(mod2 < 0)
        {
            mod2 += 128;
        }
        
        char outputChar1 = (char) mod1;
        char outputChar2 = (char) mod2;
        
        return "" + outputChar1 + outputChar2;
    }
    
    public String encrypt(String regularMessage)
    {
    	String encryptedMessage = "";
    	if(regularMessage.length()%2 == 1)
    	{
    		regularMessage = regularMessage + " ";
    	}
    	
    	for(int i = 0; i < regularMessage.length(); i+=2)
    	{
    		encryptedMessage += transform(regularMessage.charAt(i), regularMessage.charAt(i+1), enMatrix);
    	}
    	
    	return encryptedMessage;
    }
    
    public String decrypt(String encryptedMessage)
    {
    	String decryptedMessage = "";
    	
    	for(int i = 0; i < encryptedMessage.length(); i+=2)
    	{
    		decryptedMessage += transform(encryptedMessage.charAt(i), encryptedMessage.charAt(i+1), deMatrix);
    	}
    	
    	return decryptedMessage;
    	
    }
    

    public static void main(String[] args) {
        new MyClient();
    }
}
