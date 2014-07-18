package com.snowplowanalytics.snowplow.tracker;

public enum DevicePlatform {
    Web {
        public String toString() {
            return "web";
        }
    },
    Mobile {
        public String toString() {
            return "mob";
        }
    },
    Desktop {
        public String toString() {
            return "pc";
        }
    },
    ServerSideApp {
        public String toString() {
            return "srv";
        }
    },
    General {
        public String toString() {
            return "app";
        }
    },
    ConnectedTV {
        public String toString() {
            return "tv";
        }
    },
    GameConsole {
        public String toString() {
            return "cnsl";
        }
    },
    InternetOfThings {
        public String toString() {
            return "iot";
        }
    }
}
