param([string]$Jdk,[string]$Gson)
$ErrorActionPreference='Stop'
$base=$PSScriptRoot
if(-not $Jdk){$Jdk=Join-Path $env:APPDATA 'FreesmLauncher\java\java-runtime-delta\bin'}
if(-not $Gson){$Gson=if(Test-Path "$base\runtime\gson.jar"){"$base\runtime\gson.jar"}else{Join-Path $env:APPDATA 'FreesmLauncher\libraries\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar'}}
New-Item -ItemType Directory -Force -Path "$base\build\classes","$base\runtime" | Out-Null
if([IO.Path]::GetFullPath($gson) -ne [IO.Path]::GetFullPath("$base\runtime\gson.jar")){Copy-Item -LiteralPath $gson -Destination "$base\runtime\gson.jar" -Force}
$sources=@(Get-ChildItem "$base\src" -Recurse -Filter '*.java' | ForEach-Object FullName)
& "$jdk\javac.exe" --release 21 -encoding UTF-8 -cp $gson -d "$base\build\classes" @sources
if($LASTEXITCODE){throw 'No compiló el lanzador'}
& "$jdk\jar.exe" cfm "$base\runtime\launch.jar" "$base\launcher.mf" -C "$base\build\classes" .
if($LASTEXITCODE){throw 'No se empaquetó el lanzador'}
& "$jdk\java.exe" -cp "$base\runtime\launch.jar;$gson" local.puchamon.LauncherTests
if($LASTEXITCODE){throw 'Fallaron las pruebas unitarias'}
