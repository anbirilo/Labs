param([switch]$Run)

$ErrorActionPreference = "Stop"
Set-Location (Split-Path -Parent $MyInvocation.MyCommand.Path)

function Find-Javac {
    $onPath = Get-Command javac -ErrorAction SilentlyContinue
    if ($onPath) { return $onPath.Source }
    $patterns = @(
        "$env:JAVA_HOME\bin\javac.exe",
        "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe",
        "C:\Program Files\Java\*\bin\javac.exe",
        "C:\Program Files\Eclipse Adoptium\*\bin\javac.exe",
        "C:\Program Files\Microsoft\jdk-*\bin\javac.exe",
        "C:\Program Files\Amazon Corretto\*\bin\javac.exe"
    )
    foreach ($pattern in $patterns) {
        $found = @(Get-Item $pattern -ErrorAction SilentlyContinue) | Select-Object -First 1
        if ($found) { return $found.FullName }
    }
    throw "Не найден javac. Установите JDK 8 или новее и добавьте его в PATH."
}

$javac = Find-Javac
$versionText = (& $javac -version 2>&1 | Out-String)
if ($versionText -match "1\.8") {
    $releaseArgs = @("-source", "1.8", "-target", "1.8")
} else {
    $releaseArgs = @("--release", "8")
}

$junit = Join-Path (Get-Location) "lib\junit-platform-console-standalone-1.9.3.jar"
if (-not (Test-Path $junit)) {
    New-Item -ItemType Directory -Force -Path "lib" | Out-Null
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.9.3/junit-platform-console-standalone-1.9.3.jar" -OutFile $junit
}

New-Item -ItemType Directory -Force -Path "out\main", "out\test" | Out-Null
$main = @(Get-ChildItem -Path "src\main\java" -Recurse -Filter *.java | ForEach-Object { $_.FullName })
$tests = @(Get-ChildItem -Path "src\test\java" -Recurse -Filter *.java | ForEach-Object { $_.FullName })

& $javac -encoding UTF-8 -Xlint:-options @releaseArgs -d "out\main" @main
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& $javac -encoding UTF-8 -Xlint:-options @releaseArgs -cp "out\main;$junit" -d "out\test" @tests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& java "-Dfile.encoding=UTF-8" -jar $junit -cp "out\main;out\test" --scan-classpath --disable-banner --details=tree
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($Run) {
    & java -Dfile.encoding=UTF-8 -cp "out\main" coffeefactory.Main
}
