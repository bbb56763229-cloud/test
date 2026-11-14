package com.example.vscodelauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val permission = "com.termux.permission.RUN_COMMAND"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var status: TextView
    private lateinit var toggle: Button
    private lateinit var open: Button
    private var running = false
    private val endpoint = "http://127.0.0.1:8080/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(42, 64, 42, 42)
            setBackgroundColor(Color.rgb(17, 24, 39))
        }
        fun label(value: String, size: Float): TextView = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        status = label("서버 확인 중...", 17f)
        toggle = Button(this).apply { text = "서버 ON" }
        open = Button(this).apply { text = "VS Code 실행"; isEnabled = false }
        val close = Button(this).apply { text = "앱 종료" }
        listOf(label("VS Code Launcher", 26f), status, toggle, open, close, label("Termux · localhost:8080", 13f)).forEach {
            root.addView(it, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 24 })
        }
        setContentView(root)
        toggle.setOnClickListener {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 100)
            } else {
                runTermux(if (running) "stop-vscode.sh" else "start-vscode-server.sh")
            }
        }
        open.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(endpoint))) }
        close.setOnClickListener { finish() }
        refresh()
    }

    private fun runTermux(script: String) {
        try {
            val intent = Intent("com.termux.RUN_COMMAND").apply {
                setClassName("com.termux", "com.termux.app.RunCommandService")
                putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/home/$script")
                putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            }
            startService(intent)
            status.text = "명령 전송됨 · 상태 확인 중..."
            listOf(1500L, 4000L, 8000L).forEach { delay ->
                handler.postDelayed({ refresh() }, delay)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Termux 호출 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun refresh() {
        thread {
            val alive = try {
                val c = URL(endpoint).openConnection() as HttpURLConnection
                c.connectTimeout = 1200
                c.readTimeout = 1200
                c.instanceFollowRedirects = false
                c.connect()
                val code = c.responseCode
                c.disconnect()
                code in 200..399 || code == 401
            } catch (_: Exception) { false }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                running = alive
                status.text = if (alive) "● 서버 실행 중" else "○ 서버 꺼짐"
                toggle.text = if (alive) "서버 OFF" else "서버 ON"
                open.isEnabled = alive
            }
        }
    }

    override fun onResume() { super.onResume(); refresh() }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            runTermux(if (running) "stop-vscode.sh" else "start-vscode-server.sh")
        } else if (requestCode == 100) {
            Toast.makeText(this, "Termux 실행 권한이 필요해", Toast.LENGTH_LONG).show()
        }
    }
}
