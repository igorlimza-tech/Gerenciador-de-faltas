package executavel.faltas;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import view.faltas.TelaLogin;

public class Main {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            TelaLogin login = new TelaLogin();
            login.setVisible(true);
        });
    }
}
