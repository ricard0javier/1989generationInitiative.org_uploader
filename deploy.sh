echo "Deleting previous function"
aws lambda delete-function \
    --region eu-central-1 \
    --function-name 1989generationinitiative_uploader \

echo "Creating function"
aws lambda create-function \
    --region eu-central-1 \
    --function-name 1989generationinitiative_uploader \
    --zip-file fileb://./build/distributions/1989generationInitiative.org_uploader-1.0.zip \
    --role arn:aws:iam::430132907316:role/initiativeRole \
    --handler org.generationinitiative.uploader.Hello::handleRequest \
    --runtime java8 \
    --timeout 10 \
    --environment Variables={JWT_CLIENT_SECRET=To_Be_Replaced}

echo "Creating alias"
aws lambda create-alias \
    --function-name 1989generationinitiative_uploader \
    --name 89ers_uploader_alias \
    --region eu-central-1 \
    --function-version \$LATEST

echo "DONE"
