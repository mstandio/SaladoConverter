package com.panozona.converter.task;

/**
 *
 * @author Marek Standio
 */
public class TaskOperation {

    public static final String OPERATION_DZT = "operationDZT";
    public static final String OPERATION_EC = "operationEC";
    public static final String OPERATION_RES = "operationRES";
    public static final String OPERATION_DEL = "operationDEL";
    public String operationType;
    public String args[];

    public TaskOperation() {
    }

    public TaskOperation(String operationType, String[] args) {
        this.operationType = operationType;
        this.args = args;
    }
}
