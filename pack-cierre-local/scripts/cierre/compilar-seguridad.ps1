$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
$jdk=Join-Path $env:APPDATA 'FreesmLauncher/java/java-runtime-delta/bin'
$base=Join-Path $project 'compat/puchamon-safety'
$classes="$base/build/classes-$([guid]::NewGuid().ToString('N'))"
New-Item -ItemType Directory -Force $classes | Out-Null
$cp=@("$client/.fabric/remappedJars/minecraft-1.21.1-0.19.5/client-intermediary.jar")+@(Get-ChildItem "$project/prueba-social-r3/server/mods" -Filter '*.jar' -File | ForEach-Object FullName)+@(Get-ChildItem "$env:APPDATA/FreesmLauncher/libraries" -Filter '*.jar' -Recurse -File | ForEach-Object FullName)
$argsList=@('-proc:none','--release','21','-encoding','UTF-8','-cp',('"'+(($cp -join ';') -replace '\\','/')+'"'),'-d',('"'+($classes -replace '\\','/')+'"'))+@(Get-ChildItem "$base/src" -Recurse -Filter '*.java' | ForEach-Object {'"'+($_.FullName -replace '\\','/')+'"'})
$argsList | Set-Content "$base/build/javac.args" -Encoding utf8NoBOM
& "$jdk/javac.exe" "@$base/build/javac.args"
if($LASTEXITCODE){throw 'No compiló la protección'}
Copy-Item "$base/resources/*" $classes -Recurse -Force
& "$jdk/jar.exe" cf "$base/build/puchamon-safety-1.0.0.jar" -C $classes .
if($LASTEXITCODE){throw 'No empaquetó la protección'}
