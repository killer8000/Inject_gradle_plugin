package com.inject.ndh

class InjectPluginExtension {
    String outputDir;
    String jarDir;
    String androidHome

    @Override
    String toString() {
        return "${outputDir},${jarDir},${androidHome}";
    }
}