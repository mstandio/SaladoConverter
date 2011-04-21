package com.panozona.converter.settings;

import com.panozona.converter.utils.Messages;

public class OPTSettings {
    
    public static final String VALUE_RESIZE_PERCENT = "OPT_resizePercent";
    public static final String VALUE_MAX_TILE_SIZE = "OPT_maxTileSize";
    public static final String VALUE_MIN_TILE_SIZE = "OPT_minTileSize";
    private int resizePercent;
    private int resizePercentDefault = 15;
    private int maxTileSize;
    private int maxTileSizeDefault = 600;
    private int minTileSize;
    private int minTileSizeDefault = 400;


    public OPTSettings() {        
        resizePercent = resizePercentDefault;
        maxTileSize = maxTileSizeDefault;
        minTileSize = minTileSizeDefault;
    }    

    public void setResizePercent(int value) {
        if (value >= 0) {
            resizePercent = value;
        } else {
            throw new IllegalArgumentException(Messages.OPT_RESIZE_PERCENT_ERROR);
        }
    }

    public void setResizePercent(String value) {
        if (value != null) {
            try {
                setResizePercent(Integer.parseInt(value));
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.OPT_RESIZE_PERCENT_ERROR);
            }
        }
    }

    public int getResizePercent() {
        return resizePercent;
    }

    public int getDefaultResizePercent() {
        return resizePercentDefault;
    }

    public boolean resizePercentChanged() {
        return (resizePercent != resizePercentDefault);
    }

    public void setMaxTileSize(int value) {
        if (value > 0 && value >= minTileSize) {
            maxTileSize = value;
        } else {
            throw new IllegalArgumentException(Messages.OPT_MAX_TILE_SIZE_ERROR);
        }
    }

    public void setMaxTileSize(String value) {
        if (value != null) {
            try {
                setMaxTileSize(Integer.parseInt(value));
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.OPT_MAX_TILE_SIZE_ERROR);
            }
        }
    }

    public int getMaxTileSize() {
        return maxTileSize;
    }

    public int getDefaultMaxTileSize() {
        return maxTileSizeDefault;
    }

    public boolean maxTileSizeChanged() {
        return (maxTileSize != maxTileSizeDefault);
    }

    public void setMinTileSize(int value) {
        if (value > 0 && value <= maxTileSize) {
            minTileSize = value;
        } else {
            throw new IllegalArgumentException(Messages.OPT_MIN_TILE_SIZE_ERROR);
        }
    }

    public void setMinTileSize(String value) {
        if (value != null) {
            try {
                setMinTileSize(Integer.parseInt(value));
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.OPT_MIN_TILE_SIZE_ERROR);
            }
        }
    }

    public int getMinTileSize() {
        return minTileSize;
    }

    public int getDefaultMinTileSize() {
        return minTileSizeDefault;
    }

    public boolean minTileSizeChanged() {
        return (minTileSize != minTileSizeDefault);
    }
}
