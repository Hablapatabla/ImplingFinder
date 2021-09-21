package com.hablapatabla.implingfinder.ui;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
class ImplingButton extends JButton {
    @Getter
    @Setter
    private boolean selected = false;

    @Getter
    @Setter
    private String name;

    ImplingButton(Image i, String name) {
        super.setContentAreaFilled(false);

        this.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selected = !selected;
            }
        });

        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setFocusPainted(false);
        this.setRequestFocusEnabled(false);
        this.name = name;
        this.setIcon(new ImageIcon(i));
        setBorder(new EtchedBorder());
    }

    @Override
    public void paint(Graphics g) {
        Color oldFg = getForeground();
        Color newFg = oldFg;
        ButtonModel mod = getModel();

        if (mod.isPressed()) {
            g.setColor(ColorScheme.DARK_GRAY_COLOR);
        } else if (mod.isRollover())
            g.setColor(ColorScheme.DARKER_GRAY_HOVER_COLOR);
        else {
            if (selected)
                g.setColor(ColorScheme.DARKER_GRAY_COLOR);
            else
                g.setColor(ColorScheme.DARK_GRAY_COLOR);
        }

        g.fillRect(0, 0, getWidth(), getHeight());
        setForeground(newFg);
        super.paintComponent(g);
        setForeground(oldFg);
    }
}
