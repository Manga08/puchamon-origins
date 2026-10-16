$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$client=Join-Path $env:APPDATA 'FreesmLauncher/instances/Puchamon-Origins-Prueba-r3/minecraft'
$out=Join-Path $project 'pulido-rc7'
$game=Get-CimInstance Win32_Process | Where-Object {$_.Name -match '^javaw?\.exe$' -and $_.CommandLine -like '*Puchamon-Origins-Prueba-r3*'}
if($game){throw 'Cierra primero la instancia local de Puchamon'}
$jar=Join-Path $client 'mods/puchamon-perf-qa-NO-DISTRIBUIR.jar'
if(Test-Path -LiteralPath $jar){Move-Item -LiteralPath $jar -Destination "$out/build-qa/retirado-del-cliente-$(Get-Date -Format yyyyMMdd-HHmmss).jar"}
$save=Join-Path $client 'saves/Puchamon QA visual rc6 - 2026-10'
$resolved=[IO.Path]::GetFullPath($save)
$expected=[IO.Path]::GetFullPath((Join-Path $client 'saves'))+[IO.Path]::DirectorySeparatorChar
if(!$resolved.StartsWith($expected,[StringComparison]::OrdinalIgnoreCase) -or (Split-Path $resolved -Leaf) -ne 'Puchamon QA visual rc6 - 2026-10'){throw 'Destino fuera del mundo QA'}
$stamp=Get-Date -Format yyyyMMdd-HHmmss
$expanded=Join-Path $out "restauracion-qa-$stamp"
Expand-Archive -LiteralPath "$out/respaldo-mundo-QA-antes-carga.zip" -DestinationPath $expanded
$restored=Join-Path $expanded 'Puchamon QA visual rc6 - 2026-10'
if(!(Test-Path -LiteralPath "$restored/level.dat")){throw 'No se encontró el respaldo del mundo QA'}
if(Test-Path -LiteralPath $save){Move-Item -LiteralPath $resolved -Destination "$out/mundo-ensayo-conservado-$stamp"}
Move-Item -LiteralPath $restored -Destination $save
Write-Output 'Instrumento retirado. Mundo QA previo restaurado; escenario completo conservado fuera de la instancia. Ningún mundo del servidor alterado.'
