package com.hablapatabla.implingfinder;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

/**
 * A component to display an error/info message (to be used on a plugin panel)
 * Example uses are: no ge search results found, no ge offers found.
 */
public class ImplingFinderSplashPanel extends JPanel
{
    private final JLabel noResultsTitle = new JShadowedLabel();
    private final JLabel noResultsDescription = new JShadowedLabel();

    public ImplingFinderSplashPanel()
    {
        setOpaque(false);
        setBorder(new EmptyBorder(50, 10, 0, 10));
        setLayout(new BorderLayout());

        noResultsTitle.setForeground(Color.WHITE);
        noResultsTitle.setHorizontalAlignment(SwingConstants.CENTER);

        noResultsDescription.setFont(FontManager.getRunescapeFont());
        noResultsDescription.setForeground(Color.GRAY);
        noResultsDescription.setHorizontalAlignment(SwingConstants.CENTER);

        add(noResultsTitle, BorderLayout.NORTH);
        add(noResultsDescription, BorderLayout.CENTER);

        setVisible(false);
    }

    /**
     * Changes the content of the panel to the given parameters.
     * The description has to be wrapped in html so that its text can be wrapped.
     */
    public void setContent(String title, String description)
    {
        noResultsTitle.setText(title);
        noResultsDescription.setText("<html><body style = 'text-align:center'>" + description + "</body></html>");
        setVisible(true);
    }
}