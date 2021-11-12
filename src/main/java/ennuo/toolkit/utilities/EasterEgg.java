package ennuo.toolkit.utilities;

import ennuo.toolkit.windows.Toolkit;

public class EasterEgg {
    public static void initialize(Toolkit toolkit) {
        toolkit.debugMenu.setVisible(false);

        String username = System.getProperty("user.name").toLowerCase();

        if (username.equals("veryc")) {
            toolkit.debugMenu.setVisible(true);
            toolkit.setTitle("VeryCoolMe's Modding Emporium");
        }

        if (username.equals("elija")) {
            toolkit.setTitle("Eli's Den of Cancer");
            toolkit.debugMenu.setVisible(true);
        }

        if (username.equals("dominick")) {
            toolkit.setTitle("Undertale Piracy Tool");
            toolkit.debugMenu.setVisible(true);
        }

        if (username.equals("aidan")) {
            toolkit.setTitle("Overture");
            toolkit.debugMenu.setVisible(true);
        }

        if (username.equals("joele")) {
            toolkit.setTitle("Acrosnus Toolkit");
            toolkit.debugMenu.setVisible(true);
        }

        if (username.equals("etleg") || username.equals("eddie")) {
            toolkit.setTitle("GregTool");
            toolkit.debugMenu.setVisible(true);
        }

        if (toolkit.debugMenu.isVisible()) {
            toolkit.setTitle(toolkit.getTitle() + " | Debug");
            Flags.ENABLE_NEW_SAVEDATA = true;
        }
    }
}
