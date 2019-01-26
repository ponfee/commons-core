package test.jce.ecc0;

import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.jce.implementation.ecc.ECCryptor;
import code.ponfee.commons.jce.implementation.ecc.EllipticCurve;

public class Screen extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    JTextArea ta;
    JLabel path, title;
    JTextField pathtf;
    FileDialog fd;
    JButton Load, Close, Next, fdl;
    JPanel p1, p2;
    String s, file, p;
    File targetFile;
    String filePath;

    public Screen() {
        try {
            setLayout(null);
            path = new JLabel("Path  :");
            title = new JLabel("Choose a text file for encryption");
            pathtf = new JTextField(15);
            pathtf.setFont(new Font("TimesNewRoman", Font.BOLD, 12));
            pathtf.setEditable(false);

            fdl = new JButton("Select File");
            fdl.setMnemonic('S');
            fdl.addActionListener(this);

            Load = new JButton("Load");
            Load.setMnemonic('L');
            Load.addActionListener(this);

            addc(path, 50, 40, 180, 25);
            addc(pathtf, 120, 40, 120, 25);
            addc(fdl, 300, 40, 100, 25);
            addc(Load, 50, 280, 90, 25);

            Next = new JButton("Next");
            Next.setMnemonic('N');
            Next.addActionListener(this);

            Close = new JButton("Close");
            Close.setMnemonic('C');
            Close.addActionListener(this);

            ta = new JTextArea();
            ta.setFont(new Font("Times New Roman", Font.BOLD, 12));
            ta.setEditable(false);
            ta.setText("<<Please select the file having the plain text, and click on Load. The text should appear here.>>");

            addc(new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), 50, 100, 350, 150);
            ta.setLineWrap(true);
            addc(Close, 190, 280, 90, 25);
            addc(Next, 300, 280, 90, 25);

            setTitle("Pick a text file for encrypting using ECC");
            setVisible(true);
            setSize(450, 400);
            setResizable(false);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        } catch (Exception e) {
            System.out.println("Exception Here" + e);
            System.exit(0);
        }
    }

    public void addc(JComponent c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
        add(c);
    }

    public void actionPerformed(ActionEvent a) {
        JButton b = (JButton) a.getSource();
        if (b == Close) System.exit(0);
        if (b == Next) {
            s = ta.getText();
            try {
                EllipticCurve ec = new EllipticCurve(ECParameters.secp112r1);
                View v = new View(600, 600, new ECCryptor(ec));//ch);
                v.filePath = this.filePath;
            } catch (Exception e) {
                System.out.println("EC Exception");
            }
        }
        if (b == fdl) {

            //JFileChooser x=new JFileChooser();
            File f = new File(".");
            String currdir = new String("hi");
            try {
                currdir = f.getCanonicalPath();
            } catch (Exception e) {
                System.out.println("\n Error getting the current dir");
            }

            JFileChooser chooser = new JFileChooser(currdir);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                targetFile = chooser.getSelectedFile();
                filePath = targetFile.getPath();
                pathtf.setText(filePath);
                System.out.println("\nPath is " + filePath);
            } else System.out.println("Error in opening File");
        }
        if (b == Load) {
            try {
                ta.setText("");
                FileInputStream fin = new FileInputStream(filePath);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                while (fin.available() > 0) {
                    bout.write(fin.read());
                }
                fin.close();
                s = bout.toString();
                ta.insert(s, 0);
                ta.setFont(new Font("TimesNewRoman", Font.BOLD, 12));
            } catch (Exception e) {
                System.out.println("Error at File loading :" + e);
            }
        }
    }
}
