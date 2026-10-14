$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$jdk=Join-Path $env:APPDATA 'FreesmLauncher/java/java-runtime-delta/bin'
$base=Join-Path $project 'cierre-rc6/build-qa'
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
New-Item -ItemType Directory -Force "$base/classes" | Out-Null
$cp=@("$client/.fabric/remappedJars/minecraft-1.21.1-0.19.5/client-intermediary.jar","$project/compat/puchamon-safety/build/puchamon-safety-1.0.0.jar")+@(Get-ChildItem "$project/prueba-social-r3/server/mods" -Filter '*.jar' -File | ForEach-Object FullName)+@(Get-ChildItem "$project/distribucion/build/gate/fabric" -Filter '*.jar' -File | ForEach-Object FullName)+@(Get-ChildItem "$env:APPDATA/FreesmLauncher/libraries" -Filter '*.jar' -Recurse -File | ForEach-Object FullName)
$argsList=@('-proc:none','--release','21','-encoding','UTF-8','-cp',('"'+(($cp -join ';') -replace '\\','/')+'"'),'-d',('"'+("$base/classes" -replace '\\','/')+'"'))+@(Get-ChildItem "$PSScriptRoot/qa" -Filter '*.java' -Recurse -File | ForEach-Object {'"'+($_.FullName -replace '\\','/')+'"'})
$argsList | Set-Content "$base/javac.args" -Encoding utf8NoBOM
& "$jdk/javac.exe" "@$base/javac.args"
if($LASTEXITCODE){throw 'No compiló el ensayo'}
Copy-Item "$PSScriptRoot/qa/fabric.mod.json" "$base/classes"
& "$jdk/jar.exe" cf "$base/puchamon-local-qa-NO-DISTRIBUIR.jar" -C "$base/classes" .
if($LASTEXITCODE){throw 'No empaquetó el ensayo'}
