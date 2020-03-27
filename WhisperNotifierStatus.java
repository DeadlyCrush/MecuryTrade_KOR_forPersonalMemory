package com.mercury.platform.core.misc;

public enum WhisperNotifierStatus {
    ALWAYS {
        @Override
        public String asPretty() {
            return "항상 소리로 알림";
            //return "Always play a sound";
        }
    },
    ALTAB {
        @Override
        public String asPretty() {

            return "다른창을 보고있을 때만";
            //return "Only when tabbed out";
        }
    },
    NONE {
        @Override
        public String asPretty() {

            return "소리 없음";
            //return "Never";
        }
    };

    public static WhisperNotifierStatus valueOfPretty(String s) {
        for (WhisperNotifierStatus status : WhisperNotifierStatus.values()) {
            if (status.asPretty().equals(s)) {
                return status;
            }
        }
        return ALWAYS;
    }

    public abstract String asPretty();
}
