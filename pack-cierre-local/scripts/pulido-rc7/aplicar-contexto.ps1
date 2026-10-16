$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$root="$project/pulido-rc7/idiomas-20261016"
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
$server="$project/prueba-social-r3/server"
if(Get-CimInstance Win32_Process -Filter "Name='javaw.exe' OR Name='java.exe'" | Where-Object {$_.CommandLine -and $_.CommandLine -like '*Puchamon-Origins-Prueba-r3*'}){throw 'Cierra el cliente fuente para aplicar el pulido.'}
$lock="$server/world-complementos/session.lock"
if(Test-Path -LiteralPath $lock){$stream=[IO.File]::Open($lock,[IO.FileMode]::Open,[IO.FileAccess]::Read,[IO.FileShare]::None);$stream.Dispose()}
$before=@{}
foreach($rel in @('options.txt','config/defaultoptions/options.txt','config/defaultoptions/keybindings.txt','config/iris.properties')){if(Test-Path -LiteralPath "$client/$rel"){$before[$rel]=(Get-FileHash -LiteralPath "$client/$rel").Hash}}
$chapterHashes=@(Get-ChildItem -LiteralPath "$server/config/ftbquests/quests/chapters" -File | ForEach-Object {@{path=$_.FullName;hash=(Get-FileHash -LiteralPath $_.FullName).Hash}})
$backup="$root/respaldo-"+(Get-Date -Format 'yyyyMMdd-HHmmss')
New-Item -ItemType Directory -Path $backup | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$stage="$root/build-"+[guid]::NewGuid().ToString('N')
$baseJar=if(Test-Path -LiteralPath "$client/mods/puchamon-translations-1.0.0.jar"){"$client/mods/puchamon-translations-1.0.0.jar"}else{"$client/mods/puchamon-translations-1.0.1.jar"}
[IO.Compression.ZipFile]::ExtractToDirectory($baseJar,$stage)
foreach($file in Get-ChildItem -LiteralPath "$root/generado/assets" -File -Recurse){$rel=[IO.Path]::GetRelativePath("$root/generado",$file.FullName);$dest=Join-Path $stage $rel;New-Item -ItemType Directory -Force (Split-Path $dest -Parent) | Out-Null;Copy-Item -LiteralPath $file.FullName -Destination $dest}
$meta=Get-Content -LiteralPath "$stage/fabric.mod.json" -Raw | ConvertFrom-Json -AsHashtable
$meta.version='1.0.1';$meta.description='Español contextual de Puchamon: crianza, mensajes de combate, fútbol y comodidad. Sin controles ni preferencias.'
$meta | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath "$stage/fabric.mod.json" -Encoding utf8NoBOM
Copy-Item -LiteralPath "$PSScriptRoot/es-contexto-20261016.json" -Destination "$stage/FUENTE-CONTEXTUAL-20261016.json"
'Correcciones de mensajes de Cobblemon contrastadas con las claves inglesas y españolas de la versión instalada; especies y nombres propios conservados. Fuentes de texto contextual y revisión: FUENTE-CONTEXTUAL-20261016.json. La cobertura no equivale a traducir todo el modpack.' | Set-Content -LiteralPath "$stage/REVISION-20261016.txt" -Encoding utf8NoBOM
$jar="$root/puchamon-translations-1.0.1.jar"
& "$env:APPDATA/FreesmLauncher/java/java-runtime-delta/bin/jar.exe" cf $jar -C $stage .
if($LASTEXITCODE){throw 'No se empaquetó el idioma'}
foreach($spec in @(@{Name='cliente';Path=$client},@{Name='servidor';Path=$server})){
 $dest="$backup/$($spec.Name)";New-Item -ItemType Directory -Path "$dest/lang" -Force | Out-Null
 $old=if(Test-Path -LiteralPath "$($spec.Path)/mods/puchamon-translations-1.0.0.jar"){"$($spec.Path)/mods/puchamon-translations-1.0.0.jar"}else{"$($spec.Path)/mods/puchamon-translations-1.0.1.jar"}
 if(!(Test-Path -LiteralPath $old)){throw 'No se encontró la versión anterior esperada; revisar antes de aplicar.'}
 $oldName=Split-Path $old -Leaf
 Copy-Item -LiteralPath $old -Destination "$dest/$oldName"
 if((Get-FileHash -LiteralPath $old).Hash -ne (Get-FileHash -LiteralPath "$dest/$oldName").Hash){throw 'Respaldo no verificado'}
 foreach($locale in @('es_es','en_us')){Copy-Item -LiteralPath "$($spec.Path)/config/ftbquests/quests/lang/$locale.snbt" -Destination "$dest/lang/$locale.snbt"}
 Copy-Item -LiteralPath $jar -Destination "$($spec.Path)/mods/puchamon-translations-1.0.1.jar"
 if((Get-FileHash -LiteralPath $jar).Hash -ne (Get-FileHash -LiteralPath "$($spec.Path)/mods/puchamon-translations-1.0.1.jar").Hash){throw 'Copia no verificada'}
 if($oldName -ne 'puchamon-translations-1.0.1.jar'){Remove-Item -LiteralPath $old}
 foreach($locale in @('es_es','en_us')){Copy-Item -LiteralPath "$root/generado/quests/lang/$locale.snbt" -Destination "$($spec.Path)/config/ftbquests/quests/lang/$locale.snbt"}
}
foreach($entry in $before.GetEnumerator()){if((Get-FileHash -LiteralPath "$client/$($entry.Key)").Hash -ne $entry.Value){throw 'Cambió una preferencia personal'}}
foreach($entry in $chapterHashes){if((Get-FileHash -LiteralPath $entry.path).Hash -ne $entry.hash){throw 'Cambió un capítulo'}}
@{appliedLocal=$true;version='1.0.1';backup=$backup;personalPreferencesUnchanged=$true;chapterDefinitionsUnchanged=$true;runtimeLanguageCheckPending=$true} | ConvertTo-Json | Set-Content -LiteralPath "$root/aplicacion.json" -Encoding utf8NoBOM
'Aplicado localmente: traducción 1.0.1 y explicaciones para principiantes. Anterior respaldado; preferencias y capítulos intactos. Falta comprobación visual.'
