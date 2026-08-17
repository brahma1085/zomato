$services = @(
    @{ name="discovery-server"; deps="cloud-eureka-server" },
    @{ name="api-gateway"; deps="cloud-gateway,cloud-eureka" },
    @{ name="user-service"; deps="web,data-jpa,postgresql,actuator,cloud-eureka" },
    @{ name="restaurant-service"; deps="web,data-jpa,postgresql,actuator,cloud-eureka" },
    @{ name="search-service"; deps="web,data-elasticsearch,actuator,cloud-eureka" },
    @{ name="location-service"; deps="web,actuator,cloud-eureka" },
    @{ name="review-service"; deps="web,data-jpa,postgresql,actuator,cloud-eureka" },
    @{ name="recommendation-service"; deps="web,data-jpa,postgresql,actuator,cloud-eureka" },
    @{ name="ai-service"; deps="web,actuator,cloud-eureka" }
)

foreach ($svc in $services) {
    $name = $svc.name
    $deps = $svc.deps
    Write-Host "Generating $name..."
    $url = "https://start.spring.io/starter.zip?type=maven-project&language=java&baseDir=$name&groupId=com.zomato&artifactId=$name&name=$name&javaVersion=21&dependencies=$deps"
    Invoke-WebRequest -Uri $url -OutFile "$name.zip"
    Expand-Archive -Path "$name.zip" -DestinationPath "." -Force
    Remove-Item -Path "$name.zip"
}

Write-Host "All services generated successfully."
