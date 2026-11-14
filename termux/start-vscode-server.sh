#!/data/data/com.termux/files/usr/bin/bash
cd "$HOME/projects" || exit 1
if curl -fsS --max-time 2 http://127.0.0.1:8080/ >/dev/null 2>&1; then exit 0; fi
nohup code-server --bind-addr 127.0.0.1:8080 "$HOME/projects" > "$HOME/code-server.log" 2>&1 < /dev/null &
echo $! > "$HOME/.code-server-launcher.pid"
