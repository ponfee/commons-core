package test.jce.ecc0;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Login extends JFrame implements ActionListener {
    private static final long serialVersionUID = -8266964331082261609L;
    JTextField untf;
    JPasswordField pwpf;
    JLabel unl, pwl;
    JButton Next, Close;

    public Login() {
        try {
            setLayout(null);
            unl = new JLabel("User Name : ");
            untf = new JTextField(15);
            untf.setFont(new Font("Times New Roman", Font.BOLD, 12));

            addc(unl, 50, 40, 100, 25);
            addc(untf, 120, 40, 150, 25);

            pwl = new JLabel("PassWord : ");
            pwpf = new JPasswordField(15);
            pwpf.setEchoChar('*');
            pwpf.setFont(new Font("Times New Roman", Font.BOLD, 12));

            addc(pwl, 50, 80, 100, 25);
            addc(pwpf, 120, 80, 150, 25);

            Next = new JButton("Next");
            Next.setMnemonic('N');
            Next.addActionListener(this);

            addc(Next, 90, 140, 70, 20);

            Close = new JButton("Close");
            Close.setMnemonic('C');
            Close.addActionListener(this);

            addc(Close, 180, 140, 70, 20);

            setTitle("Login Screen");
            setVisible(true);
            setSize(300, 220);
            setResizable(false);
            setDefaultCloseOperation(EXIT_ON_CLOSE);

        } catch (Exception e) {
            System.out.println("Exception in Constructor:" + e);
            System.exit(0);
        }
    }

    public void addc(JComponent c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
        add(c);
    }

    public void actionPerformed(ActionEvent ae) {
        JButton b = (JButton) ae.getSource();
        if (b == Close) {
            System.exit(0);
        }
        if (b != Next) {
            return;
        }
        new Screen();
        dispose();
    }
}
