package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class WebView2Process {
    private Path _dataDir;
    private Process _process;

    public WebView2Process(int port) throws IOException {
        _dataDir = Files.createTempDirectory("pw-java-webview2-tests-");
        Path executable = Path.of("../webview2-app/bin/Debug/net6.0-windows/webview2.exe");
        
        if (!Files.exists(executable)) {
            throw new RuntimeException("Executable not found: " + executable);
        }
        ProcessBuilder pb = new ProcessBuilder().command(executable.toAbsolutePath().toString());
        Map<String, String> envMap = pb.environment();
        envMap.put("WEBVIEW2_ADDITIONAL_BROWSER_ARGUMENTS", "--remote-debugging-port=" + port);
        envMap.put("WEBVIEW2_USER_DATA_FOLDER", _dataDir.toString());
        _process = pb.start();
        // wait until "WebView2 initialized" got printed
        BufferedReader reader = new BufferedReader(new InputStreamReader(_process.getInputStream()));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                throw new RuntimeException("WebView2 process exited");
            }
            if (line.contains("WebView2 initialized")) {
                break;
            }
        }
    }

    public void dispose() {
        _process.destroy();
        try {
            _process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
