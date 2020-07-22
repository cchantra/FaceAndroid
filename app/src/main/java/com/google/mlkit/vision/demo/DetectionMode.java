package com.google.mlkit.vision.demo;

public  class DetectionMode {
    int mode;
    public void DetectionMode()
    {

        mode = Constant.AGE_OPTION;

    }
    public void setMode (int newmode)
    {
        mode = newmode;
    }
    public int getMode()
    {
        return mode;
    }

}
