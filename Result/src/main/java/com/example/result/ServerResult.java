package com.example.result;

public class ServerResult<T> {

    private T data;
    private boolean success;
    private Exception error;
    public ServerResult(){}

    public ServerResult( T data){
        this.data = data;
        this.success = true;
    }
    public ServerResult( Exception error){
        this.error = error;
        this.success = false;
    }
    public ServerResult(T data , boolean success){
        this.data = data;
        this.success = success;
    }
    public void setData(T data){
        this.data = data;
        this.success = true;
    }

    public void setError(Exception e){
        this.error = e;
        this.success = false;
    }

    public boolean getResult(){ return this.success;}

    public T getData(){return this.data;}

    public Exception getError(){ return this.error;}
}

