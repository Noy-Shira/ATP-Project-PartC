package org.example.atpprojectpartc;

// This class exists only to work around a known JavaFX limitation:
// when an Application subclass is launched directly via "java -jar"
// (without the module system), JavaFX blocks it with a "runtime
// components are missing" error, even though everything is present.
// Running this separate Launcher class instead avoids that check.
public class Launcher {
    public static void main(String[] args) {
        HelloApplication.main(args);
    }
}