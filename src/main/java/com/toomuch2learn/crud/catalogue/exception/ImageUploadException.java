package com.toomuch2learn.crud.catalogue.exception;

public class ImageUploadException extends Exception{

    private static final long serialVersionUID = 1L;

    private int code;

    public ImageUploadException(String message, Throwable e){
        super(message,e);
    }

    public ImageUploadException(int code, String message){
        super(message);
        this.code = code;
    }

    public ImageUploadException(int code, String message, Throwable e){
        super(message, e);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
