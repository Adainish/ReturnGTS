package io.github.adainish.returngts.cache;

import io.github.adainish.returngts.obj.player.GTSPlayer;

import java.util.HashMap;
import java.util.UUID;

public class PlayerCache
{
    public HashMap<UUID, GTSPlayer> playerCache = new HashMap<UUID, GTSPlayer>();
}
