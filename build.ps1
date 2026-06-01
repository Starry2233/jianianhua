$host.ui.RawUI.ForegroundColor = "DarkYellow"
Write-Host @"

 _______      ___    ___ ________  ___       ________  ________  _______           ___  ________   ________ ___  ________   ___  _________    ___    ___
|\  ___ \    |\  \  /  /|\   __  \|\  \     |\   __  \|\   __  \|\  ___ \         |\  \|\   ___  \|\  _____\\  \|\   ___  \|\  \|\___   ___\ |\  \  /  /|
\ \   __/|   \ \  \/  / | \  \|\  \ \  \    \ \  \|\  \ \  \|\  \ \   __/|        \ \  \ \  \\ \  \ \  \__/\ \  \ \  \\ \  \ \  \|___ \  \_| \ \  \/  / /
 \ \  \_|/__  \ \    / / \ \   ____\ \  \    \ \  \\\  \ \   _  _\ \  \_|/__       \ \  \ \  \\ \  \ \   __\\ \  \ \  \\ \  \ \  \   \ \  \   \ \    / /
  \ \  \_|\ \  /     \/   \ \  \___|\ \  \____\ \  \\\  \ \  \\  \\ \  \_|\ \       \ \  \ \  \\ \  \ \  \_| \ \  \ \  \\ \  \ \  \   \ \  \   \/  /  /
   \ \_______\/  /\   \    \ \__\    \ \_______\ \_______\ \__\\ _\\ \_______\       \ \__\ \__\\ \__\ \__\   \ \__\ \__\\ \__\ \__\   \ \__\__/  / /
    \|_______/__/ /\ __\    \|__|     \|_______|\|_______|\|__|\|__|\|_______|        \|__|\|__| \|__|\|__|    \|__|\|__| \|__|\|__|    \|__|\___/ /
             |__|/ \|__|                                                                                                                    \|___|/

"@
$host.ui.RawUI.ForegroundColor = "White"

Write-Host "==> Building jianianhua..." -ForegroundColor Cyan

# 强制 Gradle 每次显示欢迎信息
Remove-Item "$env:USERPROFILE\.gradle\notified" -Recurse -ErrorAction SilentlyContinue

$env:JAVA_HOME = "C:\jdk21\jdk-21.0.6+7"
& ".\gradlew" assembleDebug --no-daemon --console=plain 2>&1 | ForEach-Object {
    $line = "$_"
    if ($_ -is [System.Management.Automation.ErrorRecord]) {
        $line = $_.Exception.Message
    }

    if ($line -match "^> Task ") {
        Write-Host $line -ForegroundColor White
    } elseif ($line -match "^<=====" -or $line -match "^> " -or $line -match "^\d+% ") {
        # skip progress lines
    } elseif ($line -match "Welcome to Gradle") {
        Write-Host $line -ForegroundColor Green
    } elseif ($line -match "BUILD SUCCESSFUL") {
        Write-Host $line -ForegroundColor Green
    } elseif ($line -match "BUILD FAILED") {
        Write-Host $line -ForegroundColor Red
    } elseif ($line -match "(error|Error|ERROR):") {
        Write-Host $line -ForegroundColor Red
    } elseif ($line -match "warning|Warning") {
        Write-Host $line -ForegroundColor Yellow
    } elseif ($line -match "^BUILD" -or $line -match "^Daemon" -or $line -match "actionable") {
        Write-Host $line -ForegroundColor Gray
    } else {
        Write-Host $line
    }
}

$host.ui.RawUI.ForegroundColor = "DarkYellow"
Write-Host @"

 _______      ___    ___ ________  ___       ________  ________  _______           ___  ________   ________ ___  ________   ___  _________    ___    ___
|\  ___ \    |\  \  /  /|\   __  \|\  \     |\   __  \|\   __  \|\  ___ \         |\  \|\   ___  \|\  _____\\  \|\   ___  \|\  \|\___   ___\ |\  \  /  /|
\ \   __/|   \ \  \/  / | \  \|\  \ \  \    \ \  \|\  \ \  \|\  \ \   __/|        \ \  \ \  \\ \  \ \  \__/\ \  \ \  \\ \  \ \  \|___ \  \_| \ \  \/  / /
 \ \  \_|/__  \ \    / / \ \   ____\ \  \    \ \  \\\  \ \   _  _\ \  \_|/__       \ \  \ \  \\ \  \ \   __\\ \  \ \  \\ \  \ \  \   \ \  \   \ \    / /
  \ \  \_|\ \  /     \/   \ \  \___|\ \  \____\ \  \\\  \ \  \\  \\ \  \_|\ \       \ \  \ \  \\ \  \ \  \_| \ \  \ \  \\ \  \ \  \   \ \  \   \/  /  /
   \ \_______\/  /\   \    \ \__\    \ \_______\ \_______\ \__\\ _\\ \_______\       \ \__\ \__\\ \__\ \__\   \ \__\ \__\\ \__\ \__\   \ \__\__/  / /
    \|_______/__/ /\ __\    \|__|     \|_______|\|_______|\|__|\|__|\|_______|        \|__|\|__| \|__|\|__|    \|__|\|__| \|__|\|__|    \|__|\___/ /
             |__|/ \|__|                                                                                                                    \|___|/

"@

if ($LASTEXITCODE -eq 0) {
    Write-Host "==> BUILD SUCCESSFUL" -ForegroundColor Green
} else {
    Write-Host "==> BUILD FAILED" -ForegroundColor Red
}

$host.ui.RawUI.ForegroundColor = "White"
