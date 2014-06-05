package com.frozendogtears.multiplayer;

import java.util.HashMap;
import java.util.Map;

import com.frozendogtears.kryonet.NetworkPlayer;

public class NetworkData {
	public static Map<Integer,NetworkPlayer> players = new HashMap<Integer,NetworkPlayer>();
	public static HashMap<String, String> roomProperties = new HashMap<String, String>();
}
