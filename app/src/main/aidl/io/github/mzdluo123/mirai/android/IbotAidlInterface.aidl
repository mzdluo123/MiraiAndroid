// IbotAidlInterface.aidl
package io.github.mzdluo123.mirai.android;

// Declare any non-default types here with import statements
interface IbotAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    String[] getLog();
    void clearLog();
    void sendLog(String log);
    void runCmd(String cmd);
    byte[] getCaptcha();
    String getUrl();
    void submitVerificationResult(String result);
}