package com.panozona.converter.table;

/**
 *
 * @author Marek
 */
public class TaskOperation {
    public static final String OPERATION_DZT = "operationDZT";
    public static final String OPERATION_EC = "operationEC";
    //public static final String OPERATION_DELETE = "operationDelete"; //TODO: deleting files
    //public static final String OPERATION_RESIZE = "operationResize"; <- concept

    public String operationType;
    public String args[];

    public TaskOperation() {
    }

    public TaskOperation(String operationType, String[] args) {
        this.operationType = operationType;
        this.args = args;
    }
}
