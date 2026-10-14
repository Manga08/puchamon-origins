param([switch]$InstallSource)
$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$root=Join-Path $project 'cierre-rc6'
$server=if($InstallSource){Join-Path $project 'prueba-social-r3/server'}else{"$root/server"}
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
$port=if($InstallSource){25577}else{25579}
if(Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue){throw 'Detén limpiamente el destino primero'}
if($InstallSource){
 if(Get-CimInstance Win32_Process | Where-Object {$_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'KnotClient' -and $_.CommandLine -match 'Puchamon-Origins-Prueba-r3'}){throw 'Cierra primero el cliente social'}
 $qa=Get-Content "$root/evidencias/qa-runtime.json" -Raw | ConvertFrom-Json
 $loot=Get-Content "$root/evidencias/lootr-r6-runtime.json" -Raw | ConvertFrom-Json
 if($qa.failure -or -not $qa.allGuideReferencesExist -or -not $qa.guardImsEntity -or -not $qa.nativeUnauthenticatedPlayerProtected -or -not $loot.passed){throw 'Falta ensayo completo válido'}
}
$jdk=Join-Path $env:APPDATA 'FreesmLauncher/java/java-runtime-delta/bin'
& "$jdk/jar.exe" cf "$root/generado/puchamon-translations-1.0.0.jar" -C "$root/generado/traduccion" .
if($LASTEXITCODE){throw 'No se empaquetó la traducción'}
$sides=if($InstallSource){@($server,$client)}else{@($server)}
foreach($target in $sides){
 if($InstallSource){
  $label=if($target -eq $server){'servidor'}else{'cliente'}
  $backup="$root/respaldo-privado/pre-aplicar-$label"
  if(Test-Path -LiteralPath $backup){throw 'Respaldo preaplicación existente; revisar antes de repetir'}
  New-Item -ItemType Directory -Force -Path $backup | Out-Null
  Copy-Item -LiteralPath "$target/config" -Destination "$backup/config" -Recurse
 }
 Copy-Item -LiteralPath "$root/generado/puchamon-translations-1.0.0.jar" -Destination "$target/mods"
 Copy-Item -LiteralPath "$PSScriptRoot/correcciones-sociales" -Destination "$target/datapacks/Puchamon-Correcciones-Sociales" -Recurse
 foreach($kind in @('chapters','lang')){
  foreach($f in Get-ChildItem -LiteralPath "$root/generado/quests/$kind" -File){
   if($kind -eq 'chapters' -and (Test-Path -LiteralPath "$target/config/ftbquests/quests/$kind/$($f.Name)")){throw 'No sustituir un capítulo con IDs nuevos'}
   Copy-Item -LiteralPath $f.FullName -Destination "$target/config/ftbquests/quests/$kind/$($f.Name)"
  }
 }
}
Copy-Item -LiteralPath "$project/compat/puchamon-safety/build/puchamon-safety-1.0.0.jar" -Destination "$server/mods"
$translation="$server/config/EasyAuth/translation.conf"
(Get-Content -LiteralPath $translation -Raw) -replace 'default-language="en_us"','default-language="es_es"' | Set-Content -LiteralPath $translation -Encoding utf8NoBOM
if(-not $InstallSource){Copy-Item -LiteralPath "$root/build-qa/puchamon-local-qa-NO-DISTRIBUIR.jar" -Destination "$server/mods"}
'Candidata aplicada únicamente a software, definiciones e idioma; no mundo/mapas/cuentas/preferencias.'
