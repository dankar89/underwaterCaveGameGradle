package com.frozendogtears.cavegame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.frozendogtears.cavegame.CaveGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "CaveGame";
		cfg.fullscreen = true;
//		cfg.width = 1024;
//		cfg.height = 768;
		cfg.width = 1920;
		cfg.height = 1080;
		new LwjglApplication(new CaveGame(), cfg);
	}
}
