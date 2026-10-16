$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
$out=Join-Path $project 'pulido-rc7'
$backup=Join-Path $out ('respaldo-interfaz-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Path $backup | Out-Null
$protected=@('options.txt','config/inventoryprofilesnext','config/journeymap','journeymap','XaeroWaypoints','XaeroWorldMap')
$hashes=@()
foreach($rel in $protected){
 $src=Join-Path $client $rel
 if(!(Test-Path -LiteralPath $src)){continue}
 $dest=Join-Path $backup $rel
 New-Item -ItemType Directory -Force (Split-Path $dest -Parent) | Out-Null
 Copy-Item -LiteralPath $src -Destination $dest -Recurse
 $hashes+=@(Get-ChildItem -LiteralPath $src -File -Recurse | ForEach-Object { [ordered]@{path=$_.FullName.Substring($client.Length+1);sha256=(Get-FileHash -LiteralPath $_.FullName).Hash} })
}
$hashes | ConvertTo-Json -Depth 8 | Set-Content "$backup/archivos-protegidos.json" -Encoding utf8NoBOM
foreach($root in @($client,"$project/prueba-social-r3/server")){
 foreach($locale in @('es_es','en_us')){
  $target="$root/config/ftbquests/quests/lang/$locale.snbt"
  $name=if($root -eq $client){'cliente'}else{'servidor'}
  New-Item -ItemType Directory -Force "$backup/$name" | Out-Null
  Copy-Item -LiteralPath $target -Destination "$backup/$name/$locale.snbt"
  Copy-Item -LiteralPath "$out/generado/quests/lang/$locale.snbt" -Destination $target -Force
 }
}
$hint="$client/config/inventoryprofilesnext/integrationHints/puchamon-accessories.json"
if(Test-Path -LiteralPath $hint){Copy-Item -LiteralPath $hint -Destination "$backup/hint-anterior.json"}
New-Item -ItemType Directory -Force (Split-Path $hint -Parent) | Out-Null
Copy-Item -LiteralPath "$PSScriptRoot/puchamon-accessories.json" -Destination $hint -Force
$jar="$client/mods/puchamon-ui-polish-1.0.0.jar"
if(Test-Path -LiteralPath $jar){Copy-Item -LiteralPath $jar -Destination "$backup/traduccion-anterior.jar"}
Copy-Item -LiteralPath "$out/puchamon-ui-polish-1.0.0.jar" -Destination $jar -Force
foreach($entry in $hashes){
 if((Get-FileHash -LiteralPath (Join-Path $client $entry.path)).Hash -ne $entry.sha256){throw "Se modificó un archivo personal: $($entry.path)"}
}
Write-Output "Aplicado solo al entorno local. Respaldo: $backup. Preferencias anteriores sin cambios."
