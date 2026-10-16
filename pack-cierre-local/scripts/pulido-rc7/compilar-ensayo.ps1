$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$jdk=Join-Path $env:APPDATA 'FreesmLauncher/java/java-runtime-delta/bin'
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
$base="$project/pulido-rc7/build-qa"
$classes=Join-Path $base ('classes-'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $classes | Out-Null
$cp=@("$client/.fabric/remappedJars/minecraft-1.21.1-0.19.5/client-intermediary.jar")+@(Get-ChildItem "$project/prueba-social-r3/server/mods" -Filter '*.jar' -File | ForEach-Object FullName)+@(Get-ChildItem "$env:APPDATA/FreesmLauncher/libraries" -Filter '*.jar' -Recurse -File | ForEach-Object FullName)
$cp+=@(Get-ChildItem "$project/distribucion/build/gate/fabric" -Filter '*.jar' -File | ForEach-Object FullName)
Add-Type -AssemblyName System.IO.Compression.FileSystem
$langJar=Get-ChildItem "$client/mods" -Filter 'fabric-language-kotlin*.jar' | Select-Object -First 1
$zip=[IO.Compression.ZipFile]::OpenRead($langJar.FullName)
try{foreach($entry in $zip.Entries){if($entry.FullName -match '^META-INF/jars/.*\.jar$'){$target=Join-Path $base $entry.Name;[IO.Compression.ZipFileExtensions]::ExtractToFile($entry,$target,$true);$cp+=$target}}}finally{$zip.Dispose()}
$argsList=@('-proc:none','--release','21','-encoding','UTF-8','-cp',('"'+(($cp -join ';') -replace '\\','/')+'"'),'-d',('"'+($classes -replace '\\','/')+'"'))+@(Get-ChildItem "$PSScriptRoot/qa" -Filter '*.java' -Recurse -File | ForEach-Object {'"'+($_.FullName -replace '\\','/')+'"'})
$argsList | Set-Content "$base/javac.args" -Encoding utf8NoBOM
& "$jdk/javac.exe" "@$base/javac.args"
if($LASTEXITCODE){throw 'No compiló el ensayo'}
Copy-Item -LiteralPath "$PSScriptRoot/qa/fabric.mod.json" -Destination $classes
& "$jdk/jar.exe" cf "$base/puchamon-perf-qa-NO-DISTRIBUIR.jar" -C $classes .
if($LASTEXITCODE){throw 'No empaquetó el ensayo'}
