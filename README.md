# VS Code Launcher (Samsung DeX / Termux)

Android launcher with server ON/OFF, open VS Code, and close app.

## Build APK

GitHub Actions -> **Build Android APK** -> choose `vscode-launcher` branch -> Run workflow. On successful completion download the `VSCodeLauncher-debug` artifact and extract `app-debug.apk`.

## Termux setup (required)

Run these commands in the existing Termux installation:

```bash
mkdir -p ~/.termux ~/projects
grep -qxF 'allow-external-apps=true' ~/.termux/termux.properties 2>/dev/null || echo 'allow-external-apps=true' >> ~/.termux/termux.properties
termux-reload-settings
```

Copy the two files in `termux/` to Termux home (`/data/data/com.termux/files/home/`) and make them executable:

```bash
chmod +x ~/start-vscode-server.sh ~/stop-vscode.sh
```

The app requires the Android **Run commands in Termux environment** permission. The Google Play Termux version must support its external command API; this is not yet verified on the user's phone.

**Important:** Server OFF uses a PID recorded when server ON starts it. A code-server process started manually before installing this launcher might not stop via the button. Do not bind code-server to a public network address without authentication.
