/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.display.dialog;

import com.xl.main.Global;
import com.xl.main.RedApplication;
import com.xl.utils.FontManager;
import com.xl.utils.namemanager.MenuUtils;
import com.xl.utils.ui.OptionDialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The Class CrashReporter is the dialog which appears when an unexpected exception is encountered. It can generate a stack trace and submit it back to the
 * issue in our home page to fix.
 */
public class CrashReporter extends JDialog implements ActionListener {

    /**
     * Instantiates a new crash reporter.
     *
     * @param e the e
     */
    public CrashReporter(Throwable e) {
        super(RedApplication.getInstance(), "Oops - Crash Reporter");

        e.printStackTrace();

        if (e instanceof OutOfMemoryError) {
            // Don't issue a normal crash report but tell them that they ran out of memory.
            OptionDialogUtils.showErrorDialog(RedApplication.getInstance(), "<html>You ran out of memory!<br><br>Please look at Help &gt; Help Contents &gt; " +
                    "Configuration to see how to fix this");
            return;
        }

        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 200);
        setLocationRelativeTo(RedApplication.getInstance());

        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        gbc.gridheight = 4;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        ImageIcon sad = new ImageIcon(ClassLoader.getSystemResource("resources/cry.png"));

        getContentPane().add(new JLabel(sad), gbc);
        gbc.weightx = 0.2;
        gbc.weighty = 0.5;

        gbc.weightx = 0.8;
        gbc.gridheight = 1;
        gbc.gridx = 1;

        JLabel genericMessage = new JLabel("RED encountered a problem.", JLabel.CENTER);

        getContentPane().add(genericMessage, gbc);
        gbc.gridy++;

        JLabel sorryMessage = new JLabel("Sorry about that.  The error was: ", JLabel.CENTER);

        getContentPane().add(sorryMessage, gbc);
        gbc.gridy++;

        JLabel errorClass = new JLabel(e.getClass().getName(), JLabel.CENTER);
        errorClass.setFont(FontManager.REPORT_FONT);
        errorClass.setForeground(Color.RED);
        getContentPane().add(errorClass, gbc);

        gbc.gridy++;
        JLabel errorMessage = new JLabel(e.getLocalizedMessage(), JLabel.CENTER);
        errorMessage.setFont(FontManager.REPORT_FONT);
        errorMessage.setForeground(Color.RED);

        getContentPane().add(errorMessage, gbc);

        JPanel buttonPanel = new JPanel();

        JButton closeButton = new JButton(MenuUtils.IGNORE_BUTTON);
        closeButton.setActionCommand(MenuUtils.IGNORE_BUTTON);
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        JButton findIssues = new JButton(MenuUtils.FIND_ISSUES_BUTTON);
        findIssues.setActionCommand(MenuUtils.FIND_ISSUES_BUTTON);
        findIssues.addActionListener(this);
        buttonPanel.add(findIssues);

        JButton sendButton = new JButton(MenuUtils.REPORT_ERROR_BUTTON);
        sendButton.setActionCommand(MenuUtils.REPORT_ERROR_BUTTON);
        sendButton.addActionListener(this);
        buttonPanel.add(sendButton);

        gbc.gridy++;
        gbc.gridwidth = 2;
        getContentPane().add(buttonPanel, gbc);

        setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        try {
            if (ae.getActionCommand().equals(MenuUtils.IGNORE_BUTTON)) {
                setVisible(false);
                dispose();
            } else if (ae.getActionCommand().equals(MenuUtils.FIND_ISSUES_BUTTON)) {
                Desktop.getDesktop().browse(new URI(Global.ISSUES_PAGE));
            } else if (ae.getActionCommand().equals(MenuUtils.REPORT_ERROR_BUTTON)) {
                Desktop.getDesktop().browse(new URI(Global.NEW_ISSUE_PAGE));

            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
    }
}
