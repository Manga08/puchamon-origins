param([ValidateSet('candidate','restored')][string]$Mode='candidate')
$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$root=Join-Path $project 'cierre-rc6'
$dir=if($Mode -eq 'candidate'){"$root/server"}else{"$root/restauracion-verificada"}
$port=if($Mode -eq 'candidate'){25579}else{25580}
if(Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue){throw 'Puerto ocupado'}
if($Mode -eq 'restored'){
 $p=Get-Content "$dir/server.properties" -Raw
 $p=$p -replace '(?m)^server-port=.*','server-port=25580' -replace '(?m)^server-ip=.*','server-ip=127.0.0.1'
 $p | Set-Content "$dir/server.properties" -Encoding utf8NoBOM
 $voice="$dir/config/voicechat/voicechat-server.properties"
 (Get-Content $voice -Raw) -replace '(?m)^port=.*','port=24458' | Set-Content $voice -Encoding utf8NoBOM
}
$java=Join-Path $env:APPDATA 'FreesmLauncher/java/java-runtime-delta/bin/java.exe'
$proc=Start-Process -FilePath $java -ArgumentList @('-Duser.timezone=America/Bogota','-Xms512M','-Xmx4G','-jar','fabric-server-launch.jar','nogui') -WorkingDirectory $dir -WindowStyle Hidden -PassThru -RedirectStandardOutput "$root/evidencias/console-$Mode.out.log" -RedirectStandardError "$root/evidencias/console-$Mode.err.log"
@{mode=$Mode;pid=$proc.Id;port=$port;worldChangedOnlyInIsolatedCopy=$true} | ConvertTo-Json
