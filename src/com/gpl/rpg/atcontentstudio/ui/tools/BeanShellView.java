package com.gpl.rpg.atcontentstudio.ui.tools;

import bsh.EvalError;
import bsh.Interpreter;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.ui.WorkerDialog;
import com.jidesoft.swing.JideBoxLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class BeanShellView extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 8399265342746690313L;

    public BeanShellView() {
        super("ATCS BeanShell script pad");
        setIconImage(DefaultIcons.getMainIconImage());
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JPanel shPane = new JPanel();
        shPane.setLayout(new JideBoxLayout(shPane, JideBoxLayout.PAGE_AXIS));
        final RSyntaxTextArea shArea = new RSyntaxTextArea(30, 80);
        shArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        shPane.add(new JScrollPane(shArea), JideBoxLayout.VARY);

        JPanel shButtonPane = new JPanel();
        shButtonPane.setLayout(new JideBoxLayout(shButtonPane, JideBoxLayout.LINE_AXIS));
        shButtonPane.add(new JPanel(), JideBoxLayout.VARY);
        JButton run = new JButton("Run");
        shButtonPane.add(run, JideBoxLayout.FIX);
        shPane.add(shButtonPane, JideBoxLayout.FIX);
        splitter.setTopComponent(shPane);

        final RSyntaxTextArea outArea = new RSyntaxTextArea(20, 40);
        outArea.setEditable(false);
        JPanel outPane = new JPanel();
        outPane.setLayout(new JideBoxLayout(outPane, JideBoxLayout.PAGE_AXIS));
        JPanel outButtonPane = new JPanel();
        outButtonPane.setLayout(new JideBoxLayout(outButtonPane, JideBoxLayout.LINE_AXIS));
        outButtonPane.add(new JLabel("Output"), JideBoxLayout.FIX);
        outButtonPane.add(new JPanel(), JideBoxLayout.VARY);
        JButton outClear = new JButton("Clear");
        outButtonPane.add(outClear, JideBoxLayout.FIX);
        outPane.add(outButtonPane, JideBoxLayout.FIX);
        outPane.add(new JScrollPane(outArea), JideBoxLayout.VARY);

        final RSyntaxTextArea errArea = new RSyntaxTextArea(20, 40);
        errArea.setEditable(false);
        JPanel errPane = new JPanel();
        errPane.setLayout(new JideBoxLayout(errPane, JideBoxLayout.PAGE_AXIS));
        JPanel errButtonPane = new JPanel();
        errButtonPane.setLayout(new JideBoxLayout(errButtonPane, JideBoxLayout.LINE_AXIS));
        errButtonPane.add(new JLabel("Errors"), JideBoxLayout.FIX);
        errButtonPane.add(new JPanel(), JideBoxLayout.VARY);
        JButton errClear = new JButton("Clear");
        errButtonPane.add(errClear, JideBoxLayout.FIX);
        errPane.add(errButtonPane, JideBoxLayout.FIX);
        errPane.add(new JScrollPane(errArea), JideBoxLayout.VARY);


        JSplitPane errOut = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        errOut.setLeftComponent(outPane);
        errOut.setRightComponent(errPane);
        splitter.setBottomComponent(errOut);

        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Interpreter shInt = new Interpreter();
                final PrintStream printOut = new PrintStream(new AreaOutputStream(outArea));
                shInt.setOut(printOut);
                final PrintStream printErr = new PrintStream(new AreaOutputStream(errArea));
                shInt.setErr(printErr);

                WorkerDialog.showTaskMessage("Running your script...", null, new Runnable() {
                    @Override
                    public void run() {

                        try {
                            shInt.eval(shArea.getText());
                        } catch (EvalError e1) {
                            e1.printStackTrace(printErr);
                        }
                        printOut.flush();
                        printErr.flush();
                        printOut.close();
                        printErr.close();

                    }
                });

            }
        });

        outClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outArea.setText("");
            }
        });

        errClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                errArea.setText("");
            }
        });


        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(splitter, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    public static class AreaOutputStream extends OutputStream {

        private JTextArea textArea;

        public AreaOutputStream(JTextArea area) {
            this.textArea = area;
        }

        @Override
        public void write(int b) throws IOException {
            textArea.append(String.valueOf((char) b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }

    }

}
