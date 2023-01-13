package toolkit.utilities;

import java.util.HashMap;

import configurations.Config;
import toolkit.windows.Toolkit;

public class EasterEgg {
    private static final HashMap<String, String> users = new HashMap<>() {{
        put("veryc", "VeryCoolMe's Modding Emporium");
        put("manoplay", null);
        put("elija", "Eli's Den of Cancer");
        put("dominick", "Undertale Piracy Tool");
        put("aidan", "Overture");
        put("joele", "Acrosnus Toolkit");
        put("etleg", "GregTool");
        put("eddie", "GregTool");
        put("madbrine", "farctool3");
        put("swous", "Abandoned by Disney");
    }};

    public static void initialize(Toolkit toolkit) {
        toolkit.debugMenu.setVisible(Config.instance.isDebug);
        
        String username = System.getProperty("user.name").toLowerCase();
        for (String user : users.keySet()) {
            if (user.equals(username)) {
                String title = users.get(user);
                if (title != null)
                    toolkit.setTitle(title);
                toolkit.debugMenu.setVisible(true);
                break;
            }
        }

        if (toolkit.debugMenu.isVisible())
            toolkit.setTitle(toolkit.getTitle() + " | Debug");
    }
}
