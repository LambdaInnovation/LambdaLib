/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.core;

public enum LoadStage {
    
    PRE_INIT("PreInit"),
    INIT("Init"),
    POST_INIT("PostInit"),
    START_SERVER("StartServer");
    
    public final String name;
    
    private LoadStage(String name) {
        this.name = name;
    }
    
}
