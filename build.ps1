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

$output = & {
    $env:JAVA_HOME = "C:\jdk21\jdk-21.0.6+7"
    & ".\gradlew" assembleDebug --no-daemon --console=plain 2>&1
}

$output | ForEach-Object {
    $line = $_ | Out-String -NoNewline
    if ($line -match "Welcome to Gradle") {
        Write-Host $line -ForegroundColor Green -NoNewline
    } elseif ($line -match "BUILD SUCCESSFUL") {
        Write-Host $line -ForegroundColor Green
    } elseif ($line -match "BUILD FAILED|ERROR|error:") {
        Write-Host $line -ForegroundColor Red
    } elseif ($line -match "warning|Warning") {
        Write-Host $line -ForegroundColor Yellow
    } else {
        Write-Host $line -NoNewline
    }
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n==> Build complete!" -ForegroundColor Cyan
}
