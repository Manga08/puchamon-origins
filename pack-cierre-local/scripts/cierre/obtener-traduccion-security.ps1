$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$out=Join-Path $project 'cierre-rc6/fuentes-securitycraft'
New-Item -ItemType Directory -Force -Path $out | Out-Null
$commit='c4d35b0c8bf85c2a94c37a1f8ce37ef5ce1a6aa8'
$url="https://raw.githubusercontent.com/Geforce132/SecurityCraft/$commit/src/main/resources/assets/securitycraft/lang/es_es.json"
Invoke-WebRequest $url -OutFile "$out/es_es.json"
if((Get-FileHash "$out/es_es.json" -Algorithm SHA256).Hash -ne '3278DA786B4596AB8B2C68DB85F31774875F5EA87C27C793A56E6D9E142465C1'){throw 'La traducción no coincide con el origen fijado'}
Invoke-WebRequest "https://raw.githubusercontent.com/Geforce132/SecurityCraft/$commit/LICENSE" -OutFile "$out/LICENSE"
@{source=$url;commit=$commit;license='MIT';sha256=(Get-FileHash "$out/es_es.json" -Algorithm SHA256).Hash} | ConvertTo-Json | Set-Content "$out/origen.json" -Encoding utf8NoBOM
"Traducción SecurityCraft descargada de commit fijado, con licencia y hash."
