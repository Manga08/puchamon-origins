$ErrorActionPreference='Stop'
$base=$PSScriptRoot
$project=Split-Path $base -Parent
$source="$project\prueba-social-r3\server"
$dest="$base\build\gate-server"
if(Test-Path -LiteralPath $dest){throw 'El ensayo ya existe; no sobrescribir'}
New-Item -ItemType Directory -Path $dest | Out-Null
foreach($dir in @('libraries','versions','.fabric','config','datapacks')){Copy-Item -LiteralPath "$source\$dir" -Destination "$dest\$dir" -Recurse}
Copy-Item -LiteralPath "$source\fabric-server-launch.jar" -Destination $dest
New-Item -ItemType Directory -Path "$dest\mods","$dest\puchamon" | Out-Null
Get-ChildItem "$source\mods" -File -Filter '*.jar' | Copy-Item -Destination "$dest\mods"
Copy-Item "$base\runtime\puchamon-gate-1.0.0.jar" "$dest\mods"
Copy-Item "$base\output\Servidor-Puchamon-0.1.0-rc2\puchamon\release.json" "$dest\puchamon"
Copy-Item -LiteralPath "$source\eula.txt" -Destination $dest
Add-Type -AssemblyName System.IO.Compression.FileSystem
[IO.Compression.ZipFile]::ExtractToDirectory("$project\prueba-social-r3\evidencias\imagenes-seguridad-r5\backup\world-complementos-r4.zip","$dest\world-gate-test")
'Prepared isolated login-gate server; original server untouched.'
