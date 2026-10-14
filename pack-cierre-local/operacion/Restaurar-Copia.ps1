param([Parameter(Mandatory=$true)][string]$BackupPath,[Parameter(Mandatory=$true)][string]$Destination)
$ErrorActionPreference='Stop'
$source=(Resolve-Path -LiteralPath $BackupPath).Path
$manifest=Get-Content -LiteralPath "$source/puchamon-backup-manifest.json" -Raw | ConvertFrom-Json
if($manifest.schema -ne 1 -or -not $manifest.offline){throw 'No es un respaldo offline verificado'}
$target=$ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Destination)
if(Test-Path -LiteralPath $target){throw 'Solo se restaura en una carpeta NUEVA, nunca sobre un mundo existente'}
New-Item -ItemType Directory -Path $target -Force | Out-Null
foreach($entry in $manifest.files){
 if($entry.path -match '(^/|\\|:|(^|/)\.\.(/|$))'){throw 'Ruta no segura en el manifiesto'}
 $file=Join-Path $source $entry.path
 if((Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash -ne $entry.sha256){throw 'Respaldo alterado o incompleto'}
 $copy=Join-Path $target $entry.path
 New-Item -ItemType Directory -Force -Path (Split-Path $copy -Parent) | Out-Null
 Copy-Item -LiteralPath $file -Destination $copy
 if((Get-FileHash -LiteralPath $copy -Algorithm SHA256).Hash -ne $entry.sha256){throw 'Restauración no coincide'}
}
"Restauración verificada en carpeta NUEVA: $($manifest.files.Count) archivos. No iniciar en el mismo puerto que el servidor original."
