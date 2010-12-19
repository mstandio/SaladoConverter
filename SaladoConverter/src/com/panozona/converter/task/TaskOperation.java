package com.panozona.converter.task;

/**
 *
 * @author Marek
 */
public class TaskOperation {
    public static final String OPERATION_DZT = "operationDZT";
    public static final String OPERATION_EC = "operationEC";
    public static final String OPERATION_RES = "operationResize";
    //public static final String OPERATION_DELETE = "operationDelete"; //TODO: instead of delsrc

    public String operationType;
    public String args[];

    public TaskOperation() {
    }

    public TaskOperation(String operationType, String[] args) {
        this.operationType = operationType;
        this.args = args;
    }
}