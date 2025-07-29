#!/bin/bash

# Enterprise S3 Browser Startup Script

echo "Starting Enterprise S3 Browser..."

# Build the application if the JAR doesn't exist
if [ ! -f "backend/target/s3-browser-1.0.0.jar" ]; then
    echo "Building application..."
    cd backend
    mvn clean package -DskipTests
    cd ..
fi

# Check if MinIO is running (optional)
echo "Checking if MinIO is available..."
if curl -s http://localhost:9000/minio/health/ready > /dev/null 2>&1; then
    echo "✓ MinIO is running at http://localhost:9000"
else
    echo "⚠ MinIO not detected at http://localhost:9000"
    echo "  You can start MinIO with: docker run -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address \":9001\""
    echo "  Or configure a different S3 endpoint through the web interface"
fi

# Start the application
echo "Starting S3 Browser application..."
echo "Access the application at: http://localhost:8080"
echo "Default login: admin/admin"
echo ""

cd backend
java -jar target/s3-browser-1.0.0.jar