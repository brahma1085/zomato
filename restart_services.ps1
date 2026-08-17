Write-Host "Restarting Restaurant Service..."
Start-Job -Name "restaurant-service-restart" -ScriptBlock {
    cd "D:\GenAI\Practice\Zomato_UC\backend\restaurant-service"
    .\mvnw.cmd spring-boot:run > "D:\GenAI\Practice\Zomato_UC\backend\restaurant-service.log" 2>&1
}

Start-Sleep -Seconds 10

Write-Host "Restarting Recommendation Service..."
Start-Job -Name "recommendation-service-restart" -ScriptBlock {
    cd "D:\GenAI\Practice\Zomato_UC\backend\recommendation-service"
    .\mvnw.cmd spring-boot:run > "D:\GenAI\Practice\Zomato_UC\backend\recommendation-service.log" 2>&1
}

Write-Host "Restarting Frontend..."
Start-Job -Name "frontend-restart" -ScriptBlock {
    cd "D:\GenAI\Practice\Zomato_UC\frontend\web-ui"
    npm start
}

Write-Host "Waiting infinitely to keep jobs alive..."
while ($true) {
    Start-Sleep -Seconds 60
}
