Write-Host "Killing any lingering Java processes to free ports..."
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

$services = @(
    "discovery-server",
    "api-gateway",
    "user-service",
    "restaurant-service",
    "search-service",
    "location-service",
    "review-service",
    "recommendation-service",
    "ai-service"
)

foreach ($svc in $services) {
    Write-Host "Restarting $svc..."
    Stop-Job -Name "$svc-restart" -ErrorAction SilentlyContinue
    Remove-Job -Name "$svc-restart" -ErrorAction SilentlyContinue
    Start-Job -Name "$svc-restart" -ScriptBlock {
        param($svcName)
        cd "D:\GenAI\Practice\Zomato_UC\backend\$svcName"
        .\mvnw.cmd spring-boot:run > "D:\GenAI\Practice\Zomato_UC\backend\$svcName.log" 2>&1
    } -ArgumentList $svc
    Start-Sleep -Seconds 5
}

Write-Host "Restarting Frontend..."
Stop-Job -Name "frontend-restart" -ErrorAction SilentlyContinue
Remove-Job -Name "frontend-restart" -ErrorAction SilentlyContinue
Start-Job -Name "frontend-restart" -ScriptBlock {
    cd "D:\GenAI\Practice\Zomato_UC\frontend\web-ui"
    npm start
}

Write-Host "Waiting infinitely to keep jobs alive..."
while ($true) {
    Start-Sleep -Seconds 60
}
