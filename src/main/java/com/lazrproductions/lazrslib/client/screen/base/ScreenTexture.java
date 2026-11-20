package com.lazrproductions.lazrslib.client.screen.base;

import net.minecraft.resources.Identifier;

public class ScreenTexture {
    private Identifier location;
    private float u, v;
    private int boundsX, boundsY;
    private int width, height;

    public ScreenTexture(Identifier location, float u, float v, int boundsX, int boundsY, int width, int height) {
        this.location = location;

        this.u = u;
        this.v = v;

        this.boundsX = boundsX;
        this.boundsY = boundsY;

        this.width = width;
        this.height = height;
    }

    public Identifier getResourceLocation() {
        return location;
    }

    public float getU() {
        return u;
    }

    public float getV() {
        return v;
    }

    public int getBoundsX() {
        return boundsX;
    }

    public int getBoundsY() {
        return boundsY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** Get the width to height ratio */
    public float getAspectRatio() {
        return getBoundsX() / (float) getBoundsY();
    }
}
