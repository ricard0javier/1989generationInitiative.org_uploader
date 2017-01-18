aws lambda create-function \
    --region eu-central-1 \
    --function-name 1989generationinitiative_uploader \
    --zip-file fileb://./build/distributions/1989generationInitiative.org_uploader-1.0.zip \
    --role arn:aws:iam::430132907316:role/initiativeRole \
    --handler org.generationinitiative.uploader.Hello \
    --runtime java8
