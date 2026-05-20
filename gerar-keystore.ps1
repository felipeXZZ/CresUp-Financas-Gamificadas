# Script para gerar a keystore de assinatura do CresUp
# Execute uma única vez antes do primeiro build de release

Write-Host "=== Gerador de Keystore - CresUp ===" -ForegroundColor Cyan
Write-Host ""

# Solicita as senhas
$storePassword = Read-Host "Digite a senha da keystore (guarde em lugar seguro!)" -AsSecureString
$storePasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($storePassword)
)

$keyPassword = Read-Host "Digite a senha da chave (pode ser a mesma)" -AsSecureString
$keyPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($keyPassword)
)

# Verifica se keytool está disponível
$keytool = Get-Command keytool -ErrorAction SilentlyContinue
if (-not $keytool) {
    # Tenta encontrar no JDK do Android Studio
    $jdkPaths = @(
        "$env:LOCALAPPDATA\Android\Sdk\*\jre\bin\keytool.exe",
        "$env:ProgramFiles\Android\Android Studio\jre\bin\keytool.exe",
        "$env:ProgramFiles\Android\Android Studio\jbr\bin\keytool.exe"
    )
    foreach ($path in $jdkPaths) {
        $found = Get-Item $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) { $keytool = $found.FullName; break }
    }
    if (-not $keytool) {
        Write-Host "ERRO: keytool nao encontrado. Certifique-se de que o JDK esta instalado." -ForegroundColor Red
        exit 1
    }
}

$keystorePath = Join-Path $PSScriptRoot "cresup-release.keystore"

# Gera a keystore
& keytool -genkey -v `
    -keystore $keystorePath `
    -alias cresup-key `
    -keyalg RSA `
    -keysize 2048 `
    -validity 10000 `
    -storepass $storePasswordPlain `
    -keypass $keyPasswordPlain `
    -dname "CN=CresUp App, OU=Mobile, O=CresUp, L=Brasil, ST=SP, C=BR"

if ($LASTEXITCODE -eq 0) {
    # Atualiza keystore.properties com as senhas reais
    $propertiesContent = @"
storeFile=../cresup-release.keystore
storePassword=$storePasswordPlain
keyAlias=cresup-key
keyPassword=$keyPasswordPlain
"@
    $propertiesContent | Out-File -FilePath (Join-Path $PSScriptRoot "keystore.properties") -Encoding utf8

    Write-Host ""
    Write-Host "=== KEYSTORE CRIADA COM SUCESSO ===" -ForegroundColor Green
    Write-Host "Arquivo: $keystorePath" -ForegroundColor Yellow
    Write-Host "keystore.properties atualizado automaticamente." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "IMPORTANTE: Faca backup da keystore e das senhas!" -ForegroundColor Red
    Write-Host "Sem a keystore voce nao pode atualizar o app na Play Store." -ForegroundColor Red
} else {
    Write-Host "ERRO ao gerar keystore." -ForegroundColor Red
}
