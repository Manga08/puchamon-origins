$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
$protected=Get-Content "$project/cierre-rc6/evidencias/cliente-protegido-antes.json" -Raw | ConvertFrom-Json
$changed=@($protected | Where-Object {(Get-FileHash -LiteralPath (Join-Path $client $_.path) -Algorithm SHA256).Hash -ne $_.sha256})
$manifest=Get-Content "$project/cierre-rc6/respaldos-completos/r5-antes-del-cierre/puchamon-backup-manifest.json" -Raw | ConvertFrom-Json
$private=@($manifest.files | Where-Object {$_.path -match '^(world-complementos/|EasyAuth/|mods/luckperms/)'} )
$serverChanged=@($private | Where-Object {(Get-FileHash -LiteralPath (Join-Path "$project/prueba-social-r3/server" $_.path) -Algorithm SHA256).Hash -ne $_.sha256})
$softwareStage="$project/distribucion/output/Servidor-Puchamon-0.1.0-rc6"
if(Get-ChildItem -LiteralPath $softwareStage -Recurse -File | Where-Object {$_.FullName -match 'NO-DISTRIBUIR|puchamon-local-qa|\.db$|world-complementos|[/\\]EasyAuth[/\\]'}){throw 'Datos de laboratorio/privados presentes en el overlay'}
if($changed.Count -or $serverChanged.Count){throw 'Conservación no coincide; investigar antes de reiniciar'}
$result=@{checkedAt=(Get-Date -Format o);beforeOriginalServerRestart=$true;clientProtectedFiles=$protected.Count;clientChanged=0;privateServerFiles=$private.Count;privateServerChanged=0;noQaOrDatabasesInServerPayload=$true;clientRootJars=@(Get-ChildItem -LiteralPath "$client/mods" -File -Filter '*.jar').Count;distributedClientJars=164;distributedServerJars=@(Get-ChildItem -LiteralPath "$softwareStage/mods" -File -Filter '*.jar').Count}
$result | ConvertTo-Json | Set-Content "$project/cierre-rc6/evidencias/conservacion-final.json" -Encoding utf8NoBOM
$result | ConvertTo-Json
