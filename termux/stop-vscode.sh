#!/data/data/com.termux/files/usr/bin/bash
PIDFILE="$HOME/.code-server-launcher.pid"
if [ -f "$PIDFILE" ]; then
  PID="$(cat "$PIDFILE")"
  case "$PID" in *[!0-9]*|'') ;; *)
    if ps -p "$PID" -o args= 2>/dev/null | grep -q 'code-server'; then
      kill "$PID" 2>/dev/null || true
    fi
  ;; esac
  rm -f "$PIDFILE"
fi
