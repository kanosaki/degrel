package degrel.visualize;

import degrel.core.Element;
import degrel.visualize.view.GraphView;
import degrel.visualize.viewmodel.SimpleGraphPresenterViewModel;
import degrel.visualize.viewmodel.graphdrawer.DynamicsGraphDrawer;

import javax.swing.*;
import java.awt.event.*;

public class SimpleGraphPresenter extends JDialog {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JPanel mainPanel;
    private SimpleGraphPresenterViewModel viewmodel;

    public SimpleGraphPresenter() {
        viewmodel = new SimpleGraphPresenterViewModel();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void pushElement(Element elem) {
        viewmodel.pushElement(elem);
        this.redrawViewModel();
    }

    protected void redrawViewModel() {

    }
}
