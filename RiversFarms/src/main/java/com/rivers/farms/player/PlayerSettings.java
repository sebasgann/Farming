package com.rivers.farms.player;

import java.util.UUID;

/**
 * Per-player persisted settings.
 */
public class PlayerSettings {
    private final UUID uuid;
    private boolean tramplingEnabled;

    public PlayerSettings(UUID uuid, boolean tramplingEnabled) {
        this.uuid = uuid;
        this.tramplingEnabled = tramplingEnabled;
    }

    public UUID getUuid() { return uuid; }
    public boolean isTramplingEnabled() { return tramplingEnabled; }
    public void setTramplingEnabled(boolean tramplingEnabled) { this.tramplingEnabled = tramplingEnabled; }
}
