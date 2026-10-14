param([string]$Client,[string]$Libraries,[string]$Jdk)
$ErrorActionPreference='Stop'
$base=$PSScriptRoot
if(-not $Client){$Client=Join-Path $env:APPDATA 'FreesmLauncher\instances\Puchamon-Origins-Prueba-r3\minecraft'}
if(-not $Libraries){$Libraries=Join-Path $env:APPDATA 'FreesmLauncher\libraries'}
if(-not $Jdk){$Jdk=Join-Path $env:APPDATA 'FreesmLauncher\java\java-runtime-delta\bin'}
$libs=$Libraries
$build="$base\build\gate"
New-Item -ItemType Directory -Force -Path "$build\classes","$build\fabric" | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$api=Get-ChildItem "$client\mods" -Filter 'fabric-api-*.jar' | Select-Object -First 1
$zip=[IO.Compression.ZipFile]::OpenRead($api.FullName)
try{foreach($entry in $zip.Entries){if($entry.FullName -like 'META-INF/jars/*.jar'){
 [IO.Compression.ZipFileExtensions]::ExtractToFile($entry,(Join-Path "$build\fabric" ([IO.Path]::GetFileName($entry.FullName))),$true)
}}}finally{$zip.Dispose()}
$cp=@("$client\.fabric\remappedJars\minecraft-1.21.1-0.19.5\client-intermediary.jar")+@(Get-ChildItem "$build\fabric" -Filter '*.jar' | ForEach-Object FullName)+@(Get-ChildItem $libs -Recurse -Filter '*.jar' | ForEach-Object FullName)
$args=@('-proc:none','--release','21','-encoding','UTF-8','-cp',('"'+(($cp -join ';') -replace '\\','/')+'"'),'-d',('"'+("$build\classes" -replace '\\','/')+'"'))+@(Get-ChildItem "$base\gate\src" -Recurse -Filter '*.java' | ForEach-Object {'"'+($_.FullName -replace '\\','/')+'"'})
$args | Set-Content "$build\javac.args" -Encoding utf8NoBOM
& "$jdk\javac.exe" "@$build\javac.args"
if($LASTEXITCODE){throw 'Falló la compilación del gate'}
Copy-Item "$base\gate\resources\*" "$build\classes" -Recurse -Force
& "$jdk\jar.exe" cf "$base\runtime\puchamon-gate-1.0.0.jar" -C "$build\classes" .
if($LASTEXITCODE){throw 'Falló el empaquetado del gate'}
