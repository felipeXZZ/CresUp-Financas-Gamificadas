# Script de build de release do CresUp para Google Play Store
# Gera o AAB (Android App Bundle) pronto para upload

Write-Host "=== Build de Release - CresUp ===" -ForegroundColor Cyan
Write-Host ""

# Verifica se a keystore existe
if (-not (Test-Path "keystore.properties")) {
    Write-Host "ERRO: keystore.properties nao encontrado." -ForegroundColor Red
    Write-Host "Execute primeiro: .\gerar-keystore.ps1" -ForegroundColor Yellow
    exit 1
}

$keystoreProps = Get-Content "keystore.properties" | ConvertFrom-StringData
$keystoreFile = $keystoreProps.storeFile -replace '\.\./', ''
if (-not (Test-Path $keystoreFile)) {
    Write-Host "ERRO: Arquivo de keystore nao encontrado: $keystoreFile" -ForegroundColor Red
    Write-Host "Execute primeiro: .\gerar-keystore.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "Limpando build anterior..." -ForegroundColor Gray
.\gradlew.bat clean

Write-Host ""
Write-Host "Gerando AAB de release..." -ForegroundColor Cyan
.\gradlew.bat bundleRelease

if ($LASTEXITCODE -eq 0) {
    $aabPath = "app\build\outputs\bundle\release\app-release.aab"
    $aabSize = [math]::Round((Get-Item $aabPath).Length / 1MB, 2)

    Write-Host ""
    Write-Host "=== AAB GERADO COM SUCESSO ===" -ForegroundColor Green
    Write-Host "Arquivo: $aabPath" -ForegroundColor Yellow
    Write-Host "Tamanho: $aabSize MB" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Proximos passos:" -ForegroundColor Cyan
    Write-Host "  1. Acesse https://play.google.com/console" -ForegroundColor White
    Write-Host "  2. Selecione o app CresUp" -ForegroundColor White
    Write-Host "  3. Va em: Producao > Criar novo release" -ForegroundColor White
    Write-Host "  4. Faca upload do arquivo: $aabPath" -ForegroundColor White
    Write-Host "  5. Adicione notas da versao e clique em Revisar release" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "ERRO no build. Verifique os logs acima." -ForegroundColor Red
    Write-Host "Dica: Abra o projeto no Android Studio e sincronize o Gradle primeiro." -ForegroundColor Yellow
}
