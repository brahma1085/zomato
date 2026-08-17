Write-Host "Starting Docker Services..."
Set-Location docker
docker-compose up -d
Set-Location ..

Write-Host "Waiting for infrastructure to boot..."
Start-Sleep -Seconds 30

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

Set-Location backend

foreach ($svc in $services) {
    Write-Host "Starting $svc..."
    # Run in background jobs
    Start-Job -Name $svc -ScriptBlock {
        param($svcName)
        cd "D:\GenAI\Practice\Zomato_UC\backend\$svcName"
        .\mvnw.cmd spring-boot:run > "D:\GenAI\Practice\Zomato_UC\backend\$svcName.log" 2>&1
    } -ArgumentList $svc
    Start-Sleep -Seconds 15
}

Set-Location ..

Write-Host "Starting Frontend..."
Start-Job -Name "frontend" -ScriptBlock {
    cd "D:\GenAI\Practice\Zomato_UC\frontend\web-ui"
    npm install
    npm start
}

Write-Host "All services started as background jobs."
Write-Host "Waiting infinitely to keep jobs alive..."

# Keep the script running
while ($true) {
    Start-Sleep -Seconds 60
}
