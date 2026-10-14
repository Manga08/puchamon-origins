param([string]$Release='0.1.0-rc5',[ValidateSet('test','stable')][string]$Channel='test')
$ErrorActionPreference='Stop'
if($Release -notmatch '^[a-z0-9][a-z0-9._-]+$'){throw 'Versión inválida'}
$base=$PSScriptRoot
$source="$base\output\Puchamon-Origins-$Release-$Channel"
$staging="$base\build\instalador-limpio-$Release-$Channel"
if(Test-Path -LiteralPath $staging){throw 'El instalador ya existe; no sobrescribir'}
New-Item -ItemType Directory -Path "$staging\minecraft\puchamon" -Force | Out-Null
# Closed list: never export a used test instance or its unsup state/logs/preferences.
foreach($file in @('instance.cfg','mmc-pack.json','icon.png')){if(Test-Path -LiteralPath "$source\$file"){Copy-Item -LiteralPath "$source\$file" -Destination "$staging\$file"}}
foreach($file in @('launch.jar','gson.jar','unsup.jar','launcher.properties')){Copy-Item -LiteralPath "$source\minecraft\puchamon\$file" -Destination "$staging\minecraft\puchamon\$file"}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$localOnly=Select-String -LiteralPath "$source\minecraft\puchamon\launcher.properties" -Pattern '^allowLocalHttp=true$' -Quiet
$suffix=if($localOnly){'PRUEBA-LOCAL'}else{'INSTALADOR'}
$destination="$base\output\Puchamon-Origins-$Release-$suffix.zip"
if(Test-Path -LiteralPath $destination){throw 'ZIP existente; no sobrescribir'}
[IO.Compression.ZipFile]::CreateFromDirectory($staging,$destination,[IO.Compression.CompressionLevel]::Optimal,$false)
$zip=[IO.Compression.ZipFile]::OpenRead($destination)
try{
 $bad=@($zip.Entries | Where-Object {$_.FullName -match '(?i)(accounts|quickskin|xaero|saves|logs|installed-manifest|\.db|options\.txt|release\.json)'})
 if($bad.Count){throw 'Se detectaron archivos personales en el ZIP'}
}finally{$zip.Dispose()}
Get-FileHash -LiteralPath $destination -Algorithm SHA256
if($localOnly){'Instalador de prueba LOCAL. No enviarlo a amigos: su URL apunta a 127.0.0.1.'}else{'Instalador con endpoint HTTPS. No distribuir hasta verificar importación, acceso y versión de servidor.'}
