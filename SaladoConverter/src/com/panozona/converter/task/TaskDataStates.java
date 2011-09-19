package com.panozona.converter.task;

/** 
 * @author Marek Standio
 */
public enum TaskDataStates {

    READY {

        @Override
        public String toString() {
            return "ready";
        }
    },
    PROCESSING {

        @Override
        public String toString() {
            return "processing";
        }
    },
    DONE {

        @Override
        public String toString() {
            return "done";
        }
    },
    ERROR {

        @Override
        public String toString() {
            return "error";
        }
    },
    CANCELED {

        @Override
        public String toString() {
            return "cnceled";
        }
    }
}
