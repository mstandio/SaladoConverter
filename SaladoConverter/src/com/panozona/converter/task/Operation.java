package com.panozona.converter.task;

/**
 *
 * @author Marek Standio
 */
public class Operation {

    public static final String TYPE_DZT = "type_DZT"; // TODO: enum, perhaps ? 
    public static final String TYPE_EC = "type_EC";
    public static final String TYPE_RES = "type_RES";
    public static final String TYPE_DEL = "type_DEL";
    public String type;
    public String args[];

    public Operation() {
    }

    public Operation(String operationType, String[] args) {
        this.type = operationType;
        this.args = args;
    }
}