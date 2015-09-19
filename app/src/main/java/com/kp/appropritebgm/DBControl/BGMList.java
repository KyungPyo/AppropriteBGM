package com.kp.appropritebgm.DBControl;

/**
 * Created by KP on 2015-08-20.
 */
public class BGMList {
    int id;
    String name = null;
    String path = null;
    int innerFileCode;
    int categoryId;

    public int getId(){ return id; }
    public String getName(){ return name; }
    public String getPath(){ return path; }
    public int getInnerFileCode(){ return innerFileCode; }
    public int getCategoryId(){ return categoryId; }
}
