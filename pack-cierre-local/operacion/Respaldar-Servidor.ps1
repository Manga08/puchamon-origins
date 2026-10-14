param([Parameter(Mandatory=$true)][string]$ServerRoot,[Parameter(Mandatory=$true)][string]$BackupRoot,[Parameter(Mandatory=$true)][ValidatePattern('^[a-z0-9][a-z0-9._-]+$')][string]$Name)
$ErrorActionPreference='Stop'
$source=(Resolve-Path -LiteralPath $ServerRoot).Path
if(-not(Test-Path -LiteralPath "$source/fabric-server-launch.jar") -or -not(Test-Path -LiteralPath "$source/server.properties")){throw 'No es una raíz de servidor Fabric'}
$worldLine=Get-Content "$source/server.properties" | Where-Object {$_ -match '^level-name='} | Select-Object -First 1
$world=if($worldLine){$worldLine.Substring('level-name='.Length)}else{'world'}
if($world -notmatch '^[a-zA-Z0-9._-]+$'){throw 'Nombre de mundo no admitido por esta herramienta'}
$lock=Join-Path $source "$world/session.lock"
# Check the world's exclusive lock, not all Java processes on the computer.
if(Test-Path -LiteralPath $lock){try{$handle=[IO.File]::Open($lock,[IO.FileMode]::Open,[IO.FileAccess]::ReadWrite,[IO.FileShare]::None);$handle.Dispose()}catch{throw 'El mundo está abierto: primero stop y espera al guardado'}}
$destination=$ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath((Join-Path $BackupRoot $Name))
if($destination.StartsWith($source.TrimEnd('\')+'\',[StringComparison]::OrdinalIgnoreCase) -or $destination -eq $source){throw 'El respaldo no puede estar dentro del servidor'}
if(Test-Path -LiteralPath $destination){throw 'Nunca se sobrescribe un respaldo'}
New-Item -ItemType Directory -Path $destination -Force | Out-Null
$files=@(Get-ChildItem -LiteralPath $source -File -Recurse -Force)
if($files | Where-Object {($_.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0}){throw 'No se admiten enlaces de archivos en este respaldo'}
if(Get-ChildItem -LiteralPath $source -Directory -Recurse -Force | Where-Object {($_.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0}){throw 'No se admiten enlaces de carpetas'}
$rows=[Collections.Generic.List[object]]::new()
foreach($file in $files){
 $relative=[IO.Path]::GetRelativePath($source,$file.FullName)
 $target=Join-Path $destination $relative
 New-Item -ItemType Directory -Force -Path (Split-Path $target -Parent) | Out-Null
 Copy-Item -LiteralPath $file.FullName -Destination $target
 $hash=(Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash
 if((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -ne $hash){throw "No coincide la copia: $relative"}
 $rows.Add(@{path=$relative.Replace('\','/');bytes=$file.Length;sha256=$hash})
}
@{schema=1;offline=$true;created=(Get-Date -Format o);private=$true;files=$rows} | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath "$destination/puchamon-backup-manifest.json" -Encoding utf8NoBOM
"Respaldo COMPLETO verificado: $($rows.Count) archivos. Contiene cuentas y datos privados: no distribuir."
