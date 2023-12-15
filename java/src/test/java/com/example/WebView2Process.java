package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class WebView2Process {
  public int cdpPort;
  private Path _dataDir;
  private Process _process;
  private Path _executablePath = Path.of("../webview2-app/bin/Debug/net8.0-windows/webview2.exe");

  public WebView2Process() throws IOException {
    cdpPort = nextFreePort();
    _dataDir = Files.createTempDirectory("pw-java-webview2-tests-");

    if (!Files.exists(_executablePath)) {
      throw new RuntimeException("Executable not found: " + _executablePath);
    }
    ProcessBuilder pb = new ProcessBuilder().command(_executablePath.toAbsolutePath().toString());
    Map<String, String> envMap = pb.environment();
    envMap.put("WEBVIEW2_ADDITIONAL_BROWSER_ARGUMENTS", "--remote-debugging-port=" + cdpPort);
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

  private static final AtomicInteger nextUnusedPort = new AtomicInteger(9000);

  private static boolean available(int port) {
    try (ServerSocket ignored = new ServerSocket(port)) {
      return true;
    } catch (IOException ignored) {
      return false;
    }
  }

  static int nextFreePort() {
    for (int i = 0; i < 100; i++) {
      int port = nextUnusedPort.getAndIncrement();
      if (available(port)) {
        return port;
      }
    }
    throw new RuntimeException("Cannot find free port: " + nextUnusedPort.get());
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
