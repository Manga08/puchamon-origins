$ErrorActionPreference='Stop'
$base=$PSScriptRoot
New-Item -ItemType Directory -Force -Path "$base\runtime" | Out-Null
$version=Invoke-RestMethod 'https://api.modrinth.com/v2/version/tx2qzgzv'
$file=$version.files | Where-Object primary
Invoke-WebRequest $file.url -OutFile "$base\runtime\unsup.jar"
if((Get-FileHash "$base\runtime\unsup.jar" -Algorithm SHA512).Hash.ToLowerInvariant() -ne $file.hashes.sha512){throw 'Checksum de unsup incorrecto'}
$version | ConvertTo-Json -Depth 20 | Set-Content "$base\runtime\unsup-origin.json" -Encoding utf8
Invoke-WebRequest 'https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar' -OutFile "$base\runtime\gson.jar"
if((Get-FileHash "$base\runtime\gson.jar" -Algorithm SHA256).Hash -ne '4241C14A7727C34FEEA6507EC801318A3D4A90F070E4525681079FB94EE4C593'){throw 'Checksum de Gson incorrecto'}
'Dependencias exactas descargadas y verificadas. Ver DEPENDENCIAS.md para licencias y fuentes.'
