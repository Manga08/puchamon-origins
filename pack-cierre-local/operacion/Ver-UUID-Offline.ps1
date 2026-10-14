param([Parameter(Mandatory=$true)][ValidatePattern('^[A-Za-z0-9_]{3,16}$')][string]$Name)
$bytes=[Security.Cryptography.MD5]::HashData([Text.Encoding]::UTF8.GetBytes("OfflinePlayer:$Name"))
$bytes[6]=($bytes[6] -band 15) -bor 48
$bytes[8]=($bytes[8] -band 63) -bor 128
$hex=[Convert]::ToHexString($bytes).ToLowerInvariant()
$uuid="$($hex.Substring(0,8))-$($hex.Substring(8,4))-$($hex.Substring(12,4))-$($hex.Substring(16,4))-$($hex.Substring(20,12))"
[pscustomobject]@{NombreExacto=$Name;UUID=$uuid;Modo='offline-mode';Aviso='Se distinguen mayúsculas/minúsculas. En este servidor también se usa para una cuenta oficial que entre con ese nombre; no es el UUID Mojang.'}
